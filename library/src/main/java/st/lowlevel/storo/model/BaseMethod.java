package st.lowlevel.storo.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseMethod<T> {

    /**
     * Creates an observable that will be scheduled on the background thread and observed on the main thread
     *
     * @return Observable<T>
     */
    @NonNull
    public Flowable<T> async() {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull FlowableEmitter<T> emitter) throws Exception {
                try {
                    T result = execute();
                    emitter.onNext(result);
                    emitter.onComplete();
                } catch (Throwable t) {
                    emitter.onError(t);
                }
            }
        }, BackpressureStrategy.BUFFER)
                       .subscribeOn(Schedulers.io());
    }

    /**
     * Executes the method on a background thread
     *
     * @param callback the callback to call upon completion
     */
    public void async(@Nullable final Callback<T> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                T result = execute();
                if (callback != null) {
                    callback.onResult(result);
                }
            }
        });
        executor.shutdown();
    }

    /**
     * Executes the method on the main thread
     *
     * @return the method result
     */
    public abstract T execute();
}
