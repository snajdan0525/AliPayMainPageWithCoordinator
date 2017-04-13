package com.snalopainen.coordinatorlayout.alipay.demo.widget;

/**
 * Uri拦截器。有些云平台支持url添加参数,请求不同质量图片。
 * <p/>
 * Created by jinyan on 16/8/19.
 */
public interface BqUriIntercepter {

    String intercept(BqImageView imageView, int resizeWidth, int resizeHeight, String uri);

}
