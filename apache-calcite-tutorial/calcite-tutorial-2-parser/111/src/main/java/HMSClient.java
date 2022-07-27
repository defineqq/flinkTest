//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hive.metastore.IMetaStoreClient;
//import org.apache.hadoop.hive.metastore.RetryingMetaStoreClient;
//import org.apache.hadoop.hive.metastore.api.MetaException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
//public class HMSClient {
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(HMSClient.class);
//
//    /**
//     * 初始化HMS连接
//     * @param conf org.apache.hadoop.conf.Configuration
//     * @return IMetaStoreClient
//     * @throws MetaException 异常
//     */
//    public static IMetaStoreClient init(Configuration conf) throws MetaException {
//        try {
//            return RetryingMetaStoreClient.getProxy(conf, false);
//        } catch (MetaException e) {
//            LOGGER.error("hms连接失败", e);
//            throw e;
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//
//        Configuration conf = new Configuration();
//        conf.set("hive.metastore.uris", "thrift://10.66.206.254:10000");
//
//        // conf.addResource("hive-site.xml");
//        IMetaStoreClient client = HMSClient.init(conf);
//
//        System.out.println("----------------------------获取所有catalogs-------------------------------------");
//        client.getCatalogs().forEach(System.out::println);
//
//        System.out.println("------------------------获取catalog为hive的描述信息--------------------------------");
//        System.out.println(client.getCatalog("hive").toString());
//
//        System.out.println("--------------------获取catalog为hive的所有database-------------------------------");
//        client.getAllDatabases("hive").forEach(System.out::println);
//
//        System.out.println("---------------获取catalog为hive，database为hive的描述信息--------------------------");
//        System.out.println(client.getDatabase("hive", "hive_storage"));
//
//        System.out.println("-----------获取catalog为hive，database名为hive_storage下的所有表--------------------");
//        client.getTables("hive", "hive_storage", "*").forEach(System.out::println);
//
//        System.out.println("------获取catalog为hive，database名为hive_storage，表名为sample_table_1的描述信息-----");
//        System.out.println(client.getTable("hive", "hive_storage", "sample_table_1").toString());
//    }
//}
//
//
