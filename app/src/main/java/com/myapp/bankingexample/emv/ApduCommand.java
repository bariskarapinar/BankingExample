package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

public class ApduCommand {

    private final byte cla;
    private final byte ins;
    private final byte p1;
    private final byte p2;
    private final byte[] data;
    private final Integer le;
    private final String description;

    public ApduCommand(byte cla, byte ins, byte p1, byte p2, byte[] data, Integer le, String description) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.data = data != null ? data : new byte[0];
        this.le = le;
        this.description = description;
    }

    public byte getCla() {
        return cla;
    }

    public byte getIns() {
        return ins;
    }

    public byte getP1() {
        return p1;
    }

    public byte getP2() {
        return p2;
    }

    public byte[] getData() {
        return data;
    }

    public Integer getLe() {
        return le;
    }

    public String getDescription() {
        return description;
    }

    public byte[] toBytes() {
        int len = 4 + (data.length > 0 ? 1 + data.length : 0) + (le != null ? 1 : 0);
        byte[] bytes = new byte[len];
        int pos = 0;
        bytes[pos++] = cla;
        bytes[pos++] = ins;
        bytes[pos++] = p1;
        bytes[pos++] = p2;

        if (data.length > 0) {
            bytes[pos++] = (byte) data.length;
            System.arraycopy(data, 0, bytes, pos, data.length);
            pos += data.length;
        }

        if (le != null) {
            bytes[pos] = (byte) (le & 0xFF);
        }

        return bytes;
    }

    public String toHex() {
        return HexUtils.bytesToHex(toBytes());
    }
}
