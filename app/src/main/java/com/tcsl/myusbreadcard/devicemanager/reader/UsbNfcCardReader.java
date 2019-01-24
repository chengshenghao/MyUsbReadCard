package com.tcsl.myusbreadcard.devicemanager.reader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import com.mw.reader.MifareCard;
import com.mw.reader.Reader;
import com.mw.reader.ReaderException;
import com.tcsl.myusbreadcard.devicemanager.usb.UsbConfigConstants;


/**
 * 描述:USB接口NFC读卡器（明华，明泰，非root模式）
 * <p/>作者：wyh
 * <br/>创建时间: 2017/5/6 13:57
 */

public class UsbNfcCardReader extends NfcReader {


    /**
     * 读卡周期
     */
    private static final long TIME_INTERVAL = 200L;

    private Reader mReader;
    private MifareCard mNfcCard;

    //暂停读卡
    private boolean mPause = false;

    private UsbManager mUsbManager;

    private UsbAuthResultReceiver mReceiver;

    /**
     * 读卡器对应的usb接口
     */
    private UsbDevice mUsbDevice;

    /**
     * USB授权是否获取到
     */
    private boolean mUsbAuth;

    public UsbNfcCardReader(Context context) {
        super(context);
        initReader();
    }

    @Override
    protected void initReaderInternal() {
        mAvailable = true;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        initPermissionReceiver();
        requestPermission();
    }

    /**
     * 初始化权限监听器
     */
    private void initPermissionReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbConfigConstants.ACTION_NFC_READER_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mReceiver = new UsbAuthResultReceiver();
        this.mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    public void start(Activity activity) {
        mActivity = activity;
        if (!mAvailable) {
            setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_NOT_SUPPORT));
            return;
        }
        if (mReader != null) {
            mReader.open(mUsbDevice);
        }
        doRead();
    }

    @Override
    public void stop() {
        pause();
        mReader.close();
    }

    @Override
    protected void doRead() {
        mPause = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //关闭读卡器
                    if (mPause) {
                        return;
                    }
                    synchronized (UsbNfcCardReader.class) {
                        //没有授权不执行读卡
                        if (!mUsbAuth) {
                            continue;
                        }
                        // 每轮循环延迟100毫秒
                        SystemClock.sleep(TIME_INTERVAL);
                        // 尝试打开卡片，用以检测是否有卡在读卡器上
                        try {
                            mNfcCard.openCard(MifareCard.OpenMode.STD);
                        } catch (ReaderException e) {
                            continue;
                        } catch (NullPointerException e) {
                            // FIXME: 2017/7/3 后续查看为何县城同步没有搞定!!!
                            return;
                        }
                        // 检验卡片
                        try {
                            mNfcCard.authenticate(MifareCard.KeyType.KEY_B, mBlock, mKey);
                        } catch (ReaderException e) {
                            e.printStackTrace();
                            setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_AUTH_ERROR));
                            return;
                        }
                        // 读取数据
                        byte[] data;
                        try {
                            data = mNfcCard.read(mBlock);
                            setNfcResult(NfcResult.getSuccessResult(bytesToHexString(data)));
                        } catch (ReaderException e) {
                            setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_READ_FAILED));
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onNewIntent(Intent intent) {
        //empty
    }

    @Override
    public void pause() {
        mPause = true;
    }


    @Override
    protected void onDestroy() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (mReader != null) {
            mReader.close();
        }
        mReader = null;
        mNfcCard = null;
        mUsbManager = null;
        mReceiver = null;
    }

    /**
     * 请求权限
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void requestPermission() {
        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            if (UsbConfigConstants.USB_CARD_READER_NAME.equals(device.getProductName())) {
                if (!mUsbManager.hasPermission(device)) {
                    mUsbAuth = false;
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(UsbConfigConstants.ACTION_NFC_READER_PERMISSION), 0);
                    mUsbManager.requestPermission(device, pendingIntent);
                    return;
                } else {
                    // 设备已打开过，重新初始化一下usb接口
                    mUsbAuth = true;
                    initDevice(device);
                    return;
                }
            } else {
                continue;
            }
        }
        setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_NO_READER));
    }

    /**
     * 初始化usb接口
     */
    public void initDevice(UsbDevice device) {
        mUsbDevice = device;
        mReader = new Reader(mUsbManager);
        if (mReader.open(device)) {
            // 打开设备成功
            mUsbAuth = true;
            mPause = false;
            mNfcCard = new MifareCard(mReader);
        } else {
            mUsbAuth = false;
            setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_OPEN_FAILED));
        }
    }

    /**
     * 外接读卡器授权receiver
     */
    public class UsbAuthResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbConfigConstants.ACTION_NFC_READER_PERMISSION)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mUsbAuth = true;
                    // 点击确定
                    if (device != null) {
                        initDevice(device);
                    }
                } else {
                    mUsbAuth = false;
                    // 点击取消
                    // 打开设备失败
                    setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_NO_READER));
                }
            }
        }
    }


    /**
     * 功能：读M卡 将字符序列转换为16进制字符串 并取前16为
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString().substring(0, 16);
    }

}
