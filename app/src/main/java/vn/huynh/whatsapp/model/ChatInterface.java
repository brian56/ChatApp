package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface ChatInterface extends BaseModelInterface {
    void getChatList(boolean onlyGroup, ChatListCallback callBack);

    void getChatDetail(String chatId, ChatDetailCallback callBack);

    void getChatDetail(boolean onlyGroup, Chat chat, ChatListCallback callBack);

    void createChat(boolean isGroup, String name, List<User> users, CreateChatCallback callBack);

    void checkSingleChatExist(String singleChatId, CheckSingleChatCallback callBack);

    void removeChatListListener();

    void addChatListListener();

    void removeChatDetailListener();

    void addChatDetailListener();

    void resetNumberUnread(String chatId, ResetUnreadMessageCallback callback);

    interface ChatListCallback {
        void loadSuccess(Chat chat);

        void updateChatStatus(Chat chat, boolean hasNewMessage);

        void loadSuccessEmptyData();

        void loadFail(String message);

        void removeSuccess(Chat chat);

        void removeFail(String message);
    }

    interface ChatDetailCallback {
        void loadSuccess(Chat chat);

        void loadFail(String message);
    }

    interface CreateChatCallback {
        void createSuccess(String chatId);

        void createFail(String message);
    }

    interface CheckSingleChatCallback {
        void exist(String chatId);

        void notExist();
    }

    interface ResetUnreadMessageCallback {
        void success();

        void fail();
    }
}
