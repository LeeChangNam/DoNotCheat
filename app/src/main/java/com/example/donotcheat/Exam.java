package com.example.donotcheat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;*/
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Exam extends AppCompatActivity {
    long mNow;
    Date mDate;
    TextureView examStart;
    Button examFinishButton;
    String userNum;
    String roomNum;
    String root = "/";
    String filename;
    StorageReference storageReference;
    StorageReference upLoadImageRef;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{ Manifest.permission.CAMERA};
    String[] permission_list = {
        Manifest.permission.CAMERA
    };
    private String cameraId;
    /*private Size imageDimensions;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    @RequiresApi(api = Build.VERSION_CODES.O)*/

    SimpleDateFormat times = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        //examFinishButton = (Button) findViewById(R.id.finish);
        //examStart = (TextureView) findViewById(R.id.cam);
        System.out.println("iiiiiiiiiiiiiiiiiiiiiii");

        Intent secondIntent = getIntent();
        userNum = secondIntent.getStringExtra("수험번호");
        roomNum = secondIntent.getStringExtra("방번호");
        storageReference = FirebaseStorage.getInstance().getReference(roomNum);
        root.concat(secondIntent.getStringExtra("방번호"));
        root.concat("/");

        //permissions();
        //capture();
        //부정행위시

        examFinishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.google.mlkit.vision.demo.MainActivity.class);
                //capture();
                finish();
                startActivity(intent);
            }
        });
    }/*
    public void permissions(){
        if (allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(Exam.this, permission_list, 1);
        }
    }
    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return times.format(mDate);
        }
        else return "0000-00-00_00:00:00";
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }
    public void capture(){
        File screenShot = ScreenShot(examStart);
        if(screenShot!=null){
            storageUpLoad(screenShot);
        }
    }
    public void storageUpLoad(File screenShot){
        //storageReference.child(userNum).child();//채워야함
    }
    private File ScreenShot(TextureView examStart) {
        examStart.setDrawingCacheEnabled(true);

        Bitmap screenBitmap = examStart.getDrawingCache();

        filename = getTime();
        File file = new File(Environment.getExternalStorageDirectory()+root.concat(userNum), filename);  //수험번호 폴더 screenshot.png 파일
        FileOutputStream os = null;

        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        examStart.setDrawingCacheEnabled(false);
        return file;
    }
    public void startCamera(){
        Log.d("test","startCamera");
        examStart.setSurfaceTextureListener(textureListener);
        Log.d("test","endCamera");
    }
    public boolean allPermissionsGranted(){
        for (String permission : REQUIRED_PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };
    private void openCamera() throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cameraId = manager.getCameraIdList()[1];// 1로하면 전면카메라
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        imageDimensions = map.getOutputSizes(SurfaceTexture.class)[1];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            manager.openCamera(cameraId, stateCallback, null);
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch ( CameraAccessException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    private void createCameraPreview() throws CameraAccessException{
        SurfaceTexture texture = examStart.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if(cameraDevice == null){
                    return;
                }

                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                Toast.makeText(getApplicationContext(), "Configuration Changed", Toast.LENGTH_LONG).show();
            }
        },null);
    }
    private void updatePreview() throws CameraAccessException{
        if (cameraDevice == null){
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
    }*/
}