package com.mingbikes.eplate;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.mingbikes.eplate.presenter.Presenter;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class BaseActivity<T extends Presenter> extends FragmentActivity {

    private T mPresenter;
    public T getPresenter() {
        return mPresenter;
    }

    public void setPresenter(T presenter) {
        this.mPresenter = presenter;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (getPresenter() != null) {
            getPresenter().resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getPresenter() != null) {
            getPresenter().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().destroy();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getPresenter() != null) {
            getPresenter().saveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().restoreInstanceState(savedInstanceState);
        }
    }

}
