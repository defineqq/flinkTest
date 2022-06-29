package com.cp.data.exposure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cp.data.exposure.bean.ImFollow;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Properties;

@Slf4j
public class Metrics {

  private static final String CONFIG_ARGUMENT = "setting";


  /**
   * Flink 入口函数
   */
  public static void main(String[] args) throws Exception {

    // 解析配置信息
    // 将配置信息放置在全局参数中，使每个task 均可读取到配置

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();





    // 全局配置参数, 反序列化
//    BizConfiguration config = ConfigTool.deserializeFromUrl(url);


// start a checkpoint every 1000 ms
    env.enableCheckpointing(100000);
// advanced options:
// set mode to exactly-once (this is the default)
    env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
// checkpoints have to complete within one minute, or are discarded
    env.getCheckpointConfig().setCheckpointTimeout(60000);
// make sure 500 ms of progress happen between checkpoints
    env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
// allow only one checkpoint to be in progress at the same time
    env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
// enable externalized checkpoints which are retained after job cancellation
    env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
// This determines if a task will be failed if an error occurs in the execution of the task’s checkpoint procedure.
    env.getCheckpointConfig().setFailOnCheckpointingErrors(true);
    env.setRestartStrategy(RestartStrategies.noRestart());//失败不重启 直接退出
    StateBackend stateBackend = new RocksDBStateBackend("hdfs://Joyme/checkpoint/who2follow", true);
    env.setStateBackend(stateBackend);


    // 配置kafka source
    // 当前值支持kafka source
    FlinkKafkaConsumer<String> consumer011 = createKafkaSource();


    SingleOutputStreamOperator<String> dataStream = env.addSource(consumer011);

    SingleOutputStreamOperator<String> Im_follow = dataStream.filter(new FilterFunction<String>() {
      @Override
      public boolean filter(String s) throws Exception {
        JSONObject ad = JSON.parseObject(s);
        boolean rs = false;

        try{
          if(

                  ad.getString("uid") != null
                  && ad.getString("vid") != null
                  && !"".equals(ad.getString("uid"))
                  && !ad.getString("vid").equals("")

          ){
            rs = true;
          }
        }catch (Exception e) {
          log.error(e.toString());
        }
//        finally {
          return rs;
//        }
      }
    });

    SingleOutputStreamOperator<ImFollow> Im_JSON = Im_follow.map(new MapFunction<String, ImFollow>() {
      @Override
      public ImFollow map(String s) throws Exception {
        JSONObject json = JSON.parseObject(s);
        String uid = json.getString("uid") == null ?"":json.getString("uid");
        String vid = json.getString("vid") == null ?"":json.getString("vid");

        return new ImFollow(uid,vid);

      }
    });




    SingleOutputStreamOperator<Tuple3<String,String,Long>> distinct = Im_JSON.flatMap(new FlatMapFunction<ImFollow, Tuple3<String,String,Long>>() {

      @Override
      public void flatMap(ImFollow imFollow, Collector<Tuple3<String,String,Long>> collector) throws Exception {
        collector.collect(new Tuple3<String,String,Long>(imFollow.getVid(),imFollow.getUid(),1L));

      }
    }).keyBy(0,1)
//            .timeWindow(Time.hours(1))
            .window(ProcessingTimeSessionWindows.withGap(Time.minutes(1)))  //会话窗口 1分钟内没有重复数据 就会自动触发 从每个记录来的那一刻开始统计时间
//            .apply(new WindowFunction<Tuple3<String, String, Long>, Object, Tuple, TimeWindow>() {
//              @Override
//              public void apply(Tuple tuple, TimeWindow timeWindow, Iterable<Tuple3<String, String, Long>> iterable, Collector<Object> collector) throws Exception {
//              }
//            })
//           如上2min的窗口划分产生的窗口类似于[00: 00, 00: 02), [00: 02, 00: 04) ...。如果某条消息在00: 01到达，那么1min之后该窗口就会触发计算。这样数据重复检测的时间跨度就缩小为了1min，这样会影响去重的效果。更精确
//            .process(new ProcessWindowFunction<WordWithCount, Object, Tuple, TimeWindow>() {
//              @Override
//              public void process(Tuple tuple, Context context, Iterable<WordWithCount> iterable, Collector<Object> collector) throws Exception {
//              }
//            })
            .reduce(new ReduceFunction<Tuple3<String,String,Long>>() {

              @Override
              public Tuple3<String,String,Long> reduce(Tuple3<String,String,Long> t1, Tuple3<String,String,Long> t2) throws Exception {
                return t1;//两条相同的数据 保留一条

              }
            })
            ;

    //reduce 数据来立即调用 process窗口触发调用
    
    
    

    SingleOutputStreamOperator<Tuple2<String,Long>> count = distinct.flatMap(new FlatMapFunction<Tuple3<String,String,Long>, Tuple2<String,Long>>() {
      @Override
      public void flatMap(Tuple3<String,String,Long> wordWithCount, Collector<Tuple2<String,Long>> collector) throws Exception {
        collector.collect(new Tuple2<String,Long>(wordWithCount.f0,1L));
      }
    }).keyBy(0)
            .timeWindow(Time.minutes(1))
            .sum(1);

    count.print();

//    Map<String, Object> conn = new HashMap<String, Object>();
//
//    conn.put("jdbc","jdbc:mysql://ecbigdata.czcrimydwklw.us-east-1.rds.amazonaws.com:3306/ecbigdata");
//    conn.put("user","ecbigdata_pro");
//    conn.put("pwd","y98aqn97a6eaw073xdhkttb5o6bqs46");
//
//    Map<String, Object> p = new HashMap<String, Object>();
//
//    p.put("conn",conn);
//    count.addSink(new MySqlSink(p));

//
//    HalfHourRankingImp redisSink = HalfHourRankingImp
//            .builder()
//            .dataRedisHost(dataRedisHost)
//            .dataRedisPort(dataRedisPort)
//            .dataRedisDB(dataRedisDB)
//            .flagRedisHost(flagRedisHost)
//            .flagRedisPort(flagRedisPort)
//            .flagRedisDB(flagRedisDB)
//            .build();
//    transformedStream.addSink(redisSink).name("RedisSink").uid("RedisSink").setParallelism(dataRedisParallelism);



    env.execute("cp_ads_video_user_most");
  }


  private static FlinkKafkaConsumer<String> createKafkaSource() {
    Properties props = new Properties();
    //神策的数据
    props.setProperty("bootstrap.servers", "10.66.100.37:9092,10.66.100.45:9092,10.66.100.83:9092,10.66.100.215:9092,10.66.100.198:9092,10.66.100.168:9092,10.66.100.80:9092,10.66.100.197:9092");
    props.setProperty("group.id", "pull_vid");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.offset.reset", "latest");
    props.setProperty("max.partition.fetch.bytes", "256");
    return new FlinkKafkaConsumer<>("cp_pull_vid_quality", new SimpleStringSchema(), props);
  }




}

//kafka示例数据
//kafka-console-consumer.sh --bootstrap-server 10.66.100.37:9092 --topic cp_pull_vid_quality
//{"vid":"16251367061947079565","ver":"4.3.80","extractor":{"extractor_host":"cp-data-report-03/10.66.102.205","extractor_name":"PullVidQuality","request_ip":"94.205.51.122","request_time":"2021-07-01 19:10:28","extractor_interceptTime":"2021-07-01 19:10:29","collector_url":"media_pull_quality_data"},"stat":[{"vrp":0,"acache":0,"connectime":0,"blocktime":0,"vcache":0,"blockcnt":0,"vrs":0,"sblockcnt":0,"fstime":0,"delaytime":0,"ars":0,"t":0,"arp":0,"dnstime":0,"sblocktime":0}],"serverip":"","os":"iOS","uid":"794530649653772288","timestamp":1625137828,"playurl":"http://joy-oclive.linkv.fun/yolo/16251367061947079565.flv?wsSecret=9a8454cc2e94898eb06477d07a0459be&wsABStime=60de2ae2"}
//alias bigdata_ford='mysql -hbigdata-ford.czcrimydwklw.us-east-1.rds.amazonaws.com -P3306 -ubigdata_ford_pro -p7rq8ipypv8pphps9xhvbmnhlm7r8bi -Dbigdata_ford'