package com.android.yzd.iceolate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.silencedut.taskscheduler.TaskScheduler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class StatusActivity extends AppCompatActivity {


    public static final String KEY_DATA = "KEY_DATA";
    private BleDevice mBleDevice;
    //    private BluetoothGatt mBleDeviceGatt;
    private BluetoothGattCharacteristic mGattCharacteristic;
    private BluetoothGattCharacteristic mReadBluetoothGattCharacteristic;

    List<Float> tempList = new ArrayList<Float>();
    private TextView mTemp1;
    private TextView mTemp2;
    private TextView mTemp3;
    private TextView mTemp4;
    private TextView mTemp5;
    private TextView mTemp6;
    private TextView mTemp7;
    private TextView mTemp8;
    private TextView mTemp9;
    private TextView mTemp10;


    public static void startActivity(Context context, BleDevice bleDevice) {
        Intent intent = new Intent(context, StatusActivity.class);
        intent.putExtra(KEY_DATA, bleDevice);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        BUtils instance = BUtils.getInstance();
        mBleDevice = instance.getBleDevice();
        mGattCharacteristic = instance.getWriteBluetoothGattCharacteristic();
        mReadBluetoothGattCharacteristic = instance.getReadBluetoothGattCharacteristic();
        initView();
        for (int i = 0; i < 10; i++) {
            tempList.add(0f);
        }
    }

    private void initView() {
        mTemp1 = (TextView) findViewById(R.id.temp1);
        mTemp2 = (TextView) findViewById(R.id.temp2);
        mTemp3 = (TextView) findViewById(R.id.temp3);
        mTemp4 = (TextView) findViewById(R.id.temp4);
        mTemp5 = (TextView) findViewById(R.id.temp5);
        mTemp6 = (TextView) findViewById(R.id.temp6);
        mTemp7 = (TextView) findViewById(R.id.temp7);
        mTemp8 = (TextView) findViewById(R.id.temp8);
        mTemp9 = (TextView) findViewById(R.id.temp9);
        mTemp10 = (TextView) findViewById(R.id.temp10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyData();
        writeTemp();
    }

    private void writeTemp() {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().write(
                        mBleDevice,
                        mGattCharacteristic.getService().getUuid().toString(),
                        mGattCharacteristic.getUuid().toString(),
                        CommandControl.getInstance().writeTemplete(),
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess() {
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                            }
                        });
            }
        }, 1000);
    }


    private void notifyData() {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().indicate(
                        mBleDevice,
                        mReadBluetoothGattCharacteristic.getService().getUuid().toString(),
                        mReadBluetoothGattCharacteristic.getUuid().toString(),
                        new BleIndicateCallback() {
                            @Override
                            public void onIndicateSuccess() {
                                // 打开通知操作成功
//                                Toast.makeText(StatusActivity.this, "打开通知操作成功", Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onIndicateFailure(BleException exception) {
                                // 打开通知操作失败
//                                Toast.makeText(StatusActivity.this, "失败", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                // 打开通知后，设备发过来的数据将在这里出现
//                                Toast.makeText(StatusActivity.this, "成功收到", Toast.LENGTH_LONG).show();
                                try {
                                    int[] bytes = new int[2];
                                    bytes[0] = 0;
                                    bytes[1] = 0;
                                    int j = 0;
                                    for (int i = 0; i < data.length; i++) {
                                        if (i % 2 == 0) {
                                            bytes[0] = data[i] & 0xFF;
                                        } else {
                                            bytes[1] = data[i] & 0xFF;
                                            float parseInt = bytes[0] * 255 + bytes[1];
                                            float nowTemp;
                                            if (parseInt < 100) {
                                                nowTemp = -(100 - parseInt) / 10;
                                            } else {
                                                nowTemp = (parseInt - 100) / 10;
                                            }
                                            tempList.add(j++, nowTemp);
                                        }
                                    }
                                    updateTemp();
                                } catch (Exception e) {
//                                    Toast.makeText(StatusActivity.this, "转化失败", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
            }
        }, 0);

    }

    private void updateTemp() {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (tempList.get(0) > 39) {
                    mTemp1.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp1.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(1) > 39) {
                    mTemp2.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp2.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(2) > 39) {
                    mTemp3.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp3.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(3) > 39) {
                    mTemp4.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp4.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(4) > 39) {
                    mTemp5.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp5.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(5) > 39) {
                    mTemp6.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp6.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(6) > 39) {
                    mTemp7.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp7.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(7) > 39) {
                    mTemp8.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp8.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(8) > 39) {
                    mTemp9.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp9.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                if (tempList.get(9) > 39) {
                    mTemp10.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                } else {
                    mTemp10.setTextColor(getResources().getColor(R.color.qmui_config_color_75_pure_black));
                }
                DecimalFormat df = new DecimalFormat("0.0");
                mTemp1.setText(df.format(tempList.get(0)));
                mTemp2.setText(df.format(tempList.get(1)));
                mTemp3.setText(df.format(tempList.get(2)));
                mTemp4.setText(df.format(tempList.get(3)));
                mTemp5.setText(df.format(tempList.get(4)));
                mTemp6.setText(df.format(tempList.get(5)));
                mTemp7.setText(df.format(tempList.get(6)));
                mTemp8.setText(df.format(tempList.get(7)));
                mTemp9.setText(df.format(tempList.get(8)));
                mTemp10.setText(df.format(tempList.get(9)));
            }
        }, 0);


    }
}
