// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebResourceErrorCompat;

import java.util.concurrent.CountDownLatch;

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

    @SuppressLint("RequiresFeature")
    static GeneratedAndroidWebView.WebResourceErrorData createWebResourceErrorData(
            WebResourceErrorCompat error) {
        final GeneratedAndroidWebView.WebResourceErrorData errorData =
                new GeneratedAndroidWebView.WebResourceErrorData();
        errorData.setErrorCode((long) error.getErrorCode());
        errorData.setDescription(error.getDescription().toString());

        return errorData;
    }

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
                    public void reply(String reply){
                        result[0] = reply;
                        latch.countDown();
                    }
                }
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];

    }

    /**
     * Passes arguments from {@link WebViewClient#onPageFinished} to Dart.
     */
    public void onPageFinished(
            WebViewClient webViewClient, WebView webView, String urlArg, Reply<Void> callback) {
        onPageFinished(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                urlArg,
                callback);
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

    /**
     * Passes arguments from {@link androidx.webkit.WebViewClientCompat#onReceivedError(WebView,
     * WebResourceRequest, WebResourceError)} to Dart.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onReceivedRequestError(
            WebViewClient webViewClient,
            WebView webView,
            WebResourceRequest request,
            WebResourceErrorCompat error,
            Reply<Void> callback) {
        onReceivedRequestError(
                instanceManager.getInstanceId(webViewClient),
                instanceManager.getInstanceId(webView),
                createWebResourceRequestData(request),
                createWebResourceErrorData(error),
                callback);
    }

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
