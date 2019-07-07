package vn.huynh.whatsapp.base;

/**
 * Created by duong on 5/28/2019.
 */

public interface CRUDInterface {
    void onLoadSuccess(Object t);

    void onLoadSuccessEmptyData();

    void onCreateSuccess(Object t);

    void onUpdateSuccess(Object t);

    void onDeleteSuccess(Object t);

    void onFail(String error);
}
