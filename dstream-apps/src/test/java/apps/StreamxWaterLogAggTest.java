package apps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamxWaterLogAggTest {


    @Test
    public void testCode() {

        String ss = "1071, 1072, 1073, 1074, 1075, 1076, 1095, 1097, 1098,1102, 1103, 1104, 1105, 100101106, 100101111, 100101135, 100101136, 100101140, 100101141, 100101142, 100101143, 200101105, 200101112, 200101137, 200101138, 200101142, 200101143, 200101144, 200101145, 200101155, 100101152, 200101156, 100101153, 100101160, 200101167, 200101174, 100101166, 200101179, 100101171, 100101172, 200101180, 200101181, 100101174, 200101191, 100101176, 200101193, 100101178, 200101194, 100101179, 200101195, 100101180, 200101196, 100101181, 200101197, 100101182, 100101183, 200101198, 100101184, 200101199";

        List<String> list = Arrays.asList(ss.replace(" ", "").split(","));


        List<String> actions = Arrays.asList(1071, 1072, 1073, 1074, 1075, 1076, 1095, 1097, 1098,
                1102, 1103, 1104, 1105, 100101106, 100101111, 100101135, 100101136,
                100101140, 100101141, 100101142, 100101143, 200101105, 200101112,
                200101137, 200101138, 200101142, 200101143, 200101144, 200101145,
                200101155, 100101152, 200101156, 100101153, 100101160, 200101167, 200101174, 100101166, 200101179, 100101171, 100101172, 200101180, 200101181, 100101174, 200101191, 100101176
                , 200101193, 100101178, 200101194, 100101179, 200101195, 100101180, 200101196, 100101181, 200101197, 100101182, 100101183, 200101198, 100101184, 200101199).stream().map(v -> v.toString()).collect(Collectors.toList());
        if (actions.contains("200101181")) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }
    }

    @Test
    public void algTest() {
        int[] nums1 = {2, 7, 11, 15};
        int[] nums2 = {1, 10, 4, 11};

        int[] ints = advantageCount(nums1, nums2);
        System.out.println(ints);
    }

    public int[] advantageCount(int[] nums1, int[] nums2) {


        int n = nums1.length;
        Integer[] idx1 = new Integer[n];
        Integer[] idx2 = new Integer[n];
        for (int i = 0; i < n; ++i) {

            idx1[i] = i;
            idx2[i] = i;
        }

        Arrays.sort(idx1, (i, j) -> nums1[i] - nums1[j]);

        Arrays.sort(idx2, (i, j) -> nums2[i] - nums2[j]);

        int[] ans = new int[n];
        int left = 0, right = n - 1;
        for (int i = 0; i < n; ++i) {

            if (nums1[idx1[i]] > nums2[idx2[left]]) {
                ans[idx2[left]] = nums1[idx1[i]];
                ++left;
            } else {
                ans[idx2[right]] = nums1[idx1[i]];
                --right;
            }
        }

        return ans;
    }

}