package com.myapp.emvposexample.emv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Educational Tag Dictionary: EMV BER-TLV Tag Catalog (60+ Standard Tags).
 *
 * <p>Comprehensive registry of standard BER-TLV data object tags defined across EMV Books 1, 2, 3, and 4.
 * Defines tag hex identifiers, full name, short description, data format specification (Numeric, ASCII, Binary),
 * Tag Class (Application, Context, Universal), and Tag Type (Primitive vs Constructed).</p>
 *
 * @author EMV L2 Kernel Engineering Group
 */
public class EmvTagDictionary {

    private static final Map<String, EmvTag> TAGS = new HashMap<>();

    static {
        // Application Identifiers & Labels
        addTag("4F", "Application Identifier (AID)", "Application Identifier (Visa, Mastercard, Troy, Amex AID)", "Application Identifier (AID) identifying the card application", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("50", "Application Label", "Application Label (Card Mnemonic Name)", "Mnemonic name associated with the AID", "ans", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("57", "Track 2 Equivalent Data", "Track 2 Equivalent Data (PAN, Expiry Date)", "Contains PAN and Expiry Date formatted as Magnetic Track 2", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5A", "Application Primary Account Number (PAN)", "Card Primary Account Number (PAN)", "Main Card Account Number", "cn", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F20", "Cardholder Name", "Cardholder Full Name", "Name of the cardholder", "ans", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F24", "Application Expiration Date", "Application Expiration Date (YYMMDD)", "Date after which application expires", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F25", "Application Effective Date", "Application Effective Date (YYMMDD)", "Date from which application is valid", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F28", "Issuer Country Code", "Issuer Country Code (ISO 3166-1 Numeric)", "Country code of the issuer", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F2A", "Transaction Currency Code", "Transaction Currency Code (ISO 4217 Numeric)", "Currency code of the transaction", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F30", "Service Code", "Service Code (Magnetic Stripe Service Code)", "Service code on magnetic stripe / chip", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F34", "Application PAN Sequence Number (PSN)", "PAN Sequence Number (PSN)", "Sequence number to differentiate cards with same PAN", "n 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);

        // Constructed Template Tags
        addTag("61", "Application Template", "Application Directory Entry Template", "Contains AID and Application Label in Directory Entry", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("6F", "FCI Template", "File Control Information (FCI) Template", "Contains FCI data for a file or application selection", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("70", "READ RECORD Response Template", "READ RECORD SFI Data Record Template", "Contains data records read from card SFIs", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("71", "Issuer Script Template 1", "Issuer Script Template 1 (Pre-authorization Commands)", "Contains proprietary commands sent by issuer before 2nd Generate AC", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("72", "Issuer Script Template 2", "Issuer Script Template 2 (Post-authorization Commands)", "Contains proprietary commands sent by issuer after 2nd Generate AC", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("77", "Response Message Template Format 2", "Response Message Template Format 2 (Explicit TLVs)", "Constructed format containing explicit TLVs for GPO/AC responses", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("80", "Response Message Template Format 1", "Response Message Template Format 1 (Concatenated AIP/AFL)", "Primitive format containing concatenated AIP and AFL", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.PRIMITIVE);

        // Core EMV Cryptography & Protocol Control Tags
        addTag("82", "Application Interchange Profile (AIP)", "Application Interchange Profile (Card Capabilities)", "Indicates card capabilities (SDA, DDA, CDA, CVM, Terminal Risk Mgmt)", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("84", "Dedicated File (DF) Name", "Dedicated File Name / Selected AID", "Name of the Selected Application File", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("88", "Short File Identifier (SFI)", "Short File Identifier (SFI)", "Identifies a file containing records on the card", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8A", "Authorization Response Code (ARC)", "Authorization Response Code (e.g. 00 Approved)", "Authorization Response Code returned by Issuer or Terminal", "an 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8C", "Card Risk Management Data Object List 1 (CDOL1)", "Card Risk Management Data Object List 1", "List of tags terminal must send in First Generate AC", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8D", "Card Risk Management Data Object List 2 (CDOL2)", "Card Risk Management Data Object List 2", "List of tags terminal must send in Second Generate AC", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8E", "Cardholder Verification Method (CVM) List", "Cardholder Verification Method List", "List of CVM rules and conditions evaluated by terminal", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8F", "Certification Authority Public Key Index", "CA Public Key Index (Scheme Root PK)", "Identifies the CA public key stored in terminal for SDA/DDA verification", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);

        // Security & Transaction Attributes
        addTag("90", "Issuer Public Key Certificate", "Issuer Public Key Certificate", "Issuer PK certificate for Offline Data Authentication", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("91", "Issuer Authentication Data (ARPC)", "Issuer Authentication Data (ARPC Cryptogram)", "Contains ARPC generated by Issuer host to authenticate host to card", "b 8-16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("94", "Application File Locator (AFL)", "Application File Locator (SFI/Records to read)", "Defines SFI and record numbers to read during transaction", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("95", "Terminal Verification Results (TVR)", "Terminal Verification Results (5-Byte Bitmask)", "5-byte bit field reflecting Offline, PIN, Floor Limit, CVM, and Risk status", "b 40", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9A", "Transaction Date", "Transaction Date (YYMMDD)", "Local date transaction was performed", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9C", "Transaction Type", "Transaction Type Code", "Identifies transaction intent (Purchase, Cash, Refund)", "n 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F02", "Amount, Authorised (Numeric)", "Amount Authorised (Transaction Cents)", "Authorized transaction amount", "n 12", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F03", "Amount, Other (Numeric)", "Amount Other (Cashback Cents)", "Secondary amount (e.g. cashback)", "n 12", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F06", "Application Identifier (AID) - Terminal", "Terminal Selected AID", "AID configured on terminal", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F07", "Application Usage Control (AUC)", "Application Usage Control (Restrictions)", "Indicates card usage restrictions", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F08", "Application Version Number (Card)", "Card Application Version Number", "Version number of application on card", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F09", "Application Version Number (Terminal)", "Terminal Application Version Number", "Version number supported by terminal", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F10", "Issuer Application Data (IAD)", "Issuer Application Data (Field 55 IAD)", "Proprietary issuer data sent to issuer host in Field 55", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F1A", "Terminal Country Code", "Terminal Country Code (ISO 3166-1 Numeric)", "Country code of terminal", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F1E", "Interface Device (IFD) Serial Number", "POS Terminal Hardware Serial Number", "Unique hardware serial number of POS terminal", "an 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F21", "Transaction Time", "Transaction Time (HHMMSS)", "Local time transaction was performed", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F26", "Application Cryptogram (AC)", "Application Cryptogram (ARQC/TC/AAC Hash)", "8-byte cryptographic hash generated by card chip for online/offline auth", "b 64", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F27", "Cryptogram Information Data (CID)", "Cryptogram Information Data (AC Type)", "Indicates type of cryptogram generated (ARQC, TC, AAC) and advice required", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F33", "Terminal Capabilities", "Terminal Hardware Capabilities", "3-byte field describing terminal hardware capabilities", "b 24", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F34", "Cardholder Verification Method (CVM) Results", "CVM Results (Applied Method & Result)", "3-byte result of CVM processing performed by terminal", "b 24", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F36", "Application Transaction Counter (ATC)", "Application Transaction Counter (ATC)", "2-byte counter incremented by chip on every transaction", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F37", "Unpredictable Number (UN)", "Unpredictable Number (4-Byte Random Salt)", "4-byte random challenge generated by terminal to prevent relay attacks", "b 32", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F46", "ICC Public Key Certificate", "Card Chip Public Key Certificate", "Chip PK certificate decrypted using Issuer PK during DDA/CDA", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("A5", "FCI Proprietary Template", "FCI Proprietary Template", "Contains proprietary data in FCI Response", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
    }

    private static void addTag(String hex, String name, String descEn, String longDesc, String format, EmvTag.TagClass tagClass, EmvTag.TagType tagType) {
        TAGS.put(hex.toUpperCase(), new EmvTag(hex.toUpperCase(), name, descEn, longDesc, format, tagClass, tagType));
    }

    /**
     * Looks up an {@link EmvTag} specification by its upper-case hexadecimal tag code.
     *
     * @param tagHex Upper/lower-case tag hex string (e.g. "9F26", "95").
     * @return Registered {@link EmvTag} instance, or a dynamic default instance if unknown.
     */
    public static EmvTag getTag(String tagHex) {
        if (tagHex == null) {
            return new EmvTag("00", "Unknown Tag", "Unknown Tag", "Unknown BER-TLV Tag", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.PRIMITIVE);
        }
        String cleanTag = tagHex.toUpperCase().trim();
        if (TAGS.containsKey(cleanTag)) {
            return TAGS.get(cleanTag);
        }
        return new EmvTag(cleanTag, "Proprietary Tag (" + cleanTag + ")", "Proprietary Tag", "Proprietary / Unknown Tag 0x" + cleanTag, "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
    }

    /**
     * Returns all registered standard EMV tags in this catalog.
     *
     * @return Collection of {@link EmvTag} entries.
     */
    public static Collection<EmvTag> getAllTags() {
        return TAGS.values();
    }
}
