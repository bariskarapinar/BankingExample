package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.HashMap;
import java.util.Map;

public class ApduResponse {

    private static final Map<String, String> SW_DICTIONARY = new HashMap<>();

    static {
        SW_DICTIONARY.put("9000", "İşlem Başarılı (Process completed successfully)");
        SW_DICTIONARY.put("61", "Tamamlandı, okunabilir daha fazla veri var (Response bytes available)");
        SW_DICTIONARY.put("6281", "Dönen veride uyarı var (Part of returned data may be corrupted)");
        SW_DICTIONARY.put("63C2", "PIN Doğrulama Başarısız (PIN verification failed, 2 tries remaining)");
        SW_DICTIONARY.put("63C1", "PIN Doğrulama Başarısız (PIN verification failed, 1 try remaining)");
        SW_DICTIONARY.put("63C0", "PIN Deneme Limiti Doldu / Kart Bloke Olabilir (PIN try limit exceeded)");
        SW_DICTIONARY.put("6700", "Yanlış Uzunluk (Wrong length parameter Lc/Le)");
        SW_DICTIONARY.put("6881", "Güvenli İletişim Desteklenmiyor (Logical channel not supported)");
        SW_DICTIONARY.put("6982", "Güvenlik Şartları Sağlanmadı (Security status not satisfied)");
        SW_DICTIONARY.put("6983", "Doğrulama Yöntemi Bloke Oldu (Authentication method blocked)");
        SW_DICTIONARY.put("6984", "Referans Verisi Kullanılamaz (Referenced data invalidated)");
        SW_DICTIONARY.put("6985", "Kullanım Koşulları Sağlanmadı (Conditions of use not satisfied)");
        SW_DICTIONARY.put("6A81", "İşlev Desteklenmiyor (Function not supported)");
        SW_DICTIONARY.put("6A82", "Dosya veya Uygulama Bulunamadı (File / Application not found - SW1 SW2: 6A82)");
        SW_DICTIONARY.put("6A83", "Kayıt Bulunamadı (Record not found)");
        SW_DICTIONARY.put("6A86", "Yanlış P1 P2 Parametreleri (Incorrect parameters P1 P2)");
        SW_DICTIONARY.put("6A88", "Referans Verisi Bulunamadı (Referenced data not found)");
        SW_DICTIONARY.put("6D00", "Geçersiz Komut INS (Instruction code not supported)");
        SW_DICTIONARY.put("6E00", "Geçersiz Sınıf CLA (Class not supported)");
    }

    private final byte[] dataBytes;
    private final byte sw1;
    private final byte sw2;

    public ApduResponse(byte[] dataBytes, byte sw1, byte sw2) {
        this.dataBytes = dataBytes != null ? dataBytes : new byte[0];
        this.sw1 = sw1;
        this.sw2 = sw2;
    }

    public static ApduResponse fromHex(String hexWithSw) {
        byte[] bytes = HexUtils.hexToBytes(hexWithSw);
        if (bytes.length < 2) {
            return new ApduResponse(new byte[0], (byte) 0x6F, (byte) 0x00);
        }
        int dataLen = bytes.length - 2;
        byte[] data = new byte[dataLen];
        System.arraycopy(bytes, 0, data, 0, dataLen);
        return new ApduResponse(data, bytes[bytes.length - 2], bytes[bytes.length - 1]);
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public String getDataHex() {
        return HexUtils.bytesToHex(dataBytes);
    }

    public byte getSw1() {
        return sw1;
    }

    public byte getSw2() {
        return sw2;
    }

    public String getSwHex() {
        return String.format("%02X%02X", sw1 & 0xFF, sw2 & 0xFF);
    }

    public boolean isSuccess() {
        return (sw1 & 0xFF) == 0x90 && (sw2 & 0xFF) == 0x00;
    }

    public String getStatusDescription() {
        String swHex = getSwHex();
        if (SW_DICTIONARY.containsKey(swHex)) {
            return SW_DICTIONARY.get(swHex);
        }
        String sw1Prefix = String.format("%02X", sw1 & 0xFF);
        if (SW_DICTIONARY.containsKey(sw1Prefix)) {
            return SW_DICTIONARY.get(sw1Prefix);
        }
        return "Durum Kodu: 0x" + swHex;
    }
}
