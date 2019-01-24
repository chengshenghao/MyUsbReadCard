package com.tcsl.myusbreadcard.devicemanager.reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 描述:NFC读卡器基类
 * <p/>作者：wyh
 * <br/>创建时间: 2017/5/17 14:45
 */
public abstract class NfcReader {

    /**
     * 默认密钥
     */
    protected static final byte[] KEY_DEFAULT = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * 默认块
     */
    protected static final int BLOCK_DEFAULT = 2;

    /**
     * 默认扇区
     */
    protected static final int SECTION_DEFAULT = 0;


    /**
     * 默认卡号位数
     */
    protected static final int CARD_BITS_DEFAULT = 16;

    /**
     * 超时时间
     */
    protected static final int TIME_OUT_DEFAULT = 1000000;

    protected Context mContext;

    protected Activity mActivity;

    /**
     * 密钥
     */
    protected byte[] mKey;

    /**
     * 块
     */
    protected int mBlock;

    /**
     * 扇区
     */
    protected int mSection;

    /**
     * 卡号位数
     */
    protected int mCardBits;

    /**
     * 是否完成初始化
     */
    public boolean mInited;

    /**
     * 是否可用
     */
    protected boolean mAvailable;

    /**
     * 超时时间
     */
    protected int mTimeOut;

    /**
     * 是否暂停读卡
     */
    protected boolean mPaused;

    /**
     * NFC读卡回调
     */
    protected NfcListener mNfcListener;

    protected NfcOnsubscribe mNfcOnsubscribe;

    protected Observable<NfcResult> mNfcObservable;


    public NfcReader(Context context) {
        mContext = context.getApplicationContext();
        mInited = true;
        initNfcCallback();
        initParams();
    }

    /**
     * 初始化读卡器资源
     */
    protected void initReader() {
        initReaderInternal();
    }

    /**
     * 初始化读卡器内部资源
     */
    protected abstract void initReaderInternal();


    /**
     * 初始化NFC读卡回调
     */
    protected void initNfcCallback() {
        mNfcOnsubscribe = new NfcOnsubscribe();
        mNfcObservable = Observable.create(mNfcOnsubscribe).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 初始化读卡参数（区块，卡号位数）
     */
    protected void initParams() {
        mKey = KEY_DEFAULT;
        mBlock = BLOCK_DEFAULT;
        mSection = SECTION_DEFAULT;
        mCardBits = CARD_BITS_DEFAULT;
        mTimeOut = TIME_OUT_DEFAULT;
    }


    protected void setNfcResult(NfcResult result) {
        mNfcOnsubscribe.setNfcResult(result);
        mNfcObservable.subscribe(new NfcObserver());
    }

    /**
     * 开始读卡
     */
    public void start(Activity activity) {
        mActivity = activity;
        if (!mAvailable) {
            setNfcResult(NfcResult.getErrorResult(NfcResult.CODE_NOT_SUPPORT));
            return;
        }
        doRead();
    }

    /**
     * 停止读卡
     */
    public void stop() {
        pause();
    }

    /**
     * 开始读卡
     */
    protected abstract void doRead();

    /**
     * 旺POS需要调用此方法，其他POS此方法为空
     *
     * @param intent
     */
    public abstract void onNewIntent(Intent intent);

    /**
     * 暂停读卡
     */
    public abstract void pause();

    /**
     * 继续读卡
     */
    public void resume() {
        doRead();
    }

    /**
     * 销毁manager
     */
    public void destroy() {
        mNfcObservable.unsubscribeOn(Schedulers.io());
        mInited = false;
        mAvailable = false;
        onDestroy();
    }

    /**
     * 销毁reader
     */
    protected abstract void onDestroy();

    protected class NfcOnsubscribe implements ObservableOnSubscribe<NfcResult> {

        private NfcResult mResult;

        public void setNfcResult(NfcResult result) {
            mResult = result;
        }


        @Override
        public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<NfcResult> e) throws Exception {
            e.onNext(mResult);
            e.onComplete();
        }
    }

    protected class NfcObserver implements Observer<NfcResult> {

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

        }

        @Override
        public void onNext(NfcResult result) {
            if (mNfcListener != null) {
                mNfcListener.onNfcResult(result);
            }
        }
    }

    public void setNfcListener(NfcListener listener) {
        this.mNfcListener = listener;
    }

    protected String bytes2String(byte[] arryByte, int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buf.append(String.format("%02X", arryByte[i]));
        }
        return buf.toString();
    }

    public void setKey(byte[] key) {
        mKey = key;
    }

    public void setBlock(int block) {
        mBlock = block;
    }

    public void setSection(int section) {
        mSection = section;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void pauseReading() {
        mPaused = true;
    }

    public void resumeReading() {
        mPaused = false;
    }


}
