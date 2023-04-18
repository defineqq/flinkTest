import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

///这里用的是flink 1.14.5版本的flink
public class catalogtest {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env);
        tenv.createTable("table_a",
                TableDescriptor.forConnector("filesystem").schema(Schema.newBuilder()
                    .column("id", DataTypes.INT())
                    .column("name", DataTypes.STRING())
                    .column("age", DataTypes.INT())
                    .column("gender", DataTypes.STRING())
                .build())
                .format("csv")
                .option("path","/Users/congpeng/Desktop/flinkTest/catalog/src/main/resources/")
                .option("csv.ignore-parse-errors","true")
                .option("csv.allow-comments","true")
                .build()
                );



    }


}
