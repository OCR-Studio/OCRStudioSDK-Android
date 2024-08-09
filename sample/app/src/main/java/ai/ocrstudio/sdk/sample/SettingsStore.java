/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Store for Engines settings
public class SettingsStore {

    public static Map<String, String> options   = new HashMap<>();
    public static String            currentMode = "default";
    public static ArrayList<String> currentMask = new ArrayList<>();
    public static String            signature   = null;

    // We use ArrayList due react-native supported data structure
    public static void SetMask(ArrayList<String> mask) {
        currentMask = mask;
    }
    public static void SetMode(String mode) {
        currentMode = mode;
    }
    public static void SetSignature(String sign) {
        signature = sign;
    }

}
