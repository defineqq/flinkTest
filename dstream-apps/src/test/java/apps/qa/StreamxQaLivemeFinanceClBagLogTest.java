package apps.qa;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class StreamxQaLivemeFinanceClBagLogTest {


    @Test
    public void itemIdTest() {
        List<Integer> ITEM_IDS = getItemIds("21, 24, 1, 4, 14, 10");

        int itemId = 5;

        if (ITEM_IDS.contains(itemId)) {
            System.out.println("包含");
        } else {
            System.out.println("不包含");
        }
    }

    private List<Integer> getItemIds(String itemIds) {

        List<Integer> collect = Arrays.stream(itemIds.split(",")).map(w -> Integer.parseInt(w.toString().trim())).collect(Collectors.toList());

        return collect;
    }

    @Test
    public void timeTest() throws ParseException {
        //createdAt, "yyyy-MM-dd HH:mm:ss", "+00:00"
        SimpleDateFormat inputSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputSDF.setTimeZone(TimeZone.getTimeZone(ZoneId.of("+00:00")));

        Date date = inputSDF.parse("2022-01-10 09:07:33");
        System.out.println(date.getTime() / 1000);
        ;
    }

    @Test
    public void typeTest() {
        int type = 21;
        String types = "999,0,1,4,21";
        List<String> collect = Arrays.stream(types.split(",")).map(w -> w.trim()).collect(Collectors.toList());
        if (collect.contains(String.valueOf(type))) {
            System.out.println("包含");
        } else {
            System.out.println("不包含");
        }
    }
}