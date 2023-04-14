package apps.bi;

import UDF.common.date.DateFormat;
import junit.framework.TestCase;
import org.junit.Test;

import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ClOrderRealtimeAppTest  {


    @Test
    public void dtTest()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate localDate = LocalDateTime.parse("2022-07-18 10:50:31", fm).atZone(ZoneId.of("-7")).toLocalDate();
        String format = formatter.format(localDate);
        int dt = Integer.parseInt(format);
        System.out.println(dt);

        LocalDateTime localDateTime = LocalDateTime.parse("2022-07-20 18:50:31", fm).atZone(ZoneId.of("+8")).minusHours(15).toLocalDateTime();
        System.out.println(localDateTime.format(fm));

        java.util.Date from = Date.from(LocalDateTime.parse("2022-07-21 10:35:51", fm).atZone(ZoneId.of("+08:00")).minusHours(15).toInstant());
        System.out.println(from);


        LocalDateTime localDateTime1 = LocalDateTime.parse("2022-07-21 10:35:51", fm).atZone(ZoneId.of("+08:00")).minusHours(15).toLocalDateTime();
        System.out.println(localDateTime1);


    }
}