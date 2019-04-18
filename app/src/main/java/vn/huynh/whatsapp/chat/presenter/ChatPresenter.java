package vn.huynh.whatsapp.chat.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.ChatRepository;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.model.MessageInterface;
import vn.huynh.whatsapp.model.MessageRepository;
import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/2/2019.
 */

public class ChatPresenter implements ChatContract.Presenter {
    private ChatContract.View view;
    private ChatInterface chatInterface;
    private MessageInterface messageInterface;

    public ChatPresenter() {
        chatInterface = new ChatRepository();
        messageInterface = new MessageRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (ChatContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.chatInterface.removeListener();
        this.messageInterface.removeListener();
    }

    @Override
    public void removeListener() {
        this.chatInterface.removeListener();
        this.messageInterface.removeListener();
    }

    @Override
    public void loadChatDetail(String chatId) {
        view.showLoadingIndicator();
        chatInterface.getChatDetail(chatId, new ChatInterface.ChatDetailCallBack() {
            @Override
            public void loadSuccess(Chat chatObject) {
                if (view != null) {
                    view.showChatDetail(chatObject);
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void loadFail(String message) {
                if (view != null) {
                    view.showError(message);
                    view.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void loadChatMessage(String chatId) {
        view.showLoadingIndicator();
        messageInterface.getChatMessageData(chatId, new MessageInterface.GetChatMessageCallBack() {
            @Override
            public void loadSuccess(Message messageObjects) {
                if (view != null) {
                    view.showMessageList(messageObjects);
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void loadFail(String message) {
                if (view != null) {
                    view.showError(message);
                    view.hideLoadingIndicator();
                }
            }

//            @Override
//            public void updateMessageStatus(Message message) {
//                view.updateMessageStatus(message);
//            }
        });
    }

    @Override
    public void sendMessage(final Chat chat, final String text, final ArrayList<String> mediaUri) {
        if (mediaUri.size() > 0) {
            if(view != null)
                view.showLoadingIndicator();
        }

        messageInterface.getNewMessageId(chat.getId(), new MessageInterface.SendMessageCallBack() {
            @Override
            public void getNewMessageIdSuccess(String messageId) {
                Message message = new Message(messageId);
                message.setStatus(Message.STATUS_SENDING);
                message.setCreator(Utils.currentUserId());
                message.setText(text);
                Map<String, Object> seenUsers = new HashMap<>();
                seenUsers.put(Utils.currentUserId(), true);
                message.setSeenUsers(seenUsers);

                if(mediaUri.size() > 0) {
                    Map<String, String> mediaMap = new HashMap<>();
                    for (int i = 0; i < mediaUri.size(); i++) {
                        mediaMap.put(mediaUri.get(i), "");
                    }
                    message.setMedia(mediaMap);
                }
                if(view != null) {
                    view.addSendingMessageToList(message);
                    view.resetUI();
                }
                messageInterface.sendMessage(chat, messageId, text, mediaUri, new MessageInterface.SendMessageCallBack() {
                    @Override
                    public void sendSuccess() {
                        if(view!= null) {
                            view.hideLoadingIndicator();
                        }
//                        view.resetUI();
                    }

                    @Override
                    public void sendFail(String message) {
                        if(view != null)
                            view.showError(message);
                    }

                    @Override
                    public void getNewMessageIdSuccess(String messageId) {

                    }
                });
            }

            @Override
            public void sendSuccess() {

            }

            @Override
            public void sendFail(String error) {

            }
        });

//        messageInterface.sendMessage(chat, text, mediaUri, new MessageInterface.SendMessageCallBack() {
//            @Override
//            public void sendSuccess() {
//                view.hideLoadingIndicator();
//                view.resetUI();
//            }
//
//            @Override
//            public void sendFail(String message) {
//                view.showError(message);
//            }
//
//            @Override
//            public void getMessageId(String messageId) {
//
//            }
//        });
    }

//    @Override
//    public void getMessageId(String chatId) {
//        messageInterface.getMessageId(chatId, new MessageInterface.SendMessageCallBack() {
//            @Override
//            public void getMessageId(String messageId) {
//
//            }
//
//            @Override
//            public void sendSuccess() {
//
//            }
//
//            @Override
//            public void sendFail(String error) {
//
//            }
//        });
//    }
}
