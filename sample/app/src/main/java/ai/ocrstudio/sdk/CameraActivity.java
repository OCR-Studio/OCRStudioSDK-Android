/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk;

import static ai.ocrstudio.sdk.Session.session;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.util.concurrent.ListenableFuture;
import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKYUVFormat;
import com.databinding.ActivityCameraBinding;
import com.R;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main sample activity for documents recognition with OCRStudioSDK Android SDK
 */
public class CameraActivity extends AppCompatActivity implements Callback, OCRStudioSDKCallback {

    private final int REQUEST_CAMERA_PERMISSION = 1;

    private boolean init_once = true;
    private int imageRotationDegrees = 0;
    private final Session sessionInstance = new Session();
    private Button button;
    public static boolean pauseAnalysis = true;
    private boolean cancelButtonWasPushed = false;

    private Draw draw;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView cameraView;
    Executor executor;
    ActivityCameraBinding binding;
    RelativeLayout drawing;

    Context mContext;

    static int height;
    static int newHeight;
    static int width;
    static List<Integer> crop_rect;
    static int rotationTimes = 0;

    private double startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        // Bind to UI DRAW class
        draw = new Draw(this.getBaseContext());
        drawing = binding.drawing;
        drawing.addView(draw);

        mContext = this.getBaseContext();

        button = binding.start;
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);

        cameraView = binding.cameraView;

        // Bind label object to xml
        binding.setLabel(Label.getInstance());

        button.setOnClickListener(v -> {
            if (pauseAnalysis) {
                started();
                pauseAnalysis = false;
            } else {
                stopped();
                cancelButtonWasPushed = true;
            }
        });

        sessionInstance.initSession(this, this, this);

        if (permission(Manifest.permission.CAMERA))
            request(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);

        /**
         * * For native Android projects you can call initCamera() without ViewTreeObserver().
         * * We use TreeObserver because of an issue with flutter! After the first call of this activity in flutter
         * * we get cameraView.getHeight() equal to 0 in all subsequent calls.
         * ** We must wait for the rendering of the cameraView to be completed.
         */

        executor = Executors.newSingleThreadExecutor();

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

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer, int rowStride) {

        /** getBuffer() - The stride after the last row may not be mapped into the buffer.
         *  This is why we always calculate the byteBuffer offset.
         *  https://developer.android.com/reference/android/media/Image.Plane#getBuffer()
         */

        int bufferSize = byteBuffer.remaining();
        // The byte array size is stride * height (the leftover spaces will be filled with 0 bytes)
        byte[] bytesArray = new byte[height * rowStride];
        byteBuffer.get(bytesArray, 0, bufferSize);
        return bytesArray;
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

        imageAnalysis.setAnalyzer(executor, image -> {
            // If image analysis is in paused state
            if (pauseAnalysis) {
                image.close();
                return;
            }
            //  initialized only once:
            if (init_once) {
                // Get sensor orientation
                imageRotationDegrees = image.getImageInfo().getRotationDegrees();
                // Calculate rotation image counts
                rotationTimes = imageRotationDegrees / 90;
                // Cropping rectangle
                Rect crop = image.getCropRect();

                // Rotate crop rectangle if needed
                if (imageRotationDegrees == 0 || imageRotationDegrees == 180) {
                    // If smartphone in landscape - NOT TESTED

                    // width = image.getWidth(); // ~1280
                    // height = image.getHeight(); // ~960
                    // // Set scale for canvas drawing
                    // int heightPreview = binding.cameraView.getWidth();
                    // IdDraw.scale = (float) heightPreview / (float) height;
                    // // Calculate crop rectangle
                    // crop_rect = new Rectangle(crop.left, crop.top, crop.right - crop.left, crop.bottom);
                } else {
                    width = image.getWidth(); // ~1280
                    height = image.getHeight(); // ~960

                    // Set scale for canvas drawing
                    int heightPreview = cameraView.getHeight();
                    Draw.scale = (float) heightPreview / (float) width;
                    // Calculate crop rectangle
                    /**
                     * Rectangle:
                     * int x, X-coordinate of the top-left corner
                     * int y, Y-coordinate of the top-left corner
                     * int width, Width of the rectangle
                     * int height, Height of the rectangle
                     */

                    crop_rect = Arrays.asList(crop.top, crop.left, crop.bottom - crop.top,  crop.right);
                }
                init_once = false;
            }

            OCRStudioSDKResult result;

            // Try recognition
            try {

                /**
                 * Example for OUTPUT_IMAGE_FORMAT_YUV_420_888
                 * According to our tests RGBA_8888 has ~45ms overhead per frame (tested on Helio G90T)
                 * https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
                 */

                ImageProxy.PlaneProxy planeY = image.getPlanes()[0];
                ImageProxy.PlaneProxy planeU = image.getPlanes()[1];
                ImageProxy.PlaneProxy planeV = image.getPlanes()[2];

                OCRStudioSDKImage frame = OCRStudioSDKImage.CreateFromYUV(
                        getByteArrayFromByteBuffer(planeY.getBuffer(), planeY.getRowStride()),
                        planeY.getRowStride(),
                        planeY.getPixelStride(),
                        getByteArrayFromByteBuffer(planeU.getBuffer(), planeU.getRowStride()),
                        planeU.getRowStride(),
                        planeU.getPixelStride(),
                        getByteArrayFromByteBuffer(planeV.getBuffer(), planeV.getRowStride()),
                        planeV.getRowStride(),
                        planeV.getPixelStride(),
                        width,
                        height,
                        OCRStudioSDKYUVFormat.OCRSTUDIOSDK_YUV_FORMAT_420_888);

                /** Example for OUTPUT_IMAGE_FORMAT_RGBA_8888
                 *
                 *  ImageProxy.PlaneProxy planeRGBA = image.getPlanes()[0];
                 *  int stride = planeRGBA.getRowStride();
                 *
                 *  ByteBuffer bufferRGBA = planeRGBA.getBuffer();
                 *  byte[] frame_bytes = new byte[bufferRGBA.remaining()];
                 *  bufferRGBA.get(frame_bytes);
                 *  Image frame = Image.FromBufferExtended(frame_bytes, height, width, stride, ImagePixelFormat.IPF_RGBA, 1);
                 */

                // String base64_test_string = frame.GetBase64String().GetCStr();
                frame.RotateByNinety(rotationTimes);
                //String base64_test_string2 = frame.GetBase64String().GetCStr();

                /** According to our tests without cropping frame (W=546 H=1088) for image (W=1088 H=1088)
                 * the recognition speed decreases by ~125ms per frame (tested on Helio G90T)
                 */

                frame.CropByRect(crop_rect.get(0),
                        crop_rect.get(1),
                        crop_rect.get(2),
                        crop_rect.get(3));
                // String base64_test_string3 = frame.GetBase64String().GetCStr();
                session.ProcessImage(frame);
                result = session.CurrentResult();

            } catch (Exception e) {
                error(e.getMessage());
                finish();
                return;
            }

            // Draw overlay
//            draw.showMatching(result);

            if (result.AllTargetsFinal()) {
                // The result is terminal when the engine decides that the recognition result
                // has had enough information and ready to produce result, or when the session
                // is timed out

                // This will stop data from streaming
                imageAnalysis.clearAnalyzer();

                runOnUiThread(() -> {
                    recognized(result);
                    sessionInstance.session.Reset();
                });
            }
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
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
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
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialized(boolean engine_initialized) {
        if (engine_initialized) {
            // enable buttons
            button.setEnabled(true);
            button.setVisibility(View.VISIBLE);


            long elapsedTime = System.nanoTime() - sessionInstance.startTime;
            long t = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            Label.getInstance().message.set("Engine Ready: " + t + "ms");
        }
    }

    @Override
    public void recognized(OCRStudioSDKResult result) {
        pauseAnalysis = true;
        double elapsedTime = System.nanoTime() - startTime;
        double t = TimeUnit.MILLISECONDS.convert((long) elapsedTime, TimeUnit.NANOSECONDS);

        toast("Time:" + t);
        ResultStore.instance.addResult(result);
        Intent intent = new Intent();

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void started() {
        startTime = System.nanoTime();
        button.setText("CANCEL");
    }

    @Override
    public void stopped() {
        finish();
    }

    @Override
    public void error(String message) {
        toast(message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void visualizationReceived(String json_message) {
        draw.showMatching(json_message);
    }
}
