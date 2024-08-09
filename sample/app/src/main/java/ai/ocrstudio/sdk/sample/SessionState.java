/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

/**
 * Possible states of Session
 */
public enum SessionState {
    Empty,      // session is not created
    Creating,   // creating could take a long time
    Created,    // just created (no result)
    Processing, // processing image
    Finished,   // finished with result (successful or not)
    Error       // Fatal error
}
