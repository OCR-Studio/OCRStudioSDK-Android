/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

/**
 * Unsuccessful NFC-verification data
 */
public class SessionNfcResultError implements SessionNfcResult{
    public final String error;

    SessionNfcResultError(String error){
        this.error = error;
    }

}
