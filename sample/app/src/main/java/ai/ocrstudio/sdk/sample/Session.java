/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import ai.ocrstudio.sdk.OCRStudioSDKDelegate;
import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKItem;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKSession;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONObject;

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
    public static Session instance = new Session();

    // The session object
    private OCRStudioSDKSession session;

    //----------------------------------------------------------------------------------------------
    // PUBLIC DATA
    public MutableLiveData<SessionState> state = new MutableLiveData<>(SessionState.Empty);
    public MutableLiveData<JSONObject> visualization = new MutableLiveData<>(null);
    public boolean isRoi;
    public long startTime;
    public String error = null;

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public void createVideoSession() {
        ExecutorService SessionExecutor = Executors.newSingleThreadExecutor();
        SessionExecutor.execute(() -> {
            try {
                // Update state
                state.postValue(SessionState.Creating);

                // Delete old session here
                if(session!=null) session.delete();
                session = null;

                // Benchmark
                startTime = System.nanoTime();

                // 1. Get engine instance
                OCRStudioSDKInstance engine = Engine.getInstance();
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
                String timeStamp = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
                optionsJson.put("currentDate", timeStamp);
                // Adding session options
                sessionParamsJson.put("options", optionsJson);

                String sessionParams = sessionParamsJson.toString();

                // 3. Create recognition session
                try {
                    session = engine.CreateSession(
                            SettingsStore.signature,
                            sessionParams,
                            this);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                // Update state
                state.postValue(SessionState.Created);

            } catch (Exception e) {
                Log.e(TAG,"createVideoSession error",e);
                // Update state
                error = e.getMessage();
                state.postValue(SessionState.Created);
            }
        });
    }

    public void processImage(OCRStudioSDKImage image){
        if(state.getValue()!=SessionState.Processing)
            state.postValue(SessionState.Processing);
        session.ProcessImage(image);
    }
    public OCRStudioSDKResult getCurrentResult(){
        return session.CurrentResult();
    }

    public void stopProcessing(){
        //session.Reset(); do not! keep the session for nfc-verification
        state.postValue(SessionState.Finished);
    }

    public void delete(){
        session.delete();
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
            visualization.postValue(json);
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
}
