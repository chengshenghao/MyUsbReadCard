package com.tcsl.myusbreadcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tcsl.myusbreadcard.devicemanager.reader.NfcListener;
import com.tcsl.myusbreadcard.devicemanager.reader.NfcReader;
import com.tcsl.myusbreadcard.devicemanager.reader.NfcResult;
import com.tcsl.myusbreadcard.devicemanager.reader.UsbNfcCardReader;

public class MainActivity extends AppCompatActivity implements NfcListener {
    private NfcReader mNfcReader;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = (TextView) findViewById(R.id.tv_result);
    }

    /**
     * 读卡
     *
     * @param view
     */
    public void readCard(View view) {
        //读卡
        mNfcReader = new UsbNfcCardReader(this);
        if (mNfcReader != null) {
            mNfcReader.setNfcListener(this);
        } else {
            Toast.makeText(this, NfcResult.getErrorResult(NfcResult.CODE_NO_READER).msg, Toast.LENGTH_SHORT).show();
        }
        mNfcReader.start(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mNfcReader.onNewIntent(intent);
    }

    @Override
    public void onNfcResult(NfcResult result) {
        tvResult.setText("onNfcResult: " + result);
//        Log.i("csh", "onNfcResult: " + result);
    }
}
