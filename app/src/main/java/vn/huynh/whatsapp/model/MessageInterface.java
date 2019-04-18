package vn.huynh.whatsapp.model;

import java.util.List;

import vn.huynh.whatsapp.base.BaseModelInterface;

/**
 * Created by duong on 4/15/2019.
 */

public interface MessageInterface extends BaseModelInterface {

    void getChatMessageData(String chatId, GetChatMessageCallBack callBack);

    void sendMessage(Chat chat, String messageId, String text, List<String> mediaUriList, SendMessageCallBack callBack);

    void getNewMessageId(String chatId, SendMessageCallBack callBack);

    interface GetChatMessageCallBack {
        void loadSuccess(Message message);

        void loadFail(String error);

//        void updateMessageStatus(Message message);
    }

    interface SendMessageCallBack {
        void getNewMessageIdSuccess(String messageId);

        void sendSuccess();

        void sendFail(String error);
    }
}
