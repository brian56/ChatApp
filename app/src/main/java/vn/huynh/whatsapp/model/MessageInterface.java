package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface MessageInterface extends BaseModelInterface {

    void getChatMessageFirstPage(String chatId, GetChatMessageFirstPageCallback callBack);

    void getChatMessageLoadMore(String chatId, GetChatMessageLoadMoreCallback callBack);

    void getNewMessage(String chatId, GetNewMessageCallback callback);

//    void getChatMessageData(String chatId, GetChatMessageCallBack callBack);

    void sendMessage(Chat chat, String messageId, String text, List<String> mediaUriList, SendMessageCallBack callBack);

    void getNewMessageId(String chatId, SendMessageCallBack callBack);

    void removeMessageListener();

    void addMessageListener();

    /*interface GetChatMessageCallBack {
        void loadSuccess(Message message);

        void loadSuccessEmptyData();

        void loadFail(String error);

//        void updateMessageStatus(Message message);
    }*/

    interface SendMessageCallBack {
        void getNewMessageIdSuccess(String messageId);

        void sendSuccess();

        void sendFail(String error);
    }

    interface GetChatMessageFirstPageCallback {
        void loadSuccess(List<Message> messages, String newestMessageId);

        void loadSuccessDone(List<Message> messages, String newestMessageId);

        void loadSuccessEmptyData();

        void loadFail(String error);
    }

    interface GetChatMessageLoadMoreCallback {
        void loadSuccess(List<Message> messages);

        void loadSuccessDone(List<Message> messages);

        void loadSuccessEmptyData();

        void loadFail(String error);
    }

    interface GetNewMessageCallback {
        void getSuccess(Message message);

        void getFail(String error);
    }
}
