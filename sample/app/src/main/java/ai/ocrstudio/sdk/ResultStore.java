/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk;

import androidx.annotation.NonNull;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
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

public class ResultStore {

    /**
     * Document fields
     */
    public static class FieldInfo {

        public final String value;
        public boolean isAccepted;
        public Map<String, String> attr;

        // Fields, forensics
        FieldInfo(final String value, final boolean accepted, final Map<String, String> attr) {
            this.value = value;
            this.isAccepted = accepted;
            this.attr = attr;
        }

        // Images
        FieldInfo(final String value, final Map<String, String> attr) {
            this.value = value;
            this.attr = attr;
        }

        // Tables
        FieldInfo(final String value) {
            this.value = value;
        }
    }


    /*
     * ========================================================================
     * ===================== ResultStore Storage ==========================
     * ========================================================================
     */

    public static final ResultStore instance = new ResultStore();

    private @NonNull String docType = ""; // representation of the returned document type

    private static final Map<String, FieldInfo> fields = new HashMap<>();
    private static final Map<String, FieldInfo> forensics = new HashMap<>();
    private static final Map<String, FieldInfo> images = new HashMap<>();
    private static final Map<String, FieldInfo> tables = new HashMap<>();

    public void addResult(OCRStudioSDKResult result) {

        fields.clear();
        forensics.clear();
        images.clear();
        tables.clear();

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

                    // Receive attributes
                    Map<String, String> attr = new HashMap<>();
                    try {
                        JSONObject attrJson = new JSONObject(item_it.Item().Attributes());
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
                        fields.put(item_it.Item().Name(), new FieldInfo(item_it.Item().Value(), item_it.Item().Accepted(), attr));
                    }
                    if (itemType.equals("image")) {
                        OCRStudioSDKImage image = item_it.Item().Image();
                        images.put(item_it.Item().Name(), new FieldInfo(image.ExportBase64JPEG().CStr(), attr));

                    }
                    if (itemType.equals("table")) {
                        tables.put(item_it.Item().Name(), new FieldInfo(item_it.Item().Value()));
                    }
                }
            }
        }
    }

    public Map<String, FieldInfo> getFields() {
        return fields;
    }
    public Map<String, FieldInfo> getImages() { return images; }
    public Map<String, FieldInfo> getTables() { return tables; }
    public Map<String, FieldInfo> getForensics() {
        return forensics;
    }
    public String getType() {
        return docType;
    }

    // Decode base64 and save to local folder. JSON file will become much lighter.
    // React Native passes the json file from the native part with the images encoded
    // in base64 for quite a long time. Flutter doesn't have this problem.
    // Visually it works faster with base64 images. Tested on Mediatek Helio G95
}
