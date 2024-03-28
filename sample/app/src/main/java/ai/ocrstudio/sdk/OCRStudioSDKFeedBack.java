/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.OCRStudioSDKDelegate;

public class OCRStudioSDKFeedBack extends OCRStudioSDKDelegate {
    OCRStudioSDKCallback ocrStudioSdkCallback;

    public OCRStudioSDKFeedBack(OCRStudioSDKCallback ocrStudioSdkCallback_) {
        ocrStudioSdkCallback = ocrStudioSdkCallback_;
    }

    public void Callback(String json_message) {
        ocrStudioSdkCallback.visualizationReceived(json_message);
    }
}
