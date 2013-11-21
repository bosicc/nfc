package com.bosicc.nfc.utils;

import android.nfc.NdefRecord;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by bosicc on 11/21/13.
 */
public class NdefRecordCreator {

    /**
     * Create a TNF_ABSOLUTE_URI NDEF record
     *
     * Note: Google recommend that you use the RTD_URI type instead of TNF_ABSOLUTE_URI,
     * because it is more efficient.
     *
     * @param url
     * @return
     */
    public static NdefRecord getAbsoluteURI(String url) {
        NdefRecord uriRecord = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI ,
                url.getBytes(Charset.forName("US-ASCII")),
                new byte[0], new byte[0]);
        return uriRecord;
    }


    /**
     * Create a TNF_MIME_MEDIA NDEF record
     * @param mimeType
     * @param mimeData
     * @return
     */
    public static NdefRecord getAbsoluteURI(String mimeType, String mimeData) {
        NdefRecord mimeRecord = NdefRecord.createMime(mimeType,
                mimeData.getBytes(Charset.forName("US-ASCII")));
        return mimeRecord;
    }

    /**
     * Create a TNF_WELL_KNOWN NDEF record
     * @param payload
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    public static NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    //TODO: TNF_WELL_KNOWN with RTD_URI
    //TODO: TNF_EXTERNAL_TYPE
    //TODO: TNF_WELL_KNOWN with RTD_URI
    //TODO: TNF_WELL_KNOWN with RTD_URI
}
