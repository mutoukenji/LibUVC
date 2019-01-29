package com.camera.simplewebcam;

import android.graphics.Bitmap;
import android.util.Log;

public class CameraPreview {

    private static final String TAG = "SimpleWebCam";
    static {
        Log.d(TAG, "loadLibrary");
        System.loadLibrary("ImageProc");
    }

    public native int powerOnOffCamera(int power);
    public native int prepareCamera(int videoid);
    public native int prepareCameraWithBase(int videoid, int camerabase, int width, int height);
    public native byte[] processCamera();
    public native void stopCamera();
    public native void pixeltobmp(Bitmap bitmap);

    private static CameraPreview _inst = null;
    public static CameraPreview inst() {
        if (_inst == null) {
            _inst = new CameraPreview();
        }
        return _inst;
    }
    private CameraPreview(){}

}
