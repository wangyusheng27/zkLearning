import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Solution {
    /**
     * @param nums1 an integer array
     * @param nums2 an integer array
     * @return an integer array
     */
    public double myPow(double x, int n) {
        if (x == 0)
            return  0;
        if (n == 0)
            return  1;
        if ( n == 1)
            return x;
        boolean flag = true;
        if (n < 0){
            n = -1 * n;
            flag = false;
        }
        return flag ? myPow(x, n / 2) * myPow(x, n - n / 2) : 1 / (myPow(x, n / 2) * myPow(x, n - n / 2));
    }

    public static void main(String[] args) {

    }

    public boolean find(int[] nums1, int value){
        if (nums1 == null || nums1.length == 0)
            return  false;
        int start = 0;
        int end = nums1.length - 1;
        while (start + 1 < end){
            int middle = start + (end - start) / 2;
            if (value <= nums1[middle]){
                end = middle;
            }else {
                start = middle;
            }
        }
        if (nums1[start] == value || nums1[end] == value)
            return true;
        return false;
    }
}