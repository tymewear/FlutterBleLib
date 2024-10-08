package com.polidea.flutter_ble_lib.multiplatformbleadapter.exceptions;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.Characteristic;

public class CannotMonitorCharacteristicException extends RuntimeException {
    private Characteristic characteristic;

    public CannotMonitorCharacteristicException(Characteristic characteristic) {
        this.characteristic = characteristic;
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }
}
