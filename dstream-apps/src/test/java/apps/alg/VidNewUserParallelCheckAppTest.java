package apps.alg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import utils.HttpClientUtil;

import java.util.List;

public class VidNewUserParallelCheckAppTest {

    @Test
    public void uidTest() {
        String api_url = "http://qa.api.cms.cmcm.com/particle/board/mentor_anchor_list?app_id=1&id=mentor_anchor_list";
        String json = HttpClientUtil.get(api_url, null);
        JSONObject model = null;
        if (json != null) {
            model = JSON.parseObject(json);
        } else {
            json = HttpClientUtil.get(api_url, null);
            if (json != null) {
                model = JSON.parseObject(json);
            }
        }

        if (model != null) {
            List<String> list = model.getJSONArray("data").toJavaList(String.class);
            System.out.println(list);
        }
    }
}