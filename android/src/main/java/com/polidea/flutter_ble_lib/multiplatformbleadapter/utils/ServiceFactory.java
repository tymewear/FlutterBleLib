package com.polidea.flutter_ble_lib.multiplatformbleadapter.utils;

import android.bluetooth.BluetoothGattService;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.Service;

public class ServiceFactory {

    public Service create(String deviceId, BluetoothGattService btGattService) {
        return new Service(
                IdGenerator.getIdForKey(new IdGeneratorKey(deviceId, btGattService.getUuid(), btGattService.getInstanceId())),
                deviceId,
                btGattService
        );
    }
}
