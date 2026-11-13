/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKPixelFormat;
import ai.ocrstudio.sdk.OCRStudioSDKProcessingConfig;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKSession;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * THE ENGINE WRAPPER SINGLETON
 * Implements engine loading
 * exposes the engine loading state as a LiveData
 */
public class Engine {
    private static final String TAG = "myapp.Engine";
    private static OCRStudioSDKInstance instance;
    private static final Character separator = ':';
    private static final List<String> documents = new ArrayList<>();
    private static final List<String> sessionTypes = new ArrayList<>();
    private static final List<String> textModes = new ArrayList<>();

    private static OCRStudioSDKProcessingConfig sdkConfig;
    public enum LoadingState{
        Empty, Loading, Ready, Error
    }
    public static Boolean isTwoSidedAvailable = false;
    public static Boolean isTwoSidedEnabled = false;

    public static MutableLiveData<LoadingState> loadingState = new MutableLiveData<>(LoadingState.Empty);
    public static String error = null;

    public static OCRStudioSDKInstance getInstance() {return instance;}
    public static List<String> getTextModes() {return textModes;}
    public static void setSDKConfig(OCRStudioSDKProcessingConfig config) {sdkConfig = config;}
    public static OCRStudioSDKProcessingConfig getSDKConfig() {return sdkConfig;}


    /**
     * Load all required data from files
     * and create the OCRStudioSDKInstance object as result
     * @param context - the application context for file reading operations
     */
    public static void load(Context context) {
        if (loadingState.getValue()==LoadingState.Empty) {
            new Thread(() -> {
                try {
                    loadingState.postValue(LoadingState.Loading);

                    // Load config file
                    byte[] config_data = loadConfigFile(context);

                    // load library
                    System.loadLibrary("jniocrstudiosdk");

                    // Create Engine instance
                    JSONObject params = new JSONObject();
                    params.put("enable_lazy_initialization",    true);
                    params.put("enable_delayed_initialization", false);
                    instance = OCRStudioSDKInstance.CreateFromBuffer(config_data, params.toString());
                    Log.w("sessionParams", params.toString());

                    loadingState.postValue(LoadingState.Ready);
                } catch (Throwable e) {
                    Log.e(TAG,"Load engine",e);
                    error = e.getMessage();
                    loadingState.postValue(LoadingState.Error);
                }
            }).start();
        }
    }

    /**
     * Load the content of your config file (it should be in assets/config folder)
     * @param context - the application context
     * @return the file content as array of bytes
     * @throws IOException
     */
    private static byte[] loadConfigFile(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String configFilename = "";
        String[] file_list;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        file_list = assetManager.list("config");

        for (String file : file_list) {
            if (file.endsWith(".ocr")) {
                configFilename = file;
                break;
            }
        }

        if (configFilename.isEmpty()) {
            throw new IOException("configuration file is not found");
        }

        final String configPath = "config" + File.separator + configFilename;

        InputStream is = assetManager.open(configPath);

        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        os.close();
        return os.toByteArray();
    }

    public static String[] getDocumentsList() {

        if (documents.isEmpty()) {
            OCRStudioSDKInstance engine = instance;

            JSONObject engineJson;
            try {
                engineJson = new JSONObject(engine.Description());
                JSONArray targetGroupsArray = engineJson.getJSONArray("target_groups");
                Log.w("target_groups", targetGroupsArray.toString());
                for (int tg_index = 0; tg_index < targetGroupsArray.length(); tg_index++) {
                    JSONObject targetGroup = targetGroupsArray.getJSONObject(tg_index);
                    String mode = targetGroup.getString("target_group_type");
                    Log.w("target_group_type", mode);
                    //Getting all session types
                    JSONArray sessionTypes = engineJson.getJSONArray("session_types");
                    //If text_recognition found parsing differently
                    if (sessionTypes.toString().contains("text_recognition")) {
                        JSONArray textTargetsArray = targetGroup.getJSONArray("targets");
                        for (int textTarget_index = 0; textTarget_index < textTargetsArray.length(); textTarget_index++) {
                            String mask = textTargetsArray.getString(textTarget_index);
                            textModes.add(mode + separator + mask);
                            documents.add(mode + separator + mask);
                        }
                    }
                    JSONArray masksArray = targetGroup.getJSONArray("target_masks");
                    for (int mask_index = 0; mask_index < masksArray.length(); mask_index++) {
                        String mask = masksArray.getString(mask_index);
                        documents.add(mode + separator + mask);
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        String[] array = new String[documents.size()];
        documents.toArray(array);

        return array;
    }

    public static List<String> getSessionTypes() {
        if (sessionTypes.isEmpty()) {
            OCRStudioSDKInstance engine = instance;
            JSONObject engineJson;
            try {
                engineJson = new JSONObject(engine.Description());
                Log.w(TAG, engineJson.toString());
                JSONArray sessionTypesArray = engineJson.getJSONArray("session_types");
                for (int st_index = 0; st_index < sessionTypesArray.length(); st_index++) {
                    String sessionType = sessionTypesArray.getString(st_index);
                    sessionTypes.add(sessionType);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sessionTypes;
    }

    public static String getFaceMatching(Object imageAObject, Object imageBObject) throws JSONException {
        Log.d(TAG,"getFaceMatching "+imageAObject+" "+imageBObject);
        JSONObject sessionParamsJson = new JSONObject();
        // 2.1 Set session_type
        // session_type: "face_matching" for face comparative
        sessionParamsJson.put("session_type", "face_matching");
        // 2.2 Set target_group_type
        sessionParamsJson.put("target_group_type", "default");
        String sessionParams = sessionParamsJson.toString();
        String status = "";
        String similarity_estimation = null;
        try {
            OCRStudioSDKInstance engine = instance;
            OCRStudioSDKSession session = engine.CreateSession(SettingsStore.signature, sessionParams);
            OCRStudioSDKImage imageA = getImage(imageAObject);
            OCRStudioSDKImage imageB = getImage(imageBObject);

            // PROCESS images
            session.ProcessImage(imageA);
            session.ProcessImage(imageB);

            // Read the result
            OCRStudioSDKResult result = session.CurrentResult();
            OCRStudioSDKTarget target = result.TargetByIndex(0);
            for (OCRStudioSDKItemIterator item_it = target.ItemsBegin("string"); !item_it.IsEqualTo(target.ItemsEnd("string")); item_it.Step()) {
                if (item_it.Item().Name().equals("status")) {
                    status = item_it.Item().Value();
                }
                if (item_it.Item().Name().equals("similarity_estimation")) {
                    similarity_estimation = item_it.Item().Value();
                }
            }
            // No similarity => error => return status (which describes the error)
            if(similarity_estimation==null) throw new Exception(status);

        } catch (Exception e) {
            return "Face compare error: "+e.getMessage();
        }
        return "Similarity: " + similarity_estimation;
    }

    /**
     * Create OCRStudioSDKImage object from Base64 buffer or from Bitmap
     */
    private static OCRStudioSDKImage getImage(Object imageObject) {
        if (imageObject.getClass() == String.class) {
            return OCRStudioSDKImage.CreateFromBase64FileBuffer((String) imageObject);
        } else if (imageObject.getClass() == Bitmap.class) {

            int width = ((Bitmap) imageObject).getWidth();
            int height = ((Bitmap) imageObject).getHeight();

            int bytesPerPixel = 4; // Assuming ARGB_8888 format, where each pixel occupies 4 bytes (8 bits per channel)

            int bytesPerLine = width * bytesPerPixel;
            int bytesPerChannel = bytesPerPixel / 4; // Each channel (A, R, G, B) has 8 bits

            int bufferSize = bytesPerLine * height;
            byte[] byteArray = new byte[bufferSize];

            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            ((Bitmap) imageObject).copyPixelsToBuffer(byteBuffer);

            return OCRStudioSDKImage.CreateFromPixelBuffer(byteArray,
                    width,
                    height,
                    bytesPerLine,
                    bytesPerChannel,
                    OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);
        } else {
            return null;
        }
    }
}

