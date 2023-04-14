package apps.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
import utils.StringUtil;

public class VidRoomTypeAppTest extends TestCase {

    public void testTestRun() {

        String son = "{\"data\":{\"host\":\"\",\"uid\":\"1234913590469861377\",\"vid\":\"16699751933706393688\",\"vtype\":\"1\",\"os\":\"iOS\",\"ver\":\"4.5.55\",\"status\":0,\"area\":\"A_US\",\"countryCode\":\"UA\",\"retcode\":200,\"ip\":\"unknown\",\"brand\":\"iOS\",\"model\":\"iOS\",\"issigning\":\"1\",\"network\":\"1\",\"packages\":\"liveme\"},\"timestamp\":1669975268,\"type\":\"creat_video_log\",\"dct\":\"creat_video_log\",\"dcts\":1669975268}";

        JSONObject json = JSON.parseObject(son);
        JSONObject data = json.getJSONObject("data");
        if (data != null) {
            boolean res = data.getInteger("status") == 0 && !StringUtil.isBlank(data.getString("vid"));
            System.out.println(res);
        }

    }
}