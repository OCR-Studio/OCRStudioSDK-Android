/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.widget.Toast;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
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

public class Engine {
    private static OCRStudioSDKInstance instance;
    private static final Character separator = ':';
    private static List<String> documents = new ArrayList<>();
    private static List<String> sessionTypes = new ArrayList<>();

    public static OCRStudioSDKInstance getInstance(Context context) {
        if (instance == null) {

            byte[] bundle_data = prepareBundle(context);

            // load library
            System.loadLibrary("jniocrstudiosdk");

            try {
                instance = OCRStudioSDKInstance.CreateFromBuffer(bundle_data);
            } catch (Exception e) {
                Toast t = Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
                t.show();
            }
        }
        return instance;
    }

    private static byte[] prepareBundle(Context context) {
        AssetManager assetManager = context.getAssets();
        String bundle_name = "";
        String[] file_list;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            file_list = assetManager.list("config");

            for (String file : file_list) {
                if (file.endsWith(".ocr")) {
                    bundle_name = file;
                    break;
                }
            }

            if (bundle_name.isEmpty()) {
                throw new IOException("No configuration bundle found!");
            }

            final String bundle_path = "config" + File.separator + bundle_name;

            InputStream is = assetManager.open(bundle_path);

            byte[] buffer = new byte[0xFFFF];
            for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                os.write(buffer, 0, len);
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public static String[] getDocumentsList(Context context) {

        if (documents.size() == 0) {
            OCRStudioSDKInstance engine = getInstance(context);

            JSONObject engineJson;
            try {
                engineJson = new JSONObject(engine.Description());
                JSONArray targetGroupsArray = engineJson.getJSONArray("target_groups");
                for (int tg_index = 0; tg_index < targetGroupsArray.length(); tg_index++) {
                    JSONObject targetGroup = targetGroupsArray.getJSONObject(tg_index);
                    String mode = targetGroup.getString("target_group_type");
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

    public static List<String> getSessionTypes(Context context) {
        if (sessionTypes.size() == 0) {
            OCRStudioSDKInstance engine = getInstance(context);
            JSONObject engineJson;
            try {
                engineJson = new JSONObject(engine.Description());
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

    public static String getFaceMatching(Context context, Object imageAObject, Object imageBObject) throws JSONException {
        JSONObject sessionParamsJson = new JSONObject();
        // 2.1 Set session_type
        // session_type: "face_matching" for face comparative
        sessionParamsJson.put("session_type", "face_matching");
        // 2.2 Set target_group_type
        sessionParamsJson.put("target_group_type", "default");
        String sessionParams = sessionParamsJson.toString();
        String status = "";
        String similarity_estimation = "";
        try {
            OCRStudioSDKInstance engine = Engine.getInstance(context);
            OCRStudioSDKSession session = engine.CreateSession(SettingsStore.signature, sessionParams);
            OCRStudioSDKImage imageA = getOCRStudioSDKImage(imageAObject);
            OCRStudioSDKImage imageB = getOCRStudioSDKImage(imageBObject);
            session.ProcessImage(imageA);
            session.ProcessImage(imageB);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (status.equals("Ok")) {
            return "Similarity: " + similarity_estimation;
        } else {
            return status;
        }


    }

    private static OCRStudioSDKImage getOCRStudioSDKImage(Object imageObject) {
        if (imageObject.getClass() == String.class) {
            OCRStudioSDKImage image = OCRStudioSDKImage.CreateFromBase64FileBuffer((String) imageObject);
            return image;
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

            OCRStudioSDKImage image = OCRStudioSDKImage.CreateFromPixelBuffer(byteArray,
                    width,
                    height,
                    bytesPerLine,
                    bytesPerChannel,
                    ai.ocrstudio.sdk.OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);
            return image;
        } else {
            return null;
        }
    }
}

