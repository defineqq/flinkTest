package Launcher;


import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;

import java.time.Duration;
//nc -lk 9999
public class sinkFunction {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> stream = env.socketTextStream("localhost", 9999);
//        stream.assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks()); //禁用时间时间的推进机制
//        stream.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps()); //紧跟最大时间时间
//        stream.assignTimestampsAndWatermarks(WatermarkStrategy.forGenerator()); //自定义watermark生成算法
        SingleOutputStreamOperator<String> as = stream.assignTimestampsAndWatermarks(WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                    @Override
                    public long extractTimestamp(String s, long l) {
                        return Long.parseLong(s.split(",")[0]);
                    }
                }));

        as.windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10))).reduce(new ReduceFunction<String>() {
            @Override
            public String reduce(String s, String t1) throws Exception {
                return null;
            }
        });

        as.windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10))).trigger(new Trigger<String, TimeWindow>() {
            @Override
            public TriggerResult onElement(String s, long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                return null;
            }

            @Override
            public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                return null;
            }

            @Override
            public TriggerResult onEventTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                return null;
            }

            @Override
            public void clear(TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {

            }
        }).evictor(new Evictor<String, TimeWindow>() {
            @Override
            public void evictBefore(Iterable<TimestampedValue<String>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

            }

            @Override
            public void evictAfter(Iterable<TimestampedValue<String>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

            }
        });

        stream.print();
        env.execute();

    }
}
