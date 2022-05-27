package com.reactnativeposprinter;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BluetoothLeService extends Service {

  private final Binder binder = new LocalBinder();

  // initialize bluetooth not support
  public boolean initialize() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    return bluetoothAdapter != null;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  class LocalBinder extends Binder {
    public BluetoothLeService getService() {
      return BluetoothLeService.this;
    }
  }


}
