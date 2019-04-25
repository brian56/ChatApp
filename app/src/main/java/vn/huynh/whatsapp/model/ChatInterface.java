package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface ChatInterface extends BaseModelInterface {
    void getChatList(boolean onlyGroup, ChatListCallBack callBack);

    void getChatDetail(String chatId, ChatInterface.ChatDetailCallBack callBack);

    void getChatDetail(boolean onlyGroup, Chat chat, ChatInterface.ChatListCallBack callBack);

    void createChat(boolean isGroup, String name, List<User> users, CreateChatCallBack callBack);

    void checkSingleChatExist(String singleChatId, CheckSingleChatCallBack callBack);

    void removeChatListListener();

    void addChatListListener();

    void removeChatDetailListener();

    void addChatDetailListener();

    interface ChatListCallBack {
        void loadSuccess(Chat chat);

        void updateChatStatus(Chat chat);

        void loadSuccessEmptyData();

        void loadFail(String message);

        void removeSuccess(Chat chat);

        void removeFail(String message);
    }

    interface ChatDetailCallBack {
        void loadSuccess(Chat chat);

        void loadFail(String message);
    }

    interface CreateChatCallBack {
        void createSuccess(String chatId);

        void createFail(String message);
    }

    interface CheckSingleChatCallBack {
        void exist(String chatId);

        void notExist();
    }
}
