package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.iso8583.IsoMessage;
import com.myapp.emvposexample.iso8583.IsoMessagePacker;
import com.myapp.emvposexample.iso8583.IsoMessageUnpacker;
import com.myapp.emvposexample.iso8583.IsoMtiDecoder;
import com.myapp.emvposexample.iso8583.IsoSampleMessages;

public class Iso8583ViewModel extends ViewModel {

    private final MutableLiveData<String> packedHex = new MutableLiveData<>();
    private final MutableLiveData<IsoMessage> unpackedMessage = new MutableLiveData<>();
    private final MutableLiveData<String> headerSummary = new MutableLiveData<>();

    public LiveData<String> getPackedHex() {
        return packedHex;
    }

    public LiveData<IsoMessage> getUnpackedMessage() {
        return unpackedMessage;
    }

    public LiveData<String> getHeaderSummary() {
        return headerSummary;
    }

    public void applyPreset(int position) {
        IsoMessage msg;
        switch (position) {
            case 0:
                msg = IsoSampleMessages.create0200FinancialRequest(null);
                break;
            case 1:
                msg = IsoSampleMessages.create0210FinancialResponse("123456", "00", null);
                break;
            case 2:
                msg = IsoSampleMessages.create0100AuthRequest();
                break;
            case 3:
                msg = IsoSampleMessages.create0420ReversalAdvice();
                break;
            default:
                msg = IsoSampleMessages.create0800NetworkManagement();
                break;
        }

        String hex = IsoMessagePacker.packToHex(msg);
        packedHex.setValue(hex);
        unpackHex(hex);
    }

    public void unpackHex(String hex) {
        if (hex == null) hex = "";
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        unpackedMessage.setValue(msg);

        IsoMtiDecoder.MtiInfo mtiInfo = IsoMtiDecoder.decode(msg.getMti());
        String summary = "MTI: " + mtiInfo.getFullDescriptionTr() + "\nPrimary Bitmap: " + msg.getBitmap().toHex() + " (" + (msg.getBitmap().hasSecondaryBitmap() ? "128 Bit" : "64 Bit") + ")";
        headerSummary.setValue(summary);
    }
}
