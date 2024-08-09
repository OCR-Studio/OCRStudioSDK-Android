/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.*;

public class OCRStudioSDKDelegate {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public OCRStudioSDKDelegate(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(OCRStudioSDKDelegate obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jniocrstudiosdkJNI.delete_OCRStudioSDKDelegate(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    jniocrstudiosdkJNI.OCRStudioSDKDelegate_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    jniocrstudiosdkJNI.OCRStudioSDKDelegate_change_ownership(this, swigCPtr, true);
  }

  public void Callback(String json_message) {
    jniocrstudiosdkJNI.OCRStudioSDKDelegate_Callback(swigCPtr, this, json_message);
  }

  public OCRStudioSDKDelegate() {
    this(jniocrstudiosdkJNI.new_OCRStudioSDKDelegate(), true);
    jniocrstudiosdkJNI.OCRStudioSDKDelegate_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

}
