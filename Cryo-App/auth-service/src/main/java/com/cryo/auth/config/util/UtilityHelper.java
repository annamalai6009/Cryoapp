package com.cryo.auth.config.util;

import org.springframework.stereotype.Component;
@Component
public  class UtilityHelper {
    private static final Object lock =new Object();
    public static int[] incrementByOne(int[] arr) {
        synchronized(lock) {
            for (int j = arr.length - 1; j >= 0; j--) {
                if (arr[j] < 9) {
                    arr[j]++;
                    return arr;
                }
                arr[j] = 0;
            }
            int[] result = new int[arr.length + 1];
            result[0] = 1;
            return result;
        }
    }
}
