package com.alading.dream.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alading.dream.ApiResponse
import com.alading.dream.ApiService.get
import com.alading.dream.JsonCallback
import com.alading.dream.R
import com.alading.dream.model.User
import com.alading.libcommon.view.toast
import com.tencent.connect.UserInfo
import com.tencent.connect.auth.QQToken
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), View.OnClickListener {


    private var loginViewModel = viewModels<LoginViewModel>()

    private var actionClose: View? = null
    private var actionLogin: View? = null
    private var tencent: Tencent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)



        actionClose = findViewById<View>(R.id.action_close)
        actionLogin = findViewById<View>(R.id.action_login)

        actionClose?.setOnClickListener(this)
        actionLogin?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.action_close) {
            finish()
        } else if (v.id == R.id.action_login) {
            login()
        }
    }

    private fun login() {
        if (tencent == null) {
            tencent = Tencent.createInstance("1110337243", applicationContext)
        }
        tencent?.login(this, "all", loginListener)
    }

    val loginListener = object : IUiListener {
        override fun onComplete(p0: Any?) {
            var response = p0 as JSONObject
            try {
                val openid = response.getString("openid")
                val access_token = response.getString("access_token")
                val expires_in = response.getString("expires_in")
                val expires_time = response.getLong("expires_time")
                tencent!!.openId = openid
                tencent!!.setAccessToken(access_token, expires_in)
                val qqToken = tencent!!.qqToken
                getUserInfo(qqToken, expires_time, openid)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        override fun onCancel() {
            toast("登录取消")
        }

        override fun onError(uiError: UiError?) {

            toast("登录失败:reason" + uiError.toString())
        }

    }

    private fun getUserInfo(
        qqToken: QQToken,
        expires_time: Long,
        openid: String
    ) {
        val userInfo =
            UserInfo(applicationContext, qqToken)
        userInfo.getUserInfo(object : IUiListener {
            override fun onComplete(o: Any) {
                val response = o as JSONObject
                try {
                    val nickname = response.getString("nickname")
                    val figureurl_2 = response.getString("figureurl_2")
                    save(nickname, figureurl_2, openid, expires_time)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onError(uiError: UiError) {
                toast("登录失败:reason$uiError")
            }

            override fun onCancel() {
                toast("登录取消")
            }
        })
    }

    private fun save(
        nickname: String,
        avatar: String,
        openid: String,
        expires_time: Long
    ) {
        get<Any>("/user/insert")
            .addParam("name", nickname)
            .addParam("avatar", avatar)
            .addParam("qqOpenId", openid)
            .addParam("expires_time", expires_time)
            .execute(object : JsonCallback<User?>() {
                override fun onSuccess(response: ApiResponse<User?>) {
                    if (response.body != null) {
                        UserManager.get().save(response.body)
                        finish()
                    } else {
                        runOnUiThread {
                            toast("登陆失败")

                        }
                    }
                }

                override fun onError(response: ApiResponse<User?>) {
                    runOnUiThread {
                        toast("登陆失败,msg:" + response.msg)
                    }
                }
            })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener)
        }
    }
}

