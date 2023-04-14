import UDF.annotation.Udf;
import UDF.engine.Udfs;
import org.junit.Test;

import java.lang.annotation.Annotation;

/**
 * @author By Water Created On 2022/7/12
 */
public class UDFTest {

    @Test
    public void testStringUDF() throws Exception{

        System.out.println(String.valueOf(Udfs.eval("get_json_object('{\"show_pv\":0,\"show_uv\":0,\"livebptype\":3}', 'livebptype')")));

//        System.out.println(String.valueOf(Udfs.eval("dt_fmt(concat('160000000', '000'), 'HH', '-0')")));
    }

    @Test
    public void testAnchorUDF() throws Exception{

        System.out.println(String.valueOf(Udfs.eval("signeranchorlocal('810614269678190592', 'uid')")));
    }
}
