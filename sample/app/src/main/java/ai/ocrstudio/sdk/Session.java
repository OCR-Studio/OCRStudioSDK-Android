/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import ai.ocrstudio.sdk.OCRStudioSDKDelegate;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Session {
    public static OCRStudioSDKSession session;
    public static OCRStudioSDKFeedBack ocrStudioSdkFeedBack;

    public boolean isRoi;
    public static long startTime;

    public void initSession(Context context, Callback callback, OCRStudioSDKCallback ocrStudioSdkCallback) {

        ExecutorService SessionExecutor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        SessionExecutor.execute(() -> {
            try {
                Label.getInstance().message.set("Wait...");

                // Benchmark
                startTime = System.nanoTime();

                // 1. Get engine instance
                OCRStudioSDKInstance engine = Engine.getInstance(context);
                /// 2. Create new session params
                JSONObject sessionParamsJson = new JSONObject();
                // 2.1 Set session_type
                // session_type: "document_recognition" for recognition by one frame
                sessionParamsJson.put("session_type", "video_recognition");
                // 2.2 Set target_group_type
                sessionParamsJson.put("target_group_type", SettingsStore.currentMode);
                // 2.3 Set target_masks
                sessionParamsJson.put("target_masks", new JSONArray(SettingsStore.currentMask));
                // 2.4 Set field_geometry to draw quads during recognition
                sessionParamsJson.put("output_modes", "field_geometry");
                isRoi = false;
                // 2.5 Set custom options
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
                try {
                    ocrStudioSdkFeedBack = new OCRStudioSDKFeedBack(ocrStudioSdkCallback);
                    session = engine.CreateSession(
                            SettingsStore.signature,
                            sessionParams,
                            ocrStudioSdkFeedBack);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                handler.post(() -> {
                    callback.initialized(true);
                });

            } catch (Exception e) {
                Label.getInstance().message.set("Exception");
                handler.post(() -> callback.error("SpawnSession: "+ e.getMessage()));
            }
        });
    }
}
