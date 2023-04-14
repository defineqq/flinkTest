package apps.alg;

import models.AlgReportedDataActEnum;
import models.PotentialValUserCommModel;
import org.junit.Test;

public class PotentialValUserVisitRelateAppTest {

    @Test
    public void enumTest() {
        String name = AlgReportedDataActEnum.FOLLOW.name();
        System.out.println(name);

        AlgReportedDataActEnum algReportedDataActEnum = AlgReportedDataActEnum.valueOf("6", "6");
        assert algReportedDataActEnum.equals(AlgReportedDataActEnum.FOLLOW);
    }

    @Test
    public void timeRangeTest() {
        Long dwell = 56L;
        if (60 <= dwell && dwell <= 86400) {
            System.out.println("big");
        }

        if (30 <= dwell && dwell <= 86400) {
            System.out.println("small");
        }

    }
}