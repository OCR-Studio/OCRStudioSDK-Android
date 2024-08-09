/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.content.Context;
import android.graphics.Bitmap;

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
            Label.getInstance().message.set("Wait...");
            // 1. Get engine instance
            OCRStudioSDKInstance engine = Engine.getInstance();
            // 2. Create new session params
            JSONObject sessionParamsJson = new JSONObject();
            // 2.1 Set session_type
            // session_type: "document_recognition" for recognition by one frame
            sessionParamsJson.put("session_type", "document_recognition");
            // 2.2 Set target_group_type
            sessionParamsJson.put("target_group_type", SettingsStore.currentMode);
            // 2.3 Set target_masks
            sessionParamsJson.put("target_masks", new JSONArray(SettingsStore.currentMask));

            // 2.4 Set custom options
            JSONObject optionsJson = new JSONObject();
            Map<String, String> map = SettingsStore.options;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                optionsJson.put(key, value);
            }
            // Adding session options
            sessionParamsJson.put("options", optionsJson);

            String sessionParams = sessionParamsJson.toString();

            // 3. Spawn recognition session
            OCRStudioSDKSession session = engine.CreateSession(SettingsStore.signature, sessionParams);

            // Prepare for FromBufferExtended()
            byte[] bytes = bitmapToByteArray(imageData);
            int stride = imageData.getRowBytes();
            int height = imageData.getHeight();
            int width = imageData.getWidth();

            // Bitmap.getConfig() return ARGB_8888 pixel format. The channel order of ARGB_8888 is RGBA!
            OCRStudioSDKImage image = OCRStudioSDKImage.CreateFromPixelBuffer(bytes, width, height, stride, 1, OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);
            session.ProcessImage(image);
            OCRStudioSDKResult result = session.CurrentResult();
            ResultStore.instance.addResult(result);

            // 4. Reset session
            session.Reset();
        } catch (Exception e) {
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
