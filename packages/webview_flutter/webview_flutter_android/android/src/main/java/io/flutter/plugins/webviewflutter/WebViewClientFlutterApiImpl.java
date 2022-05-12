// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebViewClientFlutterApi;

/**
 * Flutter Api implementation for {@link WebViewClient}.
 *
 * <p>Passes arguments of callbacks methods from a {@link WebViewClient} to Dart.
 */
public class WebViewClientFlutterApiImpl extends WebViewClientFlutterApi {
    private final InstanceManager instanceManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    static GeneratedAndroidWebView.WebResourceErrorData createWebResourceErrorData(
            WebResourceError error) {
        final GeneratedAndroidWebView.WebResourceErrorData errorData =
                new GeneratedAndroidWebView.WebResourceErrorData();
        errorData.setErrorCode((long) error.getErrorCode());
        errorData.setDescription(error.getDescription().toString());

        return errorData;
    }
//
//    @SuppressLint("RequiresFeature")
//    static GeneratedAndroidWebView.WebResourceErrorData createWebResourceErrorData(
//            WebResourceErrorCompat error) {
//        final GeneratedAndroidWebView.WebResourceErrorData errorData =
//                new GeneratedAndroidWebView.WebResourceErrorData();
//        errorData.setErrorCode((long) error.getErrorCode());
//        errorData.setDescription(error.getDescription().toString());
//
//        return errorData;
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static GeneratedAndroidWebView.WebResourceRequestData createWebResourceRequestData(
            WebResourceRequest request) {
        final GeneratedAndroidWebView.WebResourceRequestData requestData =
                new GeneratedAndroidWebView.WebResourceRequestData();
        requestData.setUrl(request.getUrl().toString());
        requestData.setIsForMainFrame(request.isForMainFrame());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestData.setIsRedirect(request.isRedirect());
        }
        requestData.setHasGesture(request.hasGesture());
        requestData.setMethod(request.getMethod());
        requestData.setRequestHeaders(request.getRequestHeaders());

        return requestData;
    }

    /**
     * Creates a Flutter api that sends messages to Dart.
     *
     * @param binaryMessenger handles sending messages to Dart
     * @param instanceManager maintains instances stored to communicate with Dart objects
     */
    public WebViewClientFlutterApiImpl(
            BinaryMessenger binaryMessenger, InstanceManager instanceManager) {
        super(binaryMessenger);
        this.instanceManager = instanceManager;
    }

    /**
     * Passes arguments from {@link WebViewClient#onPageStarted} to Dart.
     */
    public void onPageStarted(
            WebViewClient webViewClient, WebView webView, String urlArg, Reply<Void> callback) {
        onPageStarted(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                urlArg,
                callback);
    }

    public String shouldInterceptRequest(WebViewClient webViewClient, WebView webView, String url) {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] result = new String[1];
        shouldInterceptRequest(instanceManager.getInstanceId(webViewClient), instanceManager.getInstanceId(webView),
                url, new Reply<String>() {
                    @Override
                    public void reply(String reply) {
                        result[0] = reply;
                        latch.countDown();

                    }
                }
        );
        try {
            latch.await(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    private Map<String, String> convertResponseHeaders(Map<String, List<String>> headers) {
        Map<String, String> responseHeaders = new HashMap<>();

        for (Map.Entry<String, List<String>> item : headers.entrySet()) {
            StringBuilder sb = new StringBuilder();
            if (!item.getValue().isEmpty()) {
                for (String headerVal : item.getValue()) {
                    sb.append(headerVal).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
            }
            responseHeaders.put(item.getKey(), sb.toString());
        }

        return responseHeaders;
    }

    /**
     * 从contentType中获取MIME类型
     *
     * @param contentType
     * @return
     */
    private String getMime(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.split(";")[0];
    }

    /**
     * 从contentType中获取编码信息
     *
     * @param contentType
     * @return
     */
    private String getCharset(String contentType) {
        if (contentType == null) {
            return null;
        }

        String[] fields = contentType.split(";");
        if (fields.length <= 1) {
            return null;
        }

        String charset = fields[1];
        if (!charset.contains("=")) {
            return null;
        }
        charset = charset.substring(charset.indexOf("=") + 1);
        return charset;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequestV2(WebViewClient webViewClient, WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        String reply = shouldInterceptRequest(webViewClient, view, url);
        if (reply == null) return null;
        boolean hasOrigin = (request.getRequestHeaders() != null && request.getRequestHeaders().containsKey("Origin"));
        boolean needsDownload = true;
        String fileName = null;
        try {
            if (!TextUtils.isEmpty(reply)) {
                JSONObject jsonObject = new JSONObject(reply);
                fileName = jsonObject.optString("filePath");
                String mimeType = jsonObject.optString("mimeType");
                String encoding = jsonObject.optString("encoding");
                needsDownload = (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(mimeType) || TextUtils.isEmpty(encoding));
                if (!TextUtils.isEmpty(fileName)) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        try {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            if (hasOrigin) {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("access-control-allow-origin", "*");
                                headers.put("access-control-allow-credentials", "true");
                                headers.put("access-control-allow-methods", "GET POST OPTIONS");
                                headers.put("Access-Control-Allow-Headers", "Content-Type");
                                return new WebResourceResponse(mimeType, encoding, 200, "ok", headers, fileInputStream);
                            }
                            return new WebResourceResponse(mimeType, encoding, fileInputStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        final String method = request.getMethod().toLowerCase();
        try {
            if ("get".equals(method) && !TextUtils.isEmpty(url)
                    && url.startsWith("http")) {
                URL uri = new URL(url);
                CookieManager cookieManager = CookieManager.getInstance();
                String cookie = cookieManager.getCookie(uri.getHost());
                HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Cookie", cookie);
                Map<String, String> requestHeaders = request.getRequestHeaders();
                if (requestHeaders != null && !requestHeaders.isEmpty()) {
                    for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                conn.setUseCaches(false);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String contentType = conn.getContentType();
                    String mimeType = getMime(contentType);
                    String charset = getCharset(contentType);
                    if (!TextUtils.isEmpty(mimeType)) {
                        Map<String, String> responseHeaders = convertResponseHeaders(conn.getHeaderFields());
                        if (hasOrigin) {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("access-control-allow-origin", "*");
                            headers.put("access-control-allow-credentials", "true");
                            headers.put("access-control-allow-methods", "GET POST OPTIONS");
                            headers.put("Access-Control-Allow-Headers", "Content-Type");
                            responseHeaders.putAll(headers);
                        }
                        if (needsDownload) {
                            if (fileName == null) {
                                fileName = view.getContext().getFilesDir().getAbsolutePath() + File.separator + "offline" + File.separator + System.currentTimeMillis();
                            }
                            try {
                                InputStream inputStream = conn.getInputStream();
                                OutputStream os = new FileOutputStream(fileName);
                                int bytesRead = 0;
                                byte[] buffer = new byte[1024];
                                while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                                os.close();
                                inputStream.close();
                                String finalFileName = fileName;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String webUrl = view.getUrl();
                                        JSONObject jsonObject = new JSONObject();
                                        if (requestHeaders != null) {
                                            try {
                                                jsonObject.put("requestHeaders", new JSONObject(requestHeaders));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        try {
                                            jsonObject.put("filePath", finalFileName);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        sendInterceptRequest(webViewClient, view, url, mimeType, webUrl, TextUtils.isEmpty(charset) ? Charset.defaultCharset().name() : charset, jsonObject.toString());
                                    }
                                });
                                return new WebResourceResponse(mimeType, TextUtils.isEmpty(charset) ? Charset.defaultCharset().name() : charset, responseCode, conn.getResponseMessage(), responseHeaders, new FileInputStream(fileName));
                            } catch (Exception e) {

                            }


                        }

                        return new WebResourceResponse(mimeType, TextUtils.isEmpty(charset) ? Charset.defaultCharset().name() : charset, responseCode, conn.getResponseMessage(), responseHeaders, conn.getInputStream());

                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendInterceptRequest(WebViewClient webViewClient, WebView webView, String requestUrl, String mimeType, String webUrl, String encoding, String requestHeaders) {
        sendInterceptRequest(instanceManager.getInstanceId(webViewClient), instanceManager.getInstanceId(webView),
                requestUrl, mimeType, webUrl, encoding, requestHeaders, new Reply<String>() {
                    @Override
                    public void reply(String reply) {

                    }
                });
    }


    /**
     * Passes arguments from {@link WebViewClient#onPageFinished} to Dart.
     */
    public void onPageFinished(
            WebViewClient webViewClient, WebView webView, String urlArg, BasicMessageChannel.Reply<Void> callback) {
//        onPageFinished(
//                instanceManager.getInstanceId(webViewClient),
//                instanceManager.getInstanceId(webView),
//                urlArg,
//                callback);
    }

    /**
     * Passes arguments from {@link WebViewClient#onReceivedError(WebView, WebResourceRequest,
     * WebResourceError)} to Dart.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceivedRequestError(
            WebViewClient webViewClient,
            WebView webView,
            WebResourceRequest request,
            WebResourceError error,
            Reply<Void> callback) {
        onReceivedRequestError(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                createWebResourceRequestData(request),
                createWebResourceErrorData(error),
                callback);
    }

//    /**
//     * Passes arguments from {@link androidx.webkit.WebViewClientCompat#onReceivedError(WebView,
//     * WebResourceRequest, WebResourceError)} to Dart.
//     */
//    public void onReceivedRequestError(
//            WebViewClient webViewClient,
//            WebView webView,
//            WebResourceRequest request,
//            WebResourceError error,
//            BasicMessageChannel.Reply<Void> callback) {
//        onReceivedRequestError(
//                instanceManager.getInstanceId(webViewClient),
//                instanceManager.getInstanceId(webView),
//                createWebResourceRequestData(request),
//                createWebResourceErrorData(error),
//                callback);
//    }

    /**
     * Passes arguments from {@link WebViewClient#onReceivedError(WebView, int, String, String)} to
     * Dart.
     */
    public void onReceivedError(
            WebViewClient webViewClient,
            WebView webView,
            Long errorCodeArg,
            String descriptionArg,
            String failingUrlArg,
            Reply<Void> callback) {
        onReceivedError(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                errorCodeArg,
                descriptionArg,
                failingUrlArg,
                callback);
    }

    /**
     * Passes arguments from {@link WebViewClient#shouldOverrideUrlLoading(WebView,
     * WebResourceRequest)} to Dart.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void requestLoading(
            WebViewClient webViewClient,
            WebView webView,
            WebResourceRequest request,
            Reply<Void> callback) {
        requestLoading(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                createWebResourceRequestData(request),
                callback);
    }

    /**
     * Passes arguments from {@link WebViewClient#shouldOverrideUrlLoading(WebView, String)} to Dart.
     */
    public void urlLoading(
            WebViewClient webViewClient, WebView webView, String urlArg, Reply<Void> callback) {
        urlLoading(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                urlArg,
                callback);
    }

    /**
     * Communicates to Dart that the reference to a {@link WebViewClient} was removed.
     *
     * @param webViewClient the instance whose reference will be removed
     * @param callback      reply callback with return value from Dart
     */
    public void dispose(WebViewClient webViewClient, Reply<Void> callback) {
        final Long instanceId = instanceManager.removeInstance(webViewClient);
        if (instanceId != null) {
            dispose(instanceId, callback);
        } else {
            callback.reply(null);

        }
    }
}
