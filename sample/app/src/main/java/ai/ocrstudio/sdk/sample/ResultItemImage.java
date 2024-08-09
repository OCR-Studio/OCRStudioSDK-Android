/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;

import java.util.Map;

public class ResultItemImage implements ResultItem {
    public final String name;
    public final Bitmap bitmap;
    public final Map<String, String> attr;

    ResultItemImage(String name, Bitmap bitmap, Map<String, String> attr){
        this.name = name;
        this.bitmap = bitmap;
        this.attr = attr;
    }

    ResultItemImage(String name, ResultStore.FieldInfo fieldInfo){
        this.name       = name;
        this.bitmap     = fieldInfo.bitmap;
        this.attr       = fieldInfo.attr;
    }


}
