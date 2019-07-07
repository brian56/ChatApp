package vn.huynh.whatsapp.base;

import java.util.List;

/**
 * Created by duong on 5/28/2019.
 */

public interface CRUDListInterface {

    void onCreateSuccess(List<Object> t);

    void onLoadSuccessEmptyData();

    void onUpdateSuccess(List<Object> t);

    void onDeleteSuccess(List<Object> t);

    void onFail(String error);
}
