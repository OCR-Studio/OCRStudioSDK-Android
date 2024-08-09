/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample.nfc;

//----------------------------------------------------------------------------------------------
// DATA
public enum NfcState {
    Disabled,
    Waiting,    // waiting for nfc-tag
    Reading,    // reading data from nfc tag
    Checking,   // checking nfc data by the engine
    Stopping,   // stopping the nfc-thread
    Error,
    Success,
    NotSupported
}
