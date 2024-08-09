/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import ai.ocrstudio.sdk.OCRStudioSDKResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.databinding.ActivityCameraBinding;
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

    private final int REQUEST_CAMERA_PERMISSION = 1;

    private Button button;

    private Draw draw;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView cameraView;
    ActivityCameraBinding binding;
    RelativeLayout drawing;

    private double startTime = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(CameraViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        // Bind to UI DRAW class
        draw = new Draw(this.getBaseContext());
        drawing = binding.drawing;
        drawing.addView(draw);

        button = binding.start;
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);

        cameraView = binding.cameraView;

        // Bind label object to xml
        binding.setLabel(Label.getInstance());

        button.setOnClickListener(v -> {
            if (model.pauseAnalysis) {
                model.pauseAnalysis = false;
            } else {
                Session.instance.stopProcessing();
            }
        });

        // Subscribe to session state
        Session.instance.state.observe(this,this::onSessionStateChanged);
        // Subscribe to visualization messages
        Session.instance.visualization.observe(this,this::onVisualizationReceived);

        Session.instance.createVideoSession();

        if (permission(Manifest.permission.CAMERA))
            request(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);

        /**
         * * For native Android projects you can call initCamera() without ViewTreeObserver().
         * * We use TreeObserver because of an issue with flutter! After the first call of this activity in flutter
         * * we get cameraView.getHeight() equal to 0 in all subsequent calls.
         * ** We must wait for the rendering of the cameraView to be completed.
         */

        binding.main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.main.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initCamera();
            }
        });
    }

    private void initCamera() {

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
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up the image analysis
        // https://developer.android.com/reference/androidx/camera/core/ImageCapture.Builder#setTargetResolution(android.util.Size)
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                // CameraX finds the closest image resolution
                .setTargetResolution(new Size(1200,720 ))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetRotation(rotation)
                .build();

        // ViewPort
        Rational aspectRatio = new Rational(cameraView.getWidth(), cameraView.getHeight());
        ViewPort viewPort = new ViewPort.Builder(aspectRatio, rotation).build();

        // Use case
        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .setViewPort(viewPort)
                .build();

        imageAnalysis.setAnalyzer(model.executor, image -> {
            // Try recognition
            try {
                OCRStudioSDKResult result = model.onVideoFrame(image, cameraView.getHeight());

                if (result!=null && result.AllTargetsFinal()) {
                    // The result is terminal when the engine decides that the recognition result
                    // has had enough information and ready to produce result, or when the session
                    // is timed out

                    // This will stop data from streaming
                    imageAnalysis.clearAnalyzer();
                    Session.instance.stopProcessing();
                }
            } catch (Exception e) {
                error(e.getMessage());
                finish();
            }

            // Free resources
            image.close();
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void toast(final String message) {

        runOnUiThread(() -> {
            Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            t.show();
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean permission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    public void request(String permission, int request_code) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, request_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) { // Permission is granted
                    granted = true;
                }
            }
            if (!granted) {
                error("Please allow Camera permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void initialized(boolean engine_initialized) {
        if (engine_initialized) {
            // enable buttons
            button.setEnabled(true);
            button.setVisibility(View.VISIBLE);


            long elapsedTime = System.nanoTime() - Session.instance.startTime;
            long t = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            Label.getInstance().message.set("Engine Ready: " + t + "ms");
        }
    }

    public void recognized() {
        OCRStudioSDKResult result = Session.instance.getCurrentResult();
        model.pauseAnalysis = true;
        double elapsedTime = System.nanoTime() - startTime;
        double t = TimeUnit.MILLISECONDS.convert((long) elapsedTime, TimeUnit.NANOSECONDS);

        toast("Time:" + t);
        ResultStore.instance.addResult(result);
        Intent intent = new Intent();

        setResult(RESULT_OK, intent);
        finish();
    }

    public void error(String message) {
        toast(message);
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
                initialized(true);
                break;
            case Processing:
                startTime = System.nanoTime();
                button.setText("CANCEL");
                break;
            case Finished:
                recognized();
                break;
            case Error:
                toast(Session.instance.error);
                break;
        }
    }

    /**
     * Session visualization message handler
     * @param json_message
     */
    public void onVisualizationReceived(JSONObject json_message) {
        if(json_message==null) return;
        draw.showMatching(json_message);
    }
}
