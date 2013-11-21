package com.bosicc.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bosicc.nfc.utils.NdefRecordCreator;

import java.util.Locale;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DemoForegroundDispatchActivity extends Activity {

    private static final String TAG = "DemoForegroundDispatchActivity";

    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    private NfcAdapter mNfcAdapter;
    private TextView infoText;
    private ProgressBar loader;


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextView for tag information display
        infoText = (TextView) findViewById(R.id.textInfo);

        loader = (ProgressBar) findViewById(R.id.progressBar);
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            showMessage(R.string.nfc_not_available);
        } else {
            showMessage(R.string.nfc_available);
        }
        showMessage("<<< DemoForegroundDispatch >>>");


        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DemoForegroundDispatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[] { NdefRecordCreator.createTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true) });
        mNfcAdapter.setNdefPushMessage(mNdefPushMessage, DemoForegroundDispatchActivity.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() ...");


//        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        try {
//            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
//                                       You should specify only the ones that you need. */
//        }
//        catch (IntentFilter.MalformedMimeTypeException e) {
//            throw new RuntimeException("fail", e);
//        }
//        intentFiltersArray = new IntentFilter[] {ndef, };

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        showLoading(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent() [getAction=" + intent.getAction() + ",type=" + intent.getType() + "]");
        setIntent(intent);
    }

    private void showLoading(boolean isShow) {
        loader.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    private void showMessage(String text) {
        infoText.setText(text);
    }

    private void showMessage(int stringResourceId) {
        infoText.setText(stringResourceId);
    }
}