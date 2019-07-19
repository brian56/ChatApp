package vn.huynh.whatsapp.chat;

import java.util.ArrayList;
import java.util.List;

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

        void showNewMessage(Message messageObject);

        void loadMessage();

        void showMessageList(List<Message> messages, boolean isDone);

        void showMessageListLoadMore(List<Message> messages, boolean isDone);

        void addSendingMessageToList(Message messageObject);

        void newMessage();

//        void updateMessageStatus(Message message);

        void resetUI();
    }

    interface Presenter extends BasePresenter {

        void resetNumberUnread(String chatId, boolean loadMessage);

        void loadChatDetail(String chatId);

        void loadMessage(String chatId);

        void loadMessageMore(String chatId);

        void listenToNewMessage(String chatId);

        void sendMessage(Chat chat, String text, ArrayList<String> mediaUri);

        void removeChatDetailListener();

        void addChatDetailListener();

        void addMessageListener();

        void removeMessageListener();

        void cancelUpload();

    }
}
