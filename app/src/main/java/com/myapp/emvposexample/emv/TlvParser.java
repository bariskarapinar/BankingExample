package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

public class TlvParser {

    public static List<TlvElement> parseHex(String hexString) {
        if (hexString == null || hexString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        byte[] bytes = HexUtils.hexToBytes(hexString);
        return parseBytes(bytes, 0, bytes.length);
    }

    public static List<TlvElement> parseBytes(byte[] data, int offset, int length) {
        List<TlvElement> elements = new ArrayList<>();
        int index = offset;
        int end = offset + length;

        while (index < end) {
            if (data[index] == (byte) 0x00 || data[index] == (byte) 0xFF) {
                index++;
                continue;
            }

            int tagStartIndex = index;
            byte b1 = data[index++];
            boolean isMultiByte = (b1 & 0x1F) == 0x1F;

            if (isMultiByte) {
                while (index < end) {
                    byte bNext = data[index++];
                    if ((bNext & 0x80) == 0) {
                        break;
                    }
                }
            }

            int tagLen = index - tagStartIndex;
            String tagHex = HexUtils.bytesToHex(data, tagStartIndex, tagLen);

            if (index >= end) break;

            int valueLength = 0;
            byte lenByte1 = data[index++];
            if ((lenByte1 & 0x80) == 0) {
                valueLength = lenByte1 & 0x7F;
            } else {
                int numLenBytes = lenByte1 & 0x7F;
                for (int i = 0; i < numLenBytes && index < end; i++) {
                    valueLength = (valueLength << 8) | (data[index++] & 0xFF);
                }
            }

            if (index + valueLength > end) {
                valueLength = end - index;
            }

            byte[] valueBytes = new byte[valueLength];
            if (valueLength > 0) {
                System.arraycopy(data, index, valueBytes, 0, valueLength);
            }
            index += valueLength;

            TlvElement element = new TlvElement(tagHex, valueBytes);

            enrichExplanation(element);

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
                element.setExplanation("Country/Currency: Turkey / TRY (0792)");
            } else if ("0840".equals(hex) || "840".equals(hex)) {
                element.setExplanation("Country/Currency: USA / USD (0840)");
            } else if ("0978".equals(hex) || "978".equals(hex)) {
                element.setExplanation("Country/Currency: Eurozone / EUR (0978)");
            }
        } else if ("9F26".equals(tagHex)) {
            element.setExplanation("AC Cryptogram Hash: " + element.getValueHex());
        } else if ("90".equals(tagHex) || "9F46".equals(tagHex) || "8F".equals(tagHex)) {
            element.setExplanation(explainSdaDdaCda(tagHex, element.getValueHex()));
        }
    }

    public static String explainSdaDdaCda(String tagHex, String valueHex) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- EMV RSA Offline Data Authentication Certificate ---\n");
        if ("8F".equals(tagHex)) {
            sb.append("CA Public Key Index (Tag 8F): ");
            sb.append("Identifies Payment Scheme CA Public Key stored in terminal (Index: 0x").append(valueHex).append(")\n");
        } else if ("90".equals(tagHex)) {
            sb.append("Issuer Public Key Certificate (Tag 90):\n");
            sb.append("  Signed by Scheme CA using RSA. Contains Issuer Public Key Modulus & Exponent.\n");
            sb.append("  Terminal decrypts Tag 90 using Scheme CA Public Key to recover Issuer Public Key.\n");
        } else if ("9F46".equals(tagHex)) {
            sb.append("ICC (Card Chip) Public Key Certificate (Tag 9F46):\n");
            sb.append("  Signed by Issuer using Issuer Private Key. Contains Chip Public Key for Dynamic DDA/CDA.\n");
            sb.append("  Terminal decrypts Tag 9F46 using recovered Issuer Public Key to perform Dynamic Authentication.\n");
        }
        return sb.toString();
    }

    public static String explainTVR(byte[] tvr) {
        if (tvr == null || tvr.length < 5) return "Invalid TVR length";
        StringBuilder sb = new StringBuilder();
        sb.append("--- Terminal Verification Results (TVR - Tag 95 - 5 Bytes / 40 Bits) ---\n");

        sb.append("Byte 1 (Offline Data Authentication Status):\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[0] & 0x80) != 0 ? "Offline data authentication not performed" : "Offline data authentication performed").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[0] & 0x40) != 0 ? "SDA Failed" : "SDA Passed / Not performed").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[0] & 0x20) != 0 ? "ICC Data missing" : "ICC Data complete").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[0] & 0x10) != 0 ? "Card appears on terminal exception list" : "Card not on exception list").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[0] & 0x08) != 0 ? "DDA Failed" : "DDA Passed / Not performed").append("\n");
        sb.append("  Bit 3 [0x04]: ").append((tvr[0] & 0x04) != 0 ? "CDA Failed" : "CDA Passed / Not performed").append("\n");

        sb.append("Byte 2 (Application & Card Expiry Status):\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[1] & 0x80) != 0 ? "ICC and Terminal versions mismatch" : "Application versions match").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[1] & 0x40) != 0 ? "Expired Card" : "Card valid").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[1] & 0x20) != 0 ? "Application not yet effective" : "Card effective").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[1] & 0x10) != 0 ? "Requested service not allowed for card product" : "Service allowed").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[1] & 0x08) != 0 ? "New Card (First Use)" : "Existing Card").append("\n");

        sb.append("Byte 3 (Cardholder Verification Status):\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[2] & 0x80) != 0 ? "Cardholder Verification (CVM) Failed" : "CVM Successful").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[2] & 0x40) != 0 ? "Unrecognised CVM" : "CVM recognised").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[2] & 0x20) != 0 ? "PIN Try Limit Exceeded" : "PIN Try Limit not exceeded").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((tvr[2] & 0x10) != 0 ? "PIN PAD absent or not working" : "PIN PAD ready").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((tvr[2] & 0x08) != 0 ? "PIN Required but not entered" : "PIN Entered").append("\n");
        sb.append("  Bit 3 [0x04]: ").append((tvr[2] & 0x04) != 0 ? "Online PIN Entered" : "Online PIN not entered").append("\n");

        sb.append("Byte 4 (Terminal Risk Management Status):\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[3] & 0x80) != 0 ? "Transaction exceeds floor limit" : "Floor limit not exceeded").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[3] & 0x40) != 0 ? "Lower consecutive offline limit exceeded" : "Lower limit valid").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[3] & 0x20) != 0 ? "Upper consecutive offline limit exceeded" : "Upper limit valid").append("\n");

        sb.append("Byte 5 (Issuer Script & Action Status):\n");
        sb.append("  Bit 8 [0x80]: ").append((tvr[4] & 0x80) != 0 ? "Transaction forced online" : "Online not forced").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((tvr[4] & 0x40) != 0 ? "Default TDOL used" : "Standard TDOL used").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((tvr[4] & 0x20) != 0 ? "Issuer Script processing failed" : "Issuer Script OK").append("\n");

        return sb.toString();
    }

    public static String explainAIP(byte[] aip) {
        if (aip == null || aip.length < 2) return "Invalid AIP length";
        StringBuilder sb = new StringBuilder();
        sb.append("--- Application Interchange Profile (AIP - Tag 82 - 2 Bytes) ---\n");
        sb.append("Byte 1 (Core Security Features):\n");
        sb.append("  Bit 8 [0x80]: ").append((aip[0] & 0x80) != 0 ? "SDA Supported (Static Data Auth)" : "SDA Not Supported").append("\n");
        sb.append("  Bit 7 [0x40]: ").append((aip[0] & 0x40) != 0 ? "DDA Supported (Dynamic Data Auth)" : "DDA Not Supported").append("\n");
        sb.append("  Bit 6 [0x20]: ").append((aip[0] & 0x20) != 0 ? "Cardholder Verification (CVM) Supported" : "CVM Not Supported").append("\n");
        sb.append("  Bit 5 [0x10]: ").append((aip[0] & 0x10) != 0 ? "Terminal Risk Management Required" : "Terminal Risk Mgmt Optional").append("\n");
        sb.append("  Bit 4 [0x08]: ").append((aip[0] & 0x08) != 0 ? "Issuer Authentication Supported" : "Issuer Auth Not Supported").append("\n");
        sb.append("  Bit 1 [0x01]: ").append((aip[0] & 0x01) != 0 ? "CDA Supported (Combined DDA/AC)" : "CDA Not Supported").append("\n");

        sb.append("Byte 2 (Interface Differentiators):\n");
        sb.append("  Bit 8 [0x80]: ").append((aip[1] & 0x80) != 0 ? "EMV Contactless Supported" : "EMV Contactless Not Supported").append("\n");
        return sb.toString();
    }

    public static String explainCID(byte cid) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Cryptogram Information Data (CID - Tag 9F27) ---\n");
        int type = cid & 0xC0;
        if (type == 0x80) {
            sb.append("Cryptogram Type: ARQC (Authorisation Request Cryptogram - Online Host Approval Requested)\n");
        } else if (type == 0x40) {
            sb.append("Cryptogram Type: TC (Transaction Certificate - Offline Approved)\n");
        } else if (type == 0x00) {
            sb.append("Cryptogram Type: AAC (Application Authentication Cryptogram - Offline Declined)\n");
        } else if (type == 0xC0) {
            sb.append("Cryptogram Type: AAR (Application Authorization Referral)\n");
        }
        sb.append("Advice Required: ").append((cid & 0x08) != 0 ? "Yes" : "No").append("\n");
        sb.append("Reason Code: 0x").append(Integer.toHexString(cid & 0x07).toUpperCase());
        return sb.toString();
    }

    public static String explainCVMResults(byte[] cvm) {
        if (cvm == null || cvm.length < 3) return "Invalid CVM Result length";
        StringBuilder sb = new StringBuilder();
        sb.append("--- CVM Results (Tag 9F34 - 3 Bytes) ---\n");
        int code = cvm[0] & 0x3F;
        sb.append("Applied CVM Method: ");
        switch (code) {
            case 0x01: sb.append("Plaintext PIN verified by ICC"); break;
            case 0x02: sb.append("Enciphered PIN verified online by Issuer Host"); break;
            case 0x03: sb.append("Plaintext PIN verified by ICC and Paper Signature"); break;
            case 0x04: sb.append("Enciphered PIN verified by ICC"); break;
            case 0x05: sb.append("Enciphered PIN verified by ICC and Paper Signature"); break;
            case 0x1E: sb.append("Paper Signature"); break;
            case 0x1F: sb.append("No CVM Required (Low Value Exemption)"); break;
            case 0x22: sb.append("Consumer Device CVM (CDCVM - Mobile Biometrics)"); break;
            default: sb.append("Custom / Other CVM Code: 0x").append(Integer.toHexString(code)); break;
        }
        sb.append("\nCVM Status/Result: ");
        int result = cvm[2] & 0xFF;
        if (result == 0x00) sb.append("Unknown / Initialized");
        else if (result == 0x01) sb.append("Failed");
        else if (result == 0x02) sb.append("Successful");
        else sb.append("Result Code: 0x").append(Integer.toHexString(result));

        return sb.toString();
    }
}
