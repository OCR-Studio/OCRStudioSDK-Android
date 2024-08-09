/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.*;

public class OCRStudioSDKTarget {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public OCRStudioSDKTarget(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(OCRStudioSDKTarget obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jniocrstudiosdkJNI.delete_OCRStudioSDKTarget(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OCRStudioSDKTarget DeepCopy() {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKTarget_DeepCopy(swigCPtr, this);
    return (cPtr == 0) ? null : new OCRStudioSDKTarget(cPtr, true);
  }

  public String Description() {
    return jniocrstudiosdkJNI.OCRStudioSDKTarget_Description(swigCPtr, this);
  }

  public int ItemsCountByType(String item_type) {
    return jniocrstudiosdkJNI.OCRStudioSDKTarget_ItemsCountByType(swigCPtr, this, item_type);
  }

  public boolean HasItem(String item_type, String item_name) {
    return jniocrstudiosdkJNI.OCRStudioSDKTarget_HasItem(swigCPtr, this, item_type, item_name);
  }

  public OCRStudioSDKItem Item(String item_type, String item_name) {
    return new OCRStudioSDKItem(jniocrstudiosdkJNI.OCRStudioSDKTarget_Item(swigCPtr, this, item_type, item_name), false);
  }

  public OCRStudioSDKItemIterator ItemsBegin(String item_type) {
    return new OCRStudioSDKItemIterator(jniocrstudiosdkJNI.OCRStudioSDKTarget_ItemsBegin(swigCPtr, this, item_type), true);
  }

  public OCRStudioSDKItemIterator ItemsEnd(String item_type) {
    return new OCRStudioSDKItemIterator(jniocrstudiosdkJNI.OCRStudioSDKTarget_ItemsEnd(swigCPtr, this, item_type), true);
  }

  public boolean IsFinal() {
    return jniocrstudiosdkJNI.OCRStudioSDKTarget_IsFinal(swigCPtr, this);
  }

}
