/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import com.R;
import ai.ocrstudio.sdk.sample.nfc.NfcState;

import org.json.JSONException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "myapp.MainActivity";

    // CONTROLS
    TextView    txtVersion;
    View        viewLoadingModal;
    TextView    txtLoadingInfo;
    TextView    resultTextField;
    View        sessionToolbar; // start session buttons (no result)
    View        resultToolbar;  // clear result button
    // Faces
    View        facesLayout;
    View        faceALayout;
    View        faceBLayout;
    ImageView   faceA;
    ImageView   faceB;
    // List
    ListView    listView;
    ResultAdapter adapter;
    // Buttons
    View        btnTarget;
    View        btnCamera;
    View        btnGallery;
    // NFC controls
    View        btnNfcActivate;
    View        btnNfcClose;
    View        nfcDialog;
    TextView    txtNfcStatus;
    View        txtNfcNotSupported;

    // VIEW MODEL
    MainViewModel model;

    private final static int GALLERY_UPLOAD = 0;
    private final static int PHOTO_A_UPLOAD = 1;
    private final static int PHOTO_B_UPLOAD = 2;
    private int upload = GALLERY_UPLOAD;
    public static Bitmap FaceFile;

    private final Character maskDocSeparator = ':';

    GalleryUpload exampleUpload = new GalleryUpload();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_example);
        //controls = DataBindingUtil.setContentView(this, R.layout.activity_example);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(MainViewModel.class);

        adapter = new ResultAdapter(this);

        // INIT CONTROLS
        txtVersion       = findViewById(R.id.version);
        viewLoadingModal = findViewById(R.id.loading_modal);
        txtLoadingInfo   = findViewById(R.id.loading_info);
        listView         = findViewById(R.id.list);
        resultTextField  = findViewById(R.id.result_info);
        sessionToolbar   = findViewById(R.id.sessionToolbar);
        resultToolbar    = findViewById(R.id.resultToolbar);
        // Faces
        facesLayout      = findViewById(R.id.facesLayout);
        faceALayout      = findViewById(R.id.faceALayout);
        faceBLayout      = findViewById(R.id.faceBLayout);
        faceA            = findViewById(R.id.faceA);
        faceB            = findViewById(R.id.faceB);
        // Buttons
        btnTarget        = findViewById(R.id.button_target);
        btnCamera        = findViewById(R.id.button_camera);
        btnGallery       = findViewById(R.id.button_gallery);

        // SET HANDLERS
        findViewById(R.id.btnClearResult).setOnClickListener(v -> model.clearResult(this));

        // NFC controls
        nfcDialog        = findViewById(R.id.nfcDialog);
        txtNfcStatus     = findViewById(R.id.txtNfcStatus);
        txtNfcNotSupported=findViewById(R.id.txt_nfc_not_supported);
        btnNfcActivate   = findViewById(R.id.btn_nfc_activate);
        btnNfcClose      = findViewById(R.id.btn_nfc_close);

        // SUBSCRIBE TO LIVE DATA
        // Engine loading state
        Engine.loadingState.observe(this,this::onEngineLoadingStateChanged);
        // Result
        model.result.resultData.observe(this, this::onResultDataChanged);
        // Faces
        model.faces.facesData.observe(this, this::onFacesDataChanged);
        // NFC state
        model.nfc.nfcState.observe(this, this::onNfcStateChanged);

        // Start engine loading
        Engine.load(this);
    }


    //----------------------------------------------------------------------------------------------
    // ENGINE LOADING
    /**
     * Engine loading state live data observer
     * @param loadingState
     */
    private void onEngineLoadingStateChanged(Engine.LoadingState loadingState){
        Log.d(TAG, "onEngineLoadingStateChanged "+loadingState);
        switch (loadingState){
            case Empty:
            case Loading:
                txtLoadingInfo.setText("Engine is loading...");
                viewLoadingModal.setVisibility(VISIBLE);
                break;
            case Error:
                txtLoadingInfo.setText(Engine.error);
                viewLoadingModal.setVisibility(VISIBLE);
                break;
            case Ready:
                viewLoadingModal.setVisibility(GONE);
                onEngineLoaded();
                break;
        }
    }
    /**
     * Updates the UI when Engine is loaded
     */
    private void onEngineLoaded(){
        txtVersion.setText("OCRStudioSDK Library Version: " + OCRStudioSDKInstance.LibraryVersion());

        // Button handlers
        btnNfcActivate.setOnClickListener(v -> model.nfc.activate(this));
        btnNfcClose.setOnClickListener(v -> model.nfc.close(this));

        // ListView
        listView.setAdapter(adapter);
        List<String> sessionTypes = Engine.getSessionTypes();

        // Start camera button
        if (sessionTypes.contains("video_recognition")) {
            btnCamera.setOnClickListener(v -> openCamera());
        } else {
            btnCamera.setVisibility(GONE);
        }

        // Load from gallery button
        if (sessionTypes.contains("document_recognition")) {
            btnGallery.setOnClickListener(v -> {
                if (isDoctypeSet()) {
                    upload = GALLERY_UPLOAD;
                    resultTextField.setText("Recognizing...");
                    mUploadActivity.launch("image/*" );
                }
            });
        } else {
            btnGallery.setVisibility(GONE);
        }

        // Compare faces panel
        if (sessionTypes.contains("face_matching")) {
            faceALayout.setOnClickListener(v -> {
                upload = PHOTO_A_UPLOAD;
                showPhotoDialog();
            });
            faceBLayout.setOnClickListener(v -> {
                upload = PHOTO_B_UPLOAD;
                showPhotoDialog();
            });
        } else {
            facesLayout.setVisibility(GONE);
        }

        // Select target button
        btnTarget.setOnClickListener(v -> openTargetSelector());
    }

    //----------------------------------------------------------------------------------------------
    // NFC
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        model.nfc.onResumeActivity(this);//enable/disable NFC detection
    }
    /**
     *    A DISCOVERED NFC TAG CALLBACK
     *    When NFC tag is discovered the next function will be called
     *    The activity must have android:launchMode="singleTop"
      */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "========== onNewIntent "+intent);
        model.nfc.onNewIntent(intent, model.result.resultData.getValue().passportKey);
    }

    /**
     * NFC-state live data observer
     * Implements NFC STATE MACHINE
     * @param nfcState
     */
    private void onNfcStateChanged(NfcState nfcState){
        Log.d(TAG,"onNfcStateChanged "+nfcState);
        btnNfcClose.setEnabled(nfcState != NfcState.Stopping);
        switch (nfcState){
            case Disabled:
                // Dialog
                nfcDialog       .setVisibility(GONE);
                // Toolbar
                btnNfcActivate      .setVisibility(VISIBLE);
                txtNfcNotSupported  .setVisibility(GONE);
                break;
            case Waiting:
                // Dialog
                nfcDialog       .setVisibility(VISIBLE);
                txtNfcStatus    .setText("Move your device to nfc-tag");
                // Toolbar
                break;
            case Reading:
                // Dialog
                nfcDialog       .setVisibility(VISIBLE);
                txtNfcStatus    .setText("Reading NFC data...");
                // Toolbar
                break;
            case Checking:
                // Dialog
                nfcDialog       .setVisibility(VISIBLE);
                txtNfcStatus    .setText("Checking NFC data by the engine...");
                // Toolbar
                break;
            case Stopping:
                nfcDialog       .setVisibility(VISIBLE);
                txtNfcStatus    .setText("Stopping NFC reading...");
                // Toolbar
                break;
            case Error:
                // Dialog
                nfcDialog       .setVisibility(VISIBLE);
                txtNfcStatus    .setText("NFC error: "+model.nfc.nfcError);
                // Toolbar
                break;
            case Success:
                // Dialog
                nfcDialog       .setVisibility(GONE);
                // Toolbar
                btnNfcActivate  .setVisibility(GONE);// hide button on success
                // Show success info
                Toast.makeText(this, "NFC-data was successfully read", Toast.LENGTH_SHORT).show();
                break;
            case NotSupported:
                // Dialog
                nfcDialog       .setVisibility(GONE);
                // Toolbar
                btnNfcActivate      .setVisibility(GONE);
                txtNfcNotSupported  .setVisibility(VISIBLE);
                break;
        }

    }

    //----------------------------------------------------------------------------------------------
    // FACES
    /**
     * Faces live data observer
     * @param facesData
     */
    private void onFacesDataChanged(@NonNull FacesModel.FacesData facesData) {
        Log.w(TAG, "onFacesDataChanged A:" + facesData.faceA + "  B:"+facesData.faceB);
        // Update controls
        if(facesData.faceA!=null)  faceA.setImageBitmap(facesData.faceA);
        else                       faceA.setImageResource(0);
        if(facesData.faceB!=null)  faceB.setImageBitmap(facesData.faceB);
        else                       faceB.setImageResource(0);
        // Start compare process
        compareFaces(facesData);
    }

    /**
     * Start faces compare process
     * @param data
     */
    private void compareFaces(FacesModel.FacesData data) {
        if (data.faceA != null && data.faceB != null) {
            resultTextField.setText("Wait...");
            try {
                String compareResult = Engine.getFaceMatching(data.faceA, data.faceB);
                Log.d(TAG,"Faces compare result: "+compareResult);
                resultTextField.setText(compareResult);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Open dialog to take a photo for faces comparing
     */
    void showPhotoDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Choose an option");

        // Create the buttons for the AlertDialog
        dialogBuilder.setPositiveButton("Gallery", (dialog, which) -> {
            mUploadActivity.launch("image/*");

        });

        dialogBuilder.setNegativeButton("Photo", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), FaceCameraActivity.class);
            mSelfieActivity.launch(intent);

        });

        // Create and show the AlertDialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    //----------------------------------------------------------------------------------------------

    // Selfie activity
    ActivityResultLauncher<Intent> mSelfieActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (upload == PHOTO_A_UPLOAD) {
                            model.faces.setFaceA(FaceFile);
                        } else { //PHOTO_B_UPLOAD only
                            model.faces.setFaceB(FaceFile);
                        }
                    }
                }
            });

    // File upload activity
    ActivityResultLauncher<String> mUploadActivity = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (upload == GALLERY_UPLOAD) {
                    model.result.clear();
                }

                if (uri == null) {
                    resultTextField.setText("No image was selected");
                    return;
                }

                try {
                    // Get bitmap from file
                    Bitmap gallery_file;

                    if (Build.VERSION.SDK_INT >=29 ) {
                        gallery_file = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), uri), (imageDecoder, imageInfo, source1) -> imageDecoder.setMutableRequired(true));

                    } else {
                        gallery_file = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    }

                    try {

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());

                        executor.execute(() -> {
                            try {
                                switch (upload) {
                                    case GALLERY_UPLOAD:
                                        exampleUpload.getResultFromGallery(gallery_file);
                                        break;
                                    case PHOTO_A_UPLOAD:
                                        model.faces.setFaceA(gallery_file);
                                        break;
                                    case PHOTO_B_UPLOAD:
                                        model.faces.setFaceB(gallery_file);
                                        break;
                                }

                            } catch (Exception e) {
                                handler.post(() -> {
                                    String err = (e.getMessage().length() >= 800) ? e.getMessage().substring(0, 800) : e.getMessage();
                                    Toast t = Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG);
                                    t.show();
                                    resultTextField.setText("Exception");
                                });
                            }
                            switch (upload) {
                                case GALLERY_UPLOAD:
                                    handler.post(this.model.result::loadResult);
                                    break;
                                case PHOTO_A_UPLOAD:
                                case PHOTO_B_UPLOAD:
                                    break;
                            }

                        });
                        switch (upload) {
                            case PHOTO_A_UPLOAD:
                                break;
                            case PHOTO_B_UPLOAD:
                                break;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    resultTextField.setText(e.getMessage());
                    e.printStackTrace();
                }
            });

    private void openCamera() {

        if (!isDoctypeSet()) {
            return;
        }

        Intent intent;
        intent = new Intent(getApplicationContext(), CameraActivity.class);
        mStartCameraActivity.launch(intent);
    }
    // Document camera activity
    ActivityResultLauncher<Intent> mStartCameraActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // We have got a successful document recognition here
                    model.result.loadResult();
                }
            });

    /**
     * Result live data observer
     * @param resultData
     */
    private void onResultDataChanged(@NonNull ResultModel.ResultData resultData){
        Log.w(TAG,"onResultDataChanged "+resultData);

        // Show/Hide  toolbars
        sessionToolbar  .setVisibility(resultData.isEmpty()?VISIBLE:GONE);
        resultToolbar   .setVisibility(resultData.isEmpty()?GONE:VISIBLE);

        // Result
        resultTextField.setText(resultData.docType);
        adapter.setData(resultData);
        adapter.notifyDataSetChanged();
    }

    private void openTargetSelector() {
        final String[] documents = Engine.getDocumentsList();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select mode:mask");
        builder.setItems(documents, (dialog, item) -> {

            String docMask = documents[item];
            int separator = docMask.indexOf(maskDocSeparator);
            String currentMode = docMask.substring(0, separator);
            String currentMask = docMask.substring(separator + 1);
            resultTextField.setText(MessageFormat.format("{0}{1}{2}", currentMode, maskDocSeparator, currentMask));
            ArrayList<String> mask_from_menu = new ArrayList<>(Arrays.asList(currentMask));
            SettingsStore.SetMode(currentMode);
            SettingsStore.SetMask(mask_from_menu);
        });

        builder.setPositiveButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isDoctypeSet() {
        if (SettingsStore.currentMask.isEmpty()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Warning");
            dialogBuilder.setMessage("Firstly choose doctype in menu");
            dialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            });
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            return false;
        }
        return true;
    }

}
