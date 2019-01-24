package com.tcsl.myusbreadcard.devicemanager.reader;

/**
 * 描述:NFC读卡回调
 * <p/>作者：wyh
 * <br/>创建时间: 2017/5/15 15:08
 */

public interface NfcListener {

    /**
     * 读卡成功
     *
     * @param result NFC读卡结果
     */
    void onNfcResult(NfcResult result);

}
