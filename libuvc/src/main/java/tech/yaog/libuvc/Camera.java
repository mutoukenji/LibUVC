package tech.yaog.libuvc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.camera.simplewebcam.CameraPreview;

public class Camera {
    private static final int IMG_WIDTH = 1920;
    private static final int IMG_HEIGHT = 1080;

    private CameraPreview cameraPreview = CameraPreview.inst();
    private int cameraId;
    private int cameraBase;
    private Bitmap bmp;
    private boolean cameraExists=false;
    private SurfaceHolder surface;
    private Rect rect;
    private Thread mainLoop = null;
    private boolean captureFinished;

    private final Object mCaptureSync = new Object();

    public void setSurfaceHolder(SurfaceHolder surface) {
        this.surface = surface;
        Canvas canvas = surface.lockCanvas();
        canvas.scale(-1, 1, canvas.getWidth() >> 1, canvas.getHeight() >> 1);
        surface.unlockCanvasAndPost(canvas);
    }

    public void setViewSize(int width, int height) {
        int dw;
        int dh;
        if(width *IMG_HEIGHT/IMG_WIDTH<= height){
            dw = 0;
            dh = (height - width *IMG_HEIGHT/IMG_WIDTH)/2;
            rect = new Rect(dw, dh, dw + width -1, dh + width *IMG_HEIGHT/IMG_WIDTH-1);
        }else{
            dw = (width - height *IMG_WIDTH/IMG_HEIGHT)/2;
            dh = 0;
            rect = new Rect(dw, dh, dw + height *IMG_WIDTH/IMG_HEIGHT -1, dh + height -1);
        }
    }

    public void closeCamera() {
        if(cameraExists){
            mainLoop.interrupt();
            cameraExists = false;
            cameraPreview.stopCamera();
        }
    }

    public void openCamera() {
        if(!cameraExists) {
            if (bmp == null) {
                bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
            }
            // /dev/videox (x=cameraId + cameraBase) is used
            int ret = cameraPreview.prepareCameraWithBase(cameraId, cameraBase, IMG_WIDTH, IMG_HEIGHT);

            if (ret != -1) cameraExists = true;

            mainLoop = new Thread(mainRun);
            mainLoop.setPriority(Thread.MAX_PRIORITY);
            mainLoop.setName("UVCPreview");
            mainLoop.start();
        }
    }

    public Bitmap captureImage(){
        synchronized (mCaptureSync) {
            try {
                while (!captureFinished) {
                    mCaptureSync.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            captureFinished = false;
            return bmp;
        }
    }

    private Runnable mainRun = new Runnable() {
        @Override
        public void run() {
            while (cameraExists && !Thread.interrupted()) {
                if (surface == null) {
                    continue;
                }
                synchronized (mCaptureSync) {
                    // obtaining a camera image (pixel data are stored in an array in JNI).
                    byte[] framebuffer = cameraPreview.processCamera();
                    // camera image to bmp
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    bmp = BitmapFactory.decodeByteArray(framebuffer, 0, framebuffer.length, options);
                    captureFinished = true;
                    mCaptureSync.notify();
                }

                if (bmp != null) {
                    Canvas canvas = surface.lockCanvas();
                    if (canvas != null) {
                        // draw camera bmp on canvas
                        canvas.drawBitmap(bmp, null, rect, null);

                        surface.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    };
}
