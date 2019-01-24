package com.tcsl.myusbreadcard.devicemanager.usb;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Set;

import static com.tcsl.myusbreadcard.devicemanager.usb.UsbConfigConstants.ACTION_TL21_USB_LABLE_PRINTER_PERMISSION;


/**
 * 描述:USB设备插入授权receiver
 * <p/>作者：wyh
 * <br/>创建时间: 18-1-9 下午4:44
 */

public class UsbEventReceiver extends BroadcastReceiver {

    private Context mContext;

    private UsbManager mUsbManager;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        if (intent.getAction() == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            if (mUsbManager == null) {
                mUsbManager = (UsbManager) context.getSystemService(Service.USB_SERVICE);
            }
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Set<String> keys = deviceList.keySet();
            for (String k : keys) {
                UsbDevice device = deviceList.get(k);
                if (checkUsbNfcReader(device)) {
                    requestUsbPermission(device);
                }
//                if (checkLabelPrinter(device)) {
//                    requestUsbPermission(device);
//                }
            }
        }
    }

//    private boolean checkLabelPrinter(UsbDevice device) {
//        return USBPort.isUsbPrinter(device);
//    }

    private void requestUsbPermission(UsbDevice device) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_TL21_USB_LABLE_PRINTER_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_TL21_USB_LABLE_PRINTER_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        }, filter);
        mUsbManager.requestPermission(device, pendingIntent);
    }

    private boolean checkUsbNfcReader(UsbDevice device) {
        if (UsbConfigConstants.USB_CARD_READER_NAME.equals(device.getProductName())) {
            return true;
        }
        return false;
    }

}
