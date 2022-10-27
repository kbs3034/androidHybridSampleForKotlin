package com.example.kotlinhybridsample.rx

import com.orhanobut.logger.Logger
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

interface IEventBusSubscription {
    fun unsubscribe()
}

interface IEventBus {
    fun publish(event: Any)

    fun <EVENT_TYPE> subscribe(eventType: Class<EVENT_TYPE>, receiver: (EVENT_TYPE) -> Unit): IEventBusSubscription

    fun <EVENT_TYPE> subscribe(eventType: Class<EVENT_TYPE>, receiver: (EVENT_TYPE) -> Unit, errorReceiver: (Throwable) -> Unit): IEventBusSubscription
}


interface ICrashReporter {
    fun logNonFatalException(error: Throwable)
}

class RxEventBusSubscription constructor(private val rxSubscription: Disposable) :
    IEventBusSubscription {
    override fun unsubscribe() = rxSubscription.dispose()
}

class RxBus constructor(
    private val crashReporter: ICrashReporter
)
    : IEventBus {
    private val publisher: PublishSubject<Any> = PublishSubject.create()

    override fun publish(event: Any) {
        if (publisher.hasObservers()) {
            publisher.onNext(event)
        }
    }

    override fun <EVENT_TYPE> subscribe(eventType: Class<EVENT_TYPE>, receiver: (EVENT_TYPE) -> Unit): IEventBusSubscription {
        return subscribe(eventType, receiver)
        { error: Throwable ->
            crashReporter.logNonFatalException(error)
        }
    }

    override fun <EVENT_TYPE> subscribe(eventType: Class<EVENT_TYPE>, receiver: (EVENT_TYPE) -> Unit, errorReceiver: (Throwable) -> Unit): IEventBusSubscription {
        val subscriber = publisher
            .toFlowable(BackpressureStrategy.BUFFER)
            .observeOn(AndroidSchedulers.mainThread())
            .ofType(eventType)
            .subscribe(
                { event ->
                    receiver(event)
                },
                { error: Throwable ->
                    errorReceiver(error)
                }
            )
        return RxEventBusSubscription(subscriber)
    }

    companion object {
        private var eventBus: RxBus? = null
        val instance: RxBus
            get() {
                if (eventBus == null) {
                    eventBus = RxBus(object : ICrashReporter {
                        override fun logNonFatalException(error: Throwable) {
                            Logger.e(error, "in sharedInstance")
                        }
                    })
                }
                return eventBus!!
            }
    }
}
