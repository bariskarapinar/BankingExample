package com.myapp.emvposexample.iso8583;

/**
 * Educational Protocol Engine: ISO 8583 Message Type Identifier (MTI) Decoder.
 *
 * <p>In ISO 8583 payment protocol messages, the <b>MTI</b> is a 4-digit numeric header
 * positioned at the start of every message payload. Each digit conveys specific protocol information
 * regarding ISO version standards, message functional classes, request/response directions, and network origin.</p>
 *
 * <h3>ISO 8583 MTI Digit Structure:</h3>
 * <ul>
 *   <li><b>Digit 1 (ISO Version Standard):</b>
 *     <code>0</code> = ISO 8583-1:1987, <code>1</code> = ISO 8583-2:1993, <code>2</code> = ISO 8583-3:2003.
 *   </li>
 *   <li><b>Digit 2 (Message Class):</b>
 *     <code>1</code> = Authorization, <code>2</code> = Financial Transaction, <code>4</code> = Reversal, <code>8</code> = Network Management.
 *   </li>
 *   <li><b>Digit 3 (Message Function):</b>
 *     <code>0</code> = Request, <code>1</code> = Response, <code>2</code> = Advice, <code>3</code> = Advice Response, <code>4</code> = Notification.
 *   </li>
 *   <li><b>Digit 4 (Message Origin):</b>
 *     <code>0</code> = Acquirer, <code>1</code> = Acquirer Repeat, <code>2</code> = Issuer, <code>3</code> = Issuer Repeat.
 *   </li>
 * </ul>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoMtiDecoder {

    /**
     * Data Transfer Object representing parsed MTI breakdown fields.
     */
    public static class MtiInfo {
        public String mti;
        public String isoVersion;
        public String messageClass;
        public String messageFunction;
        public String messageOrigin;

        /**
         * Returns a full, formatted English description of the MTI classification.
         *
         * @return Formatted MTI summary (e.g. "0200 - Financial Transaction Message Request (Acquirer, ISO 8583-1:1987)").
         */
        public String getFullDescriptionEn() {
            return mti + " - " + messageClass + " " + messageFunction + " (" + messageOrigin + ", " + isoVersion + ")";
        }

        public String getFullDescriptionTr() {
            return getFullDescriptionEn();
        }
    }

    /**
     * Decodes a 4-digit MTI string into structured classification fields.
     *
     * @param mti 4-digit numeric string (e.g. "0100", "0200", "0210", "0420", "0800").
     * @return Formatted {@link MtiInfo} containing decoded classification attributes.
     */
    public static MtiInfo decode(String mti) {
        MtiInfo info = new MtiInfo();
        info.mti = mti != null ? mti.trim() : "0000";
        if (info.mti.length() < 4) {
            info.mti = String.format("%-4s", info.mti).replace(' ', '0');
        }

        char d1 = info.mti.charAt(0); // Digit 1: ISO Version
        char d2 = info.mti.charAt(1); // Digit 2: Message Class
        char d3 = info.mti.charAt(2); // Digit 3: Message Function
        char d4 = info.mti.charAt(3); // Digit 4: Message Origin

        // Decode Digit 1: ISO Standard Version
        switch (d1) {
            case '0': info.isoVersion = "ISO 8583-1:1987"; break;
            case '1': info.isoVersion = "ISO 8583-2:1993"; break;
            case '2': info.isoVersion = "ISO 8583-3:2003"; break;
            default: info.isoVersion = "Custom Version (" + d1 + ")"; break;
        }

        // Decode Digit 2: Functional Message Class
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

        // Decode Digit 3: Directional Message Function
        switch (d3) {
            case '0': info.messageFunction = "Request"; break;
            case '1': info.messageFunction = "Response"; break;
            case '2': info.messageFunction = "Advice"; break;
            case '3': info.messageFunction = "Advice Response"; break;
            case '4': info.messageFunction = "Notification"; break;
            default: info.messageFunction = "Function (" + d3 + ")"; break;
        }

        // Decode Digit 4: Network Entity Origin
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
