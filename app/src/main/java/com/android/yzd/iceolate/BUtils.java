package com.android.yzd.iceolate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.List;
import java.util.UUID;

/**
 * <p>Title:        BUtils
 * <p>Description:
 * <p>@author:      yezd
 * <p>Copyright:    Copyright (c) 2010-2017
 * <p>Company:      @咪咕动漫
 * <p>Create Time:  2018/1/22 下午11:14
 * <p>@author:
 * <p>Update Time:
 * <p>Updater:
 * <p>Update Comments:
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BUtils {

    private static final String SERVICE_NAME = "ThinIce";
    private BleDevice mBleDevice;
    private BluetoothGatt mBluetoothGatt;

    private BUtils() {
    }

    public static BUtils getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void setBleDevice(BleDevice bleDevice) {
        mBleDevice = bleDevice;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        mBluetoothGatt = bluetoothGatt;
    }

    private static class LazyHolder {
        private static BUtils INSTANCE = new BUtils();

    }

    public BleDevice getBleDevice() {
        if (isConnect()) {
            return mBleDevice;
        }
        return mBleDevice;
    }

    public BluetoothGatt getBleDeviceGatt() {
        if (isConnect()) {
            return BleManager.getInstance().getBluetoothGatt(mBleDevice);
        }
        return mBluetoothGatt;
    }

    public BluetoothGattCharacteristic getWriteBluetoothGattCharacteristic() {
        BluetoothGatt bleDeviceGatt = getBleDeviceGatt();
        if (bleDeviceGatt != null) {
            List<BluetoothGattService> services = bleDeviceGatt.getServices();
            BluetoothGattService bluetoothGattService = services.get(4);
            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
            return characteristics.get(0);
        } else {
            return null;
        }
    }

    public BluetoothGattCharacteristic getReadBluetoothGattCharacteristic() {
        BluetoothGatt bleDeviceGatt = getBleDeviceGatt();
        if (bleDeviceGatt != null) {
            List<BluetoothGattService> services = bleDeviceGatt.getServices();
            BluetoothGattService bluetoothGattService = services.get(4);
            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
            return characteristics.get(1);
        } else {
            return null;
        }
    }

    private List<BleDevice> getServices() {
        return BleManager.getInstance().getAllConnectedDevice();
    }

    public boolean isConnect() {
        List<BleDevice> services = getServices();
        if (services != null && services.size() > 0) {
            BleDevice bleDevice = services.get(0);
            if (bleDevice != null) {
                String name = bleDevice.getName();
                if (name.equalsIgnoreCase(SERVICE_NAME)) {
                    mBleDevice = bleDevice;
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private BluetoothGattCharacteristic getCharact() {
        if (isConnect() && mBleDevice != null) {
            BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(mBleDevice);
            List<BluetoothGattService> services = gatt.getServices();
            if (services != null && services.size() == 5) {
                BluetoothGattService bluetoothGattService = services.get(4);
                List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                 return characteristics.get(0);
            }
            return null;
        } else {
            return null;
        }
    }

    private String getUUid() {
        BluetoothGattCharacteristic charact = getCharact();
        if (charact != null) {
            return charact.getUuid().toString();
        } else {
            return "";
        }
    }

    private String getServiceUUid() {
        BluetoothGattCharacteristic charact = getCharact();
        if (charact != null) {
            return charact.getService().getUuid().toString();
        } else {
            return "";
        }
    }

}
