package com.alading.dream.request

import androidx.lifecycle.LiveData
import retrofit2.http.*
import java.util.*

interface PublicApi {

    @FormUrlEncoded
    @POST("login")
    fun loginVerifySmsCode(@Field("telNum") telNum: String, @Field("password") pass_word: String): LiveData<ApiResponse<VerifySmsCodeResp>>

    @FormUrlEncoded
    @POST("login/send_sms_code")
    fun loginSendSmsCode(@Field("mobileNumber") mobileNumber: String): LiveData<ApiResponse<String>>

}
//{
//    "code": "1",
//    "data": {
//        "telNum": "18401564300",
//        "password": "123456"
//    },
//    "msg": "登陆成功！！！"
//}
data class VerifySmsCodeResp(val code: String, val msg: String,val data:UserInfo)

data class UserInfo(val telNum:String,val password:String)
