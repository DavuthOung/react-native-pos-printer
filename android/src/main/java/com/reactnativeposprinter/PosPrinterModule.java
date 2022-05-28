package com.reactnativeposprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.print.PrintHelper;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;

@ReactModule(name = PosPrinterModule.NAME)
public class PosPrinterModule extends ReactContextBaseJavaModule  implements ActivityEventListener {

  private final ReactApplicationContext reactContext;
  public static final String NAME = "PosPrinter";
  private static final boolean DEVICE_SUPPORTED = true;
  private static final boolean DEVICE_UNSUPPORTED = false;
  private static final String NOT_SUPPORT = "Device not support";
  public static final String EVENT_BLUETOOTH_NOT_SUPPORT = "EVENT_BLUETOOTH_NOT_SUPPORT";
  private static final String PROMISE_DIS_ENABLE_BT = "DIS_ENABLE_BT";
  public static final String EVENT_DEVICE_FOUND = "EVENT_DEVICE_FOUND";
  private static final String PROMISE_SCAN = "SCAN";
  public static final String EVENT_DEVICE_DISCOVER_DONE = "EVENT_DEVICE_DISCOVER_DONE";
  private static final String PROMISE_CONNECT = "CONNECT";
  public static final String EVENT_CONNECTED = "EVENT_CONNECTED";
  public static final String EVENT_CONNECTION_LOST = "EVENT_CONNECTION_LOST";
  public static final String EVENT_UNABLE_CONNECT = "EVENT_UNABLE_CONNECT";
  public static final String EVENT_DEVICE_ALREADY_PAIRED = "EVENT_DEVICE_ALREADY_PAIRED";
  public static final String CONNECTION_ERROR = "CONNECTION_ERROR";

  public static final int MESSAGE_CONNECTION_LOST = BluetoothService.MESSAGE_CONNECTION_LOST;
  public static final int MESSAGE_UNABLE_CONNECT = BluetoothService.MESSAGE_UNABLE_CONNECT;
  public static final String DEVICE_NAME = BluetoothService.DEVICE_NAME;
  public static final int MESSAGE_DEVICE_NAME = BluetoothService.MESSAGE_DEVICE_NAME;
  public static final int STATE_CONNECTED = BluetoothService.STATE_CONNECTED;

  // Name of the connected device
  private String mConnectedDeviceName = null;
  private BluetoothAdapter mBluetoothAdapter = null;

  private static final Map<String, Promise> promiseMap = Collections.synchronizedMap(new HashMap<String, Promise>());

  // Intent request codes
  private static final int REQUEST_CONNECT_DEVICE = 1;
  private static final int REQUEST_ENABLE_BT = 2;
  private static final String PROMISE_ENABLE_BT = "ENABLE_BT";

  private BluetoothService mService = null;

  // Return Intent extra
  public static String EXTRA_DEVICE_ADDRESS = "device_address";

  private final JSONArray pairedDevice = new JSONArray();
  private final JSONArray foundDevice = new JSONArray();

  public PosPrinterModule(ReactApplicationContext reactContext,BluetoothService bluetoothService) {
      super(reactContext);
      this.reactContext = reactContext;
      this.reactContext.addActivityEventListener(this);
      this.mService = bluetoothService;

      // Register for broadcasts when a device is discovered
      IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
      this.reactContext.registerReceiver(discoverReceiver, filter);
  }

  @Override
  @NonNull
  public String getName() {
      return NAME;
  }

  @Override
  public
  @Nullable
  Map<String, Object> getConstants() {
      Map<String, Object> constants = new HashMap<>();
      constants.put(EVENT_DEVICE_ALREADY_PAIRED, EVENT_DEVICE_ALREADY_PAIRED);
      constants.put(EVENT_DEVICE_DISCOVER_DONE, EVENT_DEVICE_DISCOVER_DONE);
      constants.put(EVENT_DEVICE_FOUND, EVENT_DEVICE_FOUND);
      constants.put(EVENT_CONNECTION_LOST, EVENT_CONNECTION_LOST);
      constants.put(EVENT_UNABLE_CONNECT, EVENT_UNABLE_CONNECT);
      constants.put(EVENT_CONNECTED, EVENT_CONNECTED);
      constants.put(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT);
      constants.put(DEVICE_NAME, DEVICE_NAME);
      return constants;
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
    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(event, params);
  }

  private static boolean checksDeviceSupported(Context context) {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) return DEVICE_SUPPORTED;
    else return DEVICE_UNSUPPORTED;
  }

  @ReactMethod
  public void checkBluetoothEnable(final Promise promise) {
  BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  boolean isEnabled = bluetoothAdapter.isEnabled();
  boolean support = checksDeviceSupported(this.reactContext);
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
      this.reactContext.startActivityForResult(enableIntent, REQUEST_ENABLE_BT, Bundle.EMPTY);
    }
  } catch (Exception ex){
    promise.reject(ex);
  }
}

  @ReactMethod
  public void disableBluetooth(final Promise promise) {
      BluetoothAdapter adapter = this.getBluetoothAdapter();
      if(adapter == null){
          CharSequence text = "Bluetooth disabled";
          int duration = Toast.LENGTH_SHORT;
          Toast toast = Toast.makeText(this.reactContext, text, duration);
          toast.show();
          promise.resolve(PROMISE_DIS_ENABLE_BT);
      } else {
          promise.resolve(!adapter.isEnabled() || adapter.disable());
      }
  }

  @ReactMethod
  public void scanDevices(final Promise promise) {

  }

  @ReactMethod
  public void getDevicePaired(final Promise promise) {
      WritableArray pairedDevice=Arguments.createArray();
      if (mBluetoothAdapter != null) {
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
      } else promise.reject("Error");
  }

  @ReactMethod
  public void connect(String address,Promise promise) {
      try {
          BluetoothAdapter adapter = this.getBluetoothAdapter();
          if (adapter!=null && adapter.isEnabled()) {
              BluetoothDevice device = adapter.getRemoteDevice(address);
              mService.connect(device);
              if (mService.getState() != STATE_CONNECTED) {
                promise.resolve(true);
                CharSequence text = "Device connected.";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this.reactContext, text, duration);
                toast.show();
              } else {
                promise.resolve(false);
                CharSequence text = "Device connecting failed.";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(this.reactContext, text, duration);
                toast.show();
              }
          } else {
              promise.reject("BT NOT ENABLED");
          }
      }
      catch (Exception e) {
          promise.reject(e.getMessage());
      }
  }

  @ReactMethod
  public void initializeBluetooth(String currentBluetooth, Promise promise){
    BluetoothAdapter adapter = this.getBluetoothAdapter();
    if (adapter!=null && adapter.isEnabled()) {
      BluetoothDevice device = adapter.getRemoteDevice(currentBluetooth);
      mService.connect(device);
      CharSequence text = "Device connected.";
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(this.reactContext, text, duration);
      toast.show();
      promise.resolve("BLUETOOTH_CONNECTED");
    } else {
      promise.resolve(CONNECTION_ERROR);
    }
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      BluetoothAdapter adapter = this.getBluetoothAdapter();
      switch (requestCode) {
          case REQUEST_CONNECT_DEVICE: {
              // When DeviceListActivity returns with a device to connect
              if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(
                  EXTRA_DEVICE_ADDRESS);
                // Get the BluetoothDevice object
                if (adapter!=null && BluetoothAdapter.checkBluetoothAddress(address)) {
                  BluetoothDevice device = adapter.getRemoteDevice(address);
                  // Attempt to connect to the device
                  mService.connect(device);
                }
              }
              break;
          }
          case REQUEST_ENABLE_BT: {
              Promise promise = promiseMap.remove(PROMISE_ENABLE_BT);
              // When the request to enable Bluetooth returns
              if (resultCode == Activity.RESULT_OK && promise != null) {
                  // Bluetooth is now enabled, so set up a session
                  if(adapter!=null){
                      WritableArray pairedDeivce =Arguments.createArray();
                      Set<BluetoothDevice> boundDevices = adapter.getBondedDevices();
                      for (BluetoothDevice d : boundDevices) {
                          try {
                            JSONObject obj = new JSONObject();
                            obj.put("name", d.getName());
                            obj.put("address", d.getAddress());
                            pairedDeivce.pushString(obj.toString());
                          } catch (Exception e) {
                            //ignore.
                          }
                      }
                      promise.resolve(pairedDeivce);
                  }else{
                      promise.resolve(null);
                  }
              } else {
                  if (promise != null) {
                     promise.reject("ERR", new Exception("BT NOT ENABLED"));
                  }
              }
              break;
          }
      }
  }

  @Override
  public void onNewIntent(Intent intent) {

  }

  private boolean objectFound(JSONObject obj) {
      boolean found = false;
      if (foundDevice.length() > 0) {
          for (int i = 0; i < foundDevice.length(); i++) {
              try {
                  String objAddress = obj.optString("address", "objAddress");
                  String dsAddress = ((JSONObject) foundDevice.get(i)).optString("address", "dsAddress");
                  if (objAddress.equalsIgnoreCase(dsAddress)) {
                      found = true;
                      break;
                  }
              } catch (Exception e) {
              }
          }
      }
      return found;
  }

  // The BroadcastReceiver that listens for discovered devices and
  // changes the title when discovery is finished
  private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          // When discovery finds a device
          if (BluetoothDevice.ACTION_FOUND.equals(action)) {
              // Get the BluetoothDevice object from the Intent
              BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
              if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                  JSONObject deviceFound = new JSONObject();
                  try {
                      deviceFound.put("name", device.getName());
                      deviceFound.put("address", device.getAddress());
                  } catch (Exception e) {
                    //ignore
                  }
                  if (!objectFound(deviceFound)) {
                      foundDevice.put(deviceFound);
                      WritableMap params = Arguments.createMap();
                      params.putString("device", deviceFound.toString());
                      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(EVENT_DEVICE_FOUND, params);
                  }
              }
          } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
              Promise promise = promiseMap.remove(PROMISE_SCAN);
              if (promise != null) {
                  JSONObject result = null;
                  try {
                      result = new JSONObject();
                      result.put("paired", pairedDevice);
                      result.put("found", foundDevice);
                      promise.resolve(result.toString());
                  } catch (Exception e) {
                    //ignore
                  }
                  WritableMap params = Arguments.createMap();
                  params.putString("paired", pairedDevice.toString());
                  params.putString("found", foundDevice.toString());
                  emitRNEvent(EVENT_DEVICE_DISCOVER_DONE, params);
              }
          }
      }
  };

}
