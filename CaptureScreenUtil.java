package com.dylan_wang.capturescreen;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * 作者:郭阳鹏
 * 时间:2017年03月2017/3/14日
 * 描述:截屏
 */

public class CaptureScreenUtil extends IntentService {

    private final String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/screenshot/";
    private String mImageName = System.currentTimeMillis() + ".png";

    private static final String ACTION_INIT = "captureScreen";
    private static Activity mActivity;

    /**
     * 开始截屏
     * @param activity
     */
    public static void startCaptureScreen(Activity activity) {
        mActivity = activity;
        Intent intent = new Intent(activity, CaptureScreenUtil.class);
        intent.setAction(ACTION_INIT);
        activity.startService(intent);
    }

    public CaptureScreenUtil() {
        super("CaptureScreenUtil");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT.equals(action)) {
                Log.e("screen", sdCardPath + mImageName);
                getScreenHot(mActivity.getWindow().getDecorView(), sdCardPath + mImageName);
            }
        }
    }

    /**
     * 截屏操作
     * @param view     视图
     * @param filePath 保存路径
     */
    private void getScreenHot(View view, String filePath) {
        FileOutputStream fos = null;
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            view.draw(canvas);

            File dirFile = new File(sdCardPath);
            if (!dirFile.exists()) dirFile.mkdirs();

            try {
                fos = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            } catch (FileNotFoundException e) {
                throw new InvalidParameterException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {}
            }

            updatePhotos(filePath);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity,"已经保存至"+sdCardPath+"下", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 通知系统图库更新数据
     * @param filePath
     */
    private void updatePhotos(String filePath){
        File file = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        mActivity.sendBroadcast(intent);
    }
}
