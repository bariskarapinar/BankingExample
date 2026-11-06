package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.emv.TlvElement;
import com.myapp.emvposexample.emv.TlvParser;

import java.util.List;
import java.util.Locale;

public class EmvTlvViewModel extends ViewModel {

    private final MutableLiveData<List<TlvElement>> tlvElements = new MutableLiveData<>();
    private final MutableLiveData<String> parsedCountText = new MutableLiveData<>();

    public LiveData<List<TlvElement>> getTlvElements() {
        return tlvElements;
    }

    public LiveData<String> getParsedCountText() {
        return parsedCountText;
    }

    public void parseTlv(String hexInput) {
        if (hexInput == null) hexInput = "";
        List<TlvElement> elements = TlvParser.parseHex(hexInput);
        tlvElements.setValue(elements);
        parsedCountText.setValue(String.format(Locale.US, "Parsed Elements Count: %d", elements.size()));
    }
}
