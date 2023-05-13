package com.cool.request_core.core.utils;

public class JavaUtils {
    public static String getSamePath(String url1, String url2) {
        String[] split = url1.split("/");
        String[] split1 = url2.split("/");

        StringBuilder s1 = new StringBuilder();
        int min = Math.min(split1.length,split.length);
        for (int i = 0; i < min; i++) {
            if (!split[i].equals(split1[i])){
                break;
            }else {
                s1.append(split[i]).append("/");
            }
        }

        return s1.toString();
    }
}
