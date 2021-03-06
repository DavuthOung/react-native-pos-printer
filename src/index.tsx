import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-pos-printer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const PosPrinter = NativeModules.PosPrinter
  ? NativeModules.PosPrinter
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function checkBluetoothEnable(): Promise<{}> {
  return PosPrinter.checkBluetoothEnable();
}

export function enableBluetooth(): Promise<{}> {
  return PosPrinter.enableBluetooth();
}

export function disableBluetooth(): Promise<{}> {
  return PosPrinter.disableBluetooth()
}

// export function scanDevices(): Promise<{}> {
//   return PosPrinter.scanDevices();
// }

export function connect(address: string): Promise<{}> {
  return PosPrinter.connect(address)
}

export function getDevicePaired(): Promise<{}> {
  return PosPrinter.getDevicePaired();
}

export function printPic(base64: string,options: {}): Promise<{}> {
  return PosPrinter.printPic(base64,options);
}

export function initializeBluetooth(currentDevice: string): Promise<{}> {
  return PosPrinter.initializeBluetooth(currentDevice);
}

export function printerAlign(align: number): Promise<{}> {
  return PosPrinter.printerAlign(align);
}

export function setBlob(weight: number): Promise<{}> {
  return PosPrinter.setBlob(weight);
}

export function setWidth(width: number): Promise<{}> {
  return PosPrinter.setWidth(width);
}



