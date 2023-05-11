import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;


//source ~/.bash_profile
//本地mysql 用户root  密码 11111111
//127.0.0.1
///usr/local/mysql/data

public class cdctest {


    public static void main(String[] args) throws Exception {
        System.out.println("啥啥啥");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
//        env.getCheckpointConfig().setCheckpointStorage("/");
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env);


        tenv.executeSql("create table flinka \n" +
                "(\n" +
                "        id int, \n" +
                "        primary key(id) not enforced \n" +
                ") with (\n" +
                "        'connector' = 'mysql-cdc', \n" +
                "        'hostname'='localhost', \n" +
                "        'port'='3306', \n" +
                "        'username'='root', \n" +
                "        'password'='11111111', \n" +
                "        'database-name'='mysql', \n" +
                "        'table-name'='a'\n" +
                ")"
        );

        tenv.executeSql("select * from flinka").print();



//        env.execute();




    }


}
