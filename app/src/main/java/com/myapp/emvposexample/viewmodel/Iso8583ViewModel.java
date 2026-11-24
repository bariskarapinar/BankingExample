package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.iso8583.IsoMessage;
import com.myapp.emvposexample.iso8583.IsoMessagePacker;
import com.myapp.emvposexample.iso8583.IsoMessageUnpacker;
import com.myapp.emvposexample.iso8583.IsoMtiDecoder;
import com.myapp.emvposexample.iso8583.IsoSampleMessages;

/**
 * Educational Architecture ViewModel: ISO 8583 Message Packing & Inspector State Manager.
 *
 * <p>Manages reactive LiveData state for ISO 8583 message serialization, deserialization,
 * preset selection, and primary/secondary bitmap inspection.</p>

 * @author Android Architecture & ViewModel Group
 */
public class Iso8583ViewModel extends ViewModel {

    // Reactive MutableLiveData holding packed hexadecimal ISO 8583 payload
    private final MutableLiveData<String> packedHex = new MutableLiveData<>();
    // Reactive MutableLiveData holding unpacked object-oriented IsoMessage model
    private final MutableLiveData<IsoMessage> unpackedMessage = new MutableLiveData<>();
    // Reactive MutableLiveData holding MTI and Bitmap header summary text
    private final MutableLiveData<String> headerSummary = new MutableLiveData<>();

    /**
     * Returns observable LiveData stream of packed hexadecimal ISO 8583 payload.
     *
     * @return Observable LiveData hex string.
     */
    public LiveData<String> getPackedHex() {
        return packedHex;
    }

    /**
     * Returns observable LiveData stream of unpacked {@link IsoMessage} model.
     *
     * @return Observable LiveData ISO message model.
     */
    public LiveData<IsoMessage> getUnpackedMessage() {
        return unpackedMessage;
    }

    /**
     * Returns observable LiveData stream of MTI &amp; Bitmap header summary string.
     *
     * @return Observable LiveData header summary string.
     */
    public LiveData<String> getHeaderSummary() {
        return headerSummary;
    }

    /**
     * Applies a pre-configured sample ISO 8583 message template and triggers unpacking.
     *
     * @param position Selected preset index (0 = 0200, 1 = 0210, 2 = 0100, 3 = 0420, 4 = 0800).
     */
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

        // Step 1: Pack message model into raw hex string
        String hex = IsoMessagePacker.packToHex(msg);
        packedHex.setValue(hex);
        // Step 2: Unpack hex string to update UI inspectors
        unpackHex(hex);
    }

    /**
     * Deserializes a raw hexadecimal ISO 8583 stream and posts updated LiveData state.
     *
     * @param hex Raw upper or lower-case ISO 8583 hex payload string.
     */
    public void unpackHex(String hex) {
        if (hex == null) hex = "";
        // Step 1: Execute deserialization
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        unpackedMessage.setValue(msg);

        // Step 2: Decode MTI classification
        IsoMtiDecoder.MtiInfo mtiInfo = IsoMtiDecoder.decode(msg.getMti());
        String summary = "MTI: " + mtiInfo.getFullDescriptionEn() + "\nPrimary Bitmap: " + msg.getBitmap().toHex() + " (" + (msg.getBitmap().hasSecondaryBitmap() ? "128 Bit" : "64 Bit") + ")";
        headerSummary.setValue(summary);
    }
}
