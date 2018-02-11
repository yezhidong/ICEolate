package com.android.yzd.iceolate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.silencedut.taskscheduler.TaskScheduler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import info.hoang8f.widget.FButton;

import static com.android.yzd.iceolate.R.id.imageView;
import static com.android.yzd.iceolate.SettingsActivity.Alert;
import static com.android.yzd.iceolate.SettingsActivity.IS_SHESHI;
import static com.android.yzd.iceolate.SettingsActivity.Shake;
import static com.android.yzd.iceolate.SettingsActivity.ShakeSound;
import static com.android.yzd.iceolate.SettingsActivity.Sound;
import static com.android.yzd.iceolate.SettingsActivity.TEMPERATURE_SETTING;
import static com.android.yzd.iceolate.SettingsActivity.defaulttem;

public class StatusFragment extends Fragment {

    public static final String KEY_DATA = "KEY_DATA";
    private static int errorTemp = -99;
    private BleDevice mBleDevice;
    //    private BluetoothGatt mBleDeviceGatt;
    private BluetoothGattCharacteristic mGattCharacteristic;
    private BluetoothGattCharacteristic mReadBluetoothGattCharacteristic;

    List<Float> tempList = new ArrayList<Float>();
    List<TextView> mTextViewList = new ArrayList<>();
    List<ImageView> mViewList = new ArrayList<>();
    private FButton mSwitchButton;
    private FButton mDeleteButton;
    private boolean isTurnOn = true;
    private boolean isAlert;
    private boolean isSound;
    private boolean isShake;
    private boolean isShakeSound;
    private Vibrator vibrator;
    private MediaPlayer mMediaPlayer;
    private OnDelete mListener;
    private ImageView mBatery;
    private View mBlueFlag;

    public StatusFragment() {
    }

    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
        return fragment;
    }

    private void readTemp() {
        isTurnOn = true;
        handler.postDelayed(runnable, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        isTurnOn = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isTurnOn = false;
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
                                connectTime = 0;
                                if (!isTurnOn) {
                                    return;
                                }
                                int[] bytes = new int[2];
                                bytes[0] = 0;
                                bytes[1] = 0;
                                int j = 0;
                                boolean isControl = false;
                                for (int i = 0; i < data.length; i++) {
                                    if (i == 0) {// 判别第一个字节是不是控制指令 b1
                                        int d0 = data[0] & 0xFF;
                                        if (d0 == 177) {
                                            isControl = true;
                                            String string = Integer.toBinaryString(data[5] & 0xFF);
                                            if (string != null && string.length() > 2) {
                                                String substring = string.substring(string.length() - 2, 2);
                                                int parseInt = Integer.parseInt(substring, 2);
                                                if (parseInt == 0) {
                                                    mBatery.setBackgroundResource(R.drawable.ic_battery_1);
                                                } else if (parseInt == 1) {
                                                    mBatery.setBackgroundResource(R.drawable.ic_battery_3);
                                                } else if (parseInt == 2) {
                                                    mBatery.setBackgroundResource(R.drawable.ic_battery_6);
                                                } else {
                                                    mBatery.setBackgroundResource(R.drawable.ic_battery_9);
                                                }
                                            }

                                            break;
                                        }
                                    }

                                    if (i % 2 == 0) {
                                        bytes[0] = data[i] & 0xFF;
                                    } else {
                                        float nowTemp = 0;
                                        if (bytes[0] >= 4) {
                                            nowTemp = errorTemp;
                                        } else {
                                            bytes[1] = data[i] & 0xFF;
                                            float parseInt = bytes[0] * 255 + bytes[1];
                                            if (parseInt < 100) {
                                                nowTemp = -(100 - parseInt) / 10;
                                            } else {
                                                nowTemp = (parseInt - 100) / 10;
                                            }
                                        }
                                        tempList.add(j++, nowTemp);
                                    }
                                }
                                if (!isControl) {
                                    updateTemp();
                                }
                            }
                        });
            }
        }, 1000);

    }

    private void shouldAlert() {
        if (!isAlert) {
            return;
        }

        if (isShakeSound) {
            startAlarm();
            startVibrate();
        } else if (isShake) {
            startVibrate();
        } else if (isSound) {
            startAlarm();
        }

    }

    private int connectTime = 0;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (connectTime >= 10) {
                blueFlagVisible(false);
            } else {
                blueFlagVisible(true);
            }

            connectTime++;
            handler.postDelayed(this, 1000);
            if (!isTurnOn) {
                return;
            }
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
    };

    private void blueFlagVisible(final boolean b) {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (mBlueFlag != null) {
                    if (b) {
                        mBlueFlag.setVisibility(View.VISIBLE);
                    } else {
                        mBlueFlag.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDelete) {
            mListener = (OnDelete) context;
        }
        BUtils instance = BUtils.getInstance();
        if (instance.isConnect()) {
            mBleDevice = instance.getBleDevice();
            mGattCharacteristic = instance.getWriteBluetoothGattCharacteristic();
            mReadBluetoothGattCharacteristic = instance.getReadBluetoothGattCharacteristic();
            notifyData();
            readTemp();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isAlert = (boolean) SharedPreferencesUtils.getParam(getActivity(), Alert, false);
        isSound = (boolean) SharedPreferencesUtils.getParam(getActivity(), Sound, false);
        isShake = (boolean) SharedPreferencesUtils.getParam(getActivity(), Shake, false);
        isShakeSound = (boolean) SharedPreferencesUtils.getParam(getActivity(), ShakeSound, false);
        initView(view);
    }

    private void initView(View view) {
        TextView mTemp1 = (TextView) view.findViewById(R.id.temp1);
        TextView mTemp2 = (TextView) view.findViewById(R.id.temp2);
        TextView mTemp3 = (TextView) view.findViewById(R.id.temp3);
        TextView mTemp4 = (TextView) view.findViewById(R.id.temp4);
        TextView mTemp5 = (TextView) view.findViewById(R.id.temp5);
        TextView mTemp6 = (TextView) view.findViewById(R.id.temp6);
        TextView mTemp7 = (TextView) view.findViewById(R.id.temp7);
        TextView mTemp8 = (TextView) view.findViewById(R.id.temp8);
        TextView mTemp9 = (TextView) view.findViewById(R.id.temp9);
        TextView mTemp10 = (TextView) view.findViewById(R.id.temp10);

        mBatery = (ImageView) view.findViewById(R.id.batery);
        mBlueFlag = view.findViewById(R.id.blueFlag);

        mViewList.add((ImageView) view.findViewById(R.id.bg1));
        mViewList.add((ImageView) view.findViewById(R.id.bg2));
        mViewList.add((ImageView) view.findViewById(R.id.bg3));
        mViewList.add((ImageView) view.findViewById(R.id.bg4));
        mViewList.add((ImageView) view.findViewById(R.id.bg5));
        mViewList.add((ImageView) view.findViewById(R.id.bg6));
        mViewList.add((ImageView) view.findViewById(R.id.bg7));
        mViewList.add((ImageView) view.findViewById(R.id.bg8));
        mViewList.add((ImageView) view.findViewById(R.id.bg9));
        mViewList.add((ImageView) view.findViewById(R.id.bg10));

        mTextViewList.add(mTemp1);
        mTextViewList.add(mTemp2);
        mTextViewList.add(mTemp3);
        mTextViewList.add(mTemp4);
        mTextViewList.add(mTemp5);
        mTextViewList.add(mTemp6);
        mTextViewList.add(mTemp7);
        mTextViewList.add(mTemp8);
        mTextViewList.add(mTemp9);
        mTextViewList.add(mTemp10);
        for (int i = 0; i < 10; i++) {
            tempList.add(0f);
        }
        mSwitchButton = (FButton) view.findViewById(R.id.switchButton);
        mDeleteButton = (FButton) view.findViewById(R.id.deleteButton);
        mDeleteButton.setButtonColor(getResources().getColor(R.color.qmui_config_color_red));
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTurnOn) {
                    BleControl.getInstance().close(100);
                    mSwitchButton.setText("TURN ON");
                    isTurnOn = false;
                } else {
                    BleControl.getInstance().open(100);
                    mSwitchButton.setText("TURN OFF");
                    readTemp();
                    isTurnOn = true;
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleControl.getInstance().close(0);
                isTurnOn = false;
                if (mListener != null) {
                    mListener.onDelete();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopAlarm();
        stopViberate();
    }

    /**
     * 开启震动
     */
    private void startVibrate() {
        if (vibrator == null) {
            //获取震动服务
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
        //震动模式隔1秒震动1.4秒
        long[] pattern = {1000, 1400};
        //震动重复，从数组的0开始（-1表示不重复）
        vibrator.vibrate(pattern, 0);
    }

    /**
     * 停止震动
     */
    private void stopViberate() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    /**
     * 播放系统声音
     */
    private void startAlarm() {
        // 如果为空，才构造，不为空，说明之前有构造过
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.setDataSource(getActivity(), getSystemDefultRingtoneUri());
            mMediaPlayer.setLooping(true); //循环播放
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {

        }

    }

    /**
     * 停止播放来电声音
     */
    private void stopAlarm() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    // 获取系统默认铃声的Uri
    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE);
    }

    private void updateTemp() {
        int tempetureSetting = (int) SharedPreferencesUtils.getParam(getActivity(), TEMPERATURE_SETTING, defaulttem);
        DecimalFormat df = new DecimalFormat("0.0");
        boolean isSheShi = (boolean) SharedPreferencesUtils.getParam(getActivity(), IS_SHESHI, true);
        int alertCount = 0;
        for (int i = 0; i < tempList.size(); i++) {
            if (i == 10) {
                break;
            }
            Log.d("yzd", "i=" + i);
            Float aFloat = tempList.get(i);
            if (aFloat == errorTemp || aFloat > 80) {
                mTextViewList.get(i).setText("error");
                continue;
            }
            if (aFloat > tempetureSetting) {
                mTextViewList.get(i).setTextColor(getResources().getColor(R.color.qmui_config_color_red));
                mViewList.get(i).setEnabled(false);
                alertCount++;
            } else {
                mTextViewList.get(i).setTextColor(getResources().getColor(R.color.qmui_config_color_white));
                mViewList.get(i).setEnabled(true);
            }

            if (isSheShi) {
                mTextViewList.get(i).setText(df.format(tempList.get(i)) + "℃");
            } else {
                mTextViewList.get(i).setText(df.format(tempList.get(0) * 1.8 + 32) + "℉");
            }
        }
        if (alertCount > 0) {
            shouldAlert();
        } else {
            stopViberate();
            stopAlarm();
        }

    }

    public interface OnDelete {
        void onDelete();
    }

}
