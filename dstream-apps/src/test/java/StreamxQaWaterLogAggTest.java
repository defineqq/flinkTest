import apps.StreamxWaterLogAgg;
import apps.flume.constant.PushDataConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import factory.MybatisSessionFactory;
import models.WaterLogAggModel;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.elasticsearch.common.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import scala.Tuple5;
import service.WaterLogAggMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StreamxQaWaterLogAggTest {

    @Test
    public void testGameIds() {
        String codes = "1074,1073,200101137,100101135, 200101138,100101136, 200101145, 100101145, 200101146, 100101146, 200101147,100101147, 200101148, 100101148, 200101161, 100101160, 200101162,100101161, 100101168, 200101173, 200101181, 100101174, 200101185, 100101179, 100101180, 200101186, 200101195, 100101186, 200101205, 100101188, 200101207, 100101190, 200101208, 100101191, 200101209, 100101192, 200101210, 100101193, 200101211, 100101194, 100101195, 200101212, 100101196, 200101213,200101216,100101202,200101217,100101203,200101218,100101204,200101220,100101205,200101223,100101208,200101222,100101207,102100017,202100005,102100018,202100006,102100019,202100007,102100020,202100008,100101213,200101224,100101214,200101225";
        if (joptsimple.internal.Strings.isNullOrEmpty(codes)) {
            System.out.println("ok");
        }

        List<String> collect = Arrays.stream(codes.split(",")).map(w -> w.trim()).collect(Collectors.toList());

        System.out.println(collect);
    }

    @Test
    public void testJson() {
        String value = "{\"BinLogFilename\":\"mysql-bin-changelog.000790\",\"Type\":\"INSERT\",\"Table\":\"cl_bag_log_2022_9e\",\"ServerId\":1589332687,\"BinLogPosition\":118226732,\"Database\":\"liveme_user_finance\",\"KeyNames\":[\"id\"],\"Data\":{\"ext\":\"{\\\"message\\\":\\\"sub:gold:fun_game_fighter\\\",\\\"game_id\\\":35581}\",\"pre_expiration\":4200000000,\"third_id\":0,\"country\":\"HK\",\"code\":100101214,\"after_expiration\":4200000000,\"to_app_id\":2,\"item_id\":1,\"after_num\":0,\"cnt\":-163778,\"created_at\":\"2022-03-01 02:42:36\",\"rid\":\"4b621d881b55c274264ec5afda5ed835\",\"type\":999,\"qid\":\"20229e46801840705566745807591217\",\"sid\":\"c476b101cc10688b0a6648cdeda980cb\",\"user_app_id\":2,\"uid\":\"1429706761404817409\",\"pre_num\":163778,\"updated_at\":\"2022-03-01 02:42:36\",\"expiration\":4200000000,\"id\":5351,\"app_id\":2},\"Timestamp\":\"1646102556000\",\"KeyValues\":[5351],\"Offset\":\"cl_bag_log_2022_9e:118226732\"}";
        JSONObject json = JSONObject.parseObject(value);
        JSONObject data = json.getJSONObject("Data");
        if (data != null && data.getString("uid") != null) {
            String uid = data.getString("uid");
            Long cnt = data.getLongValue("cnt");
            String action = data.getString("code");
            String ext = data.getString("ext");

            JSONObject gameId = new JSONObject();
            try {
                gameId = JSON.parseObject(ext);
            } catch (Exception e) {

            }
            String gid = "-";
            if (!Strings.isNullOrEmpty(ext) && gameId.containsKey("game_id")) {
                gid = gameId.getString("game_id");
            }

            System.out.println(uid + "===" + gid + "==" + action + "==" + cnt + "==" + data);
        }
    }

    @Test
    public void chartTest() throws IOException {
        String str = "\"152.101.167.55\" ++_ \"1646538705.297\" ++_ \"POST\" ++_ \"\" ++_ \"\" ++_ \"\" ++_ \"{\\n\\n    \\\"name\\\":\\\"kafka\\\",\\n    \\\"age\\\":18,\\n    \\\"address\\\":\\\"国家\\\",\\n    \\\"like\\\":\\\"ball\\\"\\n}\\n\\n\" ++_ \"PostmanRuntime/7.29.0\" ++_ \"\" ++_ \"\" ++_ \"\"";

        String s = StringEscapeUtils.unescapeJava("\\x22\\xE5\\x9B\\xBD\\xE5\\xAE\\xB6\\x22".replaceAll("\\\\x", "\\\\u00"));

        String txt = "\"152.101.167.55\" ++_ \"1646566452.389\" ++_ \"POST\" ++_ \"-\" ++_ \"-\" ++_ \"-\" ++_ \"{\\x0A\\x0A    \\x22name\\x22:\\x22kafka\\x22,\\x0A    \\x22age\\x22:18,\\x0A    \\x22address\\x22:\\x22\\xE5\\xAE\\xB6He\\xE5\\xAE\\x8C\\xE4\\xBA\\x8B\\x22,\\x0A    \\x22like\\x22:\\x22ball\\x22\\x0A}\\x0A\\x0A\" ++_ \"PostmanRuntime/7.29.0\" ++_ \"-\" ++_ \"-\" ++_ \"-\"";

        String[] fields = txt.split(" \\+\\+_ ");
        String clientRealIp = fields[0].replace("\"", "");
        String requestTime = new DateTime(Long.valueOf(fields[1].replace("\"", "").replace(".", "")), DateTimeZone.forID("+08:00")).toString("yyyy-MM-dd HH:mm:ss");
        String requestBody = fields[6].replace("\"", "");

        String s1 = convertRequestBody(requestBody).replaceAll("\\n", " ");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = (ObjectNode) mapper.readTree(s1);

        // 数据正确且加入 extractor 和 header,
        ObjectNode extractorMeta = mapper.createObjectNode();
        extractorMeta.put("request_time", requestTime);
        String extractorInterceptTime = new DateTime(System.currentTimeMillis(), DateTimeZone.forOffsetHours(+8)).toString("yyyy-MM-dd HH:mm:ss");
        extractorMeta.put("extractor_interceptTime", extractorInterceptTime);
        extractorMeta.put("request_ip", clientRealIp);
        extractorMeta.put("extractor_name", "CheckJson");
        System.out.println(s1);

    }

    public String convertRequestBody(String requestBody) {

        requestBody = requestBody.replace("\\x22", "\"");

        char[] radix16Array = new char[2];

        int index = 0;

        List<Byte> listByte = new LinkedList<>();

        char[] newRequestBody = requestBody.toCharArray();

        char s = ' ';

        while (index < newRequestBody.length) {

            s = newRequestBody[index];
            System.out.println("第" + s + "个字符：" + newRequestBody[index]);
            if (s == '\\' && newRequestBody[index + 1] == 'x') {

                radix16Array[0] = newRequestBody[index + 2];

                radix16Array[1] = newRequestBody[index + 3];

                listByte.add((byte) Integer.parseInt(new String(radix16Array), 16));

                index = index + 4;

            } else {

                index++;

                listByte.add((byte) s);

            }

        }

        byte[] array = new byte[listByte.size()];

        for (int i = 0; i < listByte.size(); i++) {

            array[i] = listByte.get(i);

        }

        return new String(array);

    }


    @Test
    public void jsonTest() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectNode jsonNode = (ObjectNode) mapper.readTree("{\"name\":\"kafka\"}");
            JsonNode rid1 = jsonNode.get("name");
            String rid = rid1.getTextValue();

            System.out.println(rid);


            ObjectMapper parent = new ObjectMapper();
            ObjectNode objectNode = parent.createObjectNode();
            objectNode.put(PushDataConstant.EVENT, "action");
            objectNode.put(PushDataConstant.PROJECT, "liveme");
            objectNode.put(PushDataConstant.TYPE, "trace");
            objectNode.put(PushDataConstant.PROPERTIES, jsonNode);

            String name = jsonNode.get("name").getTextValue();
            System.out.println("name is " + name);

            ObjectWriter writer = parent.writer();
            System.out.println(writer.writeValueAsString(objectNode));
        } catch (IOException e) {


        }

    }

    @Test
    public void arrayTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree("[{\"name\":\"kafka\"},{\"name\":\"kkx\"}]");

        ArrayNode nodes = (ArrayNode) jsonNode;

        ObjectWriter writer = mapper.writer();
        for (JsonNode node :
                nodes) {
            System.out.println(writer.writeValueAsString(node));
        }


    }

    @Test
    public void logicTest() throws IOException {

        String json = "{\n" +
                "    \"BinLogFilename\":\"mysql-bin-changelog.000830\",\n" +
                "    \"Type\":\"INSERT\",\n" +
                "    \"Table\":\"cl_bag_log_2022_3b\",\n" +
                "    \"ServerId\":1589332687,\n" +
                "    \"BinLogPosition\":77102304,\n" +
                "    \"Database\":\"liveme_user_finance\",\n" +
                "    \"KeyNames\":[\n" +
                "        \"id\"\n" +
                "    ],\n" +
                "    \"Data\":{\n" +
                "        \"ext\":\"{\\\"message\\\":\\\"sub:gold:liveme_mining_master\\\",\\\"game_id\\\":43573}\",\n" +
                "        \"pre_expiration\":4200000000,\n" +
                "        \"third_id\":0,\n" +
                "        \"country\":\"US\",\n" +
                "        \"code\":100101231,\n" +
                "        \"after_expiration\":4200000000,\n" +
                "        \"to_app_id\":1,\n" +
                "        \"item_id\":1,\n" +
                "        \"after_num\":0,\n" +
                "        \"cnt\":-24225053,\n" +
                "        \"created_at\":\"2022-05-11 09:54:08\",\n" +
                "        \"rid\":\"5d627b87c05d3d3685d35ebf2cdcfe57\",\n" +
                "        \"type\":999,\n" +
                "        \"qid\":\"20223b11901902308485732652987830\",\n" +
                "        \"sid\":\"26f8fe9ef309facbdb89e9ba99365846\",\n" +
                "        \"user_app_id\":1,\n" +
                "        \"uid\":\"1469215364947779585\",\n" +
                "        \"pre_num\":24225053,\n" +
                "        \"updated_at\":\"2022-05-11 09:54:08\",\n" +
                "        \"expiration\":4200000000,\n" +
                "        \"id\":3932,\n" +
                "        \"app_id\":1\n" +
                "    },\n" +
                "    \"Timestamp\":\"1652262848000\",\n" +
                "    \"KeyValues\":[\n" +
                "        3932\n" +
                "    ],\n" +
                "    \"Offset\":\"cl_bag_log_2022_3b:77102304\"\n" +
                "}";

        JSONObject ob = JSONObject.parseObject(json);
        JSONObject data = ob.getJSONObject("Data");
        if (data != null && data.getString("uid") != null) {
            String uid = data.getString("uid");
            Long cnt = data.getLongValue("cnt");
            String action = data.getString("code");
            String ext = data.getString("ext");

            JSONObject gameId = new JSONObject();
            try {
                gameId = JSON.parseObject(ext);
            } catch (Exception e) {

            }
            String gid = "-";
            if (!Strings.isNullOrEmpty(ext) && gameId.containsKey("game_id")) {
                gid = gameId.getString("game_id");
            }

            String codea = "1074,1073,200101137,100101135, 200101138,100101136, 200101145, 100101145, 200101146, 100101146, 200101147,100101147, 200101148, 100101148, 200101161, 100101160, 200101162,100101161, 100101168, 200101173, 200101181, 100101174, 200101185, 100101179, 100101180, 200101186, 200101195, 100101186, 200101205, 100101188, 200101207, 100101190, 200101208, 100101191, 200101209, 100101192, 200101210, 100101193, 200101211, 100101194, 100101195, 200101212, 100101196, 200101213,200101216,100101202,200101217,100101203,200101218,100101204,200101220,100101205,200101223,100101208,200101222,100101207,102100017,202100005,102100018,202100006,102100019,202100007,102100020,202100008,100101213,200101224,100101214,200101225,200101233,100101217,200101234,100101218,200101241,100101231,200101242,100101232";
            List<String> codes = StreamxWaterLogAgg.getGameIds(codea);

            if (codes.contains(action)) {
                System.out.println("ok");
            }
        }

    }

    @Test
    public void performanceTest() throws IOException {
        System.out.println("开始：" + LocalDateTime.now());
//        String log = "127.0.0.1 ++_ 1648087650.803 ++_ POST ++_  ++_  ++_  ++_ \n{\n    \"project\":\"test\",\n    \"event\":\"me\",\n    \"name\":\"kafka\",\n    \"age\":18,\n    \"address\":\"家He完事2\",\n    \"like\":\"ball\"\n} \n\n ++_ PostmanRuntime/7.29.0 ++_  ++_  ++_";
//
//        String[] fields = log.split(" \\+\\+_ ");
//        String clientRealIp = fields[0];
//        String requestTime = new DateTime(Long.valueOf(fields[1].replace(".", "")), DateTimeZone.forID("+08:00")).toString("yyyy-MM-dd HH:mm:ss");
//
//        String requestBody = fields[6].replace("\\n", "").replaceAll("\n","");
//
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode jsonNode = (ObjectNode) mapper.readTree(requestBody);

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 3, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));

        List<TestModel> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(new TestModel("i：" + i));
        }

        for (TestModel item :
                list) {
            poolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    procee(item);
                }
            });
        }

        for (TestModel m :
                list) {
            System.out.println("结果:" + m.getName());
        }
        System.out.println("结束：" + LocalDateTime.now());
    }


    @Test
    public void TestJson() {
        String json = "{\"ext\" : \"\"\"{\"message\":\"add:gold:lingxian_haidaobigo\",\"game_id\":36926}\"\"\" }";

        JSONObject data = JSONObject.parseObject(json);
        String ext = data.getString("ext");

        JSONObject gameId = new JSONObject();
        try {
            gameId = JSON.parseObject(ext);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void sqlTest() throws InterruptedException {
        SqlSessionFactory sqlSessionFactory = MybatisSessionFactory.getSqlSessionFactory("local");
        if (sqlSessionFactory == null) {
            sqlSessionFactory = MybatisSessionFactory.getSqlSessionFactory("local");
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        WaterLogAggMapper mapper = sqlSession.getMapper(WaterLogAggMapper.class);
        int count = 0;
        while (true) {
            count++;
            WaterLogAggModel test = mapper.select("1", "test");
            System.out.println(test + "===次数：" + count);
            Thread.sleep(5000);
            sqlSession.clearCache();
        }

    }

    private void procee(TestModel m) {
        m.setName(m.getName() + "-" + m.getName());

    }

    static class TestModel {

        public TestModel(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}