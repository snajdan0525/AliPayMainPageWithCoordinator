package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jinyan on 17/3/14.
 */

public class QiniuImageParamHelper {

    private static Pattern QINIU_PATTERN = Pattern.compile("https?:\\/\\/.*(\\?imageView2\\/2(\\/w\\/(\\d+))?(\\/h\\/(\\d+))?\\/.*\\/format\\/(\\w*))");
    private static final int GROUP_ALL_QINIU_PARAM = 1;
    private static final int GROUP_WIDTH = 3;
    private static final int GROUP_HEIGHT = 5;
    private static final int GROUP_FORMAT = 6;

    public static String changeSizeAndFormat(String uri, int resizeWidth, int resizeHeight) {
        Matcher matcher = QINIU_PATTERN.matcher(uri);
        if (matcher.matches()) {
            String sw = matcher.group(GROUP_WIDTH);
            String sh = matcher.group(GROUP_HEIGHT);
            int w = parseInt(sw, 0);
            int h = parseInt(sh, 0);

            /* 注意：替换时从后面开始替换，不然位置会变化。 */
            StringBuilder sb = new StringBuilder(uri);
            sb.replace(matcher.start(GROUP_FORMAT), matcher.end(GROUP_FORMAT), "webp");

            if (h > resizeHeight) {
                sb.replace(matcher.start(GROUP_HEIGHT), matcher.end(GROUP_HEIGHT), Integer.toString(resizeHeight));
            }

            if (w > resizeWidth) {
                sb.replace(matcher.start(GROUP_WIDTH), matcher.end(GROUP_WIDTH), Integer.toString(resizeWidth));
            }
            uri = sb.toString();
        }

        return uri;
    }

    public static String removeQiuniuParam(String uri) {
        Matcher matcher = QINIU_PATTERN.matcher(uri);
        if (matcher.matches()) {
            StringBuilder sb = new StringBuilder(uri);
            sb.delete(matcher.start(GROUP_ALL_QINIU_PARAM), matcher.end(GROUP_ALL_QINIU_PARAM));
            uri = sb.toString();
        }
        return uri;
    }

    private static int parseInt(String s, int valueIfParseFailed) {
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return valueIfParseFailed;
        }
    }

}
