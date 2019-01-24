package com.tcsl.myusbreadcard.devicemanager.reader;


/**
 * 描述:NFC读卡结果
 * <p/>作者：wyh
 * <br/>创建时间: 2017/5/16 9:26
 */
public class NfcResult {

    /**
     * 读卡成功
     */
    public static final int CODE_READ_SUCCESS = 0;

    /**
     * 读卡失败
     */
    public static final int CODE_READ_FAILED = -1;

    /**
     * 找不到读卡器
     */
    public static final int CODE_NO_READER = -2;


    /**
     * 认证失败
     */
    public static final int CODE_AUTH_ERROR = -3;

    /**
     * 读卡超时
     */
    public static final int CODE_TIME_OUT = -4;

    /**
     * 不支持nfc
     */
    public static final int CODE_NOT_SUPPORT = -5;

    /**
     * 打开读卡器失败
     */
    public static final int CODE_OPEN_FAILED = -6;

    /**
     * 卡类型不正确
     */
    public static final int CODE_WRONG_TYPE = -7;

    /**
     * 磁条卡服务被占用
     */
    public static final int CODE_SERVICE_OOCCUPY = -8;


    /**
     * 是否成功
     */
    public boolean success;

    /**
     * 状态码
     */
    public int code;

    /**
     * 状态信息
     */
    public String msg;

    /**
     * 读卡成功时有卡号
     */
    public String cardNo;

    private NfcResult(int code, String msg, String cardNo) {
        if (code < 0) {
            this.success = false;
        } else {
            this.success = true;
        }
        this.code = code;
        this.msg = msg;
        this.cardNo = cardNo;
    }

    private static NfcResult getResult(int code, String cardNo) {
        switch (code) {
            case CODE_READ_SUCCESS:
                return new NfcResult(CODE_READ_SUCCESS, "读卡成功", cardNo);
            case CODE_READ_FAILED:
                return new NfcResult(CODE_READ_FAILED, "读卡失败", null);
            case CODE_NO_READER:
                return new NfcResult(CODE_NO_READER, "找不到读卡器", null);
            case CODE_AUTH_ERROR:
                return new NfcResult(CODE_AUTH_ERROR, "认证失败", null);
            case CODE_TIME_OUT:
                return new NfcResult(CODE_TIME_OUT, "读卡超时", null);
            case CODE_NOT_SUPPORT:
                return new NfcResult(CODE_NOT_SUPPORT, "不支持读卡", null);
            case CODE_OPEN_FAILED:
                return new NfcResult(CODE_OPEN_FAILED, "打开读卡器失败", null);
            case CODE_WRONG_TYPE:
                return new NfcResult(CODE_WRONG_TYPE, "卡类型不正确", null);
            case CODE_SERVICE_OOCCUPY:
                return new NfcResult(CODE_WRONG_TYPE, "NFC服务被占用", null);
            default:
                throw new IllegalArgumentException("未知读卡结果");
        }
    }

    public static final NfcResult getErrorResult(int code) {
        return getResult(code, null);
    }

    public static final NfcResult getSuccessResult(String cardNo) {
        return getResult(CODE_READ_SUCCESS, cardNo);
    }

    @Override
    public String toString() {
        return "MagResult{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", cardNo='" + cardNo + '\'' +
                '}';
    }
}
