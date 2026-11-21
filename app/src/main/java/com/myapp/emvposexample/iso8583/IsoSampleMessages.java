package com.myapp.emvposexample.iso8583;

/**
 * Educational Message Factory: Pre-built Standard ISO 8583 Sample Messages.
 *
 * <p>Provides pre-configured ISO 8583 message templates for common financial transaction scenarios
 * (0100 Authorization Request, 0200 Financial Purchase Request, 0210 Response with ARPC, 0420 Reversal, and 0800 Logon).</p>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoSampleMessages {

    /**
     * Creates a standard ISO 8583 0100 Authorization Request sample message.
     */
    public static IsoMessage create0100AuthRequest() {
        IsoMessage msg = new IsoMessage("0100");
        msg.setHeader("6000000000"); // 5-byte TPDU transport network header
        msg.setField(2, "4543123456789012"); // Field 2: Primary Account Number (PAN)
        msg.setField(3, "000000"); // Field 3: Processing Code (000000 = Purchase)
        msg.setField(4, "000000025000"); // Field 4: Transaction Amount ($250.00 = 25000 cents)
        msg.setField(7, "0926123045"); // Field 7: Transmission Date & Time (MMDDhhmmss)
        msg.setField(11, "000123"); // Field 11: Systems Trace Audit Number (STAN)
        msg.setField(12, "123045"); // Field 12: Local Time (hhmmss)
        msg.setField(13, "0926"); // Field 13: Local Date (MMDD)
        msg.setField(14, "2812"); // Field 14: Expiration Date (YYMM)
        msg.setField(18, "5411"); // Field 18: Merchant Category Code (MCC)
        msg.setField(22, "021"); // Field 22: POS Entry Mode (021 = Magstripe Track 2)
        msg.setField(25, "00"); // Field 25: POS Condition Code (00 = Normal Customer Present)
        msg.setField(35, "4543123456789012=2812201000000000"); // Field 35: Track 2 Data
        msg.setField(41, "TERM0001"); // Field 41: Terminal ID (TID)
        msg.setField(42, "MERCHANT0000001"); // Field 42: Merchant ID (MID)
        msg.setField(43, "CAFE NEW YORK US            NEW YORK  US"); // Field 43: Merchant Location
        msg.setField(48, "POS PROMOTION CODE=DISCOUNT5"); // Field 48: Private Additional Data
        msg.setField(52, "1234567890ABCDEF"); // Field 52: Encrypted PIN Block (ISO-0)
        return msg;
    }

    /**
     * Creates an ISO 8583 0200 Financial Request sample message carrying Field 55 EMV ICC payload.
     *
     * @param field55EmvHex Optional custom BER-TLV chip hex string.
     */
    public static IsoMessage create0200FinancialRequest(String field55EmvHex) {
        IsoMessage msg = new IsoMessage("0200");
        msg.setHeader("6000000000"); // 5-byte TPDU transport network header
        msg.setField(2, "5412751234567890"); // Field 2: Primary Account Number (PAN)
        msg.setField(3, "000000"); // Field 3: Processing Code (000000 = Goods & Services)
        msg.setField(4, "000000150000"); // Field 4: Transaction Amount ($1500.00 = 150000 cents)
        msg.setField(7, "0926142010"); // Field 7: Transmission Date & Time (MMDDhhmmss)
        msg.setField(11, "000456"); // Field 11: Systems Trace Audit Number (STAN)
        msg.setField(12, "142010"); // Field 12: Local Time (hhmmss)
        msg.setField(13, "0926"); // Field 13: Local Date (MMDD)
        msg.setField(14, "2812"); // Field 14: Expiration Date (YYMM)
        msg.setField(18, "5812"); // Field 18: Merchant Category Code (MCC 5812 = Restaurants)
        msg.setField(22, "051"); // Field 22: POS Entry Mode (051 = EMV Contact Chip Reader)
        msg.setField(23, "001"); // Field 23: Card Sequence Number (PSN)
        msg.setField(25, "00"); // Field 25: POS Condition Code
        msg.setField(35, "5412751234567890D2812201000000000F"); // Field 35: Track 2 Equivalent Data
        msg.setField(41, "POS85831"); // Field 41: Terminal ID (TID)
        msg.setField(42, "MCH858300000001"); // Field 42: Merchant ID (MID)
        msg.setField(43, "RESTAURANT CHICAGO US       CHICAGO   US"); // Field 43: Merchant Location

        // Field 55: Embed BER-TLV Chip Data Container (Tag 9F26 ARQC, Tag 95 TVR, Tag 9F37 UN, Tag 9F36 ATC)
        if (field55EmvHex != null && !field55EmvHex.isEmpty()) {
            msg.setField(55, field55EmvHex);
        } else {
            msg.setField(55, "9F2608A1B2C3D4E5F678909F2701809F100706010A03A000009F3704123456789F360200A4950500000080009A032609259C01009F02060000001500005F2A02079282027C009F1A020792");
        }

        return msg;
    }

    /**
     * Creates an Issuer Host ISO 8583 0210 Response message carrying Auth Code, Field 39, and Field 55 ARPC.
     *
     * @param authCode       Authorization approval code (e.g. "681042").
     * @param responseCode   ISO Field 39 response code (e.g. "00" = Approved).
     * @param field55RespEmv Host EMV response payload (Tag 91 ARPC + Tag 8A ARC).
     */
    public static IsoMessage create0210FinancialResponse(String authCode, String responseCode, String field55RespEmv) {
        IsoMessage msg = new IsoMessage("0210");
        msg.setHeader("6000000000"); // 5-byte TPDU transport network header
        msg.setField(2, "5412751234567890"); // Field 2: Primary Account Number (PAN)
        msg.setField(3, "000000"); // Field 3: Processing Code
        msg.setField(4, "000000150000"); // Field 4: Transaction Amount
        msg.setField(7, "0926142010"); // Field 7: Transmission Date & Time
        msg.setField(11, "000456"); // Field 11: Systems Trace Audit Number (STAN)
        msg.setField(12, "142010"); // Field 12: Local Time
        msg.setField(13, "0926"); // Field 13: Local Date
        msg.setField(37, "202609260456"); // Field 37: Retrieval Reference Number (RRN)
        msg.setField(38, authCode != null ? authCode : "123456"); // Field 38: Auth Code
        msg.setField(39, responseCode != null ? responseCode : "00"); // Field 39: Response Code (00 Approved)
        msg.setField(41, "POS85831"); // Field 41: Terminal ID
        msg.setField(42, "MCH858300000001"); // Field 42: Merchant ID

        // Field 55 Host Response Container (Tag 91 Issuer Authentication Data ARPC + Tag 8A Authorization Response Code)
        if (field55RespEmv != null && !field55RespEmv.isEmpty()) {
            msg.setField(55, field55RespEmv);
        } else {
            msg.setField(55, "910A3A4B5C6D7E8F900030308A023030");
        }

        return msg;
    }

    /**
     * Creates an ISO 8583 0420 Reversal Advice sample message for transaction rollback.
     */
    public static IsoMessage create0420ReversalAdvice() {
        IsoMessage msg = new IsoMessage("0420");
        msg.setHeader("6000000000");
        msg.setField(2, "5412751234567890"); // Field 2: PAN
        msg.setField(3, "000000"); // Field 3: Processing Code
        msg.setField(4, "000000150000"); // Field 4: Amount
        msg.setField(7, "0926142200"); // Field 7: Transmission Time
        msg.setField(11, "000456"); // Field 11: STAN
        msg.setField(12, "142010"); // Field 12: Local Time
        msg.setField(13, "0926"); // Field 13: Local Date
        msg.setField(22, "051"); // Field 22: POS Entry Mode
        msg.setField(37, "202609260456"); // Field 37: RRN
        msg.setField(39, "A0"); // Field 39: Reversal Reason Code
        msg.setField(41, "POS85831"); // Field 41: Terminal ID
        msg.setField(42, "MCH858300000001"); // Field 42: Merchant ID
        msg.setField(90, "020000045609261420100000000000"); // Field 90: Original Data Elements (Original MTI, STAN, Date, Time)
        return msg;
    }

    /**
     * Creates an ISO 8583 0800 Network Management Logon sample message.
     */
    public static IsoMessage create0800NetworkManagement() {
        IsoMessage msg = new IsoMessage("0800");
        msg.setHeader("6000000000");
        msg.setField(7, "0926150000"); // Field 7: Transmission Date & Time
        msg.setField(11, "000999"); // Field 11: STAN
        msg.setField(70, "301"); // Field 70: Network Management Code (301 = Echo Test / Logon)
        return msg;
    }
}
