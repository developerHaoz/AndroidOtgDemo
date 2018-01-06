package com.developerhaoz.androidotgdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;

import java.io.IOException;

/**
 * @author Haoz
 * @date 2018/1/6.
 */
public class OtgReceiver extends BroadcastReceiver {

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    /**
     * 当前 U 盘所在文件目录
     */
    private UsbFile mRootFolder;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            // 接收到自定义广播
            case ACTION_USB_PERMISSION:
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                // 权限申请
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        readDevice(getUsbMass(usbDevice));
                    } else {
                        showToast(context, "没有插入 U 盘");
                    }
                } else {
                    showToast(context, "未获取到 U 盘权限");
                }
                break;
            // 接收到 U 盘插入广播
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                showToast(context, "U 盘已插入");
                UsbDevice attachUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (attachUsbDevice != null) {
                    permissionRequest();
                }
                break;
            // 接收到 U 盘拔出广播
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                showToast(context, "U 盘已拔出");
                break;
            default:
                break;
        }
    }

    /**
     * 进行 U 盘读写权限的申请
     */
    private void permissionRequest() {
        // 设备管理器
        UsbManager usbManager = (UsbManager) MainActivity.getContext().get().getSystemService(Context.USB_SERVICE);
        // 获取 U 盘存储设备
        UsbMassStorageDevice[] storageDevices = UsbMassStorageDevice.getMassStorageDevices(OtgApplication.getContext());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(OtgApplication.getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);

        if(storageDevices.length == 0){
            showToast(OtgApplication.getContext(), "请插入可用的 U 盘");
        }else{
            for (UsbMassStorageDevice device : storageDevices) {
                if (usbManager.hasPermission(device.getUsbDevice())) {
                    readDevice(device);
                } else {
                    // 进行权限申请
                    usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
                }
            }
        }

    }

    public UsbFile getRootFolder() {
        return mRootFolder;
    }

    private UsbMassStorageDevice getUsbMass(UsbDevice usbDevice) {
        UsbMassStorageDevice[] storageDevices = UsbMassStorageDevice.getMassStorageDevices(OtgApplication.getContext());
        for (UsbMassStorageDevice device : storageDevices) {
            if (usbDevice.equals(device.getUsbDevice())) {
                return device;
            }
        }
        return null;
    }

    /**
     * 读取 U 盘相关的信息
     *
     * @param device
     */
    private void readDevice(UsbMassStorageDevice device) {
        try {
            device.init();
            Partition partition = device.getPartitions().get(0);
            FileSystem currentFs = partition.getFileSystem();
            mRootFolder = currentFs.getRootDirectory();
            // 获取 U 盘的容量
            long capacity = currentFs.getCapacity();
            // 获取 U 盘的剩余容量
            long freeSpace = currentFs.getFreeSpace();
            // 获取 U 盘的标识
            String volumeLabel = currentFs.getVolumeLabel();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}













