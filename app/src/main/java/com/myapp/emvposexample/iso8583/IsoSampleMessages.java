package com.myapp.emvposexample.iso8583;

public class IsoSampleMessages {

    public static IsoMessage create0100AuthRequest() {
        IsoMessage msg = new IsoMessage("0100");
        msg.setHeader("6000000000");
        msg.setField(2, "4543123456789012");
        msg.setField(3, "000000");
        msg.setField(4, "000000025000");
        msg.setField(7, "0926123045");
        msg.setField(11, "000123");
        msg.setField(12, "123045");
        msg.setField(13, "0926");
        msg.setField(14, "2812");
        msg.setField(18, "5411");
        msg.setField(22, "021");
        msg.setField(25, "00");
        msg.setField(35, "4543123456789012=2812201000000000");
        msg.setField(41, "TERM0001");
        msg.setField(42, "MERCHANT0000001");
        msg.setField(43, "KAFE ISTANBUL TR            ISTANBUL  TR");
        msg.setField(48, "POS PROMOTION CODE=DISCOUNT5");
        msg.setField(52, "1234567890ABCDEF");
        return msg;
    }

    public static IsoMessage create0200FinancialRequest(String field55EmvHex) {
        IsoMessage msg = new IsoMessage("0200");
        msg.setHeader("6000000000");
        msg.setField(2, "5412751234567890");
        msg.setField(3, "000000");
        msg.setField(4, "000000150000");
        msg.setField(7, "0926142010");
        msg.setField(11, "000456");
        msg.setField(12, "142010");
        msg.setField(13, "0926");
        msg.setField(14, "2812");
        msg.setField(18, "5812");
        msg.setField(22, "051");
        msg.setField(23, "001");
        msg.setField(25, "00");
        msg.setField(35, "5412751234567890D2812201000000000F");
        msg.setField(41, "POS85831");
        msg.setField(42, "MCH858300000001");
        msg.setField(43, "RESTORAN ANKARA TR          ANKARA    TR");

        if (field55EmvHex != null && !field55EmvHex.isEmpty()) {
            msg.setField(55, field55EmvHex);
        } else {
            msg.setField(55, "9F2608A1B2C3D4E5F678909F2701809F100706010A03A000009F3704123456789F360200A4950500000080009A032609259C01009F02060000001500005F2A02079282027C009F1A020792");
        }

        return msg;
    }

    public static IsoMessage create0210FinancialResponse(String authCode, String responseCode, String field55RespEmv) {
        IsoMessage msg = new IsoMessage("0210");
        msg.setHeader("6000000000");
        msg.setField(2, "5412751234567890");
        msg.setField(3, "000000");
        msg.setField(4, "000000150000");
        msg.setField(7, "0926142010");
        msg.setField(11, "000456");
        msg.setField(12, "142010");
        msg.setField(13, "0926");
        msg.setField(37, "202609260456");
        msg.setField(38, authCode != null ? authCode : "123456");
        msg.setField(39, responseCode != null ? responseCode : "00");
        msg.setField(41, "POS85831");
        msg.setField(42, "MCH858300000001");

        if (field55RespEmv != null && !field55RespEmv.isEmpty()) {
            msg.setField(55, field55RespEmv);
        } else {
            msg.setField(55, "910A3A4B5C6D7E8F900030308A023030");
        }

        return msg;
    }

    public static IsoMessage create0420ReversalAdvice() {
        IsoMessage msg = new IsoMessage("0420");
        msg.setHeader("6000000000");
        msg.setField(2, "5412751234567890");
        msg.setField(3, "000000");
        msg.setField(4, "000000150000");
        msg.setField(7, "0926142200");
        msg.setField(11, "000456");
        msg.setField(12, "142010");
        msg.setField(13, "0926");
        msg.setField(22, "051");
        msg.setField(37, "202609260456");
        msg.setField(39, "A0");
        msg.setField(41, "POS85831");
        msg.setField(42, "MCH858300000001");
        msg.setField(90, "020000045609261420100000000000");
        return msg;
    }

    public static IsoMessage create0800NetworkManagement() {
        IsoMessage msg = new IsoMessage("0800");
        msg.setHeader("6000000000");
        msg.setField(7, "0926150000");
        msg.setField(11, "000999");
        msg.setField(70, "301");
        return msg;
    }
}
