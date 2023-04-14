package apps.alg;

import junit.framework.TestCase;
import models.PotentialValUserCommModel;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PotentialValUserRegTimeAppTest  {


    @Test
    public void redisKeyTest()
    {
        String uid = String.format(PotentialValUserCommModel.REDIS_KEY_PREFIX, "uid", "20220908");

        System.out.println(uid);
    }

    @Test
    public void timeTest()
    {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dt = LocalDate.now().format(df);
        System.out.println(dt);
    }
}