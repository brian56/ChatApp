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
import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 4/2/2019.
 */

public class ChatPresenter implements ChatContract.Presenter {
    private ChatContract.View view;
    private ChatInterface chatRepo;
    private MessageInterface messageRepo;

    public ChatPresenter() {
        chatRepo = new ChatRepository();
        messageRepo = new MessageRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (ChatContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void removeChatDetailListener() {
        this.chatRepo.removeChatDetailListener();
    }

    @Override
    public void addChatDetailListener() {
        this.chatRepo.addChatDetailListener();
    }

    @Override
    public void addMessageListener() {
        this.messageRepo.addMessageListener();
    }

    @Override
    public void removeMessageListener() {
        this.messageRepo.removeMessageListener();
    }

    @Override
    public void loadChatDetail(String chatId) {
        if (view != null)
            view.showLoadingIndicator();
        chatRepo.getChatDetail(chatId, new ChatInterface.ChatDetailCallBack() {
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
                    view.showErrorMessage(message);
                    view.hideLoadingIndicator();
                }
            }
        });
    }

    @Override
    public void loadChatMessage(String chatId) {
        if (view != null)
            view.showLoadingIndicator();
        messageRepo.getChatMessageData(chatId, new MessageInterface.GetChatMessageCallBack() {
            @Override
            public void loadSuccess(Message messageObjects) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showMessageList(messageObjects);
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showEmptyDataIndicator();
                }
            }

            @Override
            public void loadFail(String message) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showErrorIndicator();
                    view.showErrorMessage(message);
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
//        if (mediaUri.size() > 0) {
//            if(view != null)
//                view.showLoadingIndicator();
//        }

        messageRepo.getNewMessageId(chat.getId(), new MessageInterface.SendMessageCallBack() {
            @Override
            public void getNewMessageIdSuccess(String messageId) {
                Message message = new Message(messageId);
                message.setStatus(Message.STATUS_SENDING);
                message.setCreator(ChatUtils.currentUserId());
                message.setText(text);
                Map<String, Long> seenUsers = new HashMap<>();
                seenUsers.put(ChatUtils.currentUserId(), (long) 1);
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
                messageRepo.sendMessage(chat, messageId, text, mediaUri, new MessageInterface.SendMessageCallBack() {
                    @Override
                    public void sendSuccess() {
                        if(view!= null) {
                            view.hideLoadingIndicator();
                        }
                    }

                    @Override
                    public void sendFail(String message) {
                        if(view != null)
                            view.showErrorMessage(message);
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

    }
}
