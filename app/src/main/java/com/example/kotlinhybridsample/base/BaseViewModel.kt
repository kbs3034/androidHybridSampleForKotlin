package com.example.kotlinhybridsample.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

abstract class BaseViewModel: ViewModel() {

    protected val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        Timber.d("onCleared ${javaClass.simpleName}")
    }
}