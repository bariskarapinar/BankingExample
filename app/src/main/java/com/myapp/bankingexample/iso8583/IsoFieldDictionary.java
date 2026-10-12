package com.myapp.bankingexample.iso8583;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IsoFieldDictionary {

    private static final Map<Integer, IsoFieldSpec> SPECS = new HashMap<>();

    static {
        addField(1, "Secondary Bitmap", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "İkincil Bitmap (Saha 65-128 aktiflik durumu)");
        addField(2, "Primary Account Number (PAN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.LLVAR, 19, "Kart Numarası (PAN - LLVAR)");
        addField(3, "Processing Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "İşlem Kodu (Örn: 000000 Satış, 010000 Nakit, 200000 İade)");
        addField(4, "Amount, Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 12, "İşlem Tutarı (En küçük birim/kuruş cinsinden 12 hane)");
        addField(7, "Transmission Date & Time", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 10, "İletim Tarihi ve Saati (MMDDhhmmss)");
        addField(11, "Systems Trace Audit Number (STAN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Sistem İzleme Denetim Numarası (STAN - 6 hane)");
        addField(12, "Time, Local Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Yerel İşlem Saati (hhmmss)");
        addField(13, "Date, Local Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Yerel İşlem Tarihi (MMDD)");
        addField(14, "Date, Expiration", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Kart Son Kullanma Tarihi (YYMM)");
        addField(18, "Merchant Type / MCC", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Üye İşyeri Sektör Kodu (Merchant Category Code - MCC)");
        addField(22, "Point of Service Entry Mode", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "POS Giriş Modu (Örn: 021 Manyetik, 051 Çip/EMV, 071 Temassız)");
        addField(23, "Card Sequence Number (PSN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "Kart Sıra Numarası (PSN)");
        addField(25, "POS Condition Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 2, "POS Durum Kodu (Örn: 00 Normal Müşteri, 08 E-Ticaret)");
        addField(32, "Acquiring Institution ID", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.LLVAR, 11, "Anlaşmalı Banka (Acquirer) Kurum Kodu");
        addField(35, "Track 2 Data", IsoFieldSpec.DataType.TRACK2, IsoFieldSpec.LengthType.LLVAR, 37, "Manyetik İz 2 Verisi (Track 2 Data)");
        addField(37, "Retrieval Reference Number (RRN)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 12, "Referans Numarası (RRN - 12 hane)");
        addField(38, "Authorization Identification Response", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Ototrizasyon Onay Kodu (Auth Code - 6 hane)");
        addField(39, "Response Code", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 2, "Yanıt Kodu (Örn: 00 Onaylandı, 51 Yetersiz Bakiye, 54 Süresi Dolmuş Kart)");
        addField(41, "Card Acceptor Terminal ID (TID)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 8, "POS Terminal Numarası (TID - 8 hane)");
        addField(42, "Card Acceptor ID Code (MID)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 15, "Üye İşyeri Numarası (MID - 15 hane)");
        addField(43, "Card Acceptor Name/Location", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 40, "Üye İşyeri Adı ve Şehir/Ülke Bilgisi");
        addField(48, "Private Additional Data", IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Özel Ek Veri Sahası (Private Additional Data - LLLVAR)");
        addField(52, "Personal Identification Number Data", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Sifreli PIN Bloğu (ISO 9564-1 Format 0/1/3 - 8 Bayt Hex)");
        addField(54, "Additional Amounts", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.LLLVAR, 120, "Ek Tutarlar (Bakiye Bilgisi, Puan vb.)");
        addField(55, "Integrated Circuit Card (ICC) Data", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.LLLVAR, 255, "Çip / EMV Veri Sahası (BER-TLV Formatında Tag 9F26, 95, 9F37 vb.)");
        addField(62, "Private Reserved / Transaction Identifier", IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Özel Rezerve Saha / İşlem ID");
        addField(64, "Message Authentication Code (MAC)", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Mesaj Doğrulama Kodu (MAC - 8 Bayt)");
        addField(70, "Network Management Information Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "Ağ Yönetim Bilgi Kodu (Örn: 001 Echo Test, 101 Logon)");
        addField(128, "Secondary Message Authentication Code", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "İkincil MAC Kodu");
    }

    private static void addField(int num, String name, IsoFieldSpec.DataType type, IsoFieldSpec.LengthType lenType, int maxLen, String descTr) {
        SPECS.put(num, new IsoFieldSpec(num, name, type, lenType, maxLen, descTr));
    }

    public static IsoFieldSpec getFieldSpec(int num) {
        if (SPECS.containsKey(num)) {
            return SPECS.get(num);
        }
        return new IsoFieldSpec(num, "Field " + num, IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Saha " + num);
    }

    public static Collection<IsoFieldSpec> getAllSpecs() {
        return SPECS.values();
    }
}
