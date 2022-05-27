package com.reactnativeposprinter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.List;

public class BluetoothGatt implements BluetoothProfile {
  public BluetoothGatt connectGatt(Context context, boolean autoConnect, BluetoothGattCallback callback) {
    return null;
  }

  @Override
  public List<BluetoothDevice> getConnectedDevices() {
    return null;
  }

  @Override
  public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] ints) {
    return null;
  }

  @Override
  public int getConnectionState(BluetoothDevice bluetoothDevice) {

    return BluetoothProfile.STATE_DISCONNECTED;
  }
}
