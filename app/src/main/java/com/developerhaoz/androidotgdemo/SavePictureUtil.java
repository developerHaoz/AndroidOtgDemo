package com.developerhaoz.androidotgdemo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static android.content.ContentResolver.SCHEME_CONTENT;

/**
 * @author Haoz
 * @date 2018/1/6.
 */
public class SavePictureUtil {


    /**
     * 将图片保存在 U 盘中
     *
     * @param picturePathList
     * @throws IOException
     */
    public static void savePictureToUsb(List<String> picturePathList, UsbFile root, OnFinishCallback callback) throws IOException {
        for (int i = 0; i < picturePathList.size(); i++) {
            UsbFile newDir = root.createDirectory("Haoz" + System.currentTimeMillis());
            UsbFile file = newDir.createFile("Haoz" + System.currentTimeMillis() + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(picturePathList.get(i));
            OutputStream outputStream = new UsbFileOutputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            outputStream.write(out.toByteArray());
            out.flush();
            out.close();
            outputStream.flush();
            outputStream.close();
            file.flush();
            file.close();
        }
        callback.onFinish();
    }

    public interface OnFinishCallback{
        void onFinish();
    }

    public static String getPath(ContentResolver resolver, Uri uri) {
        if (uri == null) {
            return null;
        }

        if (SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, new String[]{MediaStore.Images.ImageColumns.DATA},
                        null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    return null;
                }
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return uri.getPath();
    }
}
