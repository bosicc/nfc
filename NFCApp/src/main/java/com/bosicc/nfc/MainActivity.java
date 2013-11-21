package com.bosicc.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
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

/**
 * Android-powered devices with NFC simultaneously support three main modes of operation:
 <p></p>
 1. Reader/writer mode, allowing the NFC device to read and/or write passive NFC tags and stickers.
 <p></p>
 2. P2P mode, allowing the NFC device to exchange data with other NFC peers; this operation mode is used by Android
 Beam.
 <p></p>
 3. Card emulation mode, allowing the NFC device itself to act as an NFC card. The emulated NFC card can then be
 accessed by an external NFC reader, such as an NFC point-of-sale terminal.

 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    private NfcAdapter mNfcAdapter;
    private TextView infoText;
    private ProgressBar loader;

    private boolean isReading = false;
    private boolean isWriting = false;


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
        findViewById(R.id.btnBeam).setOnClickListener(this);
        findViewById(R.id.btnRead).setOnClickListener(this);
        findViewById(R.id.btnWrite).setOnClickListener(this);
        findViewById(R.id.btnClean).setOnClickListener(this);
        findViewById(R.id.btnDemo).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() ...");
        showLoading(false);
        Intent intent = getIntent();
        /*Parse intent data*/
        resolveIntent(intent);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent() [getAction=" + intent.getAction() + ",type=" + intent.getType() + "]");
        if (intent.getAction().equals("android.intent.action.MAIN")) {
            showMessage(R.string.nfc_read);
            showLoading(true);
            Log.i(TAG, "onNewIntent() [getScheme=" + intent.getScheme() + ",getPackage=" + intent.getPackage() + "]");
        }
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // If NFC is not available, we won't be needing this menu
        if (mNfcAdapter == null) {
            return super.onCreateOptionsMenu(menu);
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                String text = "";
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    text += new String(msgs[i].getRecords()[0].getPayload());
                    Log.i(TAG, "resolveIntent() [msgs("+i+")=" + msgs[i] + "]");
                }
                showMessage(text);
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable ndef = intent.getParcelableExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                Log.i(TAG, "onNewIntent() [ndef=" + ndef + "]");
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                String info = dumpTagData(tag);
                Log.i(TAG, "onNewIntent() [id=" + getHex(id) + "]");
                Log.d(TAG, "onNewIntent() [info=" + info + "]");
                showMessage(info);
            }
            showLoading(false);
        }
    }


    /**
     * Parse incoming byte stream from NFC tag
     * @param p
     * @return
     */
    private String dumpTagData(Parcelable p) {
        //TagData data = new TagData();
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("Tag ID (reversed): ").append(getReversed(id)).append("\n");

        String prefix = "android.nfc.tech.";
        sb.append("\nTechnologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n");
        for (String tech : tag.getTechList()) {
            // ---- MifareClassic ----
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(NfcA.class.getName())) {
                sb.append('\n');
                NfcA nfcATag = NfcA.get(tag);

                sb.append(" * NfcA MaxTransceiveLength: ");
                sb.append(nfcATag.getMaxTransceiveLength());
                sb.append('\n');
            }

            // ---- MifareUltralight ----
            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append(" * Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBeam:
                startActivity(new Intent(this, BeamActivity.class));
                break;
            case R.id.btnDemo:
                startActivity(new Intent(this, DemoForegroundDispatchActivity.class));
                break;
            case R.id.btnClean:
                break;
            case R.id.btnRead:
                showMessage(R.string.nfc_read);
                showLoading(true);
                isReading = true;
                break;
            case R.id.btnWrite:
                showMessage(R.string.nfc_write);
                showLoading(true);
                isWriting = true;
                break;
        }
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