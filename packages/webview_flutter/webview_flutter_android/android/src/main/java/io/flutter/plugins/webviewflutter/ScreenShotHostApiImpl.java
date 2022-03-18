package io.flutter.plugins.webviewflutter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Build;
import android.webkit.WebView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ScreenShotHostApiImpl implements GeneratedAndroidWebView.ScreenShotHostApi {

    private final InstanceManager instanceManager;

    public ScreenShotHostApiImpl(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override
    public String screenShot(Long instanceId, String md5, String ext) {
        final WebView webView = (WebView) instanceManager.getInstance(instanceId);
        Bitmap bitmap = getWebViewBitmap(webView);
        return saveBitmap(webView.getContext(), bitmap, md5, ext);
    }

    @Override
    public void dispose(Long instanceId) {

    }

    private String saveBitmap(Context context, Bitmap bitmap, String md5, String ext) {
        File file = new File(context.getFilesDir(), md5 + File.separator + "screenshot" + File.separator + System.currentTimeMillis() + "." + ext);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(ext.equals("jpeg") ?
                    Bitmap.CompressFormat.JPEG
                    : Bitmap.CompressFormat.PNG, 100, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file.getAbsolutePath();

    }

    private Bitmap getWebViewBitmap(WebView webView) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Picture snapShot = webView.capturePicture();
                bitmap = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                snapShot.draw(canvas);
            } else {
                float scale = webView.getScale();
                int webViewHeight = (int) (webView.getContentHeight() * scale);
                bitmap = Bitmap.createBitmap(webView.getWidth(), webViewHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                //绘制
                webView.draw(canvas);
            }
            return bitmap;
        } catch (Exception e) {
        } finally {
        }
        return bitmap;
    }

}
