package vn.huynh.whatsapp.chat;

import java.util.ArrayList;

import vn.huynh.whatsapp.base.BasePresenter;
import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;

/**
 * Created by duong on 4/3/2019.
 */

public interface ChatContract {
    interface View extends BaseView {

        void showChatDetail(Chat chatObject);

        void showMessageList(Message messageObject);

        void addSendingMessageToList(Message messageObject);

        void newMessage();

//        void updateMessageStatus(Message message);

        void resetUI();
    }

    interface Presenter extends BasePresenter {

//        void getMessageId(String chatId);

        void loadChatDetail(String chatId);

        void loadChatMessage(String chatId);

        void sendMessage(Chat chat, String message, ArrayList<String> mediaUri);

        void removeChatDetailListener();

        void addChatDetailListener();

        void addMessageListener();

        void removeMessageListener();

    }
}
