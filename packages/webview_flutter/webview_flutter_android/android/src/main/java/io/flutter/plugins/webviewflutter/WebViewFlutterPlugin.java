// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformViewRegistry;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.CookieManagerHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.CustomHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.DownloadListenerHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.FlutterAssetManagerHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.JavaScriptChannelHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebChromeClientHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebSettingsHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebViewClientHostApi;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebViewHostApi;

/**
 * Java platform implementation of the webview_flutter plugin.
 *
 * <p>Register this in an add to app scenario to gracefully handle activity and context changes.
 *
 * <p>Call {@link #registerWith} to use the stable {@code io.flutter.plugin.common} package instead.
 */
public class WebViewFlutterPlugin implements FlutterPlugin, ActivityAware {
    private FlutterPluginBinding pluginBinding;
    private WebViewHostApiImpl webViewHostApi;
    private JavaScriptChannelHostApiImpl javaScriptChannelHostApi;

    /**
     * Add an instance of this to {@link io.flutter.embedding.engine.plugins.PluginRegistry} to
     * register it.
     *
     * <p>THIS PLUGIN CODE PATH DEPENDS ON A NEWER VERSION OF FLUTTER THAN THE ONE DEFINED IN THE
     * PUBSPEC.YAML. Text input will fail on some Android devices unless this is used with at least
     * flutter/flutter@1d4d63ace1f801a022ea9ec737bf8c15395588b9. Use the V1 embedding with {@link
     * #registerWith} to use this plugin with older Flutter versions.
     *
     * <p>Registration should eventually be handled automatically by v2 of the
     * GeneratedPluginRegistrant. https://github.com/flutter/flutter/issues/42694
     */
    public WebViewFlutterPlugin() {
    }

    /**
     * Registers a plugin implementation that uses the stable {@code io.flutter.plugin.common}
     * package.
     *
     * <p>Calling this automatically initializes the plugin. However plugins initialized this way
     * won't react to changes in activity or context, unlike {@link WebViewFlutterPlugin}.
     */
    @SuppressWarnings({"unused", "deprecation"})
    public static void registerWith(io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
        new WebViewFlutterPlugin().setUp(
                        registrar.messenger(),
                        registrar.platformViewRegistry(),
                        registrar.activity(),
                        registrar.view(),
                        new FlutterAssetManager.RegistrarFlutterAssetManager(
                                registrar.context().getAssets(), registrar));
    }

    private void setUp(
            BinaryMessenger binaryMessenger,
            PlatformViewRegistry viewRegistry,
            Context context,
            View containerView,
            FlutterAssetManager flutterAssetManager) {
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.initX5Environment(context.getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                // 内核初始化完成，可能为系统内核，也可能为系统内核
            }

            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            @Override
            public void onViewInitFinished(boolean isX5) {

            }
        });


        InstanceManager instanceManager = new InstanceManager();

        viewRegistry.registerViewFactory(
                "plugins.flutter.io/webview", new FlutterWebViewFactory(instanceManager));

        webViewHostApi =
                new WebViewHostApiImpl(
                        instanceManager, new WebViewHostApiImpl.WebViewProxy(), context, containerView);
        javaScriptChannelHostApi =
                new JavaScriptChannelHostApiImpl(
                        instanceManager,
                        new JavaScriptChannelHostApiImpl.JavaScriptChannelCreator(),
                        new JavaScriptChannelFlutterApiImpl(binaryMessenger, instanceManager),
                        new Handler(context.getMainLooper()));

        WebViewHostApi.setup(binaryMessenger, webViewHostApi);
        JavaScriptChannelHostApi.setup(binaryMessenger, javaScriptChannelHostApi);
        WebViewClientHostApi.setup(
                binaryMessenger,
                new WebViewClientHostApiImpl(
                        instanceManager,
                        new WebViewClientHostApiImpl.WebViewClientCreator(),
                        new WebViewClientFlutterApiImpl(binaryMessenger, instanceManager)));
        WebChromeClientHostApi.setup(
                binaryMessenger,
                new WebChromeClientHostApiImpl(
                        instanceManager,
                        new WebChromeClientHostApiImpl.WebChromeClientCreator(),
                        new WebChromeClientFlutterApiImpl(binaryMessenger, instanceManager)));
        DownloadListenerHostApi.setup(
                binaryMessenger,
                new DownloadListenerHostApiImpl(
                        instanceManager,
                        new DownloadListenerHostApiImpl.DownloadListenerCreator(),
                        new DownloadListenerFlutterApiImpl(binaryMessenger, instanceManager)));
        WebSettingsHostApi.setup(
                binaryMessenger,
                new WebSettingsHostApiImpl(
                        instanceManager, new WebSettingsHostApiImpl.WebSettingsCreator()));
        FlutterAssetManagerHostApi.setup(
                binaryMessenger, new FlutterAssetManagerHostApiImpl(flutterAssetManager));
        CookieManagerHostApi.setup(binaryMessenger, new CookieManagerHostApiImpl());
        CustomHostApi.setup(binaryMessenger, new CustomHostApiImpl(instanceManager));
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        pluginBinding = binding;
        setUp(
                binding.getBinaryMessenger(),
                binding.getPlatformViewRegistry(),
                binding.getApplicationContext(),
                null,
                new FlutterAssetManager.PluginBindingFlutterAssetManager(
                        binding.getApplicationContext().getAssets(), binding.getFlutterAssets()));
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        updateContext(activityPluginBinding.getActivity());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        updateContext(pluginBinding.getApplicationContext());
    }

    @Override
    public void onReattachedToActivityForConfigChanges(
            @NonNull ActivityPluginBinding activityPluginBinding) {
        updateContext(activityPluginBinding.getActivity());
    }

    @Override
    public void onDetachedFromActivity() {
        updateContext(pluginBinding.getApplicationContext());
    }

    private void updateContext(Context context) {
        webViewHostApi.setContext(context);
        javaScriptChannelHostApi.setPlatformThreadHandler(new Handler(context.getMainLooper()));
    }
}
