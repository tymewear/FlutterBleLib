package com.polidea.flutter_ble_lib;

import com.polidea.flutter_ble_lib.multiplatformbleadapter.Characteristic;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.Service;

import java.util.List;

public class CharacteristicsResponse {
    private final List<Characteristic> characteristics;
    private final Service service;

    public CharacteristicsResponse(List<Characteristic> characteristics, Service service) {
        this.characteristics = characteristics;
        this.service = service;
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public Service getService() {
        return service;
    }
}
