package com.polidea.flutter_ble_lib.multiplatformbleadapter.utils.mapper;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.AdvertisementData;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.ScanResult;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.utils.Constants;

public class RxScanResultToScanResultMapper {

    public ScanResult map(com.polidea.rxandroidble2.scan.ScanResult rxScanResult) {
        return new ScanResult(
                rxScanResult.getBleDevice().getMacAddress(),
                rxScanResult.getBleDevice().getName(),
                rxScanResult.getRssi(),
                Constants.MINIMUM_MTU,
                false, //isConnectable flag is not available on Android
                null, //overflowServiceUUIDs are not available on Android
                AdvertisementData.parseScanResponseData(rxScanResult.getScanRecord().getBytes())
        );
    }
}
