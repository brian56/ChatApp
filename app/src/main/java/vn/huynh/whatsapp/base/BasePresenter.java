package vn.huynh.whatsapp.base;

/**
 * Created by duong on 4/14/2019.
 */

public interface BasePresenter {
    void attachView(BaseView view);

    void detachView();
}
