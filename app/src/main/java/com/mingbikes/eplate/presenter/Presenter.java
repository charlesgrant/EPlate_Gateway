package com.mingbikes.eplate.presenter;

import android.os.Bundle;

public interface Presenter {

    void resume();

    void pause();

    /**
     * 需要停止Http等延迟回调请求
     */
    void destroy();

    /**
     * 保存状态
     */
    void saveInstanceState(Bundle outState);

    /**
     * 恢复状态
     */
    void restoreInstanceState(Bundle savedInstanceState);


}