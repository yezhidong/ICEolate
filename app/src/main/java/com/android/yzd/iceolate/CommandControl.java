package com.android.yzd.iceolate;

import android.text.TextUtils;

import com.clj.fastble.utils.HexUtil;

import java.util.Calendar;

/**
 * <p>Title:        CommandControl
 * <p>Description:
 * <p>@author:      yezd
 * <p>Copyright:    Copyright (c) 2010-2017
 * <p>Company:      @咪咕动漫
 * <p>Create Time:  2018/1/14 下午9:05
 * <p>@author:
 * <p>Update Time:
 * <p>Updater:
 * <p>Update Comments:
 */
public class CommandControl {

    private static final String READ_TEMPLETE_COMMAND = "a1";// 读取温度指令
    private static final String CONTROL_COMMAND = "b1";// 控制指令
    private static final String UPDATE_TIME_COMMAND = "c1";
    private static final String CHECK_SUM = "aa";

    private static final String Data0 = "11";
    private static final String Data1 = "22";
    private static final String Data2 = "33";
    private static final String Data3 = "44";

    private static final String CLOSE = "00";
    private static final String OPEN = "01";

    private CommandControl() {

    }

    private static class LazyHolder {
        private static final CommandControl INSTANCE = new CommandControl();
    }

    public static CommandControl getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 刷新时间
     * @return
     */
    public byte[] getTime() {
        return HexUtil.hexStringToBytes(getTimeCommandStr());
    }

    public byte[] operateMathine(boolean isOpen) {
        return HexUtil.hexStringToBytes(getOpenCommandStr(isOpen));
    }

    /**
     * 读取温度
     * @return
     */
    public byte[] writeTemplete() {
        return HexUtil.hexStringToBytes(getReadTempleteCommandStr());
    }

    private String getOpenCommandStr(boolean isOpen) {
        return CONTROL_COMMAND
                + Data0
                + Data1
                + Data2
                + Data3
                + (isOpen ? OPEN : CLOSE)
                + "00000000000000000000000000"
                + CHECK_SUM;
    }

    private String getReadTempleteCommandStr() {
        return READ_TEMPLETE_COMMAND
                + Data0
                + Data1
                + Data2
                + Data3
                + "0000000000000000000000000000"
                + CHECK_SUM;
    }

    private String getTimeCommandStr() {
        Calendar c = Calendar.getInstance();
        return UPDATE_TIME_COMMAND
                + String.valueOf(c.get(Calendar.YEAR)).substring(2, 4)
                + appendLength(String.valueOf(c.get(Calendar.MONTH) + 1))
                + appendLength(String.valueOf(c.get(Calendar.DAY_OF_MONTH)))
                + appendLength(String.valueOf(c.get(Calendar.HOUR_OF_DAY)))
                + appendLength(String.valueOf(c.get(Calendar.MINUTE)))
                + appendLength(String.valueOf(c.get(Calendar.SECOND)))
                + "000000000000000000000000"
                + CHECK_SUM;
    }

    private String appendLength(String beforeStr) {
        if (beforeStr.length() < 2) {
             return "0" + beforeStr;
        }
        return beforeStr;
    }

    /**
     * int 转化成byte类型
     * @param intType
     * @return
     */
    private byte intToByte(int intType) {
        return Integer.valueOf(intType).byteValue();
    }
}
