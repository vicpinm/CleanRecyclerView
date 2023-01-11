package com.vicpin.cleanrecycler.domain

import com.vicpinm.autosubscription.UnsubscribeListener
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

/**
 * Created by Victor on 20/01/2017.
 */
abstract class CRUseCase<T> : UnsubscribeListener {

    private var onNext: ((T) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null
    //private var subscriber: DisposableSubscriber<T>? = null
    private var compositeDisposable: CompositeDisposable? = null
    var isInProgress = false
        get() {
            return (compositeDisposable != null && compositeDisposable?.isDisposed == false)
        }

    fun execute(onNext: ((T) -> Unit)? = null, onError: ((Throwable) -> Unit)? = null, onComplete: (() -> Unit)? = null) {

        /*if (subscriber != null) {
            unsubscribe() //Unsubscribe previous subscription
        }*/

        this.onNext = onNext
        this.onError = onError
        this.onComplete = onComplete

        Flowable.defer { buildUseCase() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { it.printStackTrace() }
                .subscribe(getSubscriber())

    }

    abstract fun buildUseCase(): Flowable<T>

    fun getSubscriber(): DisposableSubscriber<T> {

        /*if (subscriber != null && subscriber!!.isDisposed) {
            subscriber = null
        }*/

        //if (subscriber == null) {
            val subscriber = object : DisposableSubscriber<T>() {
                override fun onComplete() {
                    onComplete?.invoke()
                }

                override fun onError(e: Throwable) {
                    onError?.invoke(e)

                }

                override fun onNext(t: T) {
                    onNext?.invoke(t)
                }
            }

            if(compositeDisposable == null) {
                compositeDisposable = CompositeDisposable()
            }
            compositeDisposable?.addAll(subscriber)
        //}

        return subscriber
    }

    fun unsubscribe() {
        try {
            compositeDisposable?.clear()
            compositeDisposable = null
            //subscriber?.dispose()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        //subscriber = null

    }

    override fun onUnsubscribe() {
        unsubscribe()
    }
}
