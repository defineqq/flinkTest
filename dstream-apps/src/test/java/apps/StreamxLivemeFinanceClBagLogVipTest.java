package apps;

import constant.ESConstant;
import junit.framework.TestCase;
import org.junit.Test;
import utils.TimeUtil;

public class StreamxLivemeFinanceClBagLogVipTest {

    @Test
    public void convertTime() {
        String createdAtDt = null;
        try {
            createdAtDt = TimeUtil.convertTime("2021-11-21 16:32:36", "yyyy-MM-dd HH:mm:ss", "+00:00", "yyyy-MM-dd HH:mm:ss", "+08:00");
            //log.info("时间: "+createdAt+", 转北京时间: "+createdAtDt);
            System.out.println(createdAtDt.split(" ")[0].replace("-", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}