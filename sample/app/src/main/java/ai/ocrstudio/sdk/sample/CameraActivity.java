/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.R;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Main sample activity for documents recognition
 */
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "myapp.CameraActivity";

    // VIEW MODEL
    CameraViewModel model;
    public static Boolean isPhoto = false;
    // CONTROLS
    private Button button;
    private Draw draw;
    private PreviewView cameraView;
    private TextView txtInstruction;
    private TextView txtLiveness;
    private ImageView imgLiveness;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;

    // CAMERA
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        isPhoto = getIntent().getBooleanExtra("isPhoto", false);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(CameraViewModel.class);

        // INIT CONTROLS
        // Instruction label
        txtInstruction = findViewById(R.id.instruction);
        // Liveness
        txtLiveness = findViewById(R.id.liveness_text);
        imgLiveness = findViewById(R.id.liveness_mask);
        // Frame drawing view
        draw = new Draw(this.getBaseContext());
        ViewGroup drawing = findViewById(R.id.drawing);
        drawing.addView(draw);
        // Start/stop button
        button = findViewById(R.id.start);
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);

        button.setOnClickListener(v -> {
            if (isPhoto) {
                //button.setText("take\nphoto");
                takePhoto();
            }
            else
                model.onStartStopButton();
        });
        // Camera Preview
        cameraView = findViewById(R.id.cameraView);

        // SUBSCRIBE TO DATA
        // session state
        Session.instance.state.observe(this,this::onSessionStateChanged);
        // visualization messages
        Session.instance.visualization.observe(this,this::onVisualizationReceived);
        // instruction messages
        Session.instance.instruction.observe(this,this::onInstructionReceived);
        // liveness messages
        Session.instance.liveness.observe(this,this::onLivenessReceived);

        // INIT CAMERA
        if (hasCameraPermission())
            initCamera();
        else
            requestCameraPermission();
    }

    private void initCamera() {
        Log.d(TAG,"initCamera");

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        // "cameraView.getDisplay().getRotation()" some times null object reference error
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();

        // Preview
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        // Camera
        CameraSelector cameraSelector = SettingsStore.currentSessionType.equals("liveness_detection")?
                CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

        // Set up the image analysis
        // https://developer.android.com/reference/androidx/camera/core/ImageCapture.Builder#setTargetResolution(android.util.Size)
        if(!isPhoto) {
            imageAnalysis = new ImageAnalysis.Builder()
                    // CameraX finds the closest image resolution
                    .setTargetResolution(new Size(1200, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setTargetRotation(rotation)
                    .build();
        }
        // ViewPort
        Rational aspectRatio = new Rational(cameraView.getWidth(), cameraView.getHeight());
        ViewPort viewPort = new ViewPort.Builder(aspectRatio, rotation).build();
        if(isPhoto) {
            imageCapture = new ImageCapture.Builder()
                    .setTargetResolution(new Size(1200, 720))
                    .setTargetRotation(rotation)
                    .build();
        }
        // Use case
        UseCaseGroup.Builder groupBuilder = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .setViewPort(viewPort);

        if(isPhoto)
            groupBuilder.addUseCase(imageCapture);
        else
            groupBuilder.addUseCase(imageAnalysis);

        UseCaseGroup useCaseGroup = groupBuilder.build();

        if(!isPhoto) {
            imageAnalysis.setAnalyzer(model.executor, image -> {
                // Try recognition
                try {
                    OCRStudioSDKResult result = model.onVideoFrame(image, cameraView.getHeight());

                    if (result != null && result.AllTargetsFinal()) {
                        // The result is terminal when the engine decides that the recognition result
                        // has had enough information and ready to produce result, or when the session
                        // is timed out

                        // This will stop data from streaming
                        imageAnalysis.clearAnalyzer();
                        Session.instance.stopProcessing();
                    }
                } catch (Exception e) {
                    toast(e.getMessage());
                    finish();
                }

                // Free resources
                image.close();
            });
        }

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
    }

    void toast(final String message) {
        runOnUiThread(() -> {
            Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            t.show();
        });
    }
    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Log.w("ImageCapture", "Image Captured!");
                        OCRStudioSDKImage image = model.imageProxyToOCRImage(imageProxy);
                        MainActivity.Photo = image;
                        imageProxy.close();
                        setResult(Activity.RESULT_OK); //switch to main activity with exit code for processing
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "exception" + exception.getMessage());
                    }
                }
        );
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera permission
    private final int REQUEST_CAMERA_PERMISSION = 1;

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) { // Permission is granted
                    granted = true;
                    initCamera();
                }
            }
            if (!granted) {
                toast("Please allow Camera permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Session state handler
     */
    private void onSessionStateChanged(SessionState state){
        Log.e(TAG,"onSessionStateChanged: "+state);
        switch (state){
            case Empty:
                break;
            case Creating:
                break;
            case Created:
                onSessionCreated();
                break;
            case Processing:
                button.setText("CANCEL");
                break;
            case SelfieChecking:
                onSelfieCheck();
                break;
            case Finished:
                onSessionFinished();
                break;
            case Error:
                onError(Session.instance.error);
                break;
        }
    }
    public void onSessionCreated() {
        // enable buttons
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);

//        long elapsedTime = System.nanoTime() - Session.instance.startTime;
//        long t = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
//        Label.getInstance().message.set("Engine Ready: " + t + "ms");
    }
    public void onSessionFinished() {
        model.onFinish();
//        double elapsedTime = System.nanoTime() - startTime;
//        double t = TimeUnit.MILLISECONDS.convert((long) elapsedTime, TimeUnit.NANOSECONDS);
//        toast("Time:" + t);

        // Set activity result
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        // Close the activity
        finish();
    }

    public void onSelfieCheck() {
        // Open take photo activity
        Intent intent = new Intent(this, FaceCameraActivity.class);
        startActivity(intent);
    }

    public void onError(String message) {
        Log.e(TAG,"onError : "+message);
        // Hide Camera
        cameraView.setVisibility(View.GONE);

        // Show error
        TextView txtError = findViewById(R.id.error);
        txtError.setText(message);
        txtError.setVisibility(View.VISIBLE);
    }

    /**
     * Session visualization message handler
     * @param json_message
     */
    public void onVisualizationReceived(JSONObject json_message) {
        if(json_message==null) return;
        draw.showMatching(json_message);
    }

    /**
     * Session instruction message handler
     * @param instruction
     */
    public void onInstructionReceived(String instruction) {
        txtInstruction.setText(instruction);
    }
    /**
     * Session liveness instruction message handler
     * @param instruction
     */
    public void onLivenessReceived(@Nullable LivenessInstruction instruction) {
        txtLiveness.setText(instruction!=null ? instruction.text : null);
        Integer id = instruction!=null ? instruction.mask : null;
        imgLiveness.setImageDrawable(
            id!=null ? AppCompatResources.getDrawable(this, id) : null
        );
    }
}
