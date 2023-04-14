package apps.tmp;

import com.alibaba.fastjson.JSONObject;
import constant.BinlogFieldConstant;
import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSession;
import service.AnnualFestivalMapper;
import utils.StringUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class AnnualFestivalAppTest extends TestCase {

    public void testTestRun() {

        String value = "{\n" +
                "    \"OldData\":{\n" +
                "        \"uid\":\"1470682351045582849\",\n" +
                "        \"gender\":0,\n" +
                "        \"last_login_time\":1669132088,\n" +
                "        \"updated_at\":\"2022-11-22 15:48:08\",\n" +
                "        \"project_id\":1,\n" +
                "        \"reg_time\":1639472971,\n" +
                "        \"nickname\":\"ð\u009F\u0092¥عہدنہð\u009F\u0092¥\",\n" +
                "        \"created_at\":\"2021-12-14 09:09:32\",\n" +
                "        \"id\":348191,\n" +
                "        \"avatar\":\"http://esx.esxscloud.com/small/liveme/poster/988ab48a6d5203577598da58248f557c_icon.jpeg\",\n" +
                "        \"deleted_at\":null,\n" +
                "        \"status\":0\n" +
                "    },\n" +
                "    \"Data\":{\n" +
                "        \"uid\":\"1470682351045582849\",\n" +
                "        \"gender\":0,\n" +
                "        \"last_login_time\":1669184777,\n" +
                "        \"updated_at\":\"2022-11-23 06:26:18\",\n" +
                "        \"project_id\":1,\n" +
                "        \"reg_time\":1669189051,\n" +
                "        \"nickname\":\"ð\u009F\u0092¥عہدنہð\u009F\u0092¥\",\n" +
                "        \"created_at\":\"2021-12-14 09:09:32\",\n" +
                "        \"id\":348191,\n" +
                "        \"avatar\":\"http://esx.esxscloud.com/small/liveme/poster/988ab48a6d5203577598da58248f557c_icon.jpeg\",\n" +
                "        \"deleted_at\":null,\n" +
                "        \"status\":0\n" +
                "    },\n" +
                "    \"Type\":\"UPDATE\",\n" +
                "    \"BinLogFilename\":\"mysql-bin-changelog.143801\",\n" +
                "    \"Database\":\"athena_main\",\n" +
                "    \"Table\":\"cl_user_default_attr_00fe\",\n" +
                "    \"BinLogPosition\":513436,\n" +
                "    \"Offset\":\"cl_user_default_attr_00fe:513436\",\n" +
                "    \"Timestamp\":\"1669184778000\",\n" +
                "    \"ServerId\":87391880,\n" +
                "    \"KeyNames\":[\n" +
                "        \"id\"\n" +
                "    ],\n" +
                "    \"KeyValues\":[\n" +
                "        348191\n" +
                "    ]\n" +
                "}";
        JSONObject jsonObject = JSONObject.parseObject(value);
        JSONObject data = jsonObject.getJSONObject(BinlogFieldConstant.JSON_DATA);
        LocalDateTime reg_time = LocalDateTime.ofInstant(Instant.ofEpochSecond(data.getLongValue("reg_time")), ZoneId.of("-8"));
        Instant now = reg_time.toInstant(ZoneOffset.of("-8"));
        LocalDateTime startDate = LocalDateTime.parse("20221119 00:00:00", DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
        Instant start = startDate.toInstant(ZoneOffset.of("-8"));
        if (now.isAfter(start)) {
            String uid = data.getString("uid");
            //todo:: save to mysql
            System.out.println("save");
        } else {
            System.out.println("exit");
        }

        //        Instant instant1 = reg_time.atZone(ZoneId.of("-8")).toInstant();
//
//
//        System.out.println(instant1);
//
//        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochSecond(1669171410),ZoneId.of("+0"));
//        Instant inend = end.atZone(ZoneId.of("+0")).toInstant();
//        System.out.println(inend);
//        assert instant.isBefore(instant1);

    }

    public void testJson()
    {
        String event=null;
        if (!StringUtil.isBlank(event) && event.equals("list")) {
            System.out.println("in");
        }else{
            System.out.println("out");
        }
    }
}