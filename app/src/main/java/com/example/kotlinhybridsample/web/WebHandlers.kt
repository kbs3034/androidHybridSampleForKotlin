package com.example.kotlinhybridsample.web

import org.json.JSONObject

interface SampleHandler {
    fun test(args: WebMessageArgs?): JSONObject
    fun rxTest(args: WebMessageArgs?)
}

interface NativeSystemHandler {
    fun showToast(args: WebMessageArgs?)
    fun getAppVersion(args: WebMessageArgs?): String
}

interface ApiSampleHandler {
    fun createUser(args: WebMessageArgs?)
    fun doGetListResources(args: WebMessageArgs?)
    fun doGetUserList(args: WebMessageArgs?)
    fun doGetUserListForJsonObject(args: WebMessageArgs?)
    fun doCreateUserWithField(args: WebMessageArgs?)
}
