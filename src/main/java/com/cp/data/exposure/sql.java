package com.cp.data.exposure;
//nc -l 8989

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;


//统计最近一小时内 送礼最多的人，数据每5分钟更新一次
@lombok.extern.slf4j.Slf4j
public class sql {

    public static void main(String[] args) throws Exception {

        ParameterTool tool = ParameterTool.fromArgs(args);
        int pt_window_size_day = tool.getInt("pt_window_size_day", 1);
        int pt_window_slide_min = tool.getInt("pt_window_slide_min", 20);
        int pt_window_offset_min = tool.getInt("pt_window_offset_min", 0);
        int pt_drop_window_tumb_min = tool.getInt("pt_drop_window_tumb_min", 1);

        Configuration conf = new Configuration();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setRestartStrategy(RestartStrategies.noRestart());//失败不重启 直接退出
//        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.enableCheckpointing(pt_window_slide_min*60000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(600000);
        env.getCheckpointConfig().setCheckpointTimeout(pt_window_slide_min*60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        RocksDBStateBackend rocksDBStateBackend = new RocksDBStateBackend("hdfs:///cp/checkpoint/user_sex_cross_features/offline/",true);
        env.setStateBackend(rocksDBStateBackend);
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableenv = StreamTableEnvironment.create(env, settings);
        tableenv.sqlQuery("");









        System.out.println("控制台");
//        nc -lk 49838
//        (echo '好好学习,天天向上!'; sleep 2) | telnet 192.168.1.39 1500
        DataStream<String> ds = env.socketTextStream("127.0.0.1", 49838);
        DataStream<Tuple2<Long, String>> da = ds.map(new MapFunction<String, Tuple2<Long, String>>() {
            @Override
            public Tuple2<Long, String> map(String value) throws Exception {
                String[] x = value.split(" ");
                if(x.length != 2){
                    return null;
                }
                return Tuple2.of(Long.parseLong(x[0]), x[1]);
            }
        }).filter(Objects::nonNull)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.
                                <Tuple2<Long, String>>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.f0));


        DataStream<Tuple2<Long, String>> dx = da.keyBy(1)

                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
//                .aggregate(new AggregateFunction<Tuple2<Long, String>, Object, Object>() {
//                    @Override
//                    public Object createAccumulator() {
//                        return null;
//
////                    @Override
////                    public Object add(Tuple2<Long, String> value, Object accumulator) {
////                        return null;
////                    }
////
////                    @Override
////                    public Object getResult(Object accumulator) {
////                        return null;
////                    }
////
////                    @Override
////                    public Object merge(Object a, Object b) {
////                        return null;
////                    }
////                })     }
//

                .trigger(new Trigger<Tuple2<Long, String>, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(Tuple2<Long, String> element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {

                        ValueState<Boolean> firstSeen = ctx.getPartitionedState(
                                new ValueStateDescriptor<Boolean>("first-seen", Types.BOOLEAN)
                        );
                        if(firstSeen.value() == null){
                            System.out.println("第一条数据来的时候 ctx.getCurrentWatermark() 的值是 " + ctx.getCurrentWatermark());
                            long t = (ctx.getCurrentWatermark() / 5000) * 5000 + 5000L;
                            ctx.registerEventTimeTimer(t);
                            ctx.registerEventTimeTimer(window.getEnd());
                            firstSeen.update(true);
                        }
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        if (time == window.getEnd()) {
                            return TriggerResult.FIRE_AND_PURGE;
                        } else {
                            long t = Math.max(ctx.getCurrentWatermark() / 5000 * 5000 + 5000L, time + 5000L);
                            System.out.println("打印当前watermark: " + ctx.getCurrentWatermark() +
                                    " 定时器时间： " + time +
                                    " 新定时器时间： " + t);
                            if (t < window.getEnd()) {
                                ctx.registerEventTimeTimer(t);
                            }
                            return TriggerResult.FIRE;
                        }
                    }

                    @Override
                    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
                        ValueState<Boolean> firstSeen = ctx.getPartitionedState(
                                new ValueStateDescriptor<Boolean>("first-seen", Types.BOOLEAN)
                        );
                        firstSeen.clear();
                    }
                }).process(new ProcessWindowFunction<Tuple2<Long, String>, Tuple2<Long, String>, Tuple, TimeWindow>() {
                    HashMap<String, Long> cache = new HashMap<String, Long>(){};
                    private MapState<String,String> v ;
                    @Override
                    public void process(Tuple tuple, Context context, Iterable<Tuple2<Long, String>> elements, Collector<Tuple2<Long, String>> out) throws Exception {

                        v = getRuntimeContext().getMapState(new MapStateDescriptor("qwr", String.class,String.class));
                        System.out.println("窗口开始" + String.valueOf(context.window().getStart())  +
                                " 窗口结束：" + String.valueOf(context.window().getEnd()));
                        for(Tuple2<Long, String> element:elements){
                            System.out.println("处理数据-- key: " + element.f1);
                            if(cache.get(element.f1) != null){
                                System.out.println("key已经存在: 新key-" + element.f1);
                                v.put(element.f1,String.valueOf(element.f0));
                            }else{
                                cache.put(element.f1, element.f0);
                                v.put(element.f1,String.valueOf(element.f0));
                            }

                        }
                        out.collect((Tuple2<Long, String>)
                                new Tuple2<Long,String>(Long.valueOf(cache.keySet().size()),"1"));
                    }
                });

//                //.aggregate();
//                .process(
//                        new ProcessWindowFunction<Tuple2<Long, String>,Long,Tuple, TimeWindow>() {
//                            HashMap<String, Long> cache = new HashMap<String, Long>(){};
//                            private MapState<String,String> v ;
//                            @Override
//                            public void process(Context context, Iterable<Tuple2<Long, String>> elements, Collector<Long> out) throws Exception {
//                                v = getRuntimeContext().getMapState(new MapStateDescriptor("qwr", String.class,String.class));
//                                System.out.println("窗口开始" + String.valueOf(context.window().getStart())  +
//                                        " 窗口结束：" + String.valueOf(context.window().getEnd()));
//                                for(Tuple2<Long, String> element:elements){
//                                    System.out.println("处理数据-- key: " + element.f1);
//                                    if(cache.get(element.f1) != null){
//                                        System.out.println("key已经存在: 新key-" + element.f1);
//                                        v.put(element.f1,String.valueOf(element.f0));
//                                    }else{
//                                        cache.put(element.f1, element.f0);
//                                        v.put(element.f1,String.valueOf(element.f0));
//                                    }
//
//                                }
//                                out.collect((long) cache.keySet().size());
//                            }
//                        }
//                );

        DataStream<Tuple2<Long, String>> a = dx.keyBy(1).map(new RichMapFunction<Tuple2<Long, String>, Tuple2<Long, String>>() {
            private MapState<String, String> v;

            @Override
            public Tuple2<Long, String> map(Tuple2<Long, String> value) throws Exception {
                getRuntimeContext().getMetricGroup().counter(1);
                System.out.println("@@@");
                v = getRuntimeContext().getMapState(new MapStateDescriptor("qwr", String.class, String.class));
                System.out.println(v.keys().toString());
                System.out.println(v.values().toString());
                for (String i : v.values()) {
                    System.out.println("@@");
                    System.out.println(i);
                }

                return value;
            }
        });
//        a.join(a).where().equalTo().window().allowedLateness().

        KeyedStream<Tuple2<Long, String>, Tuple> b = a.keyBy(1);


        dx.print();
        env.execute();
    }
}