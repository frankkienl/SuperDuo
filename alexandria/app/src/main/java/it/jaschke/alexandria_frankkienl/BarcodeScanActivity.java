package it.jaschke.alexandria_frankkienl;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Created by FrankkieNL on 16-10-2015.
 */
public class BarcodeScanActivity extends AppCompatActivity {

    public static final int REQUEST_CAMERA_PERMISSION = 226; //Cam in T9

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcodescan_layout);

        //Check camera permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Show explanation?
            boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
            if (shouldShow) {
                Toast.makeText(this, "Camera permission is required for Barcode scanning", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            scan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_CAMERA_PERMISSION: {
                
            }
        }
    }

    public void scan() {

        //detector
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.EAN_13)
                .build();


        //Camera
        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();

        //surface
        final SurfaceView cameraView = (SurfaceView) findViewById(R.id.camera_view);

        //then we add a callback to the camera and override the onSurfaceChanged, the onSurfaceDestroyed & the surfaceChanged
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());  //this is where it starts. In case of any e
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //--> I've put a flag so keep the screen
                //on while the camera is on so here we clear that
            }
        });


        /////////
        if (!barcodeDetector.isOperational()) {
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low memory", Toast.LENGTH_LONG).show();
            }
        }

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                //SparseArray?
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                //this is a boilerplate code also found at the documentation. Our barcode scanner is possible to find more than one barcodes so we need to tell it that we only need the one at value 0. We also set a Status code at the result
                if (barcodes.size() != 0) {
                    Intent data = new Intent();
                    String value = barcodes.valueAt(0).displayValue;
                    data.putExtra("barcode_value", value);
                    setResult(CommonStatusCodes.SUCCESS, data);
                    finish();

                    Log.v("Code found", value + "");
                }
            }
        });
    }


}