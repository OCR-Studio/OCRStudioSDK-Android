/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.CameraActivity;
import ai.ocrstudio.sdk.Engine;
import com.R;
import ai.ocrstudio.sdk.ResultStore;
import ai.ocrstudio.sdk.SettingsStore;
import com.databinding.ActivityExampleBinding;

import org.json.JSONException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Context context;
    public TextView resultTextField;
    GalleryUpload exampleUpload = new GalleryUpload();

    ListView listView;
    ResultAdapter adapter;
    ActivityExampleBinding binding;

    private ImageView faceA;
    private ImageView faceB;

    private final static int GALLERY_UPLOAD = 0;
    private final static int PHOTO_A_UPLOAD = 1;
    private final static int PHOTO_B_UPLOAD = 2;
    private int upload = GALLERY_UPLOAD;
    String score = "0";
    public static Bitmap FaceFile;

//    resources
    private Object faceAObject;
    private Object faceBObject;

    private Character maskDocSeparator = ':';

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_example);
        context = getBaseContext();

        listView = binding.list;
        resultTextField = binding.resultInfo;
        ImageButton selector = binding.selector;
        Button selectUpload = binding.gallery;
        Button selectCamera = binding.buttonCamera;
        LinearLayout faceLayout = binding.facesLayout;
        faceA = binding.faceA;
        faceB = binding.faceB;
        FrameLayout faceALayout = binding.faceALayout;
        FrameLayout faceBLayout = binding.faceBLayout;
        TextView version = binding.version;

        // ListView
        adapter = new ResultAdapter(context);
        listView.setAdapter(adapter);
        Engine.getInstance(context);
        version.setText("OCRStudioSDK Library Version: " + OCRStudioSDKInstance.LibraryVersion());

//        Uncomment next line if you want to apply first doc in doc list as default
//        setDefaultModeMask();

        List<String> sessionTypes = Engine.getSessionTypes(context);

        if (sessionTypes.contains("video_recognition")) {
            selectCamera.setOnClickListener(v -> openCamera());
        } else {
            selectCamera.setVisibility(View.GONE);
        }

        if (sessionTypes.contains("document_recognition")) {
            selectUpload.setOnClickListener(v -> {
                if (isDoctypeSet()) {
                    upload = GALLERY_UPLOAD;
                    resultTextField.setText("Recognizing...");
                    mUploadActivity.launch("image/*" );
                }
            });
        } else {
            selectUpload.setVisibility(View.GONE);
        }

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
            faceLayout.setVisibility(View.GONE);
        }

        selector.setOnClickListener(v -> openSelector());
        initSESettings();
    }

    public void showPhotoDialog() {
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

    private void initSESettings() {
       String signature = "INSERT_SIGNATURE_HERE from doc\README.html\";
       SettingsStore.SetSignature(signature);
       SettingsStore.SetForensics(false);
    }

    // Selfie activity
    ActivityResultLauncher<Intent> mSelfieActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (upload == PHOTO_A_UPLOAD) {
                            faceAObject = FaceFile;
                            faceA.setImageBitmap(FaceFile);
                        } else { //PHOTO_B_UPLOAD only
                            faceBObject = FaceFile;
                            faceB.setImageBitmap(FaceFile);

                        }
                        compareFaces();
                    }
                }
            });

    // File upload activity
    ActivityResultLauncher<String> mUploadActivity = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (upload == GALLERY_UPLOAD) {
                    adapter.clear();
                }

                if (uri == null) {
                    resultTextField.setText("No image was selected");
                    return;
                }

                try {
                    // Get bitmap from file
                    Bitmap gallery_file;

                    if (Build.VERSION.SDK_INT >=29 ) {
                        gallery_file = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), uri), (imageDecoder, imageInfo, source1) -> imageDecoder.setMutableRequired(true));

                    } else {
                        gallery_file = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    }

                    try {

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());

                        executor.execute(() -> {
                            try {
                                switch (upload) {
                                    case GALLERY_UPLOAD:
                                        exampleUpload.getResultFromGallery(context, gallery_file);
                                        break;
                                    case PHOTO_A_UPLOAD:
                                        faceAObject = gallery_file;

                                        break;
                                    case PHOTO_B_UPLOAD:
                                        faceBObject = gallery_file;

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
                                    handler.post(this::renderResult);
                                    break;
                                case PHOTO_A_UPLOAD:
                                    handler.post(this::compareFaces);
                                case PHOTO_B_UPLOAD:
                                    handler.post(this::compareFaces);
                                    break;
                            }

                        });
                        switch (upload) {
                            case PHOTO_A_UPLOAD:
                                faceA.setImageBitmap(gallery_file);
                                if (faceAObject != null) {
                                    resultTextField.setText("Wait...");
                                }
                                break;
                            case PHOTO_B_UPLOAD:
                                faceB.setImageBitmap(gallery_file);
                                if (faceBObject != null) {
                                    resultTextField.setText("Wait...");
                                }
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
        // reset state of UI
        resultTextField.setText("");
        faceA.setImageResource(0);
        faceB.setImageResource(0);
        faceAObject = null;
        faceBObject = null;

        // Reset items in result adapter
        if (adapter != null) {
            adapter.clear();
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
                    renderResult();
                }
            });

    public void renderResult() {
        // Get data from store
        Map<String, ResultStore.FieldInfo> fields = ResultStore.instance.getFields();
        Map<String, ResultStore.FieldInfo> images = ResultStore.instance.getImages();
        Map<String, ResultStore.FieldInfo> forensics = ResultStore.instance.getForensics();
        Map<String, ResultStore.FieldInfo> tables = ResultStore.instance.getTables();

        // Get docType
        String docType = ResultStore.instance.getType();

        // Check if document found
        if (docType.isEmpty()) {
            docType = "Document not found";
            resultTextField.setText(docType);
            return;
        }

        // Add first section to result view
        adapter = new ResultAdapter(context);

        adapter.addItem(ResultStore.instance.getType(), "section");
        // Put fields ti result
        for (Map.Entry<String, ResultStore.FieldInfo> set : fields.entrySet()) {
            Pair<String, ResultStore.FieldInfo> tempMap = new Pair(set.getKey(), set.getValue());
            adapter.addItem(tempMap, "field");
        }

        if (!images.isEmpty()) {
            adapter.addItem("Images", "section");
            // Put images to result
            for (Map.Entry<String, ResultStore.FieldInfo> img : images.entrySet()) {
                Pair<String, ResultStore.FieldInfo> tempMap = new Pair(img.getKey(), img.getValue());
                adapter.addItem(tempMap, "image");
            }
        }

        // Put forensics results
        if (!forensics.isEmpty()) {
            adapter.addItem("Forensics", "section");
            for (Map.Entry<String, ResultStore.FieldInfo> ff : forensics.entrySet()) {
                Pair<String, ResultStore.FieldInfo> tempMap = new Pair(ff.getKey(), ff.getValue());
                adapter.addItem(tempMap, "field");
            }
        }

        // Put table results
        if (!tables.isEmpty()) {
            adapter.addItem("Tables", "section");
            for (Map.Entry<String, ResultStore.FieldInfo> ff : tables.entrySet()) {
                Pair<String, ResultStore.FieldInfo> tempMap = new Pair(ff.getKey(), ff.getValue());
                adapter.addItem(tempMap, "table");
            }
        }

        listView.setAdapter(adapter);

        if (!images.containsKey("photo")) {
            docType = "Photo not found in document";
            resultTextField.setText(docType);
            return;
        } else {
            // Fill document photo
            faceAObject = Objects.requireNonNull(images.get("photo")).value;
            byte[] bytes = Base64.decode((String) faceAObject, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            faceA.setImageBitmap(bmp);
            compareFaces();
        }

        resultTextField.setText(docType);
    }

    private void openSelector() {
        final String[] documents = Engine.getDocumentsList(context);

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

    private void setDefaultModeMask() {
        // Set first item of list as default to escape starting recognition without type
        String defaultModeMask = Engine.getDocumentsList(context)[0];
        int separator = defaultModeMask.indexOf(maskDocSeparator);
        String currentMode = defaultModeMask.substring(0, separator);
        String currentMask = defaultModeMask.substring(separator + 1);
        resultTextField.setText(MessageFormat.format("{0}{1}{2}", currentMode, maskDocSeparator, currentMask));
        ArrayList<String> mask_from_menu = new ArrayList<>(Arrays.asList(currentMask));
        SettingsStore.SetMode(currentMode);
        SettingsStore.SetMask(mask_from_menu);
    }

    private void compareFaces() {
        if (faceAObject != null && faceBObject != null) {
            resultTextField.setText("Wait...");
            try {
                resultTextField.setText(Engine.getFaceMatching(context, faceAObject, faceBObject));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isDoctypeSet() {
        if (SettingsStore.currentMask.size() == 0) {
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
