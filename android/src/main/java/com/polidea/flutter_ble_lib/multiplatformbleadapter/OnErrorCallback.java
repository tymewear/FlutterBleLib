package com.polidea.flutter_ble_lib.multiplatformbleadapter;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.errors.BleError;

public interface OnErrorCallback {

    void onError(BleError error);
}
