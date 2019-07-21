package vn.huynh.whatsapp.base;

/**
 * Created by duong on 4/14/2019.
 */

public interface BaseView {
    void showLoadingIndicator();

    void hideLoadingIndicator();

    void showEmptyDataIndicator();

    void showErrorIndicator();

    void showErrorMessage(String error);

    void initData();

    void setEvents();

    void resetData();
}
