package com.myapp.bankingexample.iso8583;

public class IsoMtiDecoder {

    public static class MtiInfo {
        public String mti;
        public String isoVersion;
        public String messageClass;
        public String messageFunction;
        public String messageOrigin;

        public String getFullDescriptionTr() {
            return mti + " - " + messageClass + " " + messageFunction + " (" + messageOrigin + ", " + isoVersion + ")";
        }
    }

    public static MtiInfo decode(String mti) {
        MtiInfo info = new MtiInfo();
        info.mti = mti != null ? mti.trim() : "0000";
        if (info.mti.length() < 4) {
            info.mti = String.format("%-4s", info.mti).replace(' ', '0');
        }

        char d1 = info.mti.charAt(0);
        char d2 = info.mti.charAt(1);
        char d3 = info.mti.charAt(2);
        char d4 = info.mti.charAt(3);

        // Digit 1: ISO Version
        switch (d1) {
            case '0': info.isoVersion = "ISO 8583-1:1987"; break;
            case '1': info.isoVersion = "ISO 8583-2:1993"; break;
            case '2': info.isoVersion = "ISO 8583-3:2003"; break;
            default: info.isoVersion = "Özel Sürüm (" + d1 + ")"; break;
        }

        // Digit 2: Message Class
        switch (d2) {
            case '1': info.messageClass = "Otorizasyon Mesajı (Authorization)"; break;
            case '2': info.messageClass = "Finansal Mesaj (Financial Transaction)"; break;
            case '3': info.messageClass = "Dosya Eşitleme / File Action"; break;
            case '4': info.messageClass = "Ters İşlem / İptal (Reversal / Chargeback)"; break;
            case '5': info.messageClass = "Uzlaşma Mesajı (Reconciliation / Settlement)"; break;
            case '6': info.messageClass = "İdari Mesaj (Administrative)"; break;
            case '8': info.messageClass = "Ağ Yönetim Mesajı (Network Management)"; break;
            default: info.messageClass = "Diğer Sınıf (" + d2 + ")"; break;
        }

        // Digit 3: Message Function
        switch (d3) {
            case '0': info.messageFunction = "İstek / İstek Yanıtı (Request)"; break;
            case '1': info.messageFunction = "Yanıt Mesajı (Response)"; break;
            case '2': info.messageFunction = "Bildirim / Tavsiye (Advice)"; break;
            case '3': info.messageFunction = "Tavsiye Yanıtı (Advice Response)"; break;
            case '4': info.messageFunction = "Duyuru (Notification)"; break;
            default: info.messageFunction = "İşlev (" + d3 + ")"; break;
        }

        // Digit 4: Message Origin
        switch (d4) {
            case '0': info.messageOrigin = "Acquirer (Anlaşmalı Banka)"; break;
            case '1': info.messageOrigin = "Acquirer Repeat (Tekrar)"; break;
            case '2': info.messageOrigin = "Issuer (Kart İhraç Eden Banka)"; break;
            case '3': info.messageOrigin = "Issuer Repeat (Tekrar)"; break;
            case '4': info.messageOrigin = "Diğer Arayüz / Gateway"; break;
            default: info.messageOrigin = "Kaynak (" + d4 + ")"; break;
        }

        return info;
    }
}
