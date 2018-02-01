package com.android.yzd.iceolate;

import com.clj.fastble.utils.HexUtil;

/**
 * <p>Title:        Test
 * <p>Description:
 * <p>@author:      yezd
 * <p>Copyright:    Copyright (c) 2010-2017
 * <p>Company:      @咪咕动漫
 * <p>Create Time:  2018/1/15 下午3:24
 * <p>@author:
 * <p>Update Time:
 * <p>Updater:
 * <p>Update Comments:
 */
public class Test {

    public static void main(String[] args) {
        CommandControl instance = CommandControl.getInstance();
        byte[] bytes = instance.getTime();
    }
}
