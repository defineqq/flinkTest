package batch;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


public class batchandstreamtest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);//完全是流式处理的代码，只要设置执行模型  就会按照批处理的方式来执行


//      流计算的方式 读文件做wordcount
//        a a j j b b
//        a d d
//        a d b
        DataStreamSource<String> streamsource = env.readTextFile("a.txt");
        streamsource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] word = s.split(" ");
                for (String w : word) {
                    collector.collect(Tuple2.of(w, 1));
                }

            }
        }).keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f0;
            }
        }).sum(1).print();


        env.execute();












//        lambda表达式 的  wordcount

        DataStreamSource<String> streamSource = env.readTextFile("dstream-apps/src/test/java/batch/a.txt");

//        lamdba表达式 本质上单方法接口的 方法实现的 另一种语法表达
        streamSource.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                return null;
            }
        });
//        从map算子接受的mapfunction接口实现来看，他是一个单方法接口，所以这个接口的实现类的核心功能，就在他的实现方法 map 上，这种情况就可以用lamdba表达式来实现

//        lamdba表达式 怎么写 ,看你要实现的方法 接受 和 返回的结果 是什么 （参数1 参数2） ->  函数体
        streamSource.map((value) -> {return value.toUpperCase();});

//        由于上面的lamdba表达式，参数列表只有一个，且函数体（return value.toUpperCase();）只有一行代码，则可以简化
        streamSource.map(value -> value.toUpperCase());

//        由于上面的lamdba表达式，函数体只有一行代码，且其中的方法调用toUpperCase 没有参数传递，则可以将方法调用，转成方法引用
        SingleOutputStreamOperator<String> upperCased = streamSource.map(String::toUpperCase);

        upperCased.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> out) throws Exception {

            }
        });

//       从上面来看 flatMap 也是一个单方法接口，所以也可以用lamdba表达式实现。 编译完 泛型会被擦除，collect()内的东西 编译完 泛型会丢失，只知道是个Tuple2
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = upperCased.flatMap((String s, Collector<Tuple2<String, Integer>> out) -> {
            String[] words = s.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word, 1));
//               可以看到 在上面中  out是有泛型的，但是在lamdba里面，泛型丢失了，虽然不会报错 ，但是执行会报错的 所以返回的是个object类型的 SingleOutputStreamOperator<Object> wordAndOne

            }
        }).returns(new TypeHint<Tuple2<String,Integer>>(){});

        wordAndOne.keyBy(new KeySelector<Tuple2<String,Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f0;
            }
        });

//        同样的 也可以写成lamdba表达式的方式 他也是单方法接口 就是 一个Override

        KeyedStream<Tuple2<String, Integer>, String> keyedString = wordAndOne.keyBy(value -> value.f0);

        keyedString.sum(1).print();

        env.execute();

    }
}
