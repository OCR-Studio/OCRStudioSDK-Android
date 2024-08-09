/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

/**
 * FACE COMPARE VIEW LOGIC IMPLEMENTATION
 * TO USE AS A PART OF A VIEWMODEL
 */
public class FacesModel {
    private final String TAG = "myapp.FacesModel";

    //----------------------------------------------------------------------------------------------
    // DATA
    public static class FacesData{
        public final Bitmap faceA;
        public final Bitmap faceB;
        public FacesData(Bitmap faceA, Bitmap faceB){
            this.faceA = faceA;
            this.faceB = faceB;
        }
    }
    public MutableLiveData<FacesData> facesData = new MutableLiveData<>(new FacesData(null, null));


    //----------------------------------------------------------------------------------------------
    // INITIALIZATION
    /** CONSTRUCTOR  */
    public FacesModel(){
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
        Log.d(TAG, "clear");
        facesData.setValue(new FacesData(null,null));
    }

    public void setFaceA(Bitmap faceA){
        Log.d(TAG, "setFaceA");
        facesData.postValue(new FacesData(
            faceA,
            facesData.getValue().faceB
        ));
    }
    public void setFaceB(Bitmap faceB){
        Log.d(TAG, "setFaceB");
        facesData.postValue(new FacesData(
            facesData.getValue().faceA,
            faceB
        ));
    }

}
