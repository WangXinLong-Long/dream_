package com.alading.libcommon.extention;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.ConcurrentHashMap;

public class LiveDataBus {
    private LiveDataBus() {
    }

    static class Lazy {
        public static LiveDataBus instance = new LiveDataBus();
    }

    public  static  LiveDataBus get() {
        return Lazy.instance;
    }

    private ConcurrentHashMap<String, StickyLiveData> mHashMap = new ConcurrentHashMap<>();


    public StickyLiveData with(String eventName) {
        StickyLiveData liveData = mHashMap.get(eventName);
        if (liveData == null) {
            liveData = new StickyLiveData(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }

    public class StickyLiveData<T> extends LiveData<T> {
        private String mEventName;
        private T mStickyData;
        private int mVersion = 0;

        public StickyLiveData(String eventName) {
            mEventName = eventName;
        }

        @Override
        public void setValue(T value) {
            mVersion++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            mVersion++;
            super.postValue(value);
        }

        public void setStickyData(T stickyData) {
            mStickyData = stickyData;
            setValue(stickyData);
        }

        public void postStickyData(T stickyData) {
            mStickyData = stickyData;
            postValue(stickyData);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);

        }

        private void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean sticky) {
            super.observe(owner, new WrapperObserver(this, observer, sticky));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        mHashMap.remove(mEventName);
                    }
                }
            });
        }

        class WrapperObserver<T> implements Observer<T> {
            private StickyLiveData<T> mLiveData;
            private Observer<T> mObserver;
            private boolean mSticky;
            private int mLastVersion;

            public WrapperObserver(StickyLiveData liveData, Observer<T> observer, boolean sticky) {


                mLiveData = liveData;
                mObserver = observer;
                mSticky = sticky;

                //比如先使用StickyLiveData发送了一条数据。StickyLiveData#version=1
                //那当我们创建WrapperObserver注册进去的时候，就至少需要把它的version和 StickyLiveData的version保持一致
                //用以过滤老数据，否则 岂不是会收到老的数据？
                mLastVersion = mLiveData.mVersion;
            }

            @Override
            public void onChanged(T t) {
                if (mLastVersion >= mLiveData.mVersion) {
                    if (mSticky && mLiveData.mStickyData != null) {
                        mObserver.onChanged(mLiveData.mStickyData);
                        return;
                    }
                }
                mLastVersion = mLiveData.mVersion;
                mObserver.onChanged(t);
            }
        }
    }


}
