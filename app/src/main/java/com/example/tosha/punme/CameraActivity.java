//package com.example.tosha.punme;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.hardware.Camera;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.FrameLayout;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
///**
// * Created by Tosha on 1/29/2017.
// */
//public class CameraActivity extends Activity {
//
//    private Camera mCamera;
//    private CameraPreview mPreview;
//    private final String TAG = "CameraActivity";
//
//    private Camera mCamera;
//    private SurfaceView mPreview;
//    private MediaRecorder mMediaRecorder;
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
//        releaseCamera();              // release the camera immediately on pause event
//    }
//
//    private void releaseMediaRecorder(){
//        if (mMediaRecorder != null) {
//            mMediaRecorder.reset();   // clear recorder configuration
//            mMediaRecorder.release(); // release the recorder object
//            mMediaRecorder = null;
//            mCamera.lock();           // lock camera for later use
//        }
//    }
//
//    private void releaseCamera(){
//        if (mCamera != null){
//            mCamera.release();        // release the camera for other applications
//            mCamera = null;
//        }
//    }
//    /** Check if this device has a camera */
//    private boolean checkCameraHardware(Context context) {
//        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//            // this device has a camera
//            return true;
//        } else {
//            // no camera on this device
//            return false;
//        }
//    }
//
//    /** A safe way to get an instance of the Camera object. */
//    public static Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        }
//        catch (Exception e){
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }
//
//    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//
//            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (pictureFile == null){
//                Log.d(TAG, "Error creating media file, check storage permissions: " +
//                        e.getMessage());
//                return;
//            }
//
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            } catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
//            }
//        }
//    };
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.content_picture);
//
//        // Add a listener to the Capture button
//        Button captureButton = (Button) findViewById(R.id.button_capture);
//        captureButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // get an image from the camera
//                        mCamera.takePicture(null, null, mPicture);
//                    }
//                }
//        );
//
//        // Create an instance of Camera
//        mCamera = getCameraInstance();
//
//        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(r.id.camera_preview);
//        preview.addView(mPreview);
//    }
//}