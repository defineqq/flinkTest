package apps.alg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class ProfileFeatureKeepAliveTest extends TestCase {

    public void testTestRun() throws Exception {


    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(30000);
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);
        // DataStreamSource<String> source = env.fromElements("{\"act\":1,\"uid\":\"232323\"}", "{\"act\":1,\"uid\":\"232323\"}", "{\"act\":1,\"uid\":\"232323\"}");

        SingleOutputStreamOperator<Object> sink = source.filter(new FilterFunction<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {

                        try {

                            JSONObject json = JSON.parseObject(value);
                            return json.getIntValue("act") == 1 && json.getString("uid") != null;

                        } catch (Exception e) {

                        }

                        return false;

                    }
                }).process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {

                        JSONObject json = JSON.parseObject(value);

                        System.out.println("process:" + value);
                        out.collect(json.getString("uid"));
                    }

                }).keyBy(new KeySelector<String, String>() {
                    @Override
                    public String getKey(String value) throws Exception {
                        return value;
                    }
                }).window(TumblingProcessingTimeWindows.of(Time.minutes(1)))

                .reduce(new ReduceFunction<String>() {
                    @Override
                    public String reduce(String value1, String value2) throws Exception {
                        System.out.println("tran：" + value2);
                        return value2;
                    }
                }).process(new ProcessFunction<String, Object>() {
                    @Override
                    public void processElement(String value, ProcessFunction<String, Object>.Context ctx, Collector<Object> out) throws Exception {
                        System.out.println("result：" + value);
                        out.collect(value);
                    }
                });
        sink.print();

        env.execute("test");
    }
}