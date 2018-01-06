package com.developerhaoz.androidotgdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Haoz
 * @date 2018/1/6.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 1;
    private static WeakReference<Activity> mContext;
    List<String> mPicturePathList = new ArrayList<>();
    private OtgReceiver mOtgReceiver = new OtgReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = new WeakReference<Activity>(this);
        registerUDiskReceiver();
        findViewById(R.id.main_btn_export_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this
                        , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }else{
                    choosePicture();
                }

            }
        });
    }

    private void registerUDiskReceiver() {
        IntentFilter usbDeviceFileter = new IntentFilter();
        usbDeviceFileter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceFileter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceFileter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        registerReceiver(mOtgReceiver, usbDeviceFileter);
        // 注册监听自定义广播
        IntentFilter filter = new IntentFilter(OtgReceiver.ACTION_USB_PERMISSION);
        registerReceiver(mOtgReceiver, filter);
    }

    private void choosePicture() {
        Matisse.from(MainActivity.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(20)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    public static WeakReference<Activity> getContext(){
        if(mContext != null){
            return mContext;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_CHOOSE:
                    List<Uri> uriList = Matisse.obtainResult(data);
                    for (Uri uri : uriList) {
                        mPicturePathList.add(SavePictureUtil.getPath(MainActivity.this.getContentResolver(), uri));
                        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "提示", "正在导出图片...");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SavePictureUtil.savePictureToUsb(mPicturePathList, OtgApplication.mRootDirectory, new SavePictureUtil.OnFinishCallback() {
                                        @Override
                                        public void onFinish() {
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    choosePicture();
                }else {
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mOtgReceiver);
    }
}
