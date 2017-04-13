package com.snalopainen.coordinatorlayout.alipay.demo;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;

/**
 * 图片框架入口。封装底层实现。
 * Created by JinYan on 2016/7/29.
 */
public final class BqImage {

    /**
     * 所有图片默认resize。对应@dimen/resize_width_common
     */
    public static final Resize COMMON = new Resize(360, 360);
    /**
     * 高清resize。对应@dimen/resize_width_hd
     */
    public static final Resize HD = new Resize(640, 640);
    /**
     * 超高清，目前仅用于图片预览。对应@dimen/resize_width_super_hd
     */
    public static final Resize SUPER_HD = new Resize(960, 960);

    private static ImageImp imp;

    private static int defaultGlobalResizeWidth;
    private static int defaultGlobalResizeHeight;

    /**
     * Let BqImageView only load remote image file in wifi connection.
     */
    private static boolean wifiOnly = false;

    /**
     * 初始化BqImage,请在Application:onCreate中进行初始化。
     *
     * @param context
     */
    public static void initialize(Context context) {
        initialize(context, null);
    }

    public static void initialize(Context context, Object initParam) {
        if (imp == null) {
            imp = new FrescoImp();
            imp.initialize(context, initParam);

            defaultGlobalResizeWidth = COMMON.width;
            defaultGlobalResizeHeight = COMMON.height;
        }
    }

    public static boolean isWifiOnly() {
        return wifiOnly;
    }

    public static void setWifiOnly(boolean wifiOnly) {
        BqImage.wifiOnly = wifiOnly;
    }

    /**
     * 异步加载图片的bitmap。加载操作和callback调用都在后台线程进行。
     *
     * @param uri
     * @param callback
     */
    public static void loadBitmap(String uri, BqImageCallback callback) {
        imp.loadBitmap(uri, callback);
    }

    /**
     * 异步加载图片的bitmap。加载操作和callback调用都在后台线程进行。width和height只是一个建议值,具体返回bitmap大小可能不一致。
     * 参考: http://fresco-cn.org/docs/resizing-rotating.html
     *
     * @param uri
     * @param callback
     */
    public static void loadBitmap(String uri, int width, int height, final BqImageCallback callback) {
        imp.loadBitmap(uri, width, height, callback);
    }

    private static boolean HANDLE_QINIU = true;


    /******
     * uri 拦截器 本拦截器为默认拦截器。TODO: refine me: intercepter应该由使用者传入。
     ****/
    private static BqUriIntercepter globalIntercepter = new BqUriIntercepter() {
        @Override
        public String intercept(BqImageView imageView, int resizeWidth, int resizeHeight, String uri) {
            /* workaround: 有些uri是文件路径,这里转一下。主要是防止用错。 */
            Uri u = Uri.parse(uri);
            String schema = u.getScheme();
            if (TextUtils.isEmpty(schema)) {
                return Uri.fromFile(new File(uri)).toString();
            } else if ("https".equalsIgnoreCase(schema)) {
                uri = "http" + uri.substring(5);
            }

            if (HANDLE_QINIU) {
                uri = QiniuImageParamHelper.changeSizeAndFormat(uri, resizeWidth, resizeHeight);
            }

            return uri;
        }
    };

    /**
     * 设置全局到uri拦截器
     *
     * @param intercepter
     */
    public static void setGlobalUriIntercepter(BqUriIntercepter intercepter) {
        globalIntercepter = intercepter;
    }

    public static BqUriIntercepter getGlobalIntercepter() {
        return globalIntercepter;
    }

    public static void setDefaultGlobalResize(int width, int height) {
        defaultGlobalResizeWidth = width;
        defaultGlobalResizeHeight = height;
    }

    public static int getDefaultGlobalResizeWidth() {
        return defaultGlobalResizeWidth;
    }

    public static int getDefaultGlobalResizeHeight() {
        return defaultGlobalResizeHeight;
    }

    /*******图片效果*******/
    /**
     * bqimage支持的各种效果在这里添加定义。
     */
    public static interface Processors {
        int NONE = 0;
        int BLUR = 1;
        int SHADOW = 2;
        int MIRROR = 3;/* 左右镜面对称 */
    }

    /**
     * 根据uri活动图片本地缓存路径
     *
     * @param loadUri
     * @return
     */
    public static File getCachedImageOnDisk(String loadUri) {
        File localFile = null;
        if (loadUri != null) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(loadUri), null);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache()
                        .getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache()
                        .getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }

    public static void clearCache() {
        imp.clearCache();
    }

    public static long getImageCacheSize() {
        return imp.getCacheSize();
    }

    /**
     * 图片分辨率，用于定义各种场景的图片resize
     */
    public static class Resize {
        public final int width;
        public final int height;

        public Resize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
