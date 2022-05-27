package com.reactnativeposprinter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BluetoothLE extends Service {
  private Binder binder = new LocalBinder();
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
