package apps.alg;

import junit.framework.TestCase;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamxFollowListThreeMonthTest {


    @Test
    public void test() {

        List<String> list = new ArrayList<>();

        list.add("a");
        list.add("b");


        System.out.println(Arrays.toString(list.toArray()).replace("[", "").replace("]", ""));
    }

    @Test
    public void timeTest() {
        String time = "1630476047";
        long saveTime = Long.parseLong(time);
        long start = Instant.ofEpochSecond(saveTime).getEpochSecond();
        long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).getEpochSecond();
        System.out.println("now:" + now + ",start:" + start);
        //3个月就是 3600
        long days = 3600 * 24 * 92;
        long result = now - start;
        System.out.println("result:" + result);
        if (result <= days) {
            System.out.println("是三个月内的");
        } else {
            System.out.println("不是三个月内的");
        }
    }

    @Test
    public void splitTest() {
        List<String> hmget = new ArrayList<>();
        String str = "1463814603535425537,kakak,iii";
        String[] split = str.split(",");
        for (int i = 0; i < split.length; i++) {
            //不要重复的
            if (!hmget.contains(split[i])) {
                hmget.add(split[i]);
            }
        }

        System.out.println(hmget.size());

        List<Integer> removeIndex = new ArrayList<>();
        removeIndex.add(0);
        for (int i = 0; i < removeIndex.size(); i++) {

            int integer = removeIndex.get(i);
            hmget.remove(integer);
        }

        hmget.add("123".trim());

        for (int i = 0; i < hmget.size(); i++) {
            System.out.println(hmget.get(i));
        }


        System.out.println("result:" + hmget.stream().collect(Collectors.joining(",")));
    }

    @Test
    public void testProcess() {
        String json = "{\"extractor\":{\"o\":1097446492,\"f\":\"(dev=10302,ino=21194896)\",\"s\":123600664267,\"e\":\"data02.data.liveme.com.cm\",\"n\":\"access_log.2022051011\",\"c\":123600664294},\"properties\":{\"$is_login_id\":true,\"vid\":\"16521462061423565268\",\"$ip\":\"34.227.218.223\",\"fcounry\":\"CN\",\"buid\":\"\",\"$lib_version\":\"1.10.0\",\"status\":2,\"followuid\":\"1351448790992822273\",\"source\":7.0,\"farea\":\"A_CT\",\"$lib\":\"php\",\"act\":1},\"ip\":\"34.227.218.223\",\"event\":\"Im_follow\",\"ver\":2,\"time\":1652154362561,\"project_id\":14,\"project\":\"production\",\"user_id\":4174411607252839257,\"type\":\"track\",\"map_id\":\"LM:4w4CISEPgbrLYpJiDWAc1Q5pSDLlxloN7GkapAbB6NzxgYFoquWTmO0eFauz6Rca\",\"lib\":{\"$lib_version\":\"1.10.0\",\"$lib_method\":\"unknown\",\"$lib_detail\":\"unknown\",\"$lib\":\"php\",\"$app_version\":\"unknown\"},\"recv_time\":1652154367317,\"distinct_id\":\"1453636370059894785\"}";


    }
}