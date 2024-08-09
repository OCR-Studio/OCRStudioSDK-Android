/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import ai.ocrstudio.sdk.sample.nfc.FaceImageError;
import ai.ocrstudio.sdk.sample.nfc.FaceImageSuccess;
import ai.ocrstudio.sdk.sample.nfc.PassportData;
import ai.ocrstudio.sdk.sample.nfc.PassportKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RECOGNITION RESULT VIEW LOGIC IMPLEMENTATION
 * TO USE AS A PART OF A VIEWMODEL
 */

public class ResultModel {
    private final String TAG = "myapp.ResultModel";

    //----------------------------------------------------------------------------------------------
    // DATA
    public static class ResultData {
        public String docType = "";
        public Bitmap photo = null;
        public PassportKey passportKey = null;
        // Items
        public ArrayList<ResultItem> mData = new ArrayList<>();// data items

        public boolean isEmpty() { return mData.isEmpty(); }

        public void addItem(ResultItem item) {
            mData.add(item);
        }

        // COPY CONSTRUCTOR
        public ResultData copy(){
            ResultData data = new ResultData();
            data.docType    = docType;
            data.photo      = photo;
            data.passportKey= passportKey;
            data.mData      = mData;
            return data;
        }
    }
    public MutableLiveData<ResultData> resultData = new MutableLiveData<>(new ResultData());


    //----------------------------------------------------------------------------------------------
    // INITIALIZATION
    /** CONSTRUCTOR  */
    public ResultModel(){
        Log.d(TAG, "constructor");

    }
    /** DESTRUCTOR
     * must be called in ViewModel.onCleared() */
    public void onCleared() {
        Log.d(TAG, "destructor onCleared");
    }

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public void clear(){
        Log.e(TAG, "--- clear result");
        // Update live data
        resultData.setValue(new ResultData());
    }

    /**
     *  Loads a successful document recognition result from ResultStore
     *  so ResultStore must be filled
     */
    public void loadResult(){
        Log.d(TAG, "loadResult");
        ResultData data = new ResultData();

        // Get data from ResultStore
        Map<String, ResultStore.FieldInfo> fields = ResultStore.instance.getFields();
        Map<String, ResultStore.FieldInfo> images = ResultStore.instance.getImages();

        // Get docType
        data.docType = ResultStore.instance.getType();

        // Check if document found
        if (data.docType.isEmpty()) {
            data.docType = "Document not found";
        }else {

            // Add first section to result view
            data.addItem(new ResultItemSection(ResultStore.instance.getType()));

            // Add fields to data
            for (Map.Entry<String, ResultStore.FieldInfo> set : fields.entrySet()) {
                data.addItem(new ResultItemField(set.getKey(), set.getValue()));
            }

            // Add images to data
            if (!images.isEmpty()) {
                data.addItem(new ResultItemSection("Images"));
                for (Map.Entry<String, ResultStore.FieldInfo> set : images.entrySet()) {
                    data.addItem(new ResultItemImage(set.getKey(), set.getValue()));
                }
            }

            // PHOTO
            if (images.containsKey("photo")) {
                // Fill document photo
                String faceAObject = Objects.requireNonNull(images.get("photo")).value;
                byte[] bytes = Base64.decode((String) faceAObject, Base64.DEFAULT);
                data.photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }else{
                data.photo = null;
            }

            // PASSPORT KEY
            try {
                data.passportKey = new PassportKey(
                    fields.get("mrz_number").value,
                    PassportKey.dateFrom_DD_MM_YYYY(
                        fields.get("mrz_expiry_date").value),
                    PassportKey.dateFrom_DD_MM_YYYY(
                        fields.get("mrz_birth_date").value)
                );
                Log.d(TAG,data.passportKey.toString());
            }catch (Exception e){
                Log.e(TAG,"MRZ parsing error "+e.getMessage());
                data.passportKey = null;
            }

        }

        // Update live data
        resultData.setValue(data);
    }

    /**
     *  Appends a successful nfc-reading
     */
    public void appendNfcResult(PassportData passportData, SessionNfcResult verificationResult){
        Log.e(TAG,"appendNfcResult");
        if(passportData==null) return;
        // Get current data
        ResultData data = Objects.requireNonNull(resultData.getValue()).copy();

        // APPEND PASSPORT DATA
        data.addItem(new ResultItemSection("NFC Data"));
        // MRZ info
        data.addItem(new ResultItemField(
                "MRZ",
                passportData.mrzInfo.toString(),
                null, null
        ));


        // Face Images
        passportData.faceImages.forEach(faceImage -> {
            // SUCCESSFUL IMAGES
            if(faceImage instanceof FaceImageSuccess) {
                HashMap<String,String> attr = new HashMap<>();
                ResultItemImage item = new ResultItemImage(
                        "NFC-photo",
                        ((FaceImageSuccess) faceImage).bitmap,
                        attr
                );
                attr.put("size", item.bitmap.getWidth()+"x"+item.bitmap.getHeight());
                data.addItem(item);
            }
            // INCORRECT IMAGES
            if(faceImage instanceof FaceImageError) {
                data.addItem(new ResultItemField(
                        "NFC-photo",
                        ((FaceImageError) faceImage).error,
                        null,
                        null
                ));
            }
        });

        // APPEND VERIFICATION RESULT
        if(verificationResult!=null){
            data.addItem(new ResultItemSection("NFC Verification"));
            // SUCCESS
            if(verificationResult instanceof SessionNfcResultSuccess) {
                SessionNfcResultSuccess result = (SessionNfcResultSuccess) verificationResult;
                // MRZ check
                data.addItem(
                        new ResultItemField(
                                "Data fraud attempt",
                                formatFraudTestValue(result.dataCheck),// value
                                result.dataCheck!=null,//accepted
                                null
                        )
                );
                // PHOTO check
                data.addItem(
                        new ResultItemField(
                                "Face fraud attempt",
                                formatFraudTestValue(result.faceCheck),// value
                                result.faceCheck!=null,//accepted
                                null
                        )
                );

            }
            // ERROR
            if(verificationResult instanceof SessionNfcResultError) {
                SessionNfcResultError result = (SessionNfcResultError) verificationResult;
                data.addItem(
                    new ResultItemField(
                            "Error",
                            result.error,// value
                            null,//accepted
                            null
                    )
                );
            }
        }

        // Update data state
        resultData.setValue(data);
    }

    private static String formatFraudTestValue(String value){
        if(value==null)return "test skipped";
        return value;
    }

}
