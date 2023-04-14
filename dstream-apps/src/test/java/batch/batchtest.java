package batch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


public class batchtest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ExecutionEnvironment envBatch = ExecutionEnvironment.getExecutionEnvironment();


//        批处理实现一个wordcount  数据：
//        a a j j b b
//        a d d
//        a d b

        // 从 DataSource 的 operator能看到 ，其实获取的是一个dataset的数据集
        DataSource<String> stringDataSource = envBatch.readTextFile("a.txt");
        //插一句 map和mappartition 区别，这两个算子啊在调用用户函数时调用机制不同
        // mappartition 每个分区会调用一次  ,mappartition 给一个迭代器 一个partition每条数据会调用一次，
        // map是整个任务调用一次  是给一个element => 操作
        stringDataSource.flatMap(new FlatMapFunction<String, Tuple2<String,Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] word = s.split(" ");
                for (String w : word){
                    collector.collect(Tuple2.of(w,1));
                }

            }
        }).groupBy(0) //stream中是keyby  ctrl + p 提示
        .sum(1)
        .print();



    }
}
