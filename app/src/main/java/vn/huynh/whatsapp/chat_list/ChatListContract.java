package vn.huynh.whatsapp.chat_list;

import java.util.List;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.model.Chat;

/**
 * Created by duong on 4/2/2019.
 */

public interface ChatListContract {
    interface View extends BaseView {
        void showChatList(Chat chat, int position);

        void updateChatListStatus(Chat chatObject);

        void showChatListEmpty();

        void showErrorMessage(String message);

    }

    interface Presenter extends BasePresenter {

        void loadChatList(List<Chat> chatObjects);
    }
}
