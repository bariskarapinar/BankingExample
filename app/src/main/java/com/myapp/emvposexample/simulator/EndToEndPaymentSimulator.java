package com.myapp.emvposexample.simulator;

import com.myapp.emvposexample.emv.EmvTransactionEngine;
import com.myapp.emvposexample.emv.TlvElement;
import com.myapp.emvposexample.emv.TlvParser;
import com.myapp.emvposexample.iso8583.IsoFieldValue;
import com.myapp.emvposexample.iso8583.IsoMessage;
import com.myapp.emvposexample.iso8583.IsoMessagePacker;
import com.myapp.emvposexample.iso8583.IsoMessageUnpacker;
import com.myapp.emvposexample.iso8583.IsoMtiDecoder;
import com.myapp.emvposexample.iso8583.IsoSampleMessages;
import com.myapp.emvposexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Educational Simulator Engine: End-to-End 4-Party Financial Payment Flow.
 *
 * <p>Simulates the complete real-time lifecycle of an EMV chip card transaction
 * across all 4 key payment ecosystem entities:</p>
 * <ol>
 *   <li><b>POS Terminal &amp; Smart Card (EMV Level 2 Kernel):</b> Executes APDU commands (SELECT, GPO, READ RECORD, 1st GENERATE AC).</li>
 *   <li><b>POS Terminal / Acquirer:</b> Packages EMV Field 55 ICC payload into ISO 8583 0200 Financial Request.</li>
 *   <li><b>Payment Gateway &amp; Issuer Bank Host:</b> Unpacks 0200, parses Field 55 TLV tags, verifies ARQC cryptogram, evaluates balance, generates 0210 response with ARPC.</li>
 *   <li><b>POS Terminal &amp; Card (2nd Generate AC):</b> Card validates Host ARPC (Tag 91) and issues final TC (Transaction Certificate) approval.</li>
 * </ol>
 *
 * @author Payment Ecosystem Simulation Team
 */
public class EndToEndPaymentSimulator {

    public static class SimulationLogResult {
        public String statusSummary;
        public String requestIsoHex;
        public String responseIsoHex;
        public List<String> logs = new ArrayList<>();
    }

    /**
     * Runs a complete live 4-party payment transaction flow.
     *
     * @param cardScheme  Selected card brand ("Visa", "Mastercard", "Troy", "American Express").
     * @param amountCents Transaction amount in cents.
     * @param entryMode   POSIX entry mode ("051" for Chip, "071" for Contactless NFC).
     * @return Formatted {@link SimulationLogResult} containing trace logs and ISO payload dumps.
     */
    public static SimulationLogResult runFullPaymentFlow(String cardScheme, long amountCents, String entryMode) {
        SimulationLogResult result = new SimulationLogResult();
        List<String> log = result.logs;

        log.add("=========================================================================");
        log.add(" END-TO-END INTERACTIVE PAYMENT SIMULATION (POS <-> EMV <-> ISO 8583 <-> HOST)");
        log.add("=========================================================================");

        // Step 1: Execute EMV Card Terminal Flow
        EmvTransactionEngine.TransactionResult emvResult = EmvTransactionEngine.runSimulatedEmvTransaction(cardScheme, amountCents, entryMode);
        log.addAll(emvResult.logs);

        log.add("\n-------------------------------------------------------------------------");
        log.add(" [STAGE 2] POS TERMINAL / ACQUIRER: ISO 8583 MESSAGE PACKING (0200)");
        log.add("-------------------------------------------------------------------------");

        IsoMessage req0200 = IsoSampleMessages.create0200FinancialRequest(emvResult.field55Hex);
        req0200.setField(2, emvResult.pan);
        req0200.setField(4, String.format(Locale.US, "%012d", amountCents));

        if ("071".equals(entryMode)) {
            req0200.setField(22, "071");
        } else {
            req0200.setField(22, "051");
        }

        byte[] reqBytes = IsoMessagePacker.pack(req0200);
        result.requestIsoHex = HexUtils.bytesToHex(reqBytes);

        IsoMtiDecoder.MtiInfo reqMtiInfo = IsoMtiDecoder.decode(req0200.getMti());
        log.add("MTI Classification: " + reqMtiInfo.getFullDescriptionEn());
        log.add("Primary Bitmap Hex: " + req0200.getBitmap().toHex());
        log.add("Primary Bitmap Binary:\n  " + req0200.getBitmap().toBinaryString());

        log.add("\nISO 8583 0200 Financial Request Data Elements:");
        for (Map.Entry<Integer, IsoFieldValue> entry : req0200.getFields().entrySet()) {
            int fieldNum = entry.getKey();
            IsoFieldValue fVal = entry.getValue();
            if (fieldNum == 55) {
                log.add(String.format(Locale.US, "  - Field %d [%s]: (EMV TLV Data - %d Bytes)", fieldNum, fVal.getSpec().getName(), fVal.getValue().length() / 2));
            } else {
                log.add(String.format(Locale.US, "  - Field %d [%s]: %s", fieldNum, fVal.getSpec().getName(), fVal.getValue()));
            }
        }

        log.add("\n-> Outbound Raw ISO 8583 Packet Hex Dump:\n" + result.requestIsoHex);

        // Step 2: Host Processing Simulation
        log.add("\n-------------------------------------------------------------------------");
        log.add(" [STAGE 3] ISSUER BANK HOST / PROCESSOR SIMULATION PROCESSING");
        log.add("-------------------------------------------------------------------------");

        IsoMessage unpackedReq = IsoMessageUnpacker.unpack(reqBytes);
        log.add("Host Unpacked ISO 8583 Packet Successfully (Unpacked OK).");
        log.add("Host Validated Primary Account Number (PAN): " + unpackedReq.getFieldValue(2));
        log.add("Host Validated Transaction Amount: " + String.format(Locale.US, "$%.2f", Long.parseLong(unpackedReq.getFieldValue(4)) / 100.0));

        String reqField55 = unpackedReq.getFieldValue(55);
        if (reqField55 != null && !reqField55.isEmpty()) {
            log.add("\nHost Inspecting Field 55 ICC Chip Payload (BER-TLV):");
            List<TlvElement> tlvList = TlvParser.parseHex(reqField55);
            for (TlvElement tlv : tlvList) {
                log.add(String.format(Locale.US, "  * Tag %s (%s): %s", tlv.getTagHex(), tlv.getEmvTag().getName(), tlv.getValueHex()));
            }
        }

        log.add("\nHost Validated ARQC Cryptogram Signature: VERIFIED SUCCESSFUL (ARQC OK)");
        log.add("Host Account Balance Check: SUFFICIENT FUNDS");
        log.add("Host Generated Authorization Code: 681042 | Response Code: 00 (Approved)");

        // Step 3: Host 0210 Response Creation
        String arpcHex = "3A4B5C6D7E8F9000";
        String respField55 = "910A" + arpcHex + "30308A023030";

        IsoMessage resp0210 = IsoSampleMessages.create0210FinancialResponse("681042", "00", respField55);
        resp0210.setField(2, emvResult.pan);
        resp0210.setField(4, String.format(Locale.US, "%012d", amountCents));
        resp0210.setField(11, req0200.getFieldValue(11));

        byte[] respBytes = IsoMessagePacker.pack(resp0210);
        result.responseIsoHex = HexUtils.bytesToHex(respBytes);

        log.add("\n-------------------------------------------------------------------------");
        log.add(" [STAGE 4] HOST -> POS TERMINAL: ISO 8583 RESPONSE MESSAGE (0210)");
        log.add("-------------------------------------------------------------------------");
        log.add("<- Inbound ISO 8583 0210 Response Message Hex Dump:\n" + result.responseIsoHex);

        log.add("\nISO 8583 0210 Response Data Elements:");
        log.add("  - Field 38 [Auth Code]: 681042");
        log.add("  - Field 39 [Response Code]: 00 (APPROVED)");
        log.add("  - Field 55 [Host EMV Response TLV]: Tag 91 ARPC (" + arpcHex + ") + Tag 8A ARC (00)");

        // Step 4: Terminal Second Generate AC
        log.add("\n-------------------------------------------------------------------------");
        log.add(" [STAGE 5] POS TERMINAL & EMV CARD SECOND GENERATE AC (2nd FLOW)");
        log.add("-------------------------------------------------------------------------");
        log.add("POS Terminal forwards Host Tag 91 (ARPC) and Tag 8A (00) to Card Chip:");
        log.add("-> APDU TX: 80AE4000 0A" + arpcHex + "3030 (2nd GENERATE AC - TC Request)");
        log.add("<- APDU RX: 80024000 (CID: 40 - TC Transaction Certificate Approved!)");
        log.add("-> Card Chip Validated Issuer Host Authentication & Recorded Transaction to Chip Memory.");

        log.add("\n=========================================================================");
        log.add(" TRANSACTION COMPLETED SUCCESSFULLY: APPROVED (AUTH CODE: 681042)");
        log.add("=========================================================================");

        result.statusSummary = "TRANSACTION APPROVED (AUTH CODE: 681042 | ARC: 00)";
        return result;
    }
}
