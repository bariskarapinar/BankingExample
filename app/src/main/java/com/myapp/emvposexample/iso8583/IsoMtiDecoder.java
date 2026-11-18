package com.myapp.emvposexample.iso8583;

public class IsoMtiDecoder {

    public static class MtiInfo {
        public String mti;
        public String isoVersion;
        public String messageClass;
        public String messageFunction;
        public String messageOrigin;

        public String getFullDescriptionEn() {
            return mti + " - " + messageClass + " " + messageFunction + " (" + messageOrigin + ", " + isoVersion + ")";
        }

        public String getFullDescriptionTr() {
            return getFullDescriptionEn();
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

        switch (d1) {
            case '0': info.isoVersion = "ISO 8583-1:1987"; break;
            case '1': info.isoVersion = "ISO 8583-2:1993"; break;
            case '2': info.isoVersion = "ISO 8583-3:2003"; break;
            default: info.isoVersion = "Custom Version (" + d1 + ")"; break;
        }

        switch (d2) {
            case '1': info.messageClass = "Authorization Message"; break;
            case '2': info.messageClass = "Financial Transaction Message"; break;
            case '3': info.messageClass = "File Action Message"; break;
            case '4': info.messageClass = "Reversal / Chargeback Message"; break;
            case '5': info.messageClass = "Reconciliation / Settlement Message"; break;
            case '6': info.messageClass = "Administrative Message"; break;
            case '8': info.messageClass = "Network Management Message"; break;
            default: info.messageClass = "Other Class (" + d2 + ")"; break;
        }

        switch (d3) {
            case '0': info.messageFunction = "Request"; break;
            case '1': info.messageFunction = "Response"; break;
            case '2': info.messageFunction = "Advice"; break;
            case '3': info.messageFunction = "Advice Response"; break;
            case '4': info.messageFunction = "Notification"; break;
            default: info.messageFunction = "Function (" + d3 + ")"; break;
        }

        switch (d4) {
            case '0': info.messageOrigin = "Acquirer"; break;
            case '1': info.messageOrigin = "Acquirer Repeat"; break;
            case '2': info.messageOrigin = "Issuer"; break;
            case '3': info.messageOrigin = "Issuer Repeat"; break;
            case '4': info.messageOrigin = "Other Gateway"; break;
            default: info.messageOrigin = "Origin (" + d4 + ")"; break;
        }

        return info;
    }
}
