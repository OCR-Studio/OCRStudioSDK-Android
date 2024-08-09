/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKItem;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A singleton which keeps the result data when switching between activities
 */
public class ResultStore {
    private static final String TAG = "myapp.ResultStore";

    /**
     * Document fields
     */
    public static class FieldInfo {

        public final String                 value;
        public final Boolean                isAccepted;
        public final Map<String, String>    attr;
        public final Bitmap                 bitmap;

        public FieldInfo(String value, Boolean accepted, Map<String, String> attr, Bitmap bitmap) {
            this.value      = value;
            this.isAccepted = accepted;
            this.attr       = attr;
            this.bitmap     = bitmap;
        }
    }

    public static final ResultStore instance = new ResultStore();

    private @NonNull String docType = ""; // representation of the returned document type

    private static final Map<String, FieldInfo> fields = new HashMap<>();
    private static final Map<String, FieldInfo> images = new HashMap<>();

    public void addResult(OCRStudioSDKResult result) {

        fields.clear();
        images.clear();
        docType = "";
        List<String> itemsType = new ArrayList<>();

        for (int target_i = 0; target_i < result.TargetsCount(); ++target_i) {
            OCRStudioSDKTarget target = result.TargetByIndex(target_i);

            try {
                JSONObject targetJson = new JSONObject(target.Description());
                String specific_type = targetJson.getString("specific_type");
                JSONArray itemTypesArray = targetJson.getJSONArray("item_types");
                for (int item_i = 0; item_i < itemTypesArray.length(); item_i++) {
                    itemsType.add(itemTypesArray.getString(item_i));
                }
                docType += specific_type;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            for (String itemType : itemsType) {
                for (OCRStudioSDKItemIterator item_it = target.ItemsBegin(itemType); !item_it.IsEqualTo(target.ItemsEnd(itemType)); item_it.Step()) {
                    OCRStudioSDKItem item = item_it.Item();

                    // Receive attributes
                    Map<String, String> attr = new HashMap<>();
                    try {
                        JSONObject attrJson = new JSONObject(item.Attributes());
                        Iterator<String> keys = attrJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = attrJson.getString(key);
                            attr.put(key, value);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    if (itemType.equals("string")) {
                        fields.put(item.Name(), new FieldInfo(
                                item.Value(),
                                item.Accepted(),
                                attr,
                                null
                        ));
                    }
                    if (itemType.equals("image")) {
                        try {
                            OCRStudioSDKImage image = item.Image();
                            // Get image bitmap
                            String str   = image.ExportBase64JPEG().CStr();
                            byte[] bytes = Base64.decode(str, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            // Create image item
                            images.put(item.Name(), new FieldInfo(
                                    str,
                                    true,
                                    attr,
                                    bitmap
                            ));
                        }catch(Exception e){
                            Log.e(TAG,"result image parsing",e);
                        }

                    }
                }
            }
        }
    }

    public Map<String, FieldInfo>   getFields() {
        return fields;
    }
    public Map<String, FieldInfo>   getImages() { return images; }
    public String                   getType()   { return docType; }

    // Decode base64 and save to local folder. JSON file will become much lighter.
    // React Native passes the json file from the native part with the images encoded
    // in base64 for quite a long time. Flutter doesn't have this problem.
    // Visually it works faster with base64 images. Tested on Mediatek Helio G95
}
