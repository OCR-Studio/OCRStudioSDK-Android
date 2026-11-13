/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.R;

import ai.ocrstudio.sdk.OCRStudioSDKDelegate;
import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKItem;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKPixelFormat;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKSession;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;


/**
 * SESSION WRAPPER SINGLETON
 * Receives session data via callback and exposes them as a LiveData
 */
public class Session extends OCRStudioSDKDelegate {
    static String TAG = "myapp.Session";

    // Pattern Singleton
    private Session(){}
    public static final Session instance = new Session();

    // The session object
    private OCRStudioSDKSession session;

    //----------------------------------------------------------------------------------------------
    // PUBLIC DATA
    public MutableLiveData<SessionState> state          = new MutableLiveData<>(SessionState.Empty);
    public MutableLiveData<JSONObject>   visualization  = new MutableLiveData<>(null);
    public MutableLiveData<String>       instruction    = new MutableLiveData<>(null);
    public MutableLiveData<LivenessInstruction>
                                         liveness       = new MutableLiveData<>(null);

    //----------------------------------------------------------------------------------------------
    // PRIVATE FIELDS

    public boolean isRoi;
    public long startTime;
    public String error = null;

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public void createVideoSession() {
        Log.d(TAG, "createVideoSession");
        // Update state immediately
        state.setValue(SessionState.Creating);
        visualization.setValue(null);
        instruction.setValue(null);
        liveness.setValue(null);
        // Start a separate thread
        ExecutorService SessionExecutor = Executors.newSingleThreadExecutor();

        SessionExecutor.execute(() -> {
            try {
                // Update state
                //state.postValue(SessionState.Creating);

                // Delete old session here
                if(session!=null) delete();

                // Benchmark
                startTime = System.nanoTime();

                // 1. Get engine instance
                OCRStudioSDKInstance engine = Engine.getInstance();
                // 2. Create new session params
                JSONObject sessionParamsJson = new JSONObject();
                // 2.1 Set session_type
                // session_type: "document_recognition" for recognition by one frame
                sessionParamsJson.put("session_type", SettingsStore.currentSessionType);
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
                String timeStamp = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
                optionsJson.put("currentDate", timeStamp);
                // Adding session options
                optionsJson.put("sessionTimeout", Engine.isTwoSidedEnabled ? "10.0" : "5.0");
                sessionParamsJson.put("options", optionsJson);

                String sessionParams = sessionParamsJson.toString();
                // 3. Create recognition session
                try {
                    if (Engine.isTwoSidedEnabled) {
                        session = engine.CreateSession(SettingsStore.signature, sessionParams, this);
                        session.RegisterConfig(Engine.getSDKConfig());
                        Log.d(TAG, "Config registered");
                    } else {
                        session = engine.CreateSession(SettingsStore.signature, sessionParams, this);
                    }

                } catch (Exception exception) {
                    Log.e(TAG,"createVideoSession error",exception);
                    System.out.println(exception.toString());
                    throw new RuntimeException(exception);
                }

                // Update state
                state.postValue(SessionState.Created);

            } catch (Exception e) {
                Log.e(TAG,"createVideoSession error",e);
                session = null;
                // Update state
                error = "createVideoSession error\n"+e.getMessage();
                state.postValue(SessionState.Error);
            }
        });
    }

    public void processImage(OCRStudioSDKImage image) {
        try {
            //Label.getInstance().message.set("Wait...");
            // 1. Get engine instance
            OCRStudioSDKInstance engine = Engine.getInstance();
            // 2. Create new session params
            JSONObject sessionParamsJson = new JSONObject();
            // 2.1 Set session_type
            // session_type: "document_recognition" or "text_recognition" for recognition by one frame
            sessionParamsJson.put("session_type", SettingsStore.currentSessionType);
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

            session.ProcessImage(image);
            //Session.instance.state.postValue(SessionState.Processing);
            OCRStudioSDKResult result = session.CurrentResult();
            ResultStore.instance.addResult(result);

            // 4. Reset session
            session.Reset();
        } catch (Exception e) {
            Log.e("processImage", e.toString());
            throw new RuntimeException(e);
        }
    }
    public void processVideoFrame(OCRStudioSDKImage image){
        SessionState stateValue = state.getValue();
        Log.d(TAG, "processImage state:"+stateValue);
        if(stateValue==SessionState.SelfieChecking)
            return;// not allowed in this case
        if(stateValue!=SessionState.Processing)
            state.postValue(SessionState.Processing);
        // Do process
        session.ProcessImage(image);
        // Read a liveness instruction from the result
        if(SettingsStore.currentSessionType.equals("liveness_detection")) {
            OCRStudioSDKResult result = session.CurrentResult();
            try {
                JSONObject json = new JSONObject(result.TargetByIndex(0).Description());
                String code = json.optString("instruction_code");
                liveness.postValue(getLivenessInstruction(code));
            } catch (JSONException e) {
                Log.e(TAG, "processImage", e);
            }
        }
    }
    static private LivenessInstruction getLivenessInstruction(String code){
        switch (code){
            case "CT": return new LivenessInstruction("Complete", null);
            case "HoldStill": return new LivenessInstruction("Hold still", null);
            case "Straight" : return new LivenessInstruction("Look straight into the camera",  R.drawable.mask_s);
            case "Right" : return new LivenessInstruction("Rotate head to the right",       R.drawable.mask_r);
            case "Left" : return new LivenessInstruction("Rotate head to the left",        R.drawable.mask_l);
            case "Up" : return new LivenessInstruction("Rotate head to the up",          R.drawable.mask_u);
            case "Down" : return new LivenessInstruction("Rotate head to the down",        R.drawable.mask_d);
            default  : return new LivenessInstruction("<"+code+">", null);
        }
    }
    public void checkSelfie(Bitmap bitmap) {
        try {
            OCRStudioSDKImage image = imageFromBitmap(bitmap);
            session.ProcessImage(image);
            stopProcessing();
        } catch (Exception e) {
            Log.e(TAG,"checkSelfie",e);
        }
    }

    public OCRStudioSDKResult getCurrentResult(){
        return session.CurrentResult();
    }

    public void stopProcessing(){
        //session.Reset(); do not! keep the session for nfc-verification
        state.postValue(SessionState.Finished);
    }

    public void delete(){
        if(this.session!=null) {
            this.session.Reset();
            this.session = null;
        }
        state.postValue(SessionState.Empty);
    }

    /**
     * SESSION CALLBACK
     * A way to get info from OCRStudioSDKSession object
     * @param json_message
     */
    @Override
    public void Callback(String json_message) {
        try {
            JSONObject json = new JSONObject(json_message);
            Log.w(TAG, "   --> Callback: " + json);
            switch (json.getString("type")){
                case "detection_result" :
                case "segmentation_result" :
                    visualization.postValue(json);
                    break;
                case "instruction" :
                    String instrCode = json.getJSONObject("data")
                            .getString("instruction");
                    // Check Selfie?
                    if(instrCode.compareTo("provide_selfie_photo")==0){
                        Log.e(TAG,"provide_selfie_photo received");
                        state.postValue(SessionState.SelfieChecking);
                    }
                    // Set value
                    instruction.postValue(instrCode);
                    break;
                case "common" :
                    String message = json.optString("message", "");
                    // Set value
                    if(!message.isEmpty())
                        instruction.postValue(message);
                    break;
                case "session_end" :
                    break;
            }
        }catch (Exception e){
            Log.e(TAG,"Session Callback json parsing error",e);
        }
    }

    private static final String FRAUD_CHECK_ITEMS_TYPE = "fraud_attempt";
    private static final String DATA_CHECK_ITEM_NAME = "fraud_attempt_data";
    private static final String FACE_CHECK_ITEM_NAME = "fraud_attempt_face";

    public SessionNfcResult processNfc(SessionNfcData nfcData){
        Log.w(TAG,"processNfc, mrz:"+nfcData.mrz);
        try {
            OCRStudioSDKTarget target = session.CurrentResult().TargetByIndex(0);
            // Pack photo to string
            String photoString = nfcData.image.ExportBase64JPEG().CStr();

            // PREPARE DATA
            JSONObject doc = new JSONObject(target.Description());
            JSONObject json = new JSONObject()
                    .put("doc_type", doc.getString("specific_type"))
                    .put(
                        "physical_fields", new JSONObject()
                        .put(
                                "rfid_mrz", new JSONObject()
                                        .put("value", nfcData.mrz)
                                        .put("type", "String")
                        )
                        .put(
                                "rfid_photo", new JSONObject()
                                        .put("value", photoString)
                                        .put("type", "Image")
                        )
                    );

            // PROCESS
            session.ProcessData(json.toString());

            // PARSE THE RESULT
            target = session.CurrentResult().TargetByIndex(0); // RETAKE THE RESULT TARGET
            Map<String,String> items = loadTargetItems(target, FRAUD_CHECK_ITEMS_TYPE);
            return new SessionNfcResultSuccess(
                    items.get(DATA_CHECK_ITEM_NAME),
                    items.get(FACE_CHECK_ITEM_NAME)
            );
        }catch(Exception e){
            Log.e(TAG,"processNfc error",e);
            return new SessionNfcResultError(e.getMessage());
        }
    }

    private Map<String,String> loadTargetItems(OCRStudioSDKTarget target, String type){
        HashMap<String,String> map = new HashMap<>();
        OCRStudioSDKItemIterator iterator    = target.ItemsBegin(type);
        OCRStudioSDKItemIterator iteratorEnd = target.ItemsEnd  (type);
        while (!iterator.IsEqualTo(iteratorEnd)) {
            OCRStudioSDKItem item = iterator.Item();
            map.put(item.Name(), item.Value());
            iterator.Step();
        }
        return map;
    }

    public static OCRStudioSDKImage imageFromBitmap(@NonNull Bitmap bitmap) throws Exception {
        // Bitmap to byte array
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        //ByteBuffer byteBuffer = ImageUtil.createDirectByteBuffer(bitmap); doesn't work here!!!
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        byte[] bytes = byteBuffer.array();

        int stride = bitmap.getRowBytes();
        int height = bitmap.getHeight();
        int width  = bitmap.getWidth();

        return OCRStudioSDKImage.CreateFromPixelBuffer(bytes, width, height, stride, 1, OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);
    }

}
