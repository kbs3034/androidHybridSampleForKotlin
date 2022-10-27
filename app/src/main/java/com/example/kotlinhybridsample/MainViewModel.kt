package com.example.kotlinhybridsample

import com.example.kotlinhybridsample.api.RetrofitClient
import com.example.kotlinhybridsample.api.data.sample.SampleUser
import com.example.kotlinhybridsample.api.data.sample.SampleUserList
import com.example.kotlinhybridsample.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody

import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainViewModel : BaseViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.d("onCleared")
    }

    fun createUser(callback:(ResponseBody?)->Unit) {
        compositeDisposable.add(
            RetrofitClient.getInstance().createUserObservable(SampleUser())
                .subscribe({
                    callback.invoke(it)
                }, {
                    Timber.e(it)
                    callback.invoke(null)
                })
        )
    }

    fun doGetListResouces(callback:(ResponseBody?)->Unit) {
        compositeDisposable.add(
            RetrofitClient.getInstance().doGetListResoucesObservable()
                .subscribe({
                    callback.invoke(it)
                }, {
                    Timber.e(it)
                    callback.invoke(null)
                })
        )
    }


    fun doGetUserList(page:String, callback:(SampleUserList?)->Unit) {
        compositeDisposable.add(
            RetrofitClient.getInstance().doGetUserListObservable(page)
                .subscribe({
                    callback.invoke(it)
                }, {
                    Timber.e(it)
                    callback.invoke(null)
                })
        )
    }

    fun doGetUserListForJsonObject(page:String, callback:(ResponseBody?)->Unit) {
        compositeDisposable.add(
            RetrofitClient.getInstance().doGetUserListForJsonObjectObservable(page)
                .subscribe({
                    callback.invoke(it)
                }, {
                    Timber.e(it)
                    callback.invoke(null)
                })
        )
    }

    fun doCreateUserWithField(name:String, job:String, callback:(ResponseBody?)->Unit) {
        compositeDisposable.add(
            RetrofitClient.getInstance().doCreateUserWithFieldObservable(name, job)
                .subscribe({
                    callback.invoke(it)
                }, {
                    Timber.e(it)
                    callback.invoke(null)
                })
        )
    }

}