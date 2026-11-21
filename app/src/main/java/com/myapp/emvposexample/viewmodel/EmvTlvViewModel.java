package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.emv.TlvElement;
import com.myapp.emvposexample.emv.TlvParser;

import java.util.List;
import java.util.Locale;

/**
 * Educational Architecture ViewModel: EMV BER-TLV Parser Reactive State Manager.
 *
 * <p>Manages reactive LiveData streams for the BER-TLV Decoder tab. Encapsulates BER-TLV parsing logic
 * off the main UI thread, preserving state across Android configuration changes (screen rotations).</p>
 *
 * @author Android Architecture & ViewModel Group
 */
public class EmvTlvViewModel extends ViewModel {

    // Reactive MutableLiveData holding parsed BER-TLV tree nodes
    private final MutableLiveData<List<TlvElement>> tlvElements = new MutableLiveData<>();
    // Reactive MutableLiveData holding total parsed element count summary string
    private final MutableLiveData<String> parsedCountText = new MutableLiveData<>();

    /**
     * Returns observable LiveData stream of parsed {@link TlvElement} nodes.
     *
     * @return Observable LiveData list of parsed BER-TLV elements.
     */
    public LiveData<List<TlvElement>> getTlvElements() {
        return tlvElements;
    }

    /**
     * Returns observable LiveData stream of parsed element count summary string.
     *
     * @return Observable LiveData count summary string.
     */
    public LiveData<String> getParsedCountText() {
        return parsedCountText;
    }

    /**
     * Parses raw hexadecimal payload string and posts updated LiveData state to observers.
     *
     * @param hexInput Upper or lower-case hex payload string.
     */
    public void parseTlv(String hexInput) {
        if (hexInput == null) hexInput = "";
        // Step 1: Execute recursive BER-TLV parsing
        List<TlvElement> elements = TlvParser.parseHex(hexInput);
        // Step 2: Post parsed nodes to UI observers
        tlvElements.setValue(elements);
        // Step 3: Update parsed element count string
        parsedCountText.setValue(String.format(Locale.US, "Parsed Elements Count: %d", elements.size()));
    }
}
