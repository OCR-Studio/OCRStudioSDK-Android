/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.OCRStudioSDKResult;

public interface Callback {
  void initialized(boolean engine_initialized);
  void recognized(OCRStudioSDKResult result);
  void started();
  void stopped();
  void error(String message);
}
