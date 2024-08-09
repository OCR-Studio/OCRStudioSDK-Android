/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.*;

public class OCRStudioSDKInstance {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public OCRStudioSDKInstance(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(OCRStudioSDKInstance obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jniocrstudiosdkJNI.delete_OCRStudioSDKInstance(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public static OCRStudioSDKInstance CreateStandalone(String json_instance_init_params) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateStandalone__SWIG_0(json_instance_init_params);
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static OCRStudioSDKInstance CreateStandalone() {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateStandalone__SWIG_1();
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static OCRStudioSDKInstance CreateFromPath(String configuration_filename, String json_instance_init_params) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateFromPath__SWIG_0(configuration_filename, json_instance_init_params);
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static OCRStudioSDKInstance CreateFromPath(String configuration_filename) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateFromPath__SWIG_1(configuration_filename);
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static OCRStudioSDKInstance CreateFromBuffer(byte[] configuration_buffer, String json_instance_init_params) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateFromBuffer__SWIG_0(configuration_buffer, json_instance_init_params);
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static OCRStudioSDKInstance CreateFromBuffer(byte[] configuration_buffer) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateFromBuffer__SWIG_1(configuration_buffer);
    return (cPtr == 0) ? null : new OCRStudioSDKInstance(cPtr, true);
  }

  public static String LibraryVersion() {
    return jniocrstudiosdkJNI.OCRStudioSDKInstance_LibraryVersion();
  }

  public String Description() {
    return jniocrstudiosdkJNI.OCRStudioSDKInstance_Description(swigCPtr, this);
  }

  public OCRStudioSDKSession CreateSession(String authorization_signature, String json_session_params, OCRStudioSDKDelegate callback_delegate) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateSession__SWIG_0(swigCPtr, this, authorization_signature, json_session_params, OCRStudioSDKDelegate.getCPtr(callback_delegate), callback_delegate);
    return (cPtr == 0) ? null : new OCRStudioSDKSession(cPtr, true);
  }

  public OCRStudioSDKSession CreateSession(String authorization_signature, String json_session_params) {
    long cPtr = jniocrstudiosdkJNI.OCRStudioSDKInstance_CreateSession__SWIG_1(swigCPtr, this, authorization_signature, json_session_params);
    return (cPtr == 0) ? null : new OCRStudioSDKSession(cPtr, true);
  }

}