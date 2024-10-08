package com.polidea.flutter_ble_lib.multiplatformbleadapter;

import android.content.Context;

public interface BleAdapterCreator {
    BleAdapter createAdapter(Context context);
}
