package com.example.ezrent.screens;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ezrent.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.Collections;

public class FacialDetectionActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "FacialDetectionActivity";

    private TextureView textureView;
    private FaceDetector faceDetector;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;

    private boolean eyeClosedDetected = false;
    private Handler handler;
    private Runnable periodicFaceDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_detection);

        textureView = findViewById(R.id.textureView);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        handler = new Handler();
        periodicFaceDetection = new Runnable() {
            @Override
            public void run() {
                detectFace();
                handler.postDelayed(this, 500); // Run every 500 milliseconds
            }
        };
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getFrontCameraId(manager);
            if (cameraId == null) {
                Toast.makeText(this, "No front camera found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private String getFrontCameraId(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId;
            }
        }
        return null;
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    captureSession = session;
                    captureRequest = captureRequestBuilder.build();
                    try {
                        captureSession.setRepeatingRequest(captureRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    handler.post(periodicFaceDetection); // Start periodic face detection
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(FacialDetectionActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void detectFace() {
        Bitmap bitmap = textureView.getBitmap();
        if (bitmap == null) {
            Toast.makeText(this, "Unable to capture image", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.size() > 0) {
                        boolean isLivenessDetected = false;

                        for (Face face : faces) {
                            // Check if eyes are open
                            if (face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null) {
                                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                float rightEyeOpenProb = face.getRightEyeOpenProbability();

                                // Set a threshold to detect if the eyes are blinking
                                if (leftEyeOpenProb < 0.5 && rightEyeOpenProb < 0.5) {
                                    eyeClosedDetected = true;
                                }

                                if (eyeClosedDetected && leftEyeOpenProb > 0.5 && rightEyeOpenProb > 0.5) {
                                    isLivenessDetected = true;
                                    break;
                                }
                            }
                        }

                        if (isLivenessDetected) {
                            handler.removeCallbacks(periodicFaceDetection); // Stop periodic detection
                            // Face detected, proceed to contact owner
                            String ownerPhoneNumber = getIntent().getStringExtra("ownerPhoneNumber");
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://wa.me/" + ownerPhoneNumber + "?text=" + Uri.encode("Hello, I am interested in your rental house.")));
                            startActivity(intent);
                        } else {
                            Toast.makeText(FacialDetectionActivity.this, "Please blink your eyes to confirm liveliness.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // No face detected, show a message
                        Toast.makeText(FacialDetectionActivity.this, "No face detected. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FacialDetectionActivity.this, "Face detection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Face detection failed", e);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textureView.setSurfaceTextureListener(textureListener);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(periodicFaceDetection); // Stop handler when activity is destroyed
    }
}
