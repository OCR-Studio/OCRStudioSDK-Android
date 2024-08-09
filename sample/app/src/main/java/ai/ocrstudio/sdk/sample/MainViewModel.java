/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import ai.ocrstudio.sdk.sample.nfc.NfcModel;
import ai.ocrstudio.sdk.sample.nfc.NfcState;

/**
 * View model for MainActivity
 * consists of three parts:
 *    - result model
 *    - nfc model
 *    - faces model
 */
public class MainViewModel extends ViewModel {
    private final String TAG = "myapp.MainViewModel";

    // Image recognition result visible fields
    final ResultModel result = new ResultModel();

    // NFC-logic
    final NfcModel nfc = new NfcModel();

    // Faces compare logic
    final FacesModel faces = new FacesModel();

    // CONSTRUCTOR
    public MainViewModel(){
        Log.d(TAG, "constructor");
        initSettings();
        result.resultData.observeForever(this::onResultDataChanged);
        nfc.nfcState.observeForever(this::onNfcStateChanged);
    }
    private void initSettings() {
        String signature = "INSERT_SIGNATURE_HERE from doc\README.md";
        SettingsStore.SetSignature(signature);
    }

    // DESTRUCTOR
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "destructor - onCleared");
        nfc.onCleared();
        result.onCleared();
        faces.onCleared();
    }

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public void clearResult(Activity activity){
        // Clear the result
        result.clear();
        // Reset NFC
        nfc.close(activity);
        // Reset state of UI
        faces.clear();
        // Delete Session
        Session.instance.delete();
    }

    private void onResultDataChanged(@NonNull ResultModel.ResultData resultData) {
        Log.w(TAG, "onResultDataChanged " + resultData);
        // Set the result photo as a fist face to compare
        faces.setFaceA(resultData.photo);
    }

    private void onNfcStateChanged(NfcState nfcState) {
        Log.d(TAG, "onNfcStateChanged " + nfcState);
        if(nfcState==NfcState.Success){
            // Add data to result
            result.appendNfcResult(nfc.passportData, nfc.verificationResult);
        }
    }

}
