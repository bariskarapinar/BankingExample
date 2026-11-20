package com.myapp.emvposexample.iso8583;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Educational Protocol Dictionary: ISO 8583 Data Elements (Fields 1 to 128) & Field 48 Parser.
 *
 * <p>Catalog of standard financial data elements defined by ISO 8583-1:1987 / 1993 / 2003 standards.
 * Each data element specifies format type (Numeric, Alpha, Binary, Track2), length type (FIXED, LLVAR, LLLVAR),
 * maximum length, and field description.</p>

 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoFieldDictionary {

    private static final Map<Integer, IsoFieldSpec> SPECS = new HashMap<>();

    static {
        addField(1, "Secondary Bitmap", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Secondary Bitmap (Indicates presence of Fields 65-128)");
        addField(2, "Primary Account Number (PAN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.LLVAR, 19, "Primary Account Number (PAN - LLVAR)");
        addField(3, "Processing Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Processing Code (e.g. 000000 Purchase, 010000 Cash, 200000 Refund)");
        addField(4, "Amount, Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 12, "Transaction Amount (12-digit numeric in cents)");
        addField(7, "Transmission Date & Time", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 10, "Transmission Date & Time (MMDDhhmmss)");
        addField(11, "Systems Trace Audit Number (STAN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Systems Trace Audit Number (STAN - 6 digits)");
        addField(12, "Time, Local Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Local Transaction Time (hhmmss)");
        addField(13, "Date, Local Transaction", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Local Transaction Date (MMDD)");
        addField(14, "Date, Expiration", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Card Expiration Date (YYMM)");
        addField(18, "Merchant Type / MCC", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 4, "Merchant Category Code (MCC)");
        addField(22, "Point of Service Entry Mode", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "POS Entry Mode (e.g. 021 Magstripe, 051 Chip/EMV, 071 Contactless)");
        addField(23, "Card Sequence Number (PSN)", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "Card Sequence Number (PSN)");
        addField(25, "POS Condition Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 2, "POS Condition Code (e.g. 00 Customer Present, 08 E-Commerce)");
        addField(32, "Acquiring Institution ID", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.LLVAR, 11, "Acquiring Institution ID (Acquirer Code)");
        addField(35, "Track 2 Data", IsoFieldSpec.DataType.TRACK2, IsoFieldSpec.LengthType.LLVAR, 37, "Track 2 Magnetic Stripe Data");
        addField(37, "Retrieval Reference Number (RRN)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 12, "Retrieval Reference Number (RRN - 12 chars)");
        addField(38, "Authorization Identification Response", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 6, "Authorization Response Code (Auth Code - 6 chars)");
        addField(39, "Response Code", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 2, "Authorization Response Code (e.g. 00 Approved, 51 Insufficient Funds, 54 Expired Card)");
        addField(41, "Card Acceptor Terminal ID (TID)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 8, "POS Terminal ID (TID - 8 chars)");
        addField(42, "Card Acceptor ID Code (MID)", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 15, "Merchant ID (MID - 15 chars)");
        addField(43, "Card Acceptor Name/Location", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.FIXED, 40, "Merchant Name, City, and Country");
        addField(48, "Private Additional Data", IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Private Additional Data (LLLVAR Subfields)");
        addField(52, "Personal Identification Number Data", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Encrypted PIN Block (ISO 9564-1 Format 0/1/3 - 8 Bytes Hex)");
        addField(54, "Additional Amounts", IsoFieldSpec.DataType.ALPHANUMERIC, IsoFieldSpec.LengthType.LLLVAR, 120, "Additional Amounts (Balance, Loyalty Points)");
        addField(55, "Integrated Circuit Card (ICC) Data", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.LLLVAR, 255, "ICC / EMV Data Container (BER-TLV Format Tag 9F26, 95, 9F37)");
        addField(62, "Private Reserved / Transaction Identifier", IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Private Reserved / Transaction Identifier");
        addField(64, "Message Authentication Code (MAC)", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Message Authentication Code (MAC - 8 Bytes)");
        addField(70, "Network Management Information Code", IsoFieldSpec.DataType.NUMERIC, IsoFieldSpec.LengthType.FIXED, 3, "Network Management Code (e.g. 001 Echo Test, 101 Logon)");
        addField(128, "Secondary Message Authentication Code", IsoFieldSpec.DataType.BINARY, IsoFieldSpec.LengthType.FIXED, 8, "Secondary Message Authentication Code (MAC)");
    }

    private static void addField(int num, String name, IsoFieldSpec.DataType type, IsoFieldSpec.LengthType lenType, int maxLen, String descEn) {
        SPECS.put(num, new IsoFieldSpec(num, name, type, lenType, maxLen, descEn));
    }

    public static IsoFieldSpec getFieldSpec(int num) {
        if (SPECS.containsKey(num)) {
            return SPECS.get(num);
        }
        return new IsoFieldSpec(num, "Field " + num, IsoFieldSpec.DataType.SPECIAL, IsoFieldSpec.LengthType.LLLVAR, 999, "Field " + num);
    }

    public static Collection<IsoFieldSpec> getAllSpecs() {
        return SPECS.values();
    }

    /**
     * Parses ISO 8583 Field 48 Private Data string into key-value pairs (e.g. "KEY=VALUE").
     *
     * @param field48Raw Raw Field 48 text payload
     * @return Formatted subfield breakdown string
     */
    public static String parseField48Subfields(String field48Raw) {
        if (field48Raw == null || field48Raw.trim().isEmpty()) {
            return "Field 48 is empty or null.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("--- Field 48 Private Subfield Breakdown ---\n");
        String[] tokens = field48Raw.split("[;=&]");
        for (int i = 0; i < tokens.length; i++) {
            sb.append("  Subfield [").append(i + 1).append("]: ").append(tokens[i].trim()).append("\n");
        }
        return sb.toString();
    }
}
