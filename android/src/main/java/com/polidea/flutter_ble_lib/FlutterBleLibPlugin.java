package com.polidea.flutter_ble_lib;
//package com.polidea.flutter_ble_lib_example;

import android.content.Context;
import android.util.Log;

import com.polidea.flutter_ble_lib.constant.ArgumentKey;
import com.polidea.flutter_ble_lib.constant.ChannelName;
import com.polidea.flutter_ble_lib.constant.MethodName;
import com.polidea.flutter_ble_lib.delegate.BluetoothStateDelegate;
import com.polidea.flutter_ble_lib.delegate.CallDelegate;
import com.polidea.flutter_ble_lib.delegate.CharacteristicsDelegate;
import com.polidea.flutter_ble_lib.delegate.DescriptorsDelegate;
import com.polidea.flutter_ble_lib.delegate.DeviceConnectionDelegate;
import com.polidea.flutter_ble_lib.delegate.DevicesDelegate;
import com.polidea.flutter_ble_lib.delegate.LogLevelDelegate;
import com.polidea.flutter_ble_lib.delegate.DiscoveryDelegate;
import com.polidea.flutter_ble_lib.delegate.MtuDelegate;
import com.polidea.flutter_ble_lib.delegate.RssiDelegate;
import com.polidea.flutter_ble_lib.event.AdapterStateStreamHandler;
import com.polidea.flutter_ble_lib.event.CharacteristicsMonitorStreamHandler;
import com.polidea.flutter_ble_lib.event.ConnectionStateStreamHandler;
import com.polidea.flutter_ble_lib.event.RestoreStateStreamHandler;
import com.polidea.flutter_ble_lib.event.ScanningStreamHandler;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.BleAdapter;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.BleAdapterFactory;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.OnErrorCallback;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.OnEventCallback;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.ScanResult;
import com.polidea.flutter_ble_lib.multiplatformbleadapter.errors.BleError;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.content.Context;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.EventChannel;
import java.util.LinkedList;
import java.util.List;

public class FlutterBleLibPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static final String TAG = FlutterBleLibPlugin.class.getName();

    private BleAdapter bleAdapter;
    private Context context;
    private MethodChannel channel;
    private EventChannel bluetoothStateChannel;
    private EventChannel restoreStateChannel;
    private EventChannel scanningChannel;
    private EventChannel connectionStateChannel;
    private EventChannel characteristicMonitorChannel;

    private AdapterStateStreamHandler adapterStateStreamHandler = new AdapterStateStreamHandler();
    private RestoreStateStreamHandler restoreStateStreamHandler = new RestoreStateStreamHandler();
    private ScanningStreamHandler scanningStreamHandler = new ScanningStreamHandler();
    private ConnectionStateStreamHandler connectionStateStreamHandler = new ConnectionStateStreamHandler();
    private CharacteristicsMonitorStreamHandler characteristicsMonitorStreamHandler = new CharacteristicsMonitorStreamHandler();

    private List<CallDelegate> delegates = new LinkedList<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.context = binding.getApplicationContext();

        channel = new MethodChannel(binding.getBinaryMessenger(), ChannelName.FLUTTER_BLE_LIB);
        channel.setMethodCallHandler(this);

        bluetoothStateChannel = new EventChannel(binding.getBinaryMessenger(), ChannelName.ADAPTER_STATE_CHANGES);
        restoreStateChannel = new EventChannel(binding.getBinaryMessenger(), ChannelName.STATE_RESTORE_EVENTS);
        scanningChannel = new EventChannel(binding.getBinaryMessenger(), ChannelName.SCANNING_EVENTS);
        connectionStateChannel = new EventChannel(binding.getBinaryMessenger(), ChannelName.CONNECTION_STATE_CHANGE_EVENTS);
        characteristicMonitorChannel = new EventChannel(binding.getBinaryMessenger(), ChannelName.MONITOR_CHARACTERISTIC);

        bluetoothStateChannel.setStreamHandler(adapterStateStreamHandler);
        restoreStateChannel.setStreamHandler(restoreStateStreamHandler);
        scanningChannel.setStreamHandler(scanningStreamHandler);
        connectionStateChannel.setStreamHandler(connectionStateStreamHandler);
        characteristicMonitorChannel.setStreamHandler(characteristicsMonitorStreamHandler);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        bluetoothStateChannel.setStreamHandler(null);
        restoreStateChannel.setStreamHandler(null);
        scanningChannel.setStreamHandler(null);
        connectionStateChannel.setStreamHandler(null);
        characteristicMonitorChannel.setStreamHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        // Implement if your plugin needs to handle activity results or interact with the Activity lifecycle
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // Handle activity detachment for config changes
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        // Handle re-attachment after config changes
    }

    @Override
    public void onDetachedFromActivity() {
        // Clean up activity-related resources if needed
    }

    private void setupAdapter(Context context) {
        bleAdapter = BleAdapterFactory.getNewAdapter(context);
        delegates.add(new DeviceConnectionDelegate(bleAdapter, connectionStateStreamHandler));
        delegates.add(new LogLevelDelegate(bleAdapter));
        delegates.add(new DiscoveryDelegate(bleAdapter));
        delegates.add(new BluetoothStateDelegate(bleAdapter));
        delegates.add(new RssiDelegate(bleAdapter));
        delegates.add(new MtuDelegate(bleAdapter));
        delegates.add(new CharacteristicsDelegate(bleAdapter, characteristicsMonitorStreamHandler));
        delegates.add(new DevicesDelegate(bleAdapter));
        delegates.add(new DescriptorsDelegate(bleAdapter));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        Log.d(TAG, "on native side observed method: " + call.method);
        for (CallDelegate delegate : delegates) {
            if (delegate.canHandle(call)) {
                delegate.onMethodCall(call, result);
                return;
            }
        }

        switch (call.method) {
            case MethodName.CREATE_CLIENT:
                createClient(call, result);
                break;
            case MethodName.DESTROY_CLIENT:
                destroyClient(result);
                break;
            case MethodName.START_DEVICE_SCAN:
                startDeviceScan(call, result);
                break;
            case MethodName.STOP_DEVICE_SCAN:
                stopDeviceScan(result);
                break;
            case MethodName.CANCEL_TRANSACTION:
                cancelTransaction(call, result);
                break;
            case MethodName.IS_CLIENT_CREATED:
                isClientCreated(result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void isClientCreated(Result result) {
        result.success(bleAdapter != null);
    }

    private void createClient(MethodCall call, Result result) {
        if (bleAdapter != null) {
            Log.w(TAG, "Overwriting existing native client. Use BleManager#isClientCreated to check whether a client already exists.");
        }
        setupAdapter(context);
        bleAdapter.createClient(call.<String>argument(ArgumentKey.RESTORE_STATE_IDENTIFIER),
                                adapterStateStreamHandler::onNewAdapterState,
                                restoreStateStreamHandler::onRestoreEvent);
        result.success(null);
    }

    private void destroyClient(Result result) {
        if (bleAdapter != null) {
            bleAdapter.destroyClient();
        }
        scanningStreamHandler.onComplete();
        connectionStateStreamHandler.onComplete();
        bleAdapter = null;
        delegates.clear();
        result.success(null);
    }

    private void startDeviceScan(@NonNull MethodCall call, Result result) {
        List<String> uuids = call.argument(ArgumentKey.UUIDS);
        bleAdapter.startDeviceScan(uuids.toArray(new String[0]),
                                   call.argument(ArgumentKey.SCAN_MODE),
                                   call.argument(ArgumentKey.CALLBACK_TYPE),
                                   scanningStreamHandler::onScanResult,
                                   scanningStreamHandler::onError);
        result.success(null);
    }

    private void stopDeviceScan(Result result) {
        if (bleAdapter != null) {
            bleAdapter.stopDeviceScan();
        }
        scanningStreamHandler.onComplete();
        result.success(null);
    }

    private void cancelTransaction(MethodCall call, Result result) {
        if (bleAdapter != null) {
            bleAdapter.cancelTransaction(call.argument(ArgumentKey.TRANSACTION_ID));
        }
        result.success(null);
    }
}
/*
public class FlutterBleLibPlugin implements MethodCallHandler {

    static final String TAG = FlutterBleLibPlugin.class.getName();

    private BleAdapter bleAdapter;
    private Context context;
    private AdapterStateStreamHandler adapterStateStreamHandler = new AdapterStateStreamHandler();
    private RestoreStateStreamHandler restoreStateStreamHandler = new RestoreStateStreamHandler();
    private ScanningStreamHandler scanningStreamHandler = new ScanningStreamHandler();
    private ConnectionStateStreamHandler connectionStateStreamHandler = new ConnectionStateStreamHandler();
    private CharacteristicsMonitorStreamHandler characteristicsMonitorStreamHandler = new CharacteristicsMonitorStreamHandler();

    private List<CallDelegate> delegates = new LinkedList<>();

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), ChannelName.FLUTTER_BLE_LIB);

        final EventChannel bluetoothStateChannel = new EventChannel(registrar.messenger(), ChannelName.ADAPTER_STATE_CHANGES);
        final EventChannel restoreStateChannel = new EventChannel(registrar.messenger(), ChannelName.STATE_RESTORE_EVENTS);
        final EventChannel scanningChannel = new EventChannel(registrar.messenger(), ChannelName.SCANNING_EVENTS);
        final EventChannel connectionStateChannel = new EventChannel(registrar.messenger(), ChannelName.CONNECTION_STATE_CHANGE_EVENTS);
        final EventChannel characteristicMonitorChannel = new EventChannel(registrar.messenger(), ChannelName.MONITOR_CHARACTERISTIC);

        final FlutterBleLibPlugin plugin = new FlutterBleLibPlugin(registrar.context());

        channel.setMethodCallHandler(plugin);

        scanningChannel.setStreamHandler(plugin.scanningStreamHandler);
        bluetoothStateChannel.setStreamHandler(plugin.adapterStateStreamHandler);
        restoreStateChannel.setStreamHandler(plugin.restoreStateStreamHandler);
        connectionStateChannel.setStreamHandler(plugin.connectionStateStreamHandler);
        characteristicMonitorChannel.setStreamHandler(plugin.characteristicsMonitorStreamHandler);
    }

    private FlutterBleLibPlugin(Context context) {
        this.context = context;
    }

    private void setupAdapter(Context context) {
        bleAdapter = BleAdapterFactory.getNewAdapter(context);
        delegates.add(new DeviceConnectionDelegate(bleAdapter, connectionStateStreamHandler));
        delegates.add(new LogLevelDelegate(bleAdapter));
        delegates.add(new DiscoveryDelegate(bleAdapter));
        delegates.add(new BluetoothStateDelegate(bleAdapter));
        delegates.add(new RssiDelegate(bleAdapter));
        delegates.add(new MtuDelegate(bleAdapter));
        delegates.add(new CharacteristicsDelegate(bleAdapter, characteristicsMonitorStreamHandler));
        delegates.add(new DevicesDelegate(bleAdapter));
        delegates.add(new DescriptorsDelegate(bleAdapter));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        Log.d(TAG, "on native side observed method: " + call.method);
        for (CallDelegate delegate : delegates) {
            if (delegate.canHandle(call)) {
                delegate.onMethodCall(call, result);
                return;
            }
        }

        switch (call.method) {
            case MethodName.CREATE_CLIENT:
                createClient(call, result);
                break;
            case MethodName.DESTROY_CLIENT:
                destroyClient(result);
                break;
            case MethodName.START_DEVICE_SCAN:
                startDeviceScan(call, result);
                break;
            case MethodName.STOP_DEVICE_SCAN:
                stopDeviceScan(result);
                break;
            case MethodName.CANCEL_TRANSACTION:
                cancelTransaction(call, result);
                break;
            case MethodName.IS_CLIENT_CREATED:
                isClientCreated(result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void isClientCreated(Result result) {
        result.success(bleAdapter != null);
    }

    private void createClient(MethodCall call, Result result) {
        if (bleAdapter != null) {
            Log.w(TAG, "Overwriting existing native client. Use BleManager#isClientCreated to check whether a client already exists.");
        }
        setupAdapter(context);
        bleAdapter.createClient(call.<String>argument(ArgumentKey.RESTORE_STATE_IDENTIFIER),
                new OnEventCallback<String>() {
                    @Override
                    public void onEvent(String adapterState) {
                        adapterStateStreamHandler.onNewAdapterState(adapterState);
                    }
                }, new OnEventCallback<Integer>() {
                    @Override
                    public void onEvent(Integer restoreStateIdentifier) {
                        restoreStateStreamHandler.onRestoreEvent(restoreStateIdentifier);
                    }
                });
        result.success(null);
    }

    private void destroyClient(Result result) {
        if (bleAdapter != null) {
            bleAdapter.destroyClient();
        }
        scanningStreamHandler.onComplete();
        connectionStateStreamHandler.onComplete();
        bleAdapter = null;
        delegates.clear();
        result.success(null);
    }

    private void startDeviceScan(@NonNull MethodCall call, Result result) {
        List<String> uuids = call.<List<String>>argument(ArgumentKey.UUIDS);
        bleAdapter.startDeviceScan(uuids.toArray(new String[uuids.size()]),
                call.<Integer>argument(ArgumentKey.SCAN_MODE),
                call.<Integer>argument(ArgumentKey.CALLBACK_TYPE),
                new OnEventCallback<ScanResult>() {
                    @Override
                    public void onEvent(ScanResult data) {
                        scanningStreamHandler.onScanResult(data);
                    }
                }, new OnErrorCallback() {
                    @Override
                    public void onError(BleError error) {
                        scanningStreamHandler.onError(error);
                    }
                });
        result.success(null);
    }

    private void stopDeviceScan(Result result) {
        if (bleAdapter != null) {
            bleAdapter.stopDeviceScan();
        }
        scanningStreamHandler.onComplete();
        result.success(null);
    }

    private void cancelTransaction(MethodCall call, Result result) {
        if (bleAdapter != null) {
            bleAdapter.cancelTransaction(call.<String>argument(ArgumentKey.TRANSACTION_ID));
        }
        result.success(null);
    }
}
*/