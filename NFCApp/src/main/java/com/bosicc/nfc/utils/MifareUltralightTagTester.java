package com.bosicc.nfc.utils;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *
 */
public class MifareUltralightTagTester {

    private static int blockOffset = 4;
    private static int blockNumber = 16;
    private static int blockSize = 4;


    private static final String TAG = MifareUltralightTagTester.class.getSimpleName();

    /**
     * Write text to NFC Tag! Text size should be less than size of NFC tag memory! 46 bytes
     * @param tag
     * @param tagText
     * @return
     */
    public static boolean writeTag(Tag tag, String tagText) {
        boolean result = true;
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (tagText.length() < (blockNumber-blockOffset)*blockSize) {
            ArrayList<String> list = new ArrayList<String>();
            int start = 0;
            int end = blockSize;
            while (end < tagText.length()) {
                String text = tagText.substring(start, end);
                Log.d(TAG," - [" + text + "]" );
                list.add(text); 
                start +=blockSize;
                end += blockSize;
            }
            try {
                ultralight.connect();

                int page = blockOffset;
                for (String text:list) {
                    Log.d(TAG,"page=" + page + " [" + text + "]" );
                    ultralight.writePage(page, text.getBytes(Charset.forName("US-ASCII")));
                    page++;
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
                result = false;
            } finally {
                try {
                    ultralight.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing MifareUltralight...", e);
                    result = false;
                }
            }
        } else {
            Log.e(TAG, "Text size bigger than MilfareUltralight size (46 byte)");
            result = false;
        }
        return result;
    }

    public static String readTag(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            byte[] payload1 = mifare.readPages(4);
            String part1 = new String(payload1, Charset.forName("US-ASCII"));
            byte[] payload2 = mifare.readPages(8);
            String part2 = new String(payload2, Charset.forName("US-ASCII"));
            return part1 + part2;
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight message...", e);
        } finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }
}
