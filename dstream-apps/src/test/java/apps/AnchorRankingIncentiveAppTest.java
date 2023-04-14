package apps;

import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class AnchorRankingIncentiveAppTest {

    @Test
    public void uniqueKeyTest() {
        String uid = AnchorRankingIncentiveApp.uniqueKey("uid");
        System.out.println(uid);
    }

    @Test
    public void actTest() {
        List<Integer> act = Arrays.asList(1, 2, 6, 8);

        if (act.contains(12)) {
            System.out.println("有");
        }

    }

    @Test
    public void monthTest() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("+3"));

        LocalDateTime parse = LocalDateTime.parse("2022-01-09 09:08:08",DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String format = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        System.out.println(format);
    }
}