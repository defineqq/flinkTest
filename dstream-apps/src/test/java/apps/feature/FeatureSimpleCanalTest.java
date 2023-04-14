package apps.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import constant.FeatureName;
import junit.framework.TestCase;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureSimpleCanalTest extends TestCase implements Serializable {

    public void testGetTopicColumnMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (String[] topic_columns : FeatureName.SAMPLE_TOPIC_COLUMN) {
            String topicName = topic_columns[0];
            String columns = topic_columns[1];
            Map<String, String> col_map = new HashMap<>();
            String[] column_arr = columns.split("\\|");
            for (String column_str : column_arr) {
                String[] column_str_arr = column_str.split(":");
                String s_col = column_str_arr[0];
                String t_col = column_str_arr[1];
                col_map.put(s_col, t_col);

            }

            map.put(topicName, col_map);
        }

        map.forEach((w, i) -> System.out.println("topic:" + w + ",item:" + i));
    }

    public void testTestRun() {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        run(env, null);

        try {
            env.execute("test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(apps.feature.FeatureSimpleCanal.class);
    private static Map<String, String> user_col_map = getColMap(FeatureName.SAMPLE_FEATURE_USER);
    private static Map<String, String> anchor_col_map = getColMap(FeatureName.SAMPLE_FEATURE_ANCHOR);

    public void run(StreamExecutionEnvironment env, ParameterTool tool) {

        Map<String, Map<String, String>> topci_col_map = getTopicColumnMap();
        DataStream<Tuple2<String, String>> source = init(env, topci_col_map);

        source.filter(value -> filter_fun(value.f0, value.f1.trim(), topci_col_map.get(value.f0)))
                .process(new ProcessFunction<Tuple2<String, String>, Object>() {
                    @Override
                    public void processElement(Tuple2<String, String> inputObject, Context context, Collector<Object> collector) throws Exception {


                        String source_topic = inputObject.f0;

                        String s = inputObject.f1;

                        //每个topic有一些字段指标
                        Map<String, String> col_map = topci_col_map.get(source_topic);
                        if (s == null || s.length() == 0) {

                            return;
                        }
                        JSONObject json = JSON.parseObject(s);
                        if (json == null || json.isEmpty()) {
                            logger.warn("==== json is empty");
                            return;
                        }
                        JSONObject data_json = json.getJSONObject("Data");

                        if (data_json == null || data_json.isEmpty()) {
                            logger.warn("==== data_json is empty");
                            return;
                        }
                        String uid = data_json.getString("uid");
                        if (uid == null || uid.isEmpty() || uid.length() == 0) {
                            logger.warn("==== uid is empty");
                            return;
                        }
//                      编码每个topic对应需要指标的字段
                        for (String col : col_map.keySet()) {
                            String fea_name = col_map.get(col);//获取特征名称
                            String col_value = data_json.getString(col);//获取特征名称json中的值
                            if (col_value != null) {
                                //特殊对待，特征值需要手动指定
                                if (source_topic.equals("liveme_user_finance_cl_bag_canal")) {
                                    String msg_type = data_json.getString("type");
                                    String item_id = data_json.getString("item_id");
                                    if (msg_type.equals("999") && item_id.equals("1")) {
                                        fea_name = "gold_bal";
                                    } else if (msg_type.equals("999") && item_id.equals("4")) {
                                        fea_name = "diamond_bal";
                                    }

                                } else if (source_topic.equals("liveme_cl_user_extend_attr_canal")) {
                                    String col_val = String.copyValueOf(fea_name.toCharArray());
                                    if (col_value.equals("birthday")) {
                                        fea_name = "birthday";
                                    } else if (col_value.equals("countryCode")) {
                                        fea_name = "country";
                                    } else if (col_value.equals("is_verified")) {
                                        fea_name = "is_verified";
                                    }

                                    col_value = data_json.getString(col_val);
                                }
                                if (null != col_value && null != fea_name && uid != null) {
                                    save2redis(user_col_map, fea_name, col_value, uid, "alg:feature:user:profile:v1:");
                                    save2redis(anchor_col_map, fea_name, col_value, uid, "alg:feature:anchor:profile:v1:");
                                }
                            }
                        }

                        collector.collect(uid);
                    }
                })
        ;

    }


    private DataStream<Tuple2<String, String>> init(StreamExecutionEnvironment env, Map<String, Map<String, String>> topci_col_map) {
        DataStreamSource<Tuple2<String, String>> source = null;

        //注册消息
        List<DataStreamSource<Tuple2<String, String>>> list = new ArrayList<>();

        for (int i = 0; i < SAMPLE_TOPIC_COLUMN.length; i++
        ) {
            Tuple2<String, String> item = SAMPLE_TOPIC_COLUMN[i];
            DataStreamSource<Tuple2<String, String>> source1 = env.fromElements(item);
            list.add(source1);
        }

        return list.get(0).union(list.get(1)).union(list.get(2)).union(list.get(3))
                .union(list.get(4)).union(list.get(5)).union(list.get(6)).union(list.get(7));

    }

    private Tuple2<String, String>[] SAMPLE_TOPIC_COLUMN = new Tuple2[]{
            new Tuple2("liveme_cl_user_default_attr_canal", "{\"Data\":{\"uid\":\"1600776866183624705\",\"gender\":1,\"last_login_time\":1670557941,\"updated_at\":\"2022-12-09 03:52:22\",\"project_id\":1,\"reg_time\":1670557941,\"nickname\":\"Hope-323518997\",\"created_at\":\"2022-12-09 03:52:22\",\"id\":252110,\"avatar\":\"\",\"deleted_at\":null,\"status\":0},\"Type\":\"INSERT\",\"BinLogFilename\":\"mysql-bin-changelog.148774\",\"Database\":\"athena_main\",\"Table\":\"cl_user_default_attr_0018\",\"BinLogPosition\":992271,\"Offset\":\"cl_user_default_attr_0018:992271\",\"Timestamp\":\"1670557942000\",\"ServerId\":87391880,\"KeyNames\":[\"id\"],\"KeyValues\":[252110]}")
            , new Tuple2("liveme_cl_user_extend_attr_canal", "{\"OldData\":{\"val\":\"1670495675\",\"uid\":\"1600774643477787649\",\"encrypted\":0,\"updated_at\":\"2022-12-08 10:34:35\",\"project_id\":1,\"created_at\":\"2022-12-08 08:49:56\",\"id\":22542607,\"deleted_at\":null,\"key\":\"last_active_time\"},\"Data\":{\"val\":\"1670496298\",\"uid\":\"1600774643477787649\",\"encrypted\":0,\"updated_at\":\"2022-12-08 10:44:58\",\"project_id\":1,\"created_at\":\"2022-12-08 08:49:56\",\"id\":22542607,\"deleted_at\":null,\"key\":\"last_active_time\"},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.148551\",\"Database\":\"athena_main\",\"Table\":\"cl_user_extend_attr_008e\",\"BinLogPosition\":3081028,\"Offset\":\"cl_user_extend_attr_008e:3081028\",\"Timestamp\":\"1670496298000\",\"ServerId\":87391880,\"KeyNames\":[\"id\"],\"KeyValues\":[22542607]}")
            , new Tuple2("liveme_following_count_canal", "{\"OldData\":{\"uid\":1513481239212833793,\"num\":24,\"ctime\":\"2022-04-11 11:39:18\",\"mtime\":\"2022-12-08 10:40:25\"},\"Data\":{\"uid\":1513481239212833793,\"num\":25,\"ctime\":\"2022-04-11 11:39:18\",\"mtime\":\"2022-12-08 10:45:59\"},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.150213\",\"Database\":\"cheetahlivefriends\",\"Table\":\"following_count_169\",\"BinLogPosition\":294081,\"Offset\":\"following_count_169:294081\",\"Timestamp\":\"1670496358000\",\"ServerId\":1794560962,\"KeyNames\":[\"uid\"],\"KeyValues\":[1513481239212833793]}")
            , new Tuple2("liveme_cl_user_level_canal", "{\"OldData\":{\"uid\":\"1088447207767678976\",\"updated_at\":\"2022-12-08 18:46:40\",\"level\":71,\"anchor_level\":38,\"anchor_exp\":3002621,\"created_at\":\"2019-01-24 22:43:32\",\"id\":294170,\"exp\":7409464},\"Data\":{\"uid\":\"1088447207767678976\",\"updated_at\":\"2022-12-08 18:46:41\",\"level\":71,\"anchor_level\":38,\"anchor_exp\":3002621,\"created_at\":\"2019-01-24 22:43:32\",\"id\":294170,\"exp\":7409465},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.151632\",\"Database\":\"liveme\",\"Table\":\"cl_level_user_49\",\"BinLogPosition\":1308253,\"Offset\":\"cl_level_user_49:1308253\",\"Timestamp\":\"1670496401000\",\"ServerId\":1198724056,\"KeyNames\":[\"id\"],\"KeyValues\":[294170]}")
            , new Tuple2("liveme_user_finance_cl_bag_canal", "{\"OldData\":{\"ext\":\"\",\"uid\":\"1524066354174176257\",\"updated_at\":\"2022-12-08 10:49:33\",\"item_id\":3695,\"cnt\":1,\"created_at\":\"2022-05-10 16:38:47\",\"expiration\":4200000000,\"id\":1324780,\"type\":2,\"app_id\":10018},\"Data\":{\"ext\":\"\",\"uid\":\"1524066354174176257\",\"updated_at\":\"2022-12-08 10:49:35\",\"item_id\":3695,\"cnt\":0,\"created_at\":\"2022-05-10 16:38:47\",\"expiration\":4200000000,\"id\":1324780,\"type\":2,\"app_id\":10018},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.046901\",\"Database\":\"liveme_user_finance\",\"Table\":\"cl_bag_1b\",\"BinLogPosition\":132348829,\"Offset\":\"cl_bag_1b:132348829\",\"Timestamp\":\"1670496574000\",\"ServerId\":1765325179,\"KeyNames\":[\"id\"],\"KeyValues\":[1324780]}") //"cnt:gold_bal&type=999&item_id=1|cnt:diamond_bal&type=999&item_id=4"
            , new Tuple2("liveme_cl_user_vip_level_canal", "{\"OldData\":{\"ext\":\"\",\"uid\":\"1586866415622928385\",\"updated_at\":\"2022-11-02 13:21:50\",\"level\":2,\"created_at\":\"2022-11-02 13:21:50\",\"id\":98140,\"type\":3,\"exp\":349,\"app_id\":1},\"Data\":{\"ext\":\"\",\"uid\":\"1586866415622928385\",\"updated_at\":\"2022-11-30 13:21:50\",\"level\":1,\"created_at\":\"2022-11-02 13:21:50\",\"id\":98140,\"type\":3,\"exp\":348,\"app_id\":1},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.057713\",\"Database\":\"livemefinancex\",\"Table\":\"cl_level_7a\",\"BinLogPosition\":52328699,\"Offset\":\"cl_level_7a:52328699\",\"Timestamp\":\"1669814510000\",\"ServerId\":2001744086,\"KeyNames\":[\"id\"],\"KeyValues\":[98140]}")
            , new Tuple2("liveme_cms_liveme_signeranchor_canal", "{\"OldData\":{\"contract_end\":1698854399,\"brokerage_company_op_id\":0,\"second_rank\":\"D\",\"contract_id\":31,\"created_at\":\"2022-10-11 01:30:51\",\"signer_anchor_mobile_phone\":\"573203103424\",\"source\":2,\"third_rank\":null,\"brokerage_company\":\"\",\"contract_start\":1667286000,\"score\":42.0,\"uid\":\"1577976236493966337\",\"times\":3,\"trial_link\":null,\"updated_at\":\"2022-11-24 05:00:40\",\"influencer\":null,\"content_type\":null,\"payment_id\":\"\",\"content_readablity\":null,\"is_amateur\":1,\"support_start\":1667372400,\"id\":154847,\"app_id\":1,\"experienced\":null,\"verify_type\":19,\"area\":\"A_US\",\"from_competitive_products\":null,\"content_interactivity\":null,\"is_auto_renewal\":1,\"reject_reason\":\"\",\"content_type_other\":null,\"op_username\":\"isabela.nascimento@liveme.com\",\"appearance_level\":null,\"signer_anchor_email\":\"Emilyhernandezsanchez95@gmail.com\",\"is_delete\":0,\"contract_shared_revenue_ratio\":null,\"contract_type\":\"EE&SA\",\"first_rank\":\"B\",\"op_uid\":645,\"brokerage_company_id\":1354,\"name\":null,\"brokerage_company_op\":\"\",\"signer_anchor_address\":null,\"current_rank\":\"D\",\"company_settle_id\":-1,\"status\":0,\"whats_app_account\":null},\"Data\":{\"contract_end\":1698854399,\"brokerage_company_op_id\":0,\"second_rank\":\"D\",\"contract_id\":31,\"created_at\":\"2022-10-11 01:30:51\",\"signer_anchor_mobile_phone\":\"573203103424\",\"source\":2,\"third_rank\":null,\"brokerage_company\":\"\",\"contract_start\":1667286000,\"score\":42.0,\"uid\":\"1577976236493966337\",\"times\":4,\"trial_link\":null,\"updated_at\":\"2022-12-01 05:00:43\",\"influencer\":null,\"content_type\":null,\"payment_id\":\"\",\"content_readablity\":null,\"is_amateur\":1,\"support_start\":1667372400,\"id\":154847,\"app_id\":1,\"experienced\":null,\"verify_type\":19,\"area\":\"A_US\",\"from_competitive_products\":null,\"content_interactivity\":null,\"is_auto_renewal\":1,\"reject_reason\":\"\",\"content_type_other\":null,\"op_username\":\"isabela.nascimento@liveme.com\",\"appearance_level\":null,\"signer_anchor_email\":\"Emilyhernandezsanchez95@gmail.com\",\"is_delete\":0,\"contract_shared_revenue_ratio\":null,\"contract_type\":\"EE&SA\",\"first_rank\":\"B\",\"op_uid\":645,\"brokerage_company_id\":1354,\"name\":null,\"brokerage_company_op\":\"\",\"signer_anchor_address\":null,\"current_rank\":\"D\",\"company_settle_id\":-1,\"status\":0,\"whats_app_account\":null},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.360926\",\"Database\":\"new_cms_prd\",\"Table\":\"liveme_signeranchor\",\"BinLogPosition\":3134284,\"Offset\":\"liveme_signeranchor:3134284\",\"Timestamp\":\"1669870843000\",\"ServerId\":613829889,\"KeyNames\":[\"id\"],\"KeyValues\":[154847]}")
            , new Tuple2("liveme_follower_count_canal", "{\"OldData\":{\"uid\":1326316009178669057,\"num\":1984,\"ctime\":\"2021-10-03 21:30:54\",\"mtime\":\"2022-11-29 09:41:59\"},\"Data\":{\"uid\":1326316009178669057,\"num\":1983,\"ctime\":\"2021-10-03 21:30:54\",\"mtime\":\"2022-11-30 10:01:18\"},\"Type\":\"UPDATE\",\"BinLogFilename\":\"mysql-bin-changelog.147700\",\"Database\":\"cheetahlivefriends\",\"Table\":\"follower_count_286\",\"BinLogPosition\":277789,\"Offset\":\"follower_count_286:277789\",\"Timestamp\":\"1669802478000\",\"ServerId\":1794560962,\"KeyNames\":[\"uid\"],\"KeyValues\":[1326316009178669057]}")
    };

    private void save2redis(Map<String, String> fe_map, String fea_name, String col_value, String uid, String key_pre) {
        if (fe_map.containsKey(fea_name)) {
            String user_key = key_pre + uid;
            String t_filed = fe_map.get(fea_name);
            System.out.println("redis:" + user_key + ",filed:" + t_filed + ",val:" + col_value);
        }
    }

    private Boolean filter_fun(String topic, String value, Map<String, String> col_map) {
        if (null != value && value.length() > 0) {
            JSONObject json = JSON.parseObject(value);

            String sql_type = json.getString("Type");
            if (null == sql_type || sql_type.length() == 0) {
                logger.warn("type is empty");
                return false;
            }
            if (sql_type.equals("DELETE")) {
                return false;
            }
            JSONObject new_json = json.getJSONObject("Data");
            if (null == new_json || new_json.isEmpty()) {
                logger.warn("new_json is empty");
                return false;
            }
            String uid = new_json.getString("uid");
            if (uid == null || uid.isEmpty() || uid.length() == 0) {
                logger.warn("==== uid is empty");
                return false;
            }
            if (topic.equals("liveme_user_finance_cl_bag_canal")) {
                String msg_type = new_json.getString("type");
                String item_id = new_json.getString("item_id");

                if (null == msg_type || null == item_id) {
                    logger.warn("msg_type or item_id is empty");
                    return false;
                }

                if (msg_type.equals("999") && (item_id.equals("1") || item_id.equals("4"))) {
                    return true;
                }
            } else if (topic.equals("liveme_cl_user_extend_attr_canal")) {

                String key = new_json.getString("key");
                if (null == key) {
                    logger.warn("key is empty");
                    return false;
                }
//                System.out.println("filter key is " + key);
                if (key.equals("birthday") || key.equals("countryCode") || key.equals("is_verified")) {
                    return true;
                }
            } else if (sql_type.equals("INSERT")) {
                return true;
            } else if (sql_type.equals("UPDATE")) {
                for (String col : col_map.keySet()) {
                    JSONObject old_json = json.getJSONObject("OldData");


                    String old_value = String.valueOf(old_json.get(col));

                    String new_value = String.valueOf(new_json.get(col));

                    if (old_value != null && new_value != null && !old_value.equals(new_value)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Map<String, String> getColMap(String[][] col_array) {
        Map<String, String> map = new HashMap<>();

        for (String[] topic_columns : col_array) {
            map.put(topic_columns[0], topic_columns[1]);
        }
        return map;

    }


    private Map<String, Map<String, String>> getTopicColumnMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        for (String[] topic_columns : FeatureName.SAMPLE_TOPIC_COLUMN) {
            String topicName = topic_columns[0];
            String columns = topic_columns[1];
            Map<String, String> col_map = new HashMap<>();
            String[] column_arr = columns.split("\\|");
            for (String column_str : column_arr) {
                String[] column_str_arr = column_str.split(":");
                String s_col = column_str_arr[0];
                String t_col = column_str_arr[1];
                col_map.put(s_col, t_col);

            }

            map.put(topicName, col_map);
        }
        return map;

    }

}