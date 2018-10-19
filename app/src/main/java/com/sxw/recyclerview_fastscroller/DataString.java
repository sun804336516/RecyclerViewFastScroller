package com.sxw.recyclerview_fastscroller;

import java.util.Random;

/**
 * Administrator on 2018/10/19/019 10:49
 */
public class DataString {
    public static Random random = new Random();
    public static String[] mStrings = new String[]
            {
                    "百度", "滴滴", "阿里",
                    "腾讯", "360", "迅雷",
                    "网易", "易简", "字节",
            };

    public static String get() {
        return mStrings[random.nextInt(mStrings.length)];
    }
}
