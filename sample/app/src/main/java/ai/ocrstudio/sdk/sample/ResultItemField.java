/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import java.util.Map;

public class ResultItemField implements ResultItem {
    public final String name;
    public final String value;
    public final Boolean isAccepted;
    public final Map<String, String> attr;

    ResultItemField(String name, String value, Boolean isAccepted, Map<String, String> attr){
        this.name = name;
        this.value = value;
        this.isAccepted = isAccepted;
        this.attr = attr;
    }

    ResultItemField(String name, ResultStore.FieldInfo fieldInfo){
        this.name       = name;
        this.value      = fieldInfo.value;
        this.isAccepted = fieldInfo.isAccepted;
        this.attr       = fieldInfo.attr;
    }

}
