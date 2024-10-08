package com.polidea.flutter_ble_lib;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.errors.BleError;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.errors.BleErrorCode;


public class BleErrorFactory {
    
    public static BleError fromThrowable(Throwable exception) {
        return new BleError(BleErrorCode.UnknownError, exception.toString(), null);
    }
}
