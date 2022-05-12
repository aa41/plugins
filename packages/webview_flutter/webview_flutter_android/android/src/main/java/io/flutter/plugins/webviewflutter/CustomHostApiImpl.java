package io.flutter.plugins.webviewflutter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;

import com.tencent.smtt.sdk.WebView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomHostApiImpl implements GeneratedAndroidWebView.CustomHostApi {

    private final InstanceManager instanceManager;

    public CustomHostApiImpl(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override
    public void screenShot(Long instanceId, String md5, String ext, String filePath) {
        final WebView webView = (WebView) instanceManager.getInstance(instanceId);
        //  Bitmap bitmap = getViewBitmap(webView);
        // Bitmap bitmap = scrollWebView(webView);
        Bitmap bitmap = captureX5WebViewUnsharp(webView);
        saveBitmap(webView.getContext(), bitmap, md5, ext, filePath);
    }

    private Bitmap captureWebView(WebView webView) {

        int wholeWidth = webView.computeHorizontalScrollRange();
        int wholeHeight = webView.computeVerticalScrollRange();
        Bitmap x5bitmap = Bitmap.createBitmap(wholeWidth, wholeHeight, Bitmap.Config.ARGB_8888);

        Canvas x5canvas = new Canvas(x5bitmap);
        x5canvas.scale((float) wholeWidth / (float) webView.getContentWidth(), (float) wholeHeight / (float) webView.getContentHeight());

        webView.getX5WebViewExtension().snapshotWholePage(x5canvas, false, false);
        return x5bitmap;
    }

    private Bitmap captureX5WebViewUnsharp(WebView webView) {
        if (webView == null) {
            return null;
        }

        try {
            return captureWebView(webView);
        } catch (Throwable t2) {
            int width = webView.getContentWidth();
            int height = webView.getContentHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.getX5WebViewExtension().snapshotWholePage(canvas, false, false);
            return bitmap;
        }

    }

    @Override
    public String customAction(Long instanceId, String params) {
        final WebView webView = (WebView) instanceManager.getInstance(instanceId);

        if (!TextUtils.isEmpty(params)) {
            try {
                JSONObject jsonObject = new JSONObject(params);
                String method = jsonObject.optString("method");
                switch (method) {
                    case "saveWeb":
                        String filePath = jsonObject.optString("filePath");
                        webView.saveWebArchive(filePath);
                        return filePath;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return null;
    }


    @Override
    public void dispose(Long instanceId) {

    }


    public static Bitmap getViewBitmap(WebView mWebView) {
        mWebView.scrollTo(0, 0);
        mWebView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mWebView.layout(0, 0, mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight());
//        mWebView.setDrawingCacheEnabled(true);
//        mWebView.buildDrawingCache();
        Bitmap longImage = Bitmap.createBitmap(mWebView.getWidth(),
                (int) (mWebView.getContentHeight() * mWebView.getScale()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(longImage);
        //  canvas.scale(1.0f, (float) ((ScrollingView) mWebView).getWebContentHeight() / (float) mWebView.getContentHeight());
        mWebView.draw(canvas);
        return longImage;
    }

    private String saveBitmap(Context context, Bitmap bitmap, String md5, String ext, String filePath) {
        // File file = new File(context.getFilesDir(), md5 + File.separator + "screenshot" + File.separator + System.currentTimeMillis() + "." + ext);
        File file = new File(filePath);
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


    public Bitmap scrollWebView(WebView mWebView) {
        mWebView.measure(0, 0);
        int contentHeight = mWebView.getMeasuredHeight();
        int height = mWebView.getHeight();
        int totalScrollCount = contentHeight / height;
        int surplusScrollHeight = contentHeight - (totalScrollCount * height);

        List<Bitmap> datas = new ArrayList<>();
        for (int i = 0; i < totalScrollCount; i++) {
            if (i > 0) {
                mWebView.setScrollY(i * height);
            }
            Bitmap bitmap = getScreenshot(mWebView);
            datas.add(bitmap);
        }

        if (surplusScrollHeight > 0) {
            mWebView.setScrollY(contentHeight);
            Bitmap bitmap = getScreenshot(mWebView);
            datas.add(bitmap);
        }

        int bitmapWidth = datas.get(0).getWidth();
        int bitmapHeight = contentHeight;
        Bitmap bimap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bimap);
        Paint paint = new Paint();
        for (int count = datas.size(), i = 0; i < count; i++) {
            Bitmap data = datas.get(i);
            float left = 0;
            float top = i * data.getHeight();
            Rect src = null;
            RectF des = null;

            if (i == count - 1 && surplusScrollHeight > 0) {
                int srcRectTop = data.getHeight() - surplusScrollHeight;
                src = new Rect(0, srcRectTop, data.getWidth(), data.getHeight());
                des = new RectF(left, top, data.getWidth(), top + surplusScrollHeight);
            } else {
                src = new Rect(0, 0, data.getWidth(), data.getHeight());
                des = new RectF(left, top, data.getWidth(), top + data.getHeight());
            }
            //绘制图片
            canvas.drawBitmap(data, src, des, paint);
        }
        return bimap;
    }

    public Bitmap getScreenshot(WebView view) {
        view.setDrawingCacheEnabled(true);
        Bitmap drawingCache = view.getDrawingCache();
        Bitmap newBitmap = Bitmap.createBitmap(drawingCache);
        view.setDrawingCacheEnabled(false);
        return newBitmap;
    }


//    private Bitmap getWebViewBitmap(WebView webView) {
//        Bitmap bitmap = null;
//        try {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                Picture snapShot = webView.capturePicture();
//                bitmap = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                snapShot.draw(canvas);
//            } else {
//                float scale = webView.getScale();
//                int webViewHeight = (int) (webView.getContentHeight() * scale);
//                bitmap = Bitmap.createBitmap(webView.getWidth(), webViewHeight, Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                //绘制
//                webView.draw(canvas);
//            }
//            return bitmap;
//        } catch (Exception e) {
//        } finally {
//        }
//        return bitmap;
//    }

}
