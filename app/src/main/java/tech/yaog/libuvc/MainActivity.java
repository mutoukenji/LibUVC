package tech.yaog.libuvc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.getHolder().addCallback(this);

        camera = new Camera();
    }

    private boolean isStart = false;
    private boolean isCreated = false;

    @Override
    protected void onStart() {
        super.onStart();
        isStart = true;
        checkSurfaceState();
    }

    private void checkSurfaceState() {
        if (isStart && isCreated) {
            camera.setViewSize(cameraPreview.getWidth(), cameraPreview.getHeight());
            camera.setSurfaceHolder(cameraPreview.getHolder());
            camera.openCamera();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.closeCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isCreated = true;
        checkSurfaceState();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
