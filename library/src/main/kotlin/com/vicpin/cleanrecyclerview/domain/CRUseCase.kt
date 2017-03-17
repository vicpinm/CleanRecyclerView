package com.vicpin.cleanrecyclerview.domain


import com.vicpinm.autosubscription.UnsubscribeListener
import rx.Observable
import rx.Subscriber
import rx.functions.Action1
import rx.schedulers.Schedulers

/**
 * Created by Victor on 20/01/2017.
 */
abstract class CRUseCase<T> : UnsubscribeListener {

    private var onNext: Action1<T>? = null
    private var onError: Action1<Throwable>? = null
    private var subscriber: Subscriber<T>? = null

    fun execute() {
        this.onNext = null
        this.onError = null

        Observable.defer { buildUseCase() }
                .subscribeOn(Schedulers.io())
                .doOnError { it.printStackTrace() }
                .subscribe(getSubscriber())

    }


    fun execute(onNextAction: Action1<T>, onErrorAction: Action1<Throwable>) {

        this.onNext = onNextAction
        this.onError = onErrorAction

        Observable.defer { buildUseCase() }
                .subscribeOn(Schedulers.io())
                .doOnError { it.printStackTrace() }
                .subscribe(getSubscriber())
    }


    abstract fun buildUseCase(): Observable<T>

    fun getSubscriber(): Subscriber<T> {

        if (subscriber != null && subscriber!!.isUnsubscribed) {
            subscriber = null
        }

        if (subscriber == null) {
            subscriber = object : Subscriber<T>() {
                override fun onCompleted() {}

                override fun onError(e: Throwable) {
                    onError?.call(e)

                }

                override fun onNext(t: T) {
                    onNext?.call(t)

                }
            }
        }

        return subscriber as Subscriber<T>
    }

    fun unsubscribe() {
        try {
            subscriber?.unsubscribe()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        subscriber = null

    }


    override fun onUnsubscribe() {
        unsubscribe()
    }
}
