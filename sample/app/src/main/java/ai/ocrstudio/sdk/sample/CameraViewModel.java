/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.graphics.Rect;
import android.util.Log;

import androidx.camera.core.ImageProxy;
import androidx.lifecycle.ViewModel;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKYUVFormat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A view mode for CameraActivity
 */
public class CameraViewModel extends ViewModel {
    private final String TAG = "myapp.CameraViewModel";

    public final ExecutorService executor;// video analyse thread

    public boolean pauseAnalysis = true;

    private int height;
    private int width;
    private List<Integer> crop_rect;
    private int rotationTimes = 0;
    private boolean init_once = true;


    // CONSTRUCTOR
    public CameraViewModel(){
        Log.d(TAG, "constructor");
        executor = Executors.newSingleThreadExecutor();
    }
    // DESTRUCTOR
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "destructor - onCleared");
        executor.shutdown();
    }

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public OCRStudioSDKResult onVideoFrame(ImageProxy image, int heightPreview){
        Log.d(TAG, "onVideoFrame");

        // If image analysis is in paused state
        if (pauseAnalysis)
            return null;

        //  initialized only once:
        if (init_once) {
            // Get sensor orientation
            int imageRotationDegrees = image.getImageInfo().getRotationDegrees();
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

        // CREATE THE OCRStudioSDKImage OBJECT suitable for processing
        // Example for OCRSTUDIOSDK_YUV_FORMAT_420_888
        // According to our tests RGBA_8888 has ~45ms overhead per frame (tested on Helio G90T)
        // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
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

        frame.RotateByNinety(rotationTimes);


        // CROP THE IMAGE
        // According to our tests without cropping frame (W=546 H=1088) for image (W=1088 H=1088)
        // the recognition speed decreases by ~125ms per frame (tested on Helio G90T)
        frame.CropByRect(crop_rect.get(0),
                crop_rect.get(1),
                crop_rect.get(2),
                crop_rect.get(3));

        // PROCESS THE IMAGE
        Session.instance.processImage(frame);

        // RETURN THE RESULT
        return Session.instance.getCurrentResult();
    }

    private byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer, int rowStride) {
        // getBuffer() - The stride after the last row may not be mapped into the buffer.
        //  This is why we always calculate the byteBuffer offset.
        //  https://developer.android.com/reference/android/media/Image.Plane#getBuffer()

        int bufferSize = byteBuffer.remaining();
        // The byte array size is stride * height (the leftover spaces will be filled with 0 bytes)
        byte[] bytesArray = new byte[height * rowStride];
        byteBuffer.get(bytesArray, 0, bufferSize);
        return bytesArray;
    }


}
