package vn.huynh.whatsapp.chat.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String TAG = ChatPresenter.class.getSimpleName();
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
    public void loadChatDetail(final String chatId) {
        if (view != null)
            view.showLoadingIndicator();
        chatRepo.getChatDetail(chatId, new ChatInterface.ChatDetailCallback() {
            @Override
            public void loadSuccess(final Chat chatObject) {
                if (view != null) {
                    chatRepo.resetNumberUnread(chatId, new ChatInterface.ResetUnreadMessageCallback() {
                        @Override
                        public void success() {
                            view.showChatDetail(chatObject);
                            view.hideLoadingIndicator();
                        }

                        @Override
                        public void fail() {
                            view.showErrorMessage("");
                            view.hideLoadingIndicator();
                        }
                    });
                }
            }

            @Override
            public void loadFail(String message) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showErrorMessage(message);
                }
            }
        });
    }

    @Override
    public void resetNumberUnread(String chatId) {
        chatRepo.resetNumberUnread(chatId, new ChatInterface.ResetUnreadMessageCallback() {
            @Override
            public void success() {
            }

            @Override
            public void fail() {
            }
        });
    }

    @Override
    public void loadMessage(final String chatId) {
        if (view != null)
            view.showLoadingIndicator();

        messageRepo.getChatMessageFirstPage(chatId, new MessageInterface.GetChatMessageFirstPageCallback() {
            @Override
            public void loadSuccess(List<Message> messages, String newestMessageId) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showMessageList(messages, false);
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadSuccessDone(List<Message> messages, String newestMessageId) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showMessageList(messages, true);
                    //TODO: load done
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadSuccessEmptyData() {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showMessageList(null, true);
                    view.showEmptyDataIndicator();
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadFail(String error) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void loadMessageMore(String chatId) {
        messageRepo.getChatMessageLoadMore(chatId, new MessageInterface.GetChatMessageLoadMoreCallback() {
            @Override
            public void loadSuccess(List<Message> messages) {
                if (view != null) {
                    view.showMessageListLoadMore(messages, false);
                }
            }

            @Override
            public void loadSuccessDone(List<Message> messages) {
                if (view != null) {
                    view.showMessageListLoadMore(messages, true);
                    //TODO: load done
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if (view != null) {
                    view.showMessageListLoadMore(null, true);
                    //TODO: load done
                }
            }

            @Override
            public void loadFail(String error) {
                if (view != null) {
                    view.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void listenToNewMessage(String chatId) {
        messageRepo.getNewMessage(chatId, new MessageInterface.GetNewMessageCallback() {
            @Override
            public void getSuccess(Message messageObjects) {
                if (view != null) {
                    view.showMessage(messageObjects);
                }
            }

            @Override
            public void getFail(String message) {
                if (view != null) {
                    view.showErrorMessage(message);
                }
            }

        });
    }


    @Override
    public void sendMessage(final Chat chat, final String text, final ArrayList<String> mediaUri) {
        messageRepo.getNewMessageId(chat.getId(), new MessageInterface.SendMessageCallBack() {
            @Override
            public void getNewMessageIdSuccess(String messageId) {
                Message message = new Message(messageId);
                message.setStatus(Message.STATUS_SENDING);
                message.setCreator(ChatUtils.getUser().getId());
                message.setText(text);
                Map<String, Long> seenUsers = new HashMap<>();
                seenUsers.put(ChatUtils.getUser().getId(), (long) 1);
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
                        //send success
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
