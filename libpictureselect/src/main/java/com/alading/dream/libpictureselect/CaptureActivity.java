package com.alading.dream.libpictureselect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.alading.dream.libpictureselect.databinding.ActivityLayoutCaptureBinding;
import com.alading.dream.libpictureselect.lib.config.PictureConfig;
import com.alading.dream.libpictureselect.lib.config.PictureMimeType;
import com.alading.dream.libpictureselect.lib.config.PictureSelectionConfig;
import com.alading.dream.libpictureselect.lib.thread.PictureThreadUtils;
import com.alading.dream.libpictureselect.lib.tools.AndroidQTransformUtils;
import com.alading.dream.libpictureselect.lib.tools.DateUtils;
import com.alading.dream.libpictureselect.lib.tools.MediaUtils;
import com.alading.dream.libpictureselect.lib.tools.PictureFileUtils;
import com.alading.dream.libpictureselect.lib.tools.SdkVersionUtils;
import com.alading.dream.libpictureselect.lib.tools.StringUtils;
import com.alading.dream.libpictureselect.view.RecordView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CaptureActivity extends AppCompatActivity {
    public static final int REQ_CAPTURE = 10001;
    private ActivityLayoutCaptureBinding mBinding;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final int PERMISSION_CODE = 1000;
    private ArrayList<String> deniedPermission = new ArrayList<>();

    private CameraX.LensFacing mLensFacing = CameraX.LensFacing.BACK;
    private int rotation = Surface.ROTATION_0;
    private Size resolution = new Size(1280, 720);
    private Rational rational = new Rational(9, 16);
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private TextureView textureView;
    private boolean takingPicture;
    private String outputFilePath;
    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";
    private PictureSelectionConfig mConfig;


    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, PictureConfig.REQUEST_CAMERA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_capture);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        mBinding.recordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
                takingPicture = true;
                File file = createImageFile();//new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpeg");
                mBinding.captureTips.setVisibility(View.INVISIBLE);
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        if (mConfig != null) {
                            if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mConfig.cameraPath)) {
                                PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<Boolean>() {

                                    @Override
                                    public Boolean doInBackground() {
                                        return AndroidQTransformUtils.copyPathToDCIM(CaptureActivity.this,
                                                file, Uri.parse(mConfig.cameraPath));
                                    }

                                    @Override
                                    public void onSuccess(Boolean result) {
                                        PictureThreadUtils.cancel(PictureThreadUtils.getIoPool());
                                    }
                                });
                            }
                        }
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = createVideoFile();//new File(getExternalCacheDir(), System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {

                        if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(mConfig.cameraPath)) {
                            PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<Boolean>() {

                                @Override
                                public Boolean doInBackground() {
                                    return AndroidQTransformUtils.copyPathToDCIM(CaptureActivity.this,
                                            file, Uri.parse(mConfig.cameraPath));
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    PictureThreadUtils.cancel(PictureThreadUtils.getIoPool());
                                }
                            });
                        }
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });

        mConfig = PictureSelectionConfig.getInstance();
    }
    public File createImageFile() {
        if (SdkVersionUtils.checkedAndroid_Q()) {
            String diskCacheDir = PictureFileUtils.getDiskCacheDir(this);
            File rootDir = new File(diskCacheDir);
            if (!rootDir.exists() && rootDir.mkdirs()) {
            }
            boolean isOutFileNameEmpty = TextUtils.isEmpty(mConfig.cameraFileName);
            String suffix = TextUtils.isEmpty(mConfig.suffixType) ? PictureFileUtils.POSTFIX : mConfig.suffixType;
            String newFileImageName = isOutFileNameEmpty ? DateUtils.getCreateFileName("IMG_") + suffix : mConfig.cameraFileName;
            File cameraFile = new File(rootDir, newFileImageName);
            Uri outUri = getOutUri(PictureMimeType.ofImage());
            if (outUri != null) {
                mConfig.cameraPath = outUri.toString();
            }
            return cameraFile;
        } else {
            String cameraFileName = "";
            if (!TextUtils.isEmpty(mConfig.cameraFileName)) {
                boolean isSuffixOfImage = PictureMimeType.isSuffixOfImage(mConfig.cameraFileName);
                mConfig.cameraFileName = !isSuffixOfImage ? StringUtils.renameSuffix(mConfig.cameraFileName, PictureMimeType.JPEG) : mConfig.cameraFileName;
                cameraFileName = mConfig.camera ? mConfig.cameraFileName : StringUtils.rename(mConfig.cameraFileName);
            }
            File cameraFile = PictureFileUtils.createCameraFile(this,
                    PictureMimeType.ofImage(), cameraFileName, mConfig.suffixType, mConfig.outPutCameraPath);
            if (cameraFile != null) {
                mConfig.cameraPath = cameraFile.getAbsolutePath();
            }
            return cameraFile;
        }
    }


    public File createVideoFile() {
        if (SdkVersionUtils.checkedAndroid_Q()) {
            String diskCacheDir = PictureFileUtils.getVideoDiskCacheDir(this);
            File rootDir = new File(diskCacheDir);
            if (!rootDir.exists() && rootDir.mkdirs()) {
            }
            boolean isOutFileNameEmpty = TextUtils.isEmpty(mConfig.cameraFileName);
            String suffix = TextUtils.isEmpty(mConfig.suffixType) ? PictureMimeType.MP4 : mConfig.suffixType;
            String newFileImageName = isOutFileNameEmpty ? DateUtils.getCreateFileName("VID_") + suffix : mConfig.cameraFileName;
            File cameraFile = new File(rootDir, newFileImageName);
            Uri outUri = getOutUri(PictureMimeType.ofVideo());
            if (outUri != null) {
                mConfig.cameraPath = outUri.toString();
            }
            return cameraFile;
        } else {
            String cameraFileName = "";
            if (!TextUtils.isEmpty(mConfig.cameraFileName)) {
                boolean isSuffixOfImage = PictureMimeType.isSuffixOfImage(mConfig.cameraFileName);
                mConfig.cameraFileName = !isSuffixOfImage ? StringUtils
                        .renameSuffix(mConfig.cameraFileName, PictureMimeType.MP4) : mConfig.cameraFileName;
                cameraFileName = mConfig.camera ? mConfig.cameraFileName : StringUtils.rename(mConfig.cameraFileName);
            }
            File cameraFile = PictureFileUtils.createCameraFile(this,
                    PictureMimeType.ofVideo(), cameraFileName, mConfig.suffixType, mConfig.outPutCameraPath);
            mConfig.cameraPath = cameraFile.getAbsolutePath();
            return cameraFile;
        }
    }

    private Uri getOutUri(int type) {
        return type == PictureMimeType.ofVideo()
                ? MediaUtils.createVideoUri(this, mConfig.suffixType) : MediaUtils.createImageUri(this, mConfig.suffixType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
//            Intent intent = new Intent();
//            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
//            //当设备处于竖屏情况时，宽高的值 需要互换，横屏不需要
//            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
//            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
//            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
//            setResult(RESULT_OK, intent);
//            finish();

            if (!takingPicture) {
                onRecordSuccess(outputFilePath);
            } else {
                onPictureSuccess(outputFilePath);
            }
        }
    }

    public void onPictureSuccess(@NonNull  String path) {
        mConfig.cameraMimeType = PictureMimeType.ofImage();
        Intent intent = new Intent();
        intent.putExtra(PictureConfig.EXTRA_MEDIA_PATH,path);
        intent.putExtra(PictureConfig.EXTRA_CONFIG, mConfig);
        setResult(RESULT_OK, intent);
        onBackPressed();

    }

    public void onRecordSuccess(@NonNull String path) {
        mConfig.cameraMimeType = PictureMimeType.ofVideo();
        Intent intent = new Intent();
        intent.putExtra(PictureConfig.EXTRA_MEDIA_PATH, path );
        intent.putExtra(PictureConfig.EXTRA_CONFIG, mConfig);
        setResult(RESULT_OK, intent);
        onBackPressed();
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, null);
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission.add(permission);
                }
            }


            if (deniedPermission.isEmpty()) {
                bindCameraX();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.capture_permission_message))
                        .setNegativeButton(getString(R.string.capture_permission_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CaptureActivity.this.finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.capture_permission_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] denied = new String[deniedPermission.size()];
                                ActivityCompat.requestPermissions(CaptureActivity.this, deniedPermission.toArray(denied), PERMISSION_CODE);
                            }
                        }).create().show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        CameraX.unbindAll();

        //查询一下当前要使用的设备摄像头(比如后置摄像头)是否存在
        boolean hasAvailableCameraId = false;
        try {
            hasAvailableCameraId = CameraX.hasCameraWithLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }

        if (!hasAvailableCameraId) {
            showErrorToast("无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }

        //查询一下是否存在可用的cameraId.形式如：后置："0"，前置："1"
        String cameraIdForLensFacing = null;
        try {
            cameraIdForLensFacing = CameraX.getCameraFactory().cameraIdForLensFacing(mLensFacing);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(cameraIdForLensFacing)) {
            showErrorToast("无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }
        PreviewConfig config = new PreviewConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(rotation)
                .setTargetResolution(resolution)
                .setTargetAspectRatio(rational)
                .build();
        preview = new Preview(config);
        imageCapture = new ImageCapture(new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(rational)
                .setTargetResolution(resolution)
                .setLensFacing(mLensFacing)
                .setTargetRotation(rotation).build());
        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig.Builder()
                .setTargetRotation(rotation)
                .setLensFacing(mLensFacing)
                .setTargetResolution(resolution)
                .setTargetAspectRatio(rational)
                //视频帧率
                .setVideoFrameRate(25)
                //bit率
                .setBitRate(3 * 1024 * 1024).build();
        videoCapture = new VideoCapture(videoCaptureConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                textureView = mBinding.textureView;
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);
                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        //上面配置的都是我们期望的分辨率
        List<UseCase> newUseList = new ArrayList<>();
        newUseList.add(preview);
        newUseList.add(imageCapture);
        newUseList.add(videoCapture);
        Map<UseCase, Size> resolutions = CameraX.getSurfaceManager().getSuggestedResolutions(cameraIdForLensFacing, null, newUseList);
        Iterator<Map.Entry<UseCase, Size>> iterator = resolutions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UseCase, Size> next = iterator.next();
            UseCase useCase = next.getKey();
            Size size = next.getValue();
            Map<String, Size> update = new HashMap<>();
            update.put(cameraIdForLensFacing, size);
            useCase.updateSuggestedResolution(update);
        }

        CameraX.bindToLifecycle(this, preview, imageCapture, videoCapture);
    }

    private void showErrorToast(@NonNull String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();

    }
}
