import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.hive.HiveCatalog;

///这里用的是flink 1.14.5版本的flink
public class catalogtest {


    public static void main(String[] args) throws Exception {
        System.out.println("啥啥啥");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env);
        HiveCatalog hivecatalog = new HiveCatalog("hive","default","conf/hiveconf/hive-site.xml");
        tenv.registerCatalog("mycatalog",hivecatalog);

        tenv.executeSql("create table mycatalog.table1");
        tenv.executeSql("select * from  mycatalog.default.table1");//没有指定数据库，默认存放到了hive的default 中

        tenv.executeSql("create view mycatalog.default.view1");
        tenv.executeSql("select * from  mycatalog.default.view1");

        tenv.executeSql("use catalog mycatalog");
        tenv.executeSql("show catalogs").print();
        tenv.executeSql("use catalog default");
        tenv.executeSql("show catalogs").print();

        tenv.executeSql("create temporary table mycatalog.table1");
        tenv.listCatalogs()''

    }


}
