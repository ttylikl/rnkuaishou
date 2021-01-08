package com.rnkuaishou

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.kwai.auth.common.InternalResponse
import com.kwai.auth.common.KwaiConstants
import com.kwai.opensdk.auth.IKwaiAuthListener
import com.kwai.opensdk.auth.IKwaiOpenSdkAuth
import com.kwai.opensdk.auth.KwaiOpenSdkAuth
import com.kwai.opensdk.sdk.utils.LogUtil
import com.kwai.opensdk.sdk.utils.NetworkUtil
import org.json.JSONObject
import java.util.*


class RnkuaishouModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "Rnkuaishou"
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    fun multiply(a: Int, b: Int, promise: Promise) {

      promise.resolve(a * b)

    }

  var appId: String = ""

  @ReactMethod
  fun registerApp(appid: String, universalLink: String, promise: Promise) {
    if (appId != "") {
      promise.resolve("noop")
      return
    }
    var currentActivity: Activity? = getCurrentActivity()
    if (currentActivity == null) {
      promise.resolve("currentActivity is null!")
      return
    }

    var ctx: Application = currentActivity.application
    KwaiOpenSdkAuth.init(ctx)
    appId = appid
    LogUtil.i(TAG, "$universalLink is used by ios, just a placeholder.")
    promise.resolve("ok")
  }

  @ReactMethod
  fun ksauth(loginType: String, promise: Promise) {
    if(loginType=="app") {
      appLogin()
    } else if(loginType=="h5") {
      h5Login()
    }
    promise.resolve("called")
  }

  private val TAG = "RnkuaishouModule"
  private var URL_HOST = "https://open.kuaishou.com"
  private val NETWORK_MAX_RETRY_TIMES = 5

  // 测试demo用的的appId和appSecret，第三方客户端请使用分配的数据
  private var APP_ID = "ks675258470891385408"
  private var APP_SECRET = "Se-O1svMqfLvOFvqzGpevA" //"cAQmb4gjTeCW3Sf4enQDbQ"

  private fun getRequestOpenIdUrl(grantType: String, appId: String, appKey: String, code: String): String {
    val builder = StringBuilder()
    builder.append(URL_HOST)
    builder.append("/oauth2/access_token?")
    builder.append("grant_type=$grantType")
    builder.append("&app_id=$appId")
    builder.append("&app_secret=$appKey")
    builder.append("&code=$code")
    return builder.toString()
  }

  // 获取openId的网络请求，为了安全性，建议放在第三方客户端的服务器中，由第三方服务器实现这个请求接口后将openid返回第三方客户端
  private fun getOpenIdByNetwork(code: String): String? {
    var appId: String = APP_ID
    var appSecret: String = APP_SECRET

    val url: String = getRequestOpenIdUrl("code", appId, appSecret, code)
    val result = NetworkUtil.get(url, null, null)
    var openId: String? = null
    try {
      LogUtil.i(TAG, "result=$result")
      val obj = JSONObject(result)
      openId = obj.getString("open_id")
      LogUtil.i(TAG, "openId=$openId")
    } catch (t: Throwable) {
      LogUtil.e(TAG, "getOpenId exception")
    }
    return openId
  }

  private val thiz:RnkuaishouModule = this
  private var mOpenId: String? = null
  private val mKwaiOpenSdkAuth: IKwaiOpenSdkAuth? = KwaiOpenSdkAuth()
  val mKwaiAuthListener: IKwaiAuthListener = object : IKwaiAuthListener {
    override fun onSuccess(response: InternalResponse) {
      val map = Arguments.createMap()
      map.putInt("errCode", response.errorCode)
      map.putString("errorMsg", response.errorMsg)
      map.putString("authCode", response.code)
      map.putString("state", response.state)
      map.putString("type", "SendAuth.Resp")

      var ctx: ReactApplicationContext? = thiz.getReactApplicationContext();
      ctx?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.emit("KuaiShou_Resp", map)

//      Thread {
//        var result: String? = null
//        var retry = 0
//        while (null == result && retry < NETWORK_MAX_RETRY_TIMES) {
//          result = getOpenIdByNetwork(response.code)
//          retry++
//          LogUtil.i(TAG, "retry=$retry")
//        }
//        val openId = result
//        val mainHandler = Handler(Looper.getMainLooper())
//        mainHandler.post {
//          mOpenId = openId
//        }
//      }.start()
    }

    override fun onFailed(state: String, errCode: Int, errMsg: String) {
//      mOpenIdTv.setText("code error is $errCode and msg is $errMsg")
      LogUtil.i(TAG, "code error is $errCode and msg is $errMsg")
    }

    override fun onCancel() {
//      mOpenIdTv.setText("login is canceled")
      LogUtil.i(TAG, "login is canceled")
    }
  }
  private var STATE="nouse"
  private var platformList = ArrayList<String>(2)
  init {
    platformList.add(KwaiConstants.Platform.NEBULA_APP)
    platformList.add(KwaiConstants.Platform.KWAI_APP)
  }
  // app调起登录
  fun appLogin() {
    var currentActivity: Activity? = getCurrentActivity()
    mKwaiOpenSdkAuth?.sendAuthReqToKwai(currentActivity, STATE,
      KwaiConstants.LoginType.APP, mKwaiAuthListener,
      platformList.toTypedArray())
  }

  // h5调起登录
  fun h5Login() {
    var currentActivity: Activity? = getCurrentActivity()
    mKwaiOpenSdkAuth?.sendAuthReqToKwai(currentActivity, STATE,
      KwaiConstants.LoginType.H5, mKwaiAuthListener,
      platformList.toTypedArray())
  }
}


