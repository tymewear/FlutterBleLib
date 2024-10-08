package com.polidea.flutter_ble_lib.multiplatformbleadapter.utils.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.Device;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

public class RxBleDeviceToDeviceMapper {

    public Device map(@NonNull RxBleDevice rxDevice, @Nullable RxBleConnection connection) {
        Device device = new Device(rxDevice.getMacAddress(), rxDevice.getName());
        if (connection != null) {
            device.setMtu(connection.getMtu());
        }
        return device;
    }
}
