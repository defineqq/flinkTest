//package batch;
//
//
//import org.apache.flink.api.common.typeinfo.Types;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.datastream.DataStreamSource;
//import org.apache.flink.streaming.api.datastream.KeyedStream;
//import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.ProcessFunction;
//import org.apache.flink.util.Collector;
//import org.apache.flink.util.OutputTag;
//
//
//public class coGrouptest {
//    public static void main(String[] args) throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
////        id name
//        DataStreamSource<String> stream1 = env.socketTextStream("localhost", 9998);
////        id age
//        DataStreamSource<String> stream2 = env.socketTextStream("localhost", 9999);
//// nc -lp 9999
//// nc -lp 9998
//
//        SingleOutputStreamOperator<Tuple2<String, String>> s1 = stream1.process(new ProcessFunction<String, Tuple2<String, String>>() {
//            //            可以使用open方法，可以调用getruntimecontext
//            @Override
//            public void open(Configuration parameters) throws Exception {
//                super.open(parameters);
//            }
//
//            //           可以使用close方法，可以调用getruntimecontext
//            @Override
//            public void close() throws Exception {
//                super.close();
//            }
//
//            @Override
//            public void processElement(String s, Context context, Collector<Tuple2<String, String>> collector) throws Exception {
////测流输出
//                context.output(new OutputTag<String>("s1", Types.STRING), s);//测流输出
////主流输出
//                String[] arr = s.split(",");
//                collector.collect(Tuple2.of(arr[0], arr[1]));
//            }
//        });
//
//        KeyedStream<Tuple2<String, String>, String> keyedStream = s1.keyBy(tp2 -> tp2.f0);
//
//        keyedStream.process(new )
//}
//}
