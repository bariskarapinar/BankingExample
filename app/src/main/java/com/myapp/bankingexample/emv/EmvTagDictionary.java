package com.myapp.bankingexample.emv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EmvTagDictionary {

    private static final Map<String, EmvTag> TAGS = new HashMap<>();

    static {
        // Application & Profile Tags
        addTag("4F", "Application Identifier (AID)", "Uygulama Tanımlayıcısı (Visa, Mastercard, Troy, Amex vb. AID)", "Application Identifier (AID) identifying the card application", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("50", "Application Label", "Uygulama Etiketi (Kart üzerindeki uygulama ismi)", "Mnemonic name associated with the AID", "ans", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("57", "Track 2 Equivalent Data", "Manyetik İz 2 Muadili Veri (PAN ve Son Kullanma Tarihi vb.)", "Contains PAN and Expiry Date formatted as Magnetic Track 2", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5A", "Application Primary Account Number (PAN)", "Kart Numarası (PAN)", "Main Card Account Number", "cn", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F20", "Cardholder Name", "Kart Sahibi Adı Soyadı", "Name of the cardholder", "ans", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F24", "Application Expiration Date", "Son Kullanma Tarihi (YYMMDD)", "Date after which application expires", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F25", "Application Effective Date", "Uygulama Başlangıç Tarihi (YYMMDD)", "Date from which application is valid", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F28", "Issuer Country Code", "İhraççı Ülke Kodu (ISO 3166-1 numeric)", "Country code of the issuer", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F2A", "Transaction Currency Code", "İşlem Para Birimi Kodu (ISO 4217 - örn. 0949 TRY, 0840 USD)", "Currency code of the transaction", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F30", "Service Code", "Servis Kodu (Manyetik iz servis kodu)", "Service code on magnetic stripe / chip", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("5F34", "Application PAN Sequence Number (PSN)", "PAN Sıra Numarası (Aynı PAN'a sahip kartları ayırt etmek için)", "Sequence number to differentiate cards with same PAN", "n 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);

        // Files & Structures
        addTag("61", "Application Template", "Uygulama Şablonu (Directory Entry)", "Contains AID and Application Label in Directory Entry", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("6F", "FCI Template", "File Control Information (FCI) Şablonu", "Contains FCI data for a file or application selection", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("70", "READ RECORD Response Template", "READ RECORD Yanıt Şablonu", "Contains data records read from card SFIs", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("71", "Issuer Script Template 1", "İhraççı Betiği Şablonu 1 (İşlem öncesi script komutları)", "Contains proprietary commands sent by issuer before 2nd Generate AC", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("72", "Issuer Script Template 2", "İhraççı Betiği Şablonu 2 (İşlem sonrası script komutları)", "Contains proprietary commands sent by issuer after 2nd Generate AC", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("77", "Response Message Template Format 2", "Yanıt Mesaj Şablonu Format 2 (GPO ve Generate AC yanıtı)", "Constructed format containing explicit TLVs for GPO/AC responses", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("80", "Response Message Template Format 1", "Yanıt Mesaj Şablonu Format 1 (Sadece AIP ve AFL değerleri içerir)", "Primitive format containing concatenated AIP and AFL", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.PRIMITIVE);

        // Protocol & Security Tags
        addTag("82", "Application Interchange Profile (AIP)", "Uygulama Değişim Profili (Kartın desteklediği güvenlik işlevleri)", "Indicates card capabilities (SDA, DDA, CDA, CVM, Terminal Risk Mgmt)", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("84", "Dedicated File (DF) Name", "Özel Dosya (DF) Adı / AID", "Name of the Selected Application File", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("88", "Short File Identifier (SFI)", "Kısa Dosya Tanımlayıcısı (SFI)", "Identifies a file containing records on the card", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8A", "Authorization Response Code (ARC)", "Ototrizasyon Yanıt Kodu (Örn: 00 Onaylandı, 05 Reddedildi)", "Authorization Response Code returned by Issuer or Terminal", "an 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8C", "Card Risk Management Data Object List 1 (CDOL1)", "Kart Risk Yönetimi Veri Listesi 1 (1st Generate AC'de istenen veriler)", "List of tags terminal must send in First Generate AC", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8D", "Card Risk Management Data Object List 2 (CDOL2)", "Kart Risk Yönetimi Veri Listesi 2 (2nd Generate AC'de istenen veriler)", "List of tags terminal must send in Second Generate AC", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8E", "Cardholder Verification Method (CVM) List", "Kart Sahibi Doğrulama Listesi (PIN, İmza, CDCVM kuralları)", "List of CVM rules and conditions evaluated by terminal", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("8F", "Certification Authority Public Key Index", "Sertifika Otoritesi (CA) Açık Anahtar İndeksi", "Identifies the CA public key stored in terminal for SDA/DDA verification", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);

        // Terminal & Cryptogram Tags
        addTag("90", "Issuer Public Key Certificate", "İhraççı Açık Anahtar Sertifikası", "Issuer PK certificate for Offline Data Authentication", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("91", "Issuer Authentication Data (ARPC)", "İhraççı Doğrulama Verisi (Host ARPC Kriptogramı ve Response Code)", "Contains ARPC generated by Issuer host to authenticate host to card", "b 8-16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("94", "Application File Locator (AFL)", "Uygulama Dosya Konumlandırıcısı (Read Record ile okunacak SFI/Record'lar)", "Defines SFI and record numbers to read during transaction", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("95", "Terminal Verification Results (TVR)", "Terminal Doğrulama Sonuçları (5 Bayt - 40 Bit Güvenlik Durum Bayrakları)", "5-byte bit field reflecting Offline, PIN, Floor Limit, CVM, and Risk status", "b 40", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9A", "Transaction Date", "İşlem Tarihi (YYMMDD)", "Local date transaction was performed", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9C", "Transaction Type", "İşlem Tipi (00 Satış, 01 Nakit Çekme, 20 İade vb.)", "Identifies transaction intent (Purchase, Cash, Refund)", "n 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F02", "Amount, Authorised (Numeric)", "Yetkilendirilen Tutar (Kuruş/Cent cinsinden)", "Authorized transaction amount", "n 12", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F03", "Amount, Other (Numeric)", "Diğer Tutar / Nakit Avans Tutarı", "Secondary amount (e.g. cashback)", "n 12", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F06", "Application Identifier (AID) - Terminal", "Terminal Seçilen AID", "AID configured on terminal", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F07", "Application Usage Control (AUC)", "Uygulama Kullanım Kontrolü (Yerel/Uluslararası, Nakit/ATM/POS yetkileri)", "Indicates card usage restrictions", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F08", "Application Version Number (Card)", "Uygulama Sürüm Numarası (Kart)", "Version number of application on card", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F09", "Application Version Number (Terminal)", "Uygulama Sürüm Numarası (Terminal)", "Version number supported by terminal", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F10", "Issuer Application Data (IAD)", "İhraççı Uygulama Verisi (Kriptogram sürümü, CVR ve proprietary veriler)", "Proprietary issuer data sent to issuer host in Field 55", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F1A", "Terminal Country Code", "Terminal Ülke Kodu (ISO 3166-1 numeric - Örn: 0792 Türkiye)", "Country code of terminal", "n 3", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F1E", "Interface Device (IFD) Serial Number", "POS Terminal Seri Numarası", "Unique hardware serial number of POS terminal", "an 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F21", "Transaction Time", "İşlem Saati (HHMMSS)", "Local time transaction was performed", "n 6", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F26", "Application Cryptogram (AC)", "Uygulama Kriptogramı (ARQC / TC / AAC Kriptografik İmza)", "8-byte cryptographic hash generated by card chip for online/offline auth", "b 64", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F27", "Cryptogram Information Data (CID)", "Kriptogram Bilgi Verisi (00: AAC Decline, 80: ARQC Online, 40: TC Offline Approve)", "Indicates type of cryptogram generated (ARQC, TC, AAC) and advice required", "b 8", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F33", "Terminal Capabilities", "Terminal Yetenekleri (Kart Okuma, CVM ve Kripto Desteği)", "3-byte field describing terminal hardware capabilities", "b 24", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F34", "Cardholder Verification Method (CVM) Results", "CVM Sonuçları (Uygulanan doğrulama yöntemi ve sonucu - Örn. Online PIN OK)", "3-byte result of CVM processing performed by terminal", "b 24", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F35", "Terminal Type", "Terminal Tipi (Örn. 22 Attended POS, 14 Unattended ATM)", "Classifies terminal environment", "n 2", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F36", "Application Transaction Counter (ATC)", "Uygulama İşlem Sayacı (Karttaki her işlemde 1 artan sayaç)", "Counter incremented by card on every transaction", "b 16", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F37", "Unpredictable Number (UN)", "Rastgele Sayı (Terminal tarafından üretilen 4 baytlık Unpredictable Number)", "Random number generated by terminal to prevent replay attacks", "b 32", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F38", "Processing Options Data Object List (PDOL)", "İşlem Seçenekleri Veri Listesi (GPO komutunda gönderilmesi gereken terminal verileri)", "List of terminal tags requested by card in GPO", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F40", "Additional Terminal Capabilities", "İlave Terminal Yetenekleri", "5-byte field describing additional terminal features", "b 40", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F46", "ICC Public Key Certificate", "Kart Çip Açık Anahtar Sertifikası (DDA/CDA için)", "Card chip public key certificate for dynamic auth", "b", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("9F66", "Terminal Transaction Qualifiers (TTQ)", "Temassız Terminal Nitelikleri (Contactless TTQ - qVSDC / M/Chip)", "3-byte field used in contactless EMV to convey terminal capabilities", "b 24", EmvTag.TagClass.APPLICATION, EmvTag.TagType.PRIMITIVE);
        addTag("A5", "FCI Proprietary Template", "FCI Sahipli Şablon (FCI içindeki ek uygulama verileri)", "Proprietary information in FCI response", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.CONSTRUCTED);
        addTag("BF0C", "FCI Issuer Discretionary Data", "FCI İhraççı İsteğe Bağlı Verisi (FCI içindeki ek dizin kayıtları)", "Issuer discretionary data in FCI", "b", EmvTag.TagClass.CONTEXT_SPECIFIC, EmvTag.TagType.CONSTRUCTED);
    }

    private static void addTag(String hex, String name, String descTr, String descEn, String format, EmvTag.TagClass tagClass, EmvTag.TagType tagType) {
        EmvTag tag = new EmvTag(hex, name, descTr, descEn, format, tagClass, tagType);
        TAGS.put(hex.toUpperCase(), tag);
    }

    public static EmvTag getTag(String hex) {
        if (hex == null) return null;
        String cleanHex = hex.toUpperCase();
        if (TAGS.containsKey(cleanHex)) {
            return TAGS.get(cleanHex);
        }
        // Return default unknown tag fallback
        return new EmvTag(cleanHex, "Unknown Tag (" + cleanHex + ")", "Bilinmeyen Tag", "Unknown Tag", "b", EmvTag.TagClass.UNIVERSAL, EmvTag.TagType.PRIMITIVE);
    }

    public static Collection<EmvTag> getAllTags() {
        return TAGS.values();
    }
}
