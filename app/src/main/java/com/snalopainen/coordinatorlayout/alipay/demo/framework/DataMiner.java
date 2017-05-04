package com.snalopainen.coordinatorlayout.alipay.demo.framework;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.boqii.android.framework.data.cache.Cache;
import com.boqii.android.framework.data.cache.DiskCache;
import com.boqii.android.framework.data.entity.ResultEntity;
import com.boqii.android.framework.util.MD5;
import com.boqii.android.framework.util.StringUtil;
import com.boqii.android.framework.util.TaskUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据矿工：专门负责从网络或者缓存挖数据
 * "矿工"中包含请求信息、返回结果等。
 * <p/>
 * Author: sajdan
 */
public class DataMiner {

    private static final String TAG = DataMiner.class.getSimpleName();

    public static class DataMinerError {
        /**
         * 请求网路失败。如捕获ConnectionException...
         */
        public static final int ERROR_TYPE_NETWORK = 0;
        /**
         * 服务器返回数据，但是本地解析失败。
         */
        public static final int ERROR_TYPE_SERVER_ERROR = 1;
        /**
         * 服务器返回数据，但是业务逻辑状态非0
         */
        public static final int ERROR_TYPE_BUSINESS = 2;
        /**
         * 本地job错误
         */
        public static final int ERROR_TYPE_LOCAL = 3;

        private int type;
        private int errorCode;
        private String errorMsg;

        public DataMinerError(int type, int errorCode, String errorMsg) {
            this.type = type;
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }

        public int getType() {
            return type;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    public static interface DataMinerObserver {

        /**
         * 返回方式类似android事件机制。如果client没有处理请返回false，采用DataMiner默认的处理。
         *
         * @return true client已经消化了错误
         * <p/>
         * false client没有处理错误，采用DataMiner默认的处理方式：toast错误内容。
         */
        boolean onDataError(DataMiner miner, DataMinerError error);

        void onDataSuccess(DataMiner miner);
    }

    /**
     * 指派给数据矿工的本地任务（非http任务，比如io读取本地数据文件，数据库等）
     */
    public static interface DataMinerLocalJob {
        public static class LocalJobException extends Exception {
            private static final long serialVersionUID = 4353471345996100057L;
        }

        Object execute();
    }

    /**
     * 数据矿工在后台线程中的额外工作，例如：将数据持久化到本地
     */
    public static interface DataMinerExtraWork {

        void doExtraWork(DataMiner miner);

    }

    public static class DataMinerException extends RuntimeException {

        private static final long serialVersionUID = -6899936624203978437L;

        public DataMinerException(String detailMessage) {
            super(detailMessage);
        }

    }

    public enum FetchType {
        /* 默认请求方式: 如果缓存没有过期, 通知缓存数据, 任务结束。如果缓存过期, 但是maxStale没有过期, 先通知老数据, 后台继续请求服务器, 请求到新的数据后再次通知。 */
        Normal(0),
        /*  如果缓存没有过期, 通知缓存数据, 任务结束。如果缓存过期了, 先请求服务器, 如果请求成功则通知新数据, 如果请求失败则通知老数据 */
        FailThenStale(1),
        /*  无视是否有缓存, 先请求服务器, 如果请求成功则通知新数据, 如果请求失败则通知缓存数据。 */
        PreferRemote(2),
        /* 完全无视缓存。网路请求成功则通知数据, 请求失败则通知失败。 */
        OnlyRemote(3);

        FetchType(int ni) {
            value = ni;
        }

        final int value;
    }

    public static class DataMinerBuilder {

        public DataMinerBuilder httpMethod(String method) {
            httpMethod = method;
            return this;
        }

        public DataMinerBuilder url(String url) {
            this.url = url;
            return this;
        }

        public DataMinerBuilder headerMap(ArrayMap<String, String> headerMap) {
            this.headerMap = headerMap;
            return this;
        }

        public DataMinerBuilder queryMap(ArrayMap<String, String> queryMap) {
            this.queryMap = queryMap;
            return this;
        }

        public DataMinerBuilder fileMap(ArrayMap<String, UploadFile> fileMap) {
            this.fileMap = fileMap;
            return this;
        }

        public DataMinerBuilder observer(DataMinerObserver observer) {
            this.dataMinerObserver = observer;
            return this;
        }

        public DataMinerBuilder localJob(DataMinerLocalJob localJob) {
            this.localJob = localJob;
            return this;
        }

        public DataMinerBuilder dataType(Class<?> dataType) {
            this.dataType = dataType;
            return this;
        }

        public DataMinerBuilder cache(boolean cache, long maxAge, long maxStale) {
            this.cache = cache;
            this.maxAge = maxAge;
            this.maxStale = maxStale;
            return this;
        }

        public DataMiner build() {
            return localJob == null ?
                    new DataMiner(httpMethod, url, headerMap, queryMap, fileMap, dataType, cache, maxAge, maxStale, dataMinerObserver) :
                    new DataMiner(localJob, dataMinerObserver);
        }

        /**
         * http请求方法类型
         */
        private String httpMethod;
        /**
         * 请求的url
         */
        private String url;
        /**
         * 请求header的参数对
         */
        private ArrayMap<String, String> headerMap;
        /**
         * 请求的参数对
         */
        private ArrayMap<String, String> queryMap;
        /**
         * 上传的文件参数
         */
        private ArrayMap<String, UploadFile> fileMap;
        /**
         * 工作结束监听者
         */
        private DataMinerObserver dataMinerObserver;
        /**
         * 本地工作
         */
        private DataMinerLocalJob localJob;
        /**
         * 数据类型
         */
        private Class<?> dataType;
        /**
         * 是否缓存
         */
        private boolean cache;
        /**
         * 缓存过期时间
         */
        private long maxAge;
        /**
         * 缓存过期时间
         */
        private long maxStale;

    }

    public boolean isSuccess() {
        return data != null && ((ResultEntity) data).isSuccess();
    }

    public void work(FetchType fetchType) {
        if (requestStartTime != 0L) {
            throw new RuntimeException("一个矿工只能工作一次");
        }
        requestStartTime = System.currentTimeMillis();
        if (BqData.DEBUG) {
            Log.d(TAG, "数据矿工开始工作：" + getUrl());
        }
        if (localJob != null) {
            asyncDoLocalJob();
        } else {
            createFetchPolicy(fetchType).execute();
        }

        if (isShowLoadingDialog()) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.show(loadingDialogcontextRef.get(), loadingMessage);
                }
            });
        }
    }

    public void work() {
        work(FetchType.Normal);
    }

    public <T> T workSync(FetchType fetchType) {
        T result;
        requestStartTime = System.currentTimeMillis();

        if (localJob != null) {
            result = (T) localJob.execute();
        } else {
            if (BqData.DEBUG) {
                Log.d(TAG, "数据矿工开始工作：" + getUrl());
            }
            createFetchPolicy(fetchType).executeSync();
            result = (T) data;
        }
        /* 统计 */
        requestNetworkTime = System.currentTimeMillis() - requestStartTime;
        return result;
    }

    public <T> T workSync() {
        return workSync(FetchType.Normal);
    }

    public void downloadFile(File targetFile) {
        new FileDownloadFetchPolicy(this, targetFile).execute();

        if (isShowLoadingDialog()) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.show(loadingDialogcontextRef.get(), loadingMessage);
                }
            });
        }
    }

    public void downloadFileSync(File targetFile) {
        new FileDownloadFetchPolicy(this, targetFile).executeSync();
    }

    private FetchPolicy createFetchPolicy(FetchType fetchType) {
        switch (fetchType) {
            case Normal:
                return new NormalFetchPolicy(this);
            case FailThenStale:
                return new FailThenStaleFetchPolicy(this);
            case PreferRemote:
                return new PreferRemoteFetchPolicy(this);
            case OnlyRemote:
                return new OnlyRemoteFetchPolicy(this);
            default:
                return new NormalFetchPolicy(this);
        }
    }

    public boolean tryGetCache() {
        if (!cache) {
            return false;
        }
        if (localJob != null) {
            return false;
        }
        final long now = System.currentTimeMillis();
        String cacheKey = getCacheKey();
        Cache cache = DiskCache.getInstance().getCache(cacheKey);
        if (!StringUtil.isEmpty(cache.data)) {
            data = json2data(cache.data, dataType);
            /* 统计 */
            fetchCacheTime = System.currentTimeMillis() - now;
            if (BqData.DEBUG) {
                Log.d(TAG, "%%%%%%%%%%%%%%Get cache%%%%%%%%%%%%%%");
                Log.d(TAG, toString());
            }
        }
        return data != null && ((ResultEntity) data).isSuccess();
    }

    /**
     * clear cache manually
     */
    public void deleteCache() {
        DiskCache.getInstance().deleteCache(getCacheKey());
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        if (data == null) {
            throw new DataMinerException("矿工还没有挖到矿:(");
        }
        return (T) data;
    }

    public int getId() {
        return id;
    }

    public DataMiner setId(int id) {
        this.id = id;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public DataMiner setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public DataMiner showLoading(Context context, CharSequence msg) {
        loadingMessage = msg;
        loadingDialogcontextRef = new WeakReference<Context>(context);
        return this;
    }

    public DataMiner showLoading(Context context) {
        return showLoading(context, context.getText(R.string.loading));
    }

    String getCacheKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(httpMethod);
        sb.append(url);
        if (queryMap != null) {
            for (String key : queryMap.keySet()) {
                sb.append(key).append(queryMap.get(key));
            }
        }
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                sb.append(key).append(headerMap.get(key));
            }
        }
        return MD5.getMD5(sb.toString());
    }


    /**
     * 设置数据矿工在后台线程中的额外工作，例如：将数据持久化到本地 注意：extraWork是在后台线程工作的，非UI线程!
     */
    public void setExtraWork(DataMinerExtraWork extraWork) {
        this.extraWork = extraWork;
    }

    String getUrl() {
        return url;
    }

    ArrayMap<String, String> getHeaderMap() {
        return headerMap;
    }

    ArrayMap<String, String> getQueryMap() {
        return queryMap;
    }

    ArrayMap<String, UploadFile> getFileMap() {
        return fileMap;
    }

    String getHttpMethod() {
        return httpMethod;
    }

    void setResult(int errorCode, String response) {

        if (isShowLoadingDialog()) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.hide();
                }
            });
        }

        data = null;
        networkErrorCode = errorCode;

        /* 2xx都认为成功 */
        if (errorCode >= NetworkError.SUCCESS && errorCode <= NetworkError.SUCCESS_MAX) {
            final ResultEntity _data = (ResultEntity) json2data(response, dataType);
            if (_data != null) {
                /* 请求成功时才更新data（防止错误data把之前从cache中获取的data覆盖） */
                data = _data;
                if (_data.isSuccess()) {
                    if (extraWork != null) {
                        extraWork.doExtraWork(this);
                    }
                    if (dataMinerObserver != null) {
                        dataMinerObserver.onDataSuccess(this);
                    }
                } else {
                    /* dataMinerObserver.onDataError 返回true表示使用者已经消化了错误。类似Android事件机制。 */
                    if (dataMinerObserver != null
                            && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_BUSINESS, _data.getResponseStatus(), _data.getResponseMsg()))) {
                        if (_data.getResponseStatus() >= ResultEntity.RESPONSE_STATUS_ERROR_USER) {
                            BqData.notifyMsg(_data.getResponseMsg());
                        }
                    }
                }
            } else {
                /* 有可能服务器返回200, 但是数据格式不对, 导致解析失败。这时认为服务器无法访问。 */
                networkErrorCode = NetworkError.ERROR_SERVER_NOT_AVAILABLE;
                if (dataMinerObserver != null
                        && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_SERVER_ERROR, networkErrorCode, NetworkError.stringOfNetorkError(networkErrorCode)))) {
                    if (BqData.DEBUG) {
                        BqData.notifyMsg("服务器返回200,但是解析数据错误。[内部错误 release版本不显示]");
                    } else {
                        BqData.notifyMsg(NetworkError.stringOfNetorkError(networkErrorCode));
                    }
                }
            }
        } else {
            if (NetworkError.isConnectionError(errorCode)) {
                if (dataMinerObserver != null
                        && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_NETWORK, errorCode, NetworkError.stringOfNetorkError(errorCode)))) {
                    BqData.notifyMsg(NetworkError.stringOfNetorkError(errorCode));
                }
            } else {
                if (dataMinerObserver != null
                        && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_NETWORK, errorCode, NetworkError.stringOfNetorkError(errorCode)))) {
                    if (BqData.DEBUG) {
                        BqData.notifyMsg(NetworkError.stringOfNetorkError(errorCode) + " [内部错误 release版本不显示]");
                    }
                }
            }
        }

        /* 统计 */
        requestNetworkTime = System.currentTimeMillis() - requestStartTime;

        if (BqData.DEBUG) {
            Log.d(TAG, toString());
        }
    }

    void setDownloadFileResult(int errorCode, File file, String mimeType) {

        if (isShowLoadingDialog()) {
            TaskUtil.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LoadingDialog.hide();
                }
            });
        }

        data = new DownloadFile(mimeType, file);
        if (dataMinerObserver != null) {
            if (errorCode >= NetworkError.SUCCESS && errorCode <= NetworkError.SUCCESS_MAX) {
                dataMinerObserver.onDataSuccess(this);
            } else {
                if (NetworkError.isConnectionError(errorCode)) {
                    if (dataMinerObserver != null
                            && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_NETWORK, errorCode, NetworkError.stringOfNetorkError(errorCode)))) {
                        BqData.notifyMsg(NetworkError.stringOfNetorkError(errorCode));
                    }
                } else {
                    if (dataMinerObserver != null
                            && !dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_NETWORK, errorCode, NetworkError.stringOfNetorkError(errorCode)))) {
                        if (BqData.DEBUG) {
                            BqData.notifyMsg(NetworkError.stringOfNetorkError(errorCode) + " [内部错误 release版本不显示]");
                        }
                    }
                }
            }
        }
    }

    void setSuccessForLocalJob(Object result) {
        if (dataMinerObserver == null) {
            throw new DataMinerException("异步调用必须要有一个监工吧 T_T");
        }

        /* 目前假设本地工作不会失败（改进：增加异常处理）。 */
        networkErrorCode = NetworkError.SUCCESS;

        data = result;

        /* 统计 */
        requestNetworkTime = System.currentTimeMillis() - requestStartTime;

        dataMinerObserver.onDataSuccess(this);

        if (BqData.DEBUG) {
            Log.d(TAG, toString());
        }
    }

    void setFailForLocalJob(Exception e) {
        if (dataMinerObserver == null) {
            throw new DataMinerException("异步调用必须要有一个监工吧 T_T");
        }

        networkErrorCode = NetworkError.ERROR_NO_NETWORK_CONNECTION;

        /* 统计 */
        requestNetworkTime = System.currentTimeMillis() - requestStartTime;

        dataMinerObserver.onDataError(this, new DataMinerError(DataMinerError.ERROR_TYPE_LOCAL, 0, null));

        if (BqData.DEBUG) {
            Log.d(TAG, toString());
        }
    }

    private Object json2data(String response, Class<?> dataType) {
        JsonParser parser = sEntityParsers.get(dataType);
        if (parser != null) {
            try {
                return parser.parse(response);
            } catch (Throwable t) {
            }
        }
        return BqJSON.parse(response, dataType);
    }

    DataMiner(String httpMethod, String url, ArrayMap<String, String> headerMap, ArrayMap<String, String> queryMap, ArrayMap<String, UploadFile> fileMap,
              Class<?> dataType, boolean cache, long maxAge, long maxStale, DataMinerObserver dataMinerObserver) {
        id = mCount.getAndIncrement();
        this.httpMethod = httpMethod;
        this.url = url;
        this.headerMap = headerMap;
        this.queryMap = queryMap;
        this.fileMap = fileMap;
        this.dataType = dataType;
        this.dataMinerObserver = dataMinerObserver;
        this.localJob = null;
        this.cache = cache;
        this.maxAge = maxAge;
        this.maxStale = maxStale;
    }

    DataMiner(DataMinerLocalJob localJob, DataMinerObserver dataMinerObserver) {
        id = mCount.getAndIncrement();
        this.httpMethod = null;
        this.url = null;
        this.headerMap = null;
        this.queryMap = null;
        this.fileMap = null;
        this.dataMinerObserver = dataMinerObserver;
        this.localJob = localJob;
        this.dataType = null;
        this.cache = false;
        this.maxAge = 0;
        this.maxStale = 0;
    }

    private void asyncDoLocalJob() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Object data = localJob.execute();
                    // TODO refine local job错误处理
                    if (data != null) {
                        setSuccessForLocalJob(data);
                    } else {
                        setFailForLocalJob(null);
                    }
                } catch (Exception e) {
                    setFailForLocalJob(e);
                }
            }
        }).start();
    }

    private final AtomicInteger mCount = new AtomicInteger(1);

    /*************************** input ******************************/
    /**
     * 唯一标识一个request
     */
    private int id;
    /**
     * http请求方法类型
     */
    final private String httpMethod;
    /**
     * 请求的url
     */
    final private String url;
    /**
     * 请求header的参数map
     */
    final private ArrayMap<String, String> headerMap;
    /**
     * 请求的参数map
     */
    final private ArrayMap<String, String> queryMap;
    /**
     * 请求的上传文件map
     */
    final private ArrayMap<String, UploadFile> fileMap;
    /**
     * 这次指派的是本地工作
     */
    final private DataMinerLocalJob localJob;
    /**
     * response json对应的entity数据类型
     */
    final private Class<?> dataType;
    /**
     * 是否缓存
     */
    final boolean cache;
    /**
     * 缓存过期时间
     */
    final long maxAge;
    /**
     * 缓存过期时间
     */
    final long maxStale;
    /**
     * 工作结束监听者
     */
    final private DataMinerObserver dataMinerObserver;
    /**
     * 后台线程中的额外工作，例如：持久化数据到本地。
     */
    private DataMinerExtraWork extraWork;
    /**
     * 类似于view的tag，可以保存任何数据
     */
    private Object tag;
    /**
     * 用于显示loading dialog的context
     */
    private WeakReference<Context> loadingDialogcontextRef;
    /**
     * 用于显示loading dialog
     */
    private CharSequence loadingMessage;

    /*************************** output ****************************/
    /**
     * 网络请求错误代码
     */
    private int networkErrorCode = NetworkError.NOT_FINISHED;
    /**
     * 这个是真正需要获取的数据对象
     */
    private Object data;

    // 以下由于统计时间
    private long fetchCacheTime;
    private long requestStartTime = 0L;
    private long requestNetworkTime;

    private boolean isShowLoadingDialog() {
        return loadingDialogcontextRef != null && loadingDialogcontextRef.get() != null && loadingMessage != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/****************************DUMP MINER BEGIN*************************************/\n");
        if (localJob != null) {
            sb.append("LOCAL JOB").append("\n");
        } else {
            sb.append("Request URL: ").append("[").append(httpMethod).append("]").append(url).append("\n");
            sb.append("Need cache: ").append(cache);
            if (cache) {
                sb.append(", maxAge=").append(maxAge).append(", maxStale=").append(maxStale);
            }
            sb.append("\n");
            sb.append("Newtork Error Code: [").append(NetworkError.stringOfNetorkError(networkErrorCode)).append("]\n");
        }
        sb.append("Data: ");
        if (data == null) {
            sb.append("NULL");
        } else {
            ResultEntity resultEntity;
            if (data instanceof ResultEntity && !(resultEntity = (ResultEntity) data).isSuccess()) {
                sb.append("数据错误：[").append(resultEntity.getResponseStatus()).append("] ").append(resultEntity.getResponseMsg());
            } else {
                sb.append(data);
            }
        }
        sb.append("\n");
        sb.append("Fetch cache use: ").append(fetchCacheTime).append("(ms)\n");
        if (networkErrorCode == NetworkError.NOT_FINISHED) {
            sb.append("Network use: 请求未结束\n");
        } else {
            sb.append("Network use: ").append(requestNetworkTime).append("(ms)\n");
        }
        sb.append("/****************************DUMP MINER END  *************************************/\n");

        return sb.toString();
    }

    private static ArrayMap<Class, JsonParser> sEntityParsers = new ArrayMap<>();

    public static void putParser(Class clazz, JsonParser parser) {
        sEntityParsers.put(clazz, parser);
    }

}
