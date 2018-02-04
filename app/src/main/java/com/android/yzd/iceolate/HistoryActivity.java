package com.android.yzd.iceolate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.yzd.iceolate.dummy.DummyItem;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.silencedut.taskscheduler.TaskScheduler;

import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HistoryActivity extends AppCompatActivity implements HistoryItemFragment.OnListFragmentInteractionListener {

    private QMUITabSegment mTabSegment;
    private ViewPager mContentViewPager;
    private static int TAB_COUNT = 10;
    private BleDevice mBleDevice;
    private BluetoothGattCharacteristic mReadBluetoothGattCharacteristic;
    private boolean isFirstTime = true;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int calMinute;
    private int calHour;
    private int calDay;
    private int calMonth;
    private String nowTime;


    private int readCount = 0;
    private HashMap<String, ArrayList<DummyItem>> dataMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("History temperature");
        mTabSegment = (QMUITabSegment) findViewById(R.id.tabSegment);
        mContentViewPager = (ViewPager) findViewById(R.id.contentViewPager);

        BUtils instance = BUtils.getInstance();
        if (!instance.isConnect()) {
            finish();
            Toast.makeText(this, "please connect bluetooth first", Toast.LENGTH_LONG).show();
            return;
        }
        mBleDevice = instance.getBleDevice();
        mReadBluetoothGattCharacteristic = instance.getReadBluetoothGattCharacteristic();
        initTabAndPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyData();
        readHistoryTemp();
    }

    private void readHistoryTemp() {
        BleControl.getInstance().readHistory();
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
                                if (isFirstTime) {
                                    isFirstTime = false;
                                    for (int i = 0; i < data.length; i++) {
                                        int shuju = data[i] & 0xFF;
                                        if (i == 0) {
                                            year = shuju;
                                        } else if (i == 1) {
                                            month = shuju;
                                            calMonth = month;
                                        } else if (i == 2) {
                                            day = shuju;
                                            calDay = day;
                                        } else if (i == 3) {
                                            hour = shuju;
                                            calHour = hour;
                                        } else if (i == 4) {
                                            minute = shuju;
                                            calMinute = minute;
                                        } else if (i == 5) {
                                            second = shuju;
                                        }
                                    }
                                    nowTime = "20" + year + "/" + month
                                            + "/" + day + " " + hour
                                            + ":" + minute + ":" + second;
                                    Log.d("yzd", "first" + nowTime);
                                } else {

                                    calMinute = calMinute - 5;
                                    if (calMinute < 0) {
                                        calMinute = 60 + calMinute;
                                        calHour = hour - 1;
                                        if (calHour == 0) {
                                            calDay = calDay - 1;
                                            if (calDay == 0) {
                                                calMonth = calMonth - 1;
                                                if (calMonth == 1
                                                        || calMonth == 3
                                                        || calMonth == 5
                                                        || calMonth == 7
                                                        || calMonth == 8
                                                        || calMonth == 10) {
                                                    calDay = 31;
                                                } else if (calMonth == 2) {
                                                    calDay = 28;
                                                } else if (calMonth == 0){
                                                    calDay = 31;
                                                    calMonth = 12;
                                                } else {
                                                    calDay = 30;
                                                }
                                            }
                                        }
                                    }


                                    int[] bytes = new int[2];
                                    bytes[0] = 0;
                                    bytes[1] = 0;
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

                                            int i1 = i / 2 + 1;
                                            ArrayList<DummyItem> dummyItems = dataMap.get("T" + i1);
                                            if (dummyItems == null) {
                                                dummyItems = new ArrayList<DummyItem>();
                                            }
                                            DummyItem dummyItem = new DummyItem(nowTime, nowTemp);
                                            dummyItems.add(dummyItem);
                                            dataMap.put("T" + i1, dummyItems);
                                        }
                                    }

                                    nowTime = "20" + year + "/" + calMonth
                                            + "/" + calDay + " " + calHour
                                            + ":" + calMinute + ":" + second;
                                    ++readCount;
                                    Log.d("yzd", "count" + readCount);
                                    Log.d("yzd", "time" + nowTime);
                                }
                            }
                        });
            }
        }, 0);

    }

    private void initTabAndPager() {
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                mContentViewPager.setAdapter(mPagerAdapter);
                mContentViewPager.setCurrentItem(1, false);
                mTabSegment.setDefaultSelectedColor(getResources().getColor(R.color.app_color_blue));
                for (int i = 0; i < TAB_COUNT; i++) {
                    mTabSegment.addTab(new QMUITabSegment.Tab("T" + (i + 1)));
                }
                int space = QMUIDisplayHelper.dp2px(HistoryActivity.this, 32);
                mTabSegment.setHasIndicator(true);
                mTabSegment.setMode(QMUITabSegment.MODE_SCROLLABLE);
                mTabSegment.setItemSpaceInScrollMode(space);
                mTabSegment.setupWithViewPager(mContentViewPager, false);
                mTabSegment.setPadding(space, 0, space, 0);
            }
        }, 5000);
    }

    private FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Fragment getItem(int position) {
            int i = position + 1;
            ArrayList<DummyItem> dummyItems = dataMap.get("T" + i);
            return HistoryItemFragment.newInstance(dummyItems);
        }
    };

    @Override
    public void onListFragmentInteraction(DummyItem item) {

    }

}
