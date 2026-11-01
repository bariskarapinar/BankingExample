package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java BER-TLV (Basic Encoding Rules - Tag Length Value) Parser per ISO/IEC 8825-1 and EMVCo Specifications.
 *
 * <p>Handles multi-byte tags (e.g. 0x9F26, 0x5F2A, 0xBF0C), multi-byte variable length fields (e.g. 0x81 0x80),
 * recursive constructed tag parsing (e.g. 0x6F, 0x70, 0x77, 0xA5), and automatic bitwise explanation enrichment
 * for security fields such as TVR (Tag 95), AIP (Tag 82), CID (Tag 9F27), and CVM Results (Tag 9F34).</p>
 */
public class TlvParser {

    /**
     * Parses a hex string containing BER-TLV structures into a list of {@link TlvElement} items.
     *
     * @param hexString Clean or space-separated hex string
     * @return List of parsed TLV elements
     */
    public static List<TlvElement> parseHex(String hexString) {
        if (hexString == null || hexString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        byte[] bytes = HexUtils.hexToBytes(hexString);
        return parseBytes(bytes, 0, bytes.length);
    }

    /**
     * Core recursive byte array TLV parser algorithm.
     *
     * @param data Byte array payload
     * @param offset Starting byte index
     * @param length Payload length
     * @return List of root {@link TlvElement} nodes
     */
    public static List<TlvElement> parseBytes(byte[] data, int offset, int length) {
        List<TlvElement> elements = new ArrayList<>();
        int index = offset;
        int end = offset + length;

        while (index < end) {
            // Skip padding 00 or FF bytes
            if (data[index] == (byte) 0x00 || data[index] == (byte) 0xFF) {
                index++;
                continue;
            }

            // Parse Tag Field
            int tagStartIndex = index;
            byte b1 = data[index++];
            boolean isMultiByte = (b1 & 0x1F) == 0x1F;

            if (isMultiByte) {
                while (index < end) {
                    byte bNext = data[index++];
                    if ((bNext & 0x80) == 0) {
                        break; // Bit 8 = 0 indicates last byte of multi-byte tag
                    }
                }
            }

            int tagLen = index - tagStartIndex;
            String tagHex = HexUtils.bytesToHex(data, tagStartIndex, tagLen);

            if (index >= end) break;

            // Parse Length Field
            int valueLength = 0;
            byte lenByte1 = data[index++];
            if ((lenByte1 & 0x80) == 0) {
                // Single byte length (0..127)
                valueLength = lenByte1 & 0x7F;
            } else {
                // Multi-byte length header (e.g. 0x81 0x80 -> 128 bytes)
                int numLenBytes = lenByte1 & 0x7F;
                for (int i = 0; i < numLenBytes && index < end; i++) {
                    valueLength = (valueLength << 8) | (data[index++] & 0xFF);
                }
            }

            if (index + valueLength > end) {
                valueLength = end - index; // Clip to available length if malformed
            }

            // Parse Value Field
            byte[] valueBytes = new byte[valueLength];
            if (valueLength > 0) {
                System.arraycopy(data, index, valueBytes, 0, valueLength);
            }
            index += valueLength;

            TlvElement element = new TlvElement(tagHex, valueBytes);

            // Enrich known security tags with human-readable bitwise breakdowns
            enrichExplanation(element);

            // Recursive constructed tag parsing
            EmvTag emvTag = EmvTagDictionary.getTag(tagHex);
            if (emvTag.isConstructed() && valueLength > 0) {
                List<TlvElement> children = parseBytes(valueBytes, 0, valueLength);
                for (TlvElement child : children) {
                    element.addChild(child);
                }
            }

            elements.add(element);
        }

        return elements;
    }

    private static void enrichExplanation(TlvElement element) {
        String tagHex = element.getTagHex();
        byte[] val = element.getValueBytes();

        if ("95".equals(tagHex) && val.length >= 5) {
            element.setExplanation(explainTVR(val));
        } else if ("82".equals(tagHex) && val.length >= 2) {
            element.setExplanation(explainAIP(val));
        } else if ("9F27".equals(tagHex) && val.length >= 1) {
            element.setExplanation(explainCID(val[0]));
        } else if ("9F34".equals(tagHex) && val.length >= 3) {
            element.setExplanation(explainCVMResults(val));
        } else if ("5F2A".equals(tagHex) || "9F1A".equals(tagHex)) {
            String hex = element.getValueHex();
            if ("0792".equals(hex) || "792".equals(hex)) {
                element.setExplanation("Ülke/Para Birimi: Türkiye / TRY (0792)");
            } else if ("0840".equals(hex) || "840".equals(hex)) {
                element.setExplanation("Country/Currency: USA / USD (0840)");
            } else if ("0978".equals(hex) || "978".equals(hex)) {
                element.setExplanation("Country/Currency: Eurozone / EUR (0978)");
            }
        } else if ("9F26".equals(tagHex)) {
            element.setExplanation("AC Cryptogram Hash: " + element.getValueHex());
        }
    }

    /**
     * Generates a line-by-line bitwise breakdown for 5-byte Terminal Verification Results (TVR - Tag 95).
     *
     * @param tvr 5-byte TVR binary array
     * @return Formatted multi-line Turkish explanation string
     */
    public static String explainTVR(byte[] tvr) {
        if (tvr == null || tvr.length < 5) return "Geçersiz TVR uzunluğu";
        StringBuilder sb = new StringBuilder();
        sb.append("--- Terminal Verification Results (TVR - Tag 95) ---\n");

        // Byte 1
        sb.append("Bayt 1:\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[0] & 0x80) != 0 ? "Offline veriler doğrulanmadı" : "Offline veri doğrulama yapıldı").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[0] & 0x40) != 0 ? "SDA başarısız oldu (SDA Failed)" : "SDA Başarılı/Yapılmadı").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[0] & 0x20) != 0 ? "Kart verileri eksik" : "Kart verileri tam").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[0] & 0x10) != 0 ? "Kart istisna listesinde (Blacklist)" : "Kart istisna listesinde değil").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[0] & 0x08) != 0 ? "DDA başarısız oldu (DDA Failed)" : "DDA Başarılı/Yapılmadı").append("\n");
        sb.append("  Bit 3 [0x04]: ").append((tvr[0] & 0x04) != 0 ? "CDA başarısız oldu (CDA Failed)" : "CDA Başarılı/Yapılmadı").append("\n");

        // Byte 2
        sb.append("Bayt 2:\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[1] & 0x80) != 0 ? "Kart ve terminal sürümleri uyuşmuyor" : "Uygulama sürümleri uyumlu").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[1] & 0x40) != 0 ? "Süresi dolmuş kart (Expired Card)" : "Kart süresi geçerli").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[1] & 0x20) != 0 ? "Kart henüz yürürlüğe girmedi (Not effective)" : "Kart yürürlükte").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[1] & 0x10) != 0 ? "İstenen hizmet kartta izinli değil" : "İstenen hizmet kartta izinli").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[1] & 0x08) != 0 ? "Yeni kart (First Use)" : "Eski kart").append("\n");

        // Byte 3
        sb.append("Bayt 3:\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[2] & 0x80) != 0 ? "Kart Sahibi Doğrulama (CVM) başarısız" : "CVM Başarılı").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[2] & 0x40) != 0 ? "Bilinmeyen CVM kuralı" : "CVM kuralı biliniyor").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[2] & 0x20) != 0 ? "PIN Deneme Limiti Aşıldı" : "PIN deneme limiti aşılmadı").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[2] & 0x10) != 0 ? "PIN PAD arızalı veya yok" : "PIN PAD hazır").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[2] & 0x08) != 0 ? "PIN Girilmedi (PIN Required but not entered)" : "PIN Girildi").append("\n");
        sb.append("  Bit 3 [0x04]: ").append((tvr[2] & 0x04) != 0 ? "Online PIN Girildi" : "Online PIN girilmedi").append("\n");

        // Byte 4
        sb.append("Bayt 4:\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[3] & 0x80) != 0 ? "İşlem tutarı floor limit'i aştı" : "Floor limit aşılmadı").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[3] & 0x40) != 0 ? "Rastgele online seçimi tetiklendi" : "Rastgele online tetiklenmedi").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[3] & 0x20) != 0 ? "Rastgele online zorlaması yapıldı" : "Online zorlaması yapılmadı").append("\n");

        // Byte 5
        sb.append("Bayt 5:\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[4] & 0x80) != 0 ? "İşlem online zorlandı" : "Online zorlanmadı").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[4] & 0x40) != 0 ? "İşlem online cevabını bekliyor" : "Online cevap beklenmiyor").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[4] & 0x20) != 0 ? "İhraççı betiği (Issuer Script) başarısız oldu" : "Issuer script sorunsuz").append("\n");

        return sb.toString();
    }

    /**
     * Generates a line-by-line bitwise breakdown for 2-byte Application Interchange Profile (AIP - Tag 82).
     *
     * @param aip 2-byte AIP binary array
     * @return Formatted multi-line Turkish explanation string
     */
    public static String explainAIP(byte[] aip) {
        if (aip == null || aip.length < 2) return "Geçersiz AIP uzunluğu";
        StringBuilder sb = new StringBuilder();
        sb.append("--- Application Interchange Profile (AIP - Tag 82) ---\n");
        sb.append("Bayt 1:\n");
        sb.append("  Bit 8 [0x80]: ").append((aip[0] & 0x80) != 0 ? "SDA Destekleniyor" : "SDA Yok").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((aip[0] & 0x40) != 0 ? "DDA Destekleniyor" : "DDA Yok").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((aip[0] & 0x20) != 0 ? "Kart Sahibi Doğrulama (CVM) Destekleniyor" : "CVM Yok").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((aip[0] & 0x10) != 0 ? "Terminal Risk Yönetimi Zorunlu" : "Terminal Risk Mgmt Zorunlu Değil").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((aip[0] & 0x08) != 0 ? "İhraççı Doğrulama (Issuer Auth) Destekleniyor" : "Issuer Auth Yok").append("\n");
        sb.append("  Bit 1 [0x01]: ").append((aip[0] & 0x01) != 0 ? "CDA Destekleniyor" : "CDA Yok").append("\n");

        sb.append("Bayt 2:\n");
        sb.append("  Bit 8 [0x80]: ").append((aip[1] & 0x80) != 0 ? "Temassız EMV Destekleniyor" : "Temassız EMV Yok").append("\n");
        return sb.toString();
    }

    /**
     * Explains Cryptogram Information Data (CID - Tag 9F27).
     *
     * @param cid 1-byte CID value
     * @return Explanation string
     */
    public static String explainCID(byte cid) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Cryptogram Information Data (CID - Tag 9F27) ---\n");
        int type = cid & 0xC0;
        if (type == 0x80) {
            sb.append("Kriptogram Tipi: ARQC (Authorisation Request Cryptogram - Online Onay İsteyi)\n");
        } else if (type == 0x40) {
            sb.append("Kriptogram Tipi: TC (Transaction Certificate - Offline Onaylandı)\n");
        } else if (type == 0x00) {
            sb.append("Kriptogram Tipi: AAC (Application Authentication Cryptogram - Offline Reddedildi)\n");
        } else if (type == 0xC0) {
            sb.append("Kriptogram Tipi: AAR (Application Authorization Referral)\n");
        }
        sb.append("Advice Gerekli: ").append((cid & 0x08) != 0 ? "Evet" : "Hayır").append("\n");
        sb.append("Neden Kodu: 0x").append(Integer.toHexString(cid & 0x07).toUpperCase());
        return sb.toString();
    }

    /**
     * Explains CVM Results (Tag 9F34).
     *
     * @param cvm 3-byte CVM Result array
     * @return Explanation string
     */
    public static String explainCVMResults(byte[] cvm) {
        if (cvm == null || cvm.length < 3) return "Geçersiz CVM Result uzunluğu";
        StringBuilder sb = new StringBuilder();
        sb.append("--- CVM Results (Tag 9F34) ---\n");
        int code = cvm[0] & 0x3F;
        sb.append("Uygulanan CVM Yöntemi: ");
        switch (code) {
            case 0x01: sb.append("Plaintext PIN verified by ICC"); break;
            case 0x02: sb.append("Enciphered PIN verified online"); break;
            case 0x03: sb.append("Plaintext PIN verified by ICC and Paper Signature"); break;
            case 0x04: sb.append("Enciphered PIN verified by ICC"); break;
            case 0x05: sb.append("Enciphered PIN verified by ICC and Paper Signature"); break;
            case 0x1E: sb.append("Paper Signature"); break;
            case 0x1F: sb.append("No CVM Required"); break;
            case 0x22: sb.append("Consumer Device CVM (CDCVM - Biyometrik/Telefon)"); break;
            default: sb.append("Özel / Diğer CVM Kod: 0x").append(Integer.toHexString(code)); break;
        }
        sb.append("\nCVM Durum/Sonuç: ");
        int result = cvm[2] & 0xFF;
        if (result == 0x00) sb.append("Bilinmiyor / Başlatılmadı");
        else if (result == 0x01) sb.append("Başarısız (Failed)");
        else if (result == 0x02) sb.append("Başarılı (Successful)");
        else sb.append("Sonuç Kod: 0x").append(Integer.toHexString(result));

        return sb.toString();
    }
}
