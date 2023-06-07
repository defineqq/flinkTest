import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

//source ~/.bash_profile
//本地mysql 用户root  密码 11111111
//mysql -uroot -p11111111
//use mysql;
///usr/local/mysql/data

public class sqllookupjoin {


    public static void main(String[] args) throws Exception {
        System.out.println("啥啥啥");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        //        env.getCheckpointConfig().setCheckpointStorage("/");
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env);
        tenv.getConfig().getConfiguration().setLong("table.exec.state.ttl", 60 * 60 * 1000L);

        /**
         * 1,a
         * 2,b
         * 3,c
         * 4,d
         * 5,e
         */
        //nc -lk 9999
        DataStreamSource<String> s1 = env.socketTextStream("localhost", 9997);
        SingleOutputStreamOperator<Tuple2<Integer, String>> ss1 = s1.map(
            new MapFunction<String, Tuple2<Integer, String>>() {
                @Override
                public Tuple2<Integer, String> map(String s) throws Exception {
                    String[] arr = s.split(",");

                    return new Tuple2<Integer, String>(Integer.parseInt(arr[0]), arr[1]);
                }
            });

        //创建主表 需要声明处理时间属性字段
        tenv.createTemporaryView("a",ss1, Schema.newBuilder()
            .column("f0", DataTypes.INT())
            .column("f1", DataTypes.STRING())
            .columnByExpression("pt","proctime()")
            //.columnByExpression("rt","to_timestamp_ltz(f2,3)")
            //.watermark("rt","rt - interval '0' second")
            .build());
        tenv.executeSql("desc a").print();

        //创建lookup维表（jdbc connector表）

        tenv.executeSql("create table b \n" +
            "(\n" +
            "        id int, \n" +
            "        gender string, \n" +
            "        name string, \n" +
            "        primary key(id) not enforced \n" +
            ") with (\n" +
            "        'connector' = 'jdbc', \n" +
            "        'url'='jdbc:mysql://localhost:3306/mysql', \n" +
            "        'username'='root', \n" +
            "        'password'='11111111', \n" +
            "        'table-name'='score_rank' \n" +
            ")"
        );
        tenv.executeSql("desc b").print();

        // lookup join 查询
        tenv.executeSql("select a.*,c.*   from  a  JOIN  b FOR SYSTEM_TIME AS OF a.pt AS c  \n" +
            "    ON a.f0 = c.id").print();
        // lookup join 查询
        tenv.executeSql("select * from a").print();

        env.execute();




    }


}
