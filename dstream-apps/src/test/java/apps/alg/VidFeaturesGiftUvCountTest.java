package apps.alg;

import junit.framework.TestCase;

public class VidFeaturesGiftUvCountTest extends TestCase {

    public void testTestRun() {

    }

    public void testString(){

        String redisKey = "alg:feature:video:v1:%s";
        String format = String.format(redisKey, "123412");
        System.out.println(format);
    }
}