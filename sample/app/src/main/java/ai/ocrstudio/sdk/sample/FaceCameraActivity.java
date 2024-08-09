/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.R;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class FaceCameraActivity extends AppCompatActivity {

    // For face camera
    PreviewView facePreviewView;
    ImageCapture imageCapture;
    Button captureImage;
    private ListenableFuture<ProcessCameraProvider> faceCameraProviderFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_face_camera);

        // For face camera
        facePreviewView = findViewById(R.id.facePreviewView);
        captureImage = findViewById(R.id.take_face);

        faceCameraProviderFuture = ProcessCameraProvider.getInstance(this);
        faceCameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = faceCameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Set up the camera preview
        Preview preview = new Preview.Builder()
            .setTargetResolution(new Size(480, 640))
            .build();

        // Lens facing
        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        // Create image capture
        ImageCapture.Builder builder = new ImageCapture.Builder();

        imageCapture = builder
                .setTargetResolution(new Size(480, 640))
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        // Attach the preview to UI
        preview.setSurfaceProvider(facePreviewView.getSurfaceProvider());

        // Attach lifecycle owner
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, preview);

        captureImage.setOnClickListener(v -> takePicture());

    }

    public void takePicture() {
        imageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {

                Bitmap img = imageProxyToBitmap(image);
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
                MainActivity.FaceFile = img;
                image.close();
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                finish();
            }
        });
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
