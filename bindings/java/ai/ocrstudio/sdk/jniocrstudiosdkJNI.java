/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ai.ocrstudio.sdk;

import ai.ocrstudio.sdk.*;

public class jniocrstudiosdkJNI {
  public final static native void delete_OCRStudioSDKDelegate(long jarg1);
  public final static native void OCRStudioSDKDelegate_Callback(long jarg1, OCRStudioSDKDelegate jarg1_, String jarg2);
  public final static native long new_OCRStudioSDKDelegate();
  public final static native void OCRStudioSDKDelegate_director_connect(OCRStudioSDKDelegate obj, long cptr, boolean mem_own, boolean weak_global);
  public final static native void OCRStudioSDKDelegate_change_ownership(OCRStudioSDKDelegate obj, long cptr, boolean take_or_release);
  public final static native void delete_OCRStudioSDKException(long jarg1);
  public final static native long new_OCRStudioSDKException__SWIG_0(String jarg1, String jarg2);
  public final static native long new_OCRStudioSDKException__SWIG_1(long jarg1, OCRStudioSDKException jarg1_);
  public final static native String OCRStudioSDKException_Type(long jarg1, OCRStudioSDKException jarg1_);
  public final static native String OCRStudioSDKException_Message(long jarg1, OCRStudioSDKException jarg1_);
  public final static native void delete_OCRStudioSDKString(long jarg1);
  public final static native long new_OCRStudioSDKString__SWIG_0();
  public final static native long new_OCRStudioSDKString__SWIG_1(String jarg1);
  public final static native long new_OCRStudioSDKString__SWIG_2(long jarg1, OCRStudioSDKString jarg1_);
  public final static native String OCRStudioSDKString_CStr(long jarg1, OCRStudioSDKString jarg1_);
  public final static native int OCRStudioSDKString_Size(long jarg1, OCRStudioSDKString jarg1_);
  public final static native int OCRSTUDIOSDK_PIXEL_FORMAT_G_get();
  public final static native int OCRSTUDIOSDK_YUV_FORMAT_NOT_SET_get();
  public final static native int OCRStudioSDKImage_PagesCount(String jarg1);
  public final static native long OCRStudioSDKImage_PageName(String jarg1, int jarg2);
  public final static native long OCRStudioSDKImage_CreateEmpty();
  public final static native long OCRStudioSDKImage_CreateFromFile__SWIG_0(String jarg1, int jarg2, int jarg3, int jarg4);
  public final static native long OCRStudioSDKImage_CreateFromFile__SWIG_1(String jarg1, int jarg2, int jarg3);
  public final static native long OCRStudioSDKImage_CreateFromFile__SWIG_2(String jarg1, int jarg2);
  public final static native long OCRStudioSDKImage_CreateFromFile__SWIG_3(String jarg1);
  public final static native long OCRStudioSDKImage_CreateFromFileBuffer__SWIG_0(byte[] jarg1, int jarg3, int jarg4, int jarg5);
  public final static native long OCRStudioSDKImage_CreateFromFileBuffer__SWIG_1(byte[] jarg1, int jarg3, int jarg4);
  public final static native long OCRStudioSDKImage_CreateFromFileBuffer__SWIG_2(byte[] jarg1, int jarg3);
  public final static native long OCRStudioSDKImage_CreateFromFileBuffer__SWIG_3(byte[] jarg1);
  public final static native long OCRStudioSDKImage_CreateFromBase64FileBuffer__SWIG_0(String jarg1, int jarg2, int jarg3, int jarg4);
  public final static native long OCRStudioSDKImage_CreateFromBase64FileBuffer__SWIG_1(String jarg1, int jarg2, int jarg3);
  public final static native long OCRStudioSDKImage_CreateFromBase64FileBuffer__SWIG_2(String jarg1, int jarg2);
  public final static native long OCRStudioSDKImage_CreateFromBase64FileBuffer__SWIG_3(String jarg1);
  public final static native long OCRStudioSDKImage_CreateFromPixelBuffer(byte[] jarg1, int jarg3, int jarg4, int jarg5, int jarg6, int jarg7);
  public final static native long OCRStudioSDKImage_CreateFromBuffer(byte[] jarg1, int jarg3, int jarg4, int jarg5, int jarg6);
  public final static native long OCRStudioSDKImage_CreateFromYUVSimple(byte[] jarg1, int jarg3, int jarg4);
  public final static native long OCRStudioSDKImage_CreateFromYUV(byte[] jarg1, int jarg3, int jarg4, byte[] jarg5, int jarg7, int jarg8, byte[] jarg9, int jarg11, int jarg12, int jarg13, int jarg14, int jarg15);
  public final static native void delete_OCRStudioSDKImage(long jarg1);
  public final static native long OCRStudioSDKImage_DeepCopy(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native long OCRStudioSDKImage_ShallowCopy(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native void OCRStudioSDKImage_Clear(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native int OCRStudioSDKImage_ExportPixelBufferLength(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native int OCRStudioSDKImage_ExportPixelBuffer(long jarg1, OCRStudioSDKImage jarg1_, byte[] jarg2);
  public final static native long OCRStudioSDKImage_ExportBase64JPEG(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native void OCRStudioSDKImage_Scale(long jarg1, OCRStudioSDKImage jarg1_, int jarg2, int jarg3);
  public final static native long OCRStudioSDKImage_DeepCopyScaled(long jarg1, OCRStudioSDKImage jarg1_, int jarg2, int jarg3);
  public final static native void OCRStudioSDKImage_CropByQuad(long jarg1, OCRStudioSDKImage jarg1_, String jarg2, int jarg3, int jarg4);
  public final static native long OCRStudioSDKImage_DeepCopyCroppedByQuad(long jarg1, OCRStudioSDKImage jarg1_, String jarg2, int jarg3, int jarg4);
  public final static native void OCRStudioSDKImage_CropByRect(long jarg1, OCRStudioSDKImage jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native long OCRStudioSDKImage_DeepCopyCroppedByRect(long jarg1, OCRStudioSDKImage jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native long OCRStudioSDKImage_ShallowCopyCroppedByRect(long jarg1, OCRStudioSDKImage jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native void OCRStudioSDKImage_RotateByNinety(long jarg1, OCRStudioSDKImage jarg1_, int jarg2);
  public final static native long OCRStudioSDKImage_DeepCopyRotatedByNinety(long jarg1, OCRStudioSDKImage jarg1_, int jarg2);
  public final static native int OCRStudioSDKImage_Width(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native int OCRStudioSDKImage_Height(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native int OCRStudioSDKImage_BytesPerLine(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native int OCRStudioSDKImage_Channels(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native boolean OCRStudioSDKImage_OwnsPixelData(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native void OCRStudioSDKImage_ForcePixelDataOwnership(long jarg1, OCRStudioSDKImage jarg1_);
  public final static native void delete_OCRStudioSDKItem(long jarg1);
  public final static native long OCRStudioSDKItem_DeepCopy(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native String OCRStudioSDKItem_Type(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native String OCRStudioSDKItem_Name(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native String OCRStudioSDKItem_Value(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native double OCRStudioSDKItem_Confidence(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native boolean OCRStudioSDKItem_Accepted(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native String OCRStudioSDKItem_Attributes(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native boolean OCRStudioSDKItem_HasImage(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native long OCRStudioSDKItem_Image(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native String OCRStudioSDKItem_Description(long jarg1, OCRStudioSDKItem jarg1_);
  public final static native void delete_OCRStudioSDKItemIterator(long jarg1);
  public final static native long new_OCRStudioSDKItemIterator(long jarg1, OCRStudioSDKItemIterator jarg1_);
  public final static native boolean OCRStudioSDKItemIterator_IsEqualTo(long jarg1, OCRStudioSDKItemIterator jarg1_, long jarg2, OCRStudioSDKItemIterator jarg2_);
  public final static native long OCRStudioSDKItemIterator_Next(long jarg1, OCRStudioSDKItemIterator jarg1_);
  public final static native void OCRStudioSDKItemIterator_Step(long jarg1, OCRStudioSDKItemIterator jarg1_);
  public final static native String OCRStudioSDKItemIterator_Key(long jarg1, OCRStudioSDKItemIterator jarg1_);
  public final static native long OCRStudioSDKItemIterator_Item(long jarg1, OCRStudioSDKItemIterator jarg1_);
  public final static native void delete_OCRStudioSDKTarget(long jarg1);
  public final static native long OCRStudioSDKTarget_DeepCopy(long jarg1, OCRStudioSDKTarget jarg1_);
  public final static native String OCRStudioSDKTarget_Description(long jarg1, OCRStudioSDKTarget jarg1_);
  public final static native int OCRStudioSDKTarget_ItemsCountByType(long jarg1, OCRStudioSDKTarget jarg1_, String jarg2);
  public final static native boolean OCRStudioSDKTarget_HasItem(long jarg1, OCRStudioSDKTarget jarg1_, String jarg2, String jarg3);
  public final static native long OCRStudioSDKTarget_Item(long jarg1, OCRStudioSDKTarget jarg1_, String jarg2, String jarg3);
  public final static native long OCRStudioSDKTarget_ItemsBegin(long jarg1, OCRStudioSDKTarget jarg1_, String jarg2);
  public final static native long OCRStudioSDKTarget_ItemsEnd(long jarg1, OCRStudioSDKTarget jarg1_, String jarg2);
  public final static native boolean OCRStudioSDKTarget_IsFinal(long jarg1, OCRStudioSDKTarget jarg1_);
  public final static native void delete_OCRStudioSDKResult(long jarg1);
  public final static native long OCRStudioSDKResult_DeepCopy(long jarg1, OCRStudioSDKResult jarg1_);
  public final static native int OCRStudioSDKResult_TargetsCount(long jarg1, OCRStudioSDKResult jarg1_);
  public final static native long OCRStudioSDKResult_TargetByIndex(long jarg1, OCRStudioSDKResult jarg1_, int jarg2);
  public final static native boolean OCRStudioSDKResult_AllTargetsFinal(long jarg1, OCRStudioSDKResult jarg1_);
  public final static native void delete_OCRStudioSDKSession(long jarg1);
  public final static native String OCRStudioSDKSession_Description(long jarg1, OCRStudioSDKSession jarg1_);
  public final static native void OCRStudioSDKSession_ProcessImage(long jarg1, OCRStudioSDKSession jarg1_, long jarg2, OCRStudioSDKImage jarg2_);
  public final static native void OCRStudioSDKSession_ProcessData(long jarg1, OCRStudioSDKSession jarg1_, String jarg2);
  public final static native long OCRStudioSDKSession_CurrentResult(long jarg1, OCRStudioSDKSession jarg1_);
  public final static native void OCRStudioSDKSession_Reset(long jarg1, OCRStudioSDKSession jarg1_);
  public final static native long OCRStudioSDKInstance_CreateStandalone__SWIG_0(String jarg1);
  public final static native long OCRStudioSDKInstance_CreateStandalone__SWIG_1();
  public final static native long OCRStudioSDKInstance_CreateFromPath__SWIG_0(String jarg1, String jarg2);
  public final static native long OCRStudioSDKInstance_CreateFromPath__SWIG_1(String jarg1);
  public final static native long OCRStudioSDKInstance_CreateFromBuffer__SWIG_0(byte[] jarg1, String jarg3);
  public final static native long OCRStudioSDKInstance_CreateFromBuffer__SWIG_1(byte[] jarg1);
  public final static native String OCRStudioSDKInstance_LibraryVersion();
  public final static native void delete_OCRStudioSDKInstance(long jarg1);
  public final static native String OCRStudioSDKInstance_Description(long jarg1, OCRStudioSDKInstance jarg1_);
  public final static native long OCRStudioSDKInstance_CreateSession__SWIG_0(long jarg1, OCRStudioSDKInstance jarg1_, String jarg2, String jarg3, long jarg4, OCRStudioSDKDelegate jarg4_);
  public final static native long OCRStudioSDKInstance_CreateSession__SWIG_1(long jarg1, OCRStudioSDKInstance jarg1_, String jarg2, String jarg3);

  public static void SwigDirector_OCRStudioSDKDelegate_Callback(OCRStudioSDKDelegate jself, String json_message) {
    jself.Callback(json_message);
  }

  private final static native void swig_module_init();
  static {
    swig_module_init();
  }
}
