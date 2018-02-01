package com.android.yzd.iceolate;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.silencedut.taskscheduler.TaskScheduler;

/**
 * <p>Title:        BleControl
 * <p>Description:
 * <p>@author:      yezd
 * <p>Copyright:    Copyright (c) 2010-2017
 * <p>Company:      @咪咕动漫
 * <p>Create Time:  2018/1/30 上午10:26
 * <p>@author:
 * <p>Update Time:
 * <p>Updater:
 * <p>Update Comments:
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleControl {

    private final BUtils mBUtils;

    private BleControl() {
        mBUtils = BUtils.getInstance();
    }

    private static class LazyHolder {
        private static BleControl INSTANCE = new BleControl();
    }

    public static BleControl getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void open(final Context context, int delayTime) {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().write(
                        mBUtils.getBleDevice(),
                        mBUtils.getWriteBluetoothGattCharacteristic().getService().getUuid().toString(),
                        mBUtils.getWriteBluetoothGattCharacteristic().getUuid().toString(),
                        CommandControl.getInstance().operateMathine(true),
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess() {
                                Toast.makeText(context, "发送数据到设备成功", Toast.LENGTH_LONG).show();
                                // 发送数据到设备成功
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                Toast.makeText(context, "发送数据到设备失败", Toast.LENGTH_LONG).show();
                                // 发送数据到设备失败
                            }
                        });
            }
        }, delayTime);
    }
}
