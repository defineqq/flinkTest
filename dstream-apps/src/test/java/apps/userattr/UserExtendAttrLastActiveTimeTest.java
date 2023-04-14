package apps.userattr;

import constant.ESConstant;
import junit.framework.TestCase;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UserExtendAttrLastActiveTimeTest  {

    @Test
    public void testTime()
    {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong("1640749566")), ZoneOffset.UTC);
        System.out.println(localDateTime);

    }

}