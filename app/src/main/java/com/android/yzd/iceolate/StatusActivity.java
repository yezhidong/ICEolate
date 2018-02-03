package com.android.yzd.iceolate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.silencedut.taskscheduler.TaskScheduler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import info.hoang8f.widget.FButton;

import static com.android.yzd.iceolate.SettingsActivity.IS_SHESHI;

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
    private FButton mSwitchButton;
    private FButton mDeleteButton;
    private boolean isTurnOn = true;

    public static void startActivity(Context context, BleDevice bleDevice) {
        Intent intent = new Intent(context, StatusActivity.class);
        intent.putExtra(KEY_DATA, bleDevice);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
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

        mSwitchButton = (FButton) findViewById(R.id.switchButton);
        mDeleteButton = (FButton) findViewById(R.id.deleteButton);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTurnOn) {
                    BleControl.getInstance().close(100);
                    mSwitchButton.setText("TURN ON");
                } else {
                    BleControl.getInstance().open(100);
                    mSwitchButton.setText("TURN OFF");
                    readTemp();
                }
                isTurnOn = !isTurnOn;
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTurnOn) {
                    BleControl.getInstance().close(0);
                    BleManager.getInstance().disconnectAllDevice();
                    BleManager.getInstance().destroy();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyData();
        readTemp();
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (!isTurnOn) {
                return;
            }
            Log.d("yzd", "time run");
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
            handler.postDelayed(this, 3000);
        }
    };

    private void readTemp() {
        isTurnOn = true;
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onPause() {
        if (isTurnOn) {
            isTurnOn = false;
        }
        super.onPause();
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
                            }

                            @Override
                            public void onIndicateFailure(BleException exception) {
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                if (!isTurnOn) {
                                    return;
                                }
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
                            }
                        });
            }
        }, 1000);

    }

    private void updateTemp() {
        if (tempList.get(0) > 39) {
            mTemp1.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp1.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(1) > 39) {
            mTemp2.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp2.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(2) > 39) {
            mTemp3.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp3.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(3) > 39) {
            mTemp4.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp4.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(4) > 39) {
            mTemp5.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp5.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(5) > 39) {
            mTemp6.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp6.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(6) > 39) {
            mTemp7.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp7.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(7) > 39) {
            mTemp8.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp8.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(8) > 39) {
            mTemp9.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp9.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        if (tempList.get(9) > 39) {
            mTemp10.setTextColor(getResources().getColor(R.color.qmui_config_color_red));
        } else {
            mTemp10.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        }
        DecimalFormat df = new DecimalFormat("0.0");
        boolean isSheShi = (boolean) SharedPreferencesUtils.getParam(StatusActivity.this, IS_SHESHI, true);
        if (isSheShi) {
            mTemp1.setText(df.format(tempList.get(0)) + "℃");
            mTemp2.setText(df.format(tempList.get(1)) + "℃");
            mTemp3.setText(df.format(tempList.get(2)) + "℃");
            mTemp4.setText(df.format(tempList.get(3)) + "℃");
            mTemp5.setText(df.format(tempList.get(4)) + "℃");
            mTemp6.setText(df.format(tempList.get(5)) + "℃");
            mTemp7.setText(df.format(tempList.get(6)) + "℃");
            mTemp8.setText(df.format(tempList.get(7)) + "℃");
            mTemp9.setText(df.format(tempList.get(8)) + "℃");
            mTemp10.setText(df.format(tempList.get(9)) + "℃");
        } else {
            mTemp1.setText(df.format(tempList.get(0) * 1.8 + 32) + "℉");
            mTemp2.setText(df.format(tempList.get(1) * 1.8 + 32) + "℉");
            mTemp3.setText(df.format(tempList.get(2) * 1.8 + 32) + "℉");
            mTemp4.setText(df.format(tempList.get(3) * 1.8 + 32) + "℉");
            mTemp5.setText(df.format(tempList.get(4) * 1.8 + 32) + "℉");
            mTemp6.setText(df.format(tempList.get(5) * 1.8 + 32) + "℉");
            mTemp7.setText(df.format(tempList.get(6) * 1.8 + 32) + "℉");
            mTemp8.setText(df.format(tempList.get(7) * 1.8 + 32) + "℉");
            mTemp9.setText(df.format(tempList.get(8) * 1.8 + 32) + "℉");
            mTemp10.setText(df.format(tempList.get(9) * 1.8 + 32) + "℉");
        }
    }
}
