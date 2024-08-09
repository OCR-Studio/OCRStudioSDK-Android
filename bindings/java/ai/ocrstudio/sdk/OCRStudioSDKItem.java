/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.*;

public class OCRStudioSDKItem {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public OCRStudioSDKItem(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(OCRStudioSDKItem obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jniocrstudiosdkJNI.delete_OCRStudioSDKItem(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OCRStudioSDKItem DeepCopy() {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKItem_DeepCopy(swigCPtr, this);
    return (cPtr == 0) ? null : new OCRStudioSDKItem(cPtr, false);
  }

  public String Type() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Type(swigCPtr, this);
  }

  public String Name() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Name(swigCPtr, this);
  }

  public String Value() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Value(swigCPtr, this);
  }

  public double Confidence() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Confidence(swigCPtr, this);
  }

  public boolean Accepted() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Accepted(swigCPtr, this);
  }

  public String Attributes() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Attributes(swigCPtr, this);
  }

  public boolean HasImage() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_HasImage(swigCPtr, this);
  }

  public OCRStudioSDKImage Image() {
    return new OCRStudioSDKImage(jniocrstudiosdkJNI.OCRStudioSDKItem_Image(swigCPtr, this), false);
  }

  public String Description() {
    return jniocrstudiosdkJNI.OCRStudioSDKItem_Description(swigCPtr, this);
  }

}