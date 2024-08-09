/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample.nfc;

public class FaceImageError implements FaceImage{
    public final String error;

    public FaceImageError(String error){
        this.error = error;
    }
}
