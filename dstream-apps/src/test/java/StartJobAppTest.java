import junit.framework.TestCase;
import org.apache.flink.api.java.utils.ParameterTool;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StartJobAppTest {

    @Test
    public void test() {
        Map<String, String> parameterDic = new HashMap<String, String>();
        String[] args = new String[]{};
        final ParameterTool params = ParameterTool.fromArgs(args);
        String enableCp = params.get("enableCp");
        if (enableCp == null) {
            parameterDic.put("enableCp", "false");
        } else {
            parameterDic.put("enableCp", "true");
        }

        if (!Boolean.getBoolean(parameterDic.get("enableCp"))) {

            System.out.println("false");
        }
    }

    public static void main(String[] args) {
        Map<String, String> parameterDic = new HashMap<String, String>();
        final ParameterTool params = ParameterTool.fromArgs(args);
        String enableCp = params.get("enableCp");
        if (enableCp == null) {
            parameterDic.put("enableCp", "false");
        } else {
            parameterDic.put("enableCp", "true");
        }

        if (!Boolean.parseBoolean(parameterDic.get("enableCp"))) {

            System.out.println("false");
        }else{
            System.out.println("true");
        }
    }

}