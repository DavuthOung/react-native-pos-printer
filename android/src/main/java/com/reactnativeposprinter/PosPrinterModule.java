package com.reactnativeposprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import org.json.JSONObject;
import java.util.Set;

import javax.annotation.Nullable;

@ReactModule(name = PosPrinterModule.NAME)
public class PosPrinterModule extends ReactContextBaseJavaModule {
    public static final String NAME = "PosPrinter";
    private final ReactApplicationContext context;
    private static final boolean DEVICE_SUPPORTED = true;
    private static final boolean DEVICE_UNSUPPORTED = false;
    private static final Integer REQUEST_ENABLE_BT = 1;
    private static final String NOT_SUPPORT = "Device not support";
    public static final String EVENT_BLUETOOTH_NOT_SUPPORT = "EVENT_BLUETOOTH_NOT_SUPPORT";
    private static final String PROMISE_DIS_ENABLE_BT = "DIS_ENABLE_BT";
    private BluetoothAdapter mBluetoothAdapter = null;

    public PosPrinterModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.context = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    // local bluetooth adapter
    private BluetoothAdapter getBluetoothAdapter(){
      if(mBluetoothAdapter == null){
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      }
      // If the adapter is null, then Bluetooth is not supported
      if (mBluetoothAdapter == null) {
        emitRNEvent(EVENT_BLUETOOTH_NOT_SUPPORT,  Arguments.createMap());
      }
      return mBluetoothAdapter;
    }

    private void emitRNEvent(String event, @Nullable WritableMap params) {
      getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(event, params);
    }

    private static boolean checksDeviceSupported(Context context) {
      if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) return DEVICE_SUPPORTED;
      else return DEVICE_UNSUPPORTED;
    }

    @ReactMethod
    public void checkBluetoothEnable(final Promise promise) {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      boolean isEnabled = bluetoothAdapter.isEnabled();
      boolean support = checksDeviceSupported(this.context);
      if (support) {
        promise.resolve(isEnabled);
      } else promise.reject(NOT_SUPPORT);
    }

    @ReactMethod
    public void enableBluetooth(final Promise promise){
      try {
        BluetoothAdapter adapter = this.getBluetoothAdapter();
        if(adapter == null){
          promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT);
        }else if (!adapter.isEnabled()) {
          // If Bluetooth is not on, request that it be enabled.
          // setupChat() will then be called during onActivityResult
          Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          this.context.startActivityForResult(enableIntent, REQUEST_ENABLE_BT, Bundle.EMPTY);
        }
      } catch (Exception ex){
        promise.reject(ex);
      }
    }

    @ReactMethod
    public void disableBluetooth(final Promise promise) {
      BluetoothAdapter adapter = this.getBluetoothAdapter();
      if(adapter == null){
        promise.resolve(PROMISE_DIS_ENABLE_BT);
      }else {
        promise.resolve(!adapter.isEnabled() || adapter.disable());
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void scanDevices(final Promise promise) {
      BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
      mBluetoothAdapter = bluetoothManager.getAdapter();
      LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();

      mBluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
          super.onScanResult(callbackType, result);
          leDeviceListAdapter.addDevice(result.getDevice());
          leDeviceListAdapter.notifyDataSetChanged();

        }
      });

    }

    @ReactMethod
    public void getDevicePaired(final Promise promise) {
        WritableArray pairedDevice=Arguments.createArray();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                try {
                  String deviceName = device.getName();
                  String deviceHardwareAddress = device.getAddress(); // MAC address
                  JSONObject obj = new JSONObject();
                  obj.put("name", deviceName);
                  obj.put("address", deviceHardwareAddress);
                  pairedDevice.pushString(obj.toString());
                } catch (Exception e) {
                  promise.reject(e);
                }
            }
            promise.resolve(pairedDevice);
        }
    }

    @ReactMethod
    public void connect(String address,Promise promise) {
        String unspecified = "BluetoothAdapter not initialized or unspecified address.";
        String connected = "Connected";
        if (mBluetoothAdapter == null || address == null) {
          promise.reject(unspecified);
        }
        try {
          final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
          promise.resolve(connected);
        } catch (IllegalArgumentException exception) {
          promise.reject(exception);
        }
    }
}
