package com.mingbikes.eplate.presenter;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Subscription;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class BasePresenter implements Presenter {

    private List<Subscription> mRxTasks = new ArrayList<>();

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        Iterator<Subscription> iterator = mRxTasks.iterator();
        while (iterator.hasNext()) {
            Subscription subscription = iterator.next();
            if (subscription.isUnsubscribed()) {
                iterator.remove();
            }
        }
    }

    public void addRxTask(Subscription subscription) {
        mRxTasks.add(subscription);
    }

    /**
     * 需要停止Http等延迟回调请求
     */
    @Override
    public void destroy() {
        clearAllSubcription();
    }

    public void clearAllSubcription() {
        Iterator<Subscription> iterator = mRxTasks.iterator();
        while (iterator.hasNext()) {
            iterator.next().unsubscribe();
        }
        mRxTasks.clear();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {

    }
}
