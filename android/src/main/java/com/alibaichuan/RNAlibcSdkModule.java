
package com.alibaichuan;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import android.app.Application;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.ali.auth.third.core.model.Session;
import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTrade;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeCallback;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeInitCallback;
import com.alibaba.baichuan.android.trade.model.AlibcShowParams;
import com.alibaba.baichuan.android.trade.model.OpenType;
import com.alibaba.baichuan.android.trade.page.AlibcAddCartPage;
import com.alibaba.baichuan.android.trade.page.AlibcBasePage;
import com.alibaba.baichuan.android.trade.page.AlibcDetailPage;
import com.alibaba.baichuan.android.trade.page.AlibcMyCartsPage;
import com.alibaba.baichuan.android.trade.page.AlibcMyOrdersPage;
import com.alibaba.baichuan.android.trade.page.AlibcPage;
import com.alibaba.baichuan.android.trade.page.AlibcShopPage;
import com.alibaba.baichuan.trade.biz.AlibcConstants;
import com.alibaba.baichuan.trade.biz.context.AlibcTradeResult;
import com.alibaba.baichuan.trade.biz.core.taoke.AlibcTaokeParams;
import com.alibaba.baichuan.trade.biz.login.AlibcLogin;
import com.alibaba.baichuan.trade.biz.login.AlibcLoginCallback;
import com.alibaba.baichuan.trade.biz.context.AlibcResultType;

public class RNAlibcSdkModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final String TAG = "RNAlibcSdkModule";
    private final static String NOT_LOGIN = "not login";
    private final static String INVALID_TRADE_RESULT = "invalid trade result";
    private final static String INVALID_PARAM = "invalid";

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            CallbackContext.onActivityResult(requestCode, resultCode, intent);
        }
    };

    static private RNAlibcSdkModule mRNAlibcSdkModule = null;

    static public RNAlibcSdkModule sharedInstance(ReactApplicationContext context) {
        if (mRNAlibcSdkModule == null) {
            return new RNAlibcSdkModule(context);
        } else {
            return mRNAlibcSdkModule;
        }
    }

    public RNAlibcSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNAlibcSdk";
    }

    /**
     * 初始化SDK---无参数传入
     */
    @ReactMethod
    public void initTae(final Callback callback) {
        AlibcTradeSDK.asyncInit((Application) reactContext.getApplicationContext(), new AlibcTradeInitCallback() {
            @Override
            public void onSuccess() {
                callback.invoke(null, "init success");
            }

            @Override
            public void onFailure(int code, String msg) {
                WritableMap map = Arguments.createMap();
                map.putInt("code", code);
                map.putString("msg", msg);
                callback.invoke(map);
            }
        });
    }

    /**
     * 打开登录---无参数传入
     */
    @ReactMethod
    public void showLogin(final Callback callback) {
        AlibcLogin alibcLogin = AlibcLogin.getInstance();

        alibcLogin.showLogin(new AlibcLoginCallback() {
            @Override
            public void onSuccess(int code) {
                Session session = AlibcLogin.getInstance().getSession();
                WritableMap map = Arguments.createMap();
                map.putString("userNick", session.nick);
                map.putString("avatarUrl", session.avatarUrl);
                map.putString("openId", session.openId);
                map.putString("isLogin", "true");
                callback.invoke(null, map);
            }

            @Override
            public void onFailure(int code, String msg) {
                WritableMap map = Arguments.createMap();
                map.putInt("code", code);
                map.putString("msg", msg);
                callback.invoke(map);
            }
        });
    }

    /**
     * 是否登录
     */
    @ReactMethod
    public void isLogin(final Callback callback) {
        callback.invoke(null, AlibcLogin.getInstance().isLogin());
    }

    /**
     * 获取已登录的用户信息---无参数传入
     */
    @ReactMethod
    public void getUserInfo(final Callback callback) {
        if (AlibcLogin.getInstance().isLogin()) {
            Session session = AlibcLogin.getInstance().getSession();
            WritableMap map = Arguments.createMap();
            map.putString("userNick", session.nick);
            map.putString("avatarUrl", session.avatarUrl);
            map.putString("openId", session.openId);
            map.putString("isLogin", "true");
            callback.invoke(null, map);
        } else {
            callback.invoke(NOT_LOGIN);
        }
    }

    /**
     * 退出登录---无参数传入
     */
    @ReactMethod
    public void logout(final Callback callback) {
        AlibcLogin alibcLogin = AlibcLogin.getInstance();

        alibcLogin.logout(new AlibcLoginCallback() {
            public void onSuccess(int code) {
                WritableMap map = Arguments.createMap();
                map.putInt("code", code);
                map.putString("msg", "logout success");
                callback.invoke(null, "logout success");
            }

            @Override
            public void onFailure(int code, String msg) {
                WritableMap map = Arguments.createMap();
                map.putInt("code", code);
                map.putString("msg", msg);
                callback.invoke(msg);
            }
        });
    }

    /**
     * 展示
     */
    @ReactMethod
    public void show(final ReadableMap param, final Callback callback) {
        String type = param.getString("type");
        ReadableMap payload = param.getMap("payload");
        switch (type) {
            case "detail":
                this._show(new AlibcDetailPage(payload.getString("itemid")), param, callback);
                break;
            case "url":
                this._show(new AlibcPage(payload.getString("url")), param, callback);
                break;
            case "shop":
                this._show(new AlibcShopPage(payload.getString("shopid")), param, callback);
                break;
            case "orders":
                this._show(new AlibcMyOrdersPage(payload.getInt("orderStatus"), payload.getBoolean("allOrder")), param, callback);
                break;
            case "addCard":
                this._show(new AlibcAddCartPage(param.getString("itemid")), param, callback);
                break;
            case "mycard":
                this._show(new AlibcMyCartsPage(), param, callback);
                break;
            default:
                callback.invoke(INVALID_PARAM);
                break;
        }
    }

    public void showInWebView(final WebView webview, WebViewClient webViewClient, final ReadableMap param) {
        String type = param.getString("type");
        ReadableMap payload = param.getMap("payload");
        switch (type) {
            case "detail":
                this._showInWebView(webview, webViewClient, new AlibcDetailPage(payload.getString("itemid")), param);
                break;
            case "url":
                this._showInWebView(webview, webViewClient, new AlibcPage(payload.getString("url")), param);
                break;
            case "shop":
                this._showInWebView(webview, webViewClient, new AlibcShopPage(payload.getString("shopid")), param);
                break;
            case "orders":
                this._showInWebView(webview, webViewClient, new AlibcMyOrdersPage(payload.getInt("orderStatus"), payload.getBoolean("allOrder")), param);
                break;
            case "addCard":
                this._showInWebView(webview, webViewClient, new AlibcAddCartPage(payload.getString("itemid")), param);
                break;
            case "mycard":
                this._showInWebView(webview, webViewClient, new AlibcMyCartsPage(), param);
                break;
            default:
                WritableMap event = Arguments.createMap();
                event.putString("type", INVALID_PARAM);
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        webview.getId(),
                        "onTradeResult",
                        event);
                break;
        }
    }

    private void _showInWebView(final WebView webview, WebViewClient webViewClient, final AlibcBasePage page, final ReadableMap param) {
        // 处理参数
        AlibcShowParams showParams = new AlibcShowParams();
        showParams = this.dealShowParams(param);
        AlibcTaokeParams taokeParams = new AlibcTaokeParams();
        taokeParams = this.dealTaokeParams(param);
        Map<String, String> exParams = new HashMap<String, String>();
        exParams = this.dealExParams(param);

        AlibcTrade.show(getCurrentActivity(),
                webview,
                webViewClient,
                null,
                page,
                showParams,
                taokeParams,
                exParams,
                new AlibcTradeCallback() {
                    public void onTradeSuccess(AlibcTradeResult tradeResult) {
                        Log.v("ReactNative", TAG + ":onTradeSuccess");
                        WritableMap event = Arguments.createMap();
                        //打开电商组件，用户操作中成功信息回调。tradeResult：成功信息（结果类型：加购，支付；支付结果）
                        if (tradeResult.resultType.equals(AlibcResultType.TYPECART)) {
                            event.putString("type", "card");
                        } else if (tradeResult.resultType.equals(AlibcResultType.TYPEPAY)) {
                            event.putString("type", "pay");
                            event.putString("orders", "" + tradeResult.payResult.paySuccessOrders);
                        } else {
                            event.putString("type", INVALID_PARAM);
                        }
                        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                webview.getId(),
                                "onTradeResult",
                                event);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        WritableMap event = Arguments.createMap();
                        event.putString("type", "error");
                        event.putInt("code", code);
                        event.putString("msg", msg);
                        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                webview.getId(),
                                "onTradeResult",
                                event);
                    }
                });
    }

    private void _show(AlibcBasePage page, final ReadableMap param, final Callback callback) {
        // 处理参数
        AlibcShowParams showParams = new AlibcShowParams();
        showParams = this.dealShowParams(param);
        AlibcTaokeParams taokeParams = new AlibcTaokeParams();
        taokeParams = this.dealTaokeParams(param);
        Map<String, String> exParams = new HashMap<String, String>();
        exParams = this.dealExParams(param);

        AlibcTrade.show(getCurrentActivity(),
                page,
                showParams,
                taokeParams,
                exParams,
                new AlibcTradeCallback() {
                    public void onTradeSuccess(AlibcTradeResult tradeResult) {
                        Log.v("ReactNative", TAG + ":onTradeSuccess");
                        //打开电商组件，用户操作中成功信息回调。tradeResult：成功信息（结果类型：加购，支付；支付结果）
                        if (tradeResult.resultType.equals(AlibcResultType.TYPECART)) {
                            //加购成功
                            WritableMap map = Arguments.createMap();
                            map.putString("type", "card");
                            callback.invoke(null, map);
                        } else if (tradeResult.resultType.equals(AlibcResultType.TYPEPAY)) {
                            //支付成功
                            WritableMap map = Arguments.createMap();
                            map.putString("type", "pay");
                            map.putString("orders", "" + tradeResult.payResult.paySuccessOrders);
                            callback.invoke(null, map);
                        } else {
                            callback.invoke(INVALID_TRADE_RESULT);
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        WritableMap map = Arguments.createMap();
                        map.putString("type", "error");
                        map.putInt("code", code);
                        map.putString("msg", msg);
                        callback.invoke(msg);
                    }
                });
    }

    /**
     * 处理showParams公用参数
     */
    private AlibcShowParams dealShowParams(final ReadableMap param) {
        ReadableMap payload = param.getMap("payload");
        // 初始化参数
        String opentype = "html5";

        AlibcShowParams showParams = new AlibcShowParams();

        if (payload.getString("opentype") != null
                || !payload.getString("opentype").equals("")) {
            opentype = payload.getString("opentype");
        }

        if (opentype.equals("html5")) {
            showParams = new AlibcShowParams(OpenType.H5, true);
        } else {
            showParams = new AlibcShowParams(OpenType.Native, true);
        }

        return showParams;
    }

    /**
     * 处理taokeParams公用参数
     */
    private AlibcTaokeParams dealTaokeParams(final ReadableMap param) {
        ReadableMap payload = param.getMap("payload");
        // 初始化参数
        String mmpid = "mm_23448739_6500158_22182062";
        String adzoneid = "60538822";
        String tkkey = "23482513";

        // 设置mmpid
        if (payload.getString("mmpid") != null
                || !payload.getString("mmpid").equals("")) {
            mmpid = payload.getString("mmpid");
        }

        // 设置adzoneid
        if (payload.getString("adzoneid") != null
                || !payload.getString("adzoneid").equals("")) {
            adzoneid = payload.getString("adzoneid");
        }

        // 设置tkkey
        if (payload.getString("tkkey") != null
                || !payload.getString("tkkey").equals("")) {
            tkkey = payload.getString("tkkey");
        }

        AlibcTaokeParams taokeParams = new AlibcTaokeParams();
        taokeParams.setPid(mmpid);
        taokeParams.setAdzoneid(adzoneid);
        Map<String, String> taokeExParams = new HashMap<String, String>();
        taokeExParams.put("taokeAppkey", tkkey);
        taokeParams.extraParams = taokeExParams;
        return taokeParams;
    }

    /**
     * 处理exParams公用参数
     */
    private Map<String, String> dealExParams(final ReadableMap param) {
        ReadableMap payload = param.getMap("payload");
        // 初始化参数
        Map<String, String> exParams = new HashMap<String, String>();
        String isvcode = "app";
        // 设置tkkey
        if (payload.getString("isvcode") != null
                || !payload.getString("isvcode").equals("")) {
            isvcode = payload.getString("isvcode");
        }
        exParams.put(AlibcConstants.ISV_CODE, isvcode);
        return exParams;
    }
}