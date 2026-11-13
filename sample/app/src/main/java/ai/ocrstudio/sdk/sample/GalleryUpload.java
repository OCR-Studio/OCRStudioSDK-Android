/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;
import android.util.Log;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKPixelFormat;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;

public class GalleryUpload {
    public void getResultFromGallery(Bitmap imageData) {
        try {
            // Prepare for FromBufferExtended()
            byte[] bytes = bitmapToByteArray(imageData);
            int stride = imageData.getRowBytes();
            int height = imageData.getHeight();
            int width = imageData.getWidth();

            // Bitmap.getConfig() return ARGB_8888 pixel format. The channel order of ARGB_8888 is RGBA!
            OCRStudioSDKImage image = OCRStudioSDKImage.CreateFromPixelBuffer(bytes, width, height, stride, 1, OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);

            Session.instance.processImage(image);

        } catch (Exception e) {
            Log.e("Gallery", e.toString());
            throw new RuntimeException(e);
        }
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        return byteBuffer.array();
    }
}
