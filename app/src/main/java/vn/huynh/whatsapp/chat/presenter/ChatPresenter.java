package vn.huynh.whatsapp.chat.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.chat_list.ChatListContract;
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
    private ChatContract.View mViewChat;
    private ChatListContract.View mViewChatList;
    private ChatInterface mChatRepo;
    private MessageInterface mMessageRepo;

    public ChatPresenter() {
        mChatRepo = new ChatRepository();
        mMessageRepo = new MessageRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof ChatContract.View)
            this.mViewChat = (ChatContract.View) view;
        if (view instanceof ChatListContract.View)
            this.mViewChatList = (ChatListContract.View) view;
    }

    @Override
    public void detachView() {
        this.mViewChat = null;
        this.mViewChatList = null;
    }

    @Override
    public void removeChatDetailListener() {
        this.mChatRepo.removeChatDetailListener();
    }

    @Override
    public void addChatDetailListener() {
        this.mChatRepo.addChatDetailListener();
    }

    @Override
    public void addMessageListener() {
        this.mMessageRepo.addMessageListener();
    }

    @Override
    public void removeMessageListener() {
        this.mMessageRepo.removeMessageListener();
    }

    @Override
    public void loadChatDetail(final String chatId) {
        if (mViewChat != null)
            mViewChat.showLoadingIndicator();
        mChatRepo.getChatDetail(chatId, new ChatInterface.ChatDetailCallback() {
            @Override
            public void loadSuccess(final Chat chatObject) {
                if (mViewChat != null) {
                    mChatRepo.resetNumberUnread(chatId, new ChatInterface.ResetUnreadMessageCallback() {
                        @Override
                        public void success() {
                            if (mViewChat != null) {
                                mViewChat.showChatDetail(chatObject);
                                mViewChat.hideLoadingIndicator();
                            }
                        }

                        @Override
                        public void fail(String error) {
                            if (mViewChat != null) {
                                mViewChat.showErrorMessage(error);
                                mViewChat.hideLoadingIndicator();
                            }
                        }
                    });
                }
            }

            @Override
            public void loadFail(String message) {
                if (mViewChat != null) {
                    mViewChat.hideLoadingIndicator();
                    mViewChat.showErrorMessage(message);
                }
            }
        });
    }

    @Override
    public void setChatNotification(boolean turnOn, String chatId) {
        mChatRepo.setChatNotification(turnOn, chatId, new ChatInterface.TurnOffNotificationCallback() {
            @Override
            public void success(String chatId, boolean turnOn) {
                if (mViewChatList != null) {
                    mViewChatList.updateChatNotification(chatId, turnOn);
                }
            }

            @Override
            public void fail(String error) {
                if (mViewChatList != null) {
                    mViewChatList.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void resetNumberUnread(final String chatId, final boolean loadMessage) {
        mChatRepo.resetNumberUnread(chatId, new ChatInterface.ResetUnreadMessageCallback() {
            @Override
            public void success() {
                if (loadMessage) {
                    if (mViewChat != null) {
                        mViewChat.loadMessage();
                    }
                }
                if (mViewChatList != null) {
                    mViewChatList.updateNumberUnreadMessage(chatId);
                }
            }

            @Override
            public void fail(String error) {
                if (mViewChat != null)
                    mViewChat.showErrorMessage(error);
                if (mViewChatList != null)
                    mViewChatList.showErrorMessage(error);
            }
        });
    }

    @Override
    public void loadMessage(final String chatId) {
        if (mViewChat != null)
            mViewChat.showLoadingIndicator();

        mMessageRepo.getChatMessageFirstPage(chatId, new MessageInterface.GetChatMessageFirstPageCallback() {
            @Override
            public void loadSuccess(List<Message> messages, String newestMessageId) {
                if (mViewChat != null) {
                    mViewChat.hideLoadingIndicator();
                    mViewChat.showMessageList(messages, false);
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadSuccessDone(List<Message> messages, String newestMessageId) {
                if (mViewChat != null) {
                    mViewChat.hideLoadingIndicator();
                    mViewChat.showMessageList(messages, true);
                    //TODO: load done
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadSuccessEmptyData() {
                if (mViewChat != null) {
                    mViewChat.hideLoadingIndicator();
                    mViewChat.showMessageList(null, true);
                    mViewChat.showEmptyDataIndicator();
                }
                listenToNewMessage(chatId);
            }

            @Override
            public void loadFail(String error) {
                if (mViewChat != null) {
                    mViewChat.hideLoadingIndicator();
                    mViewChat.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void loadMessageMore(String chatId) {
        mMessageRepo.getChatMessageLoadMore(chatId, new MessageInterface.GetChatMessageLoadMoreCallback() {
            @Override
            public void loadSuccess(List<Message> messages) {
                if (mViewChat != null) {
                    mViewChat.showMessageListLoadMore(messages, false);
                }
            }

            @Override
            public void loadSuccessDone(List<Message> messages) {
                if (mViewChat != null) {
                    mViewChat.showMessageListLoadMore(messages, true);
                    //TODO: load done
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if (mViewChat != null) {
                    mViewChat.showMessageListLoadMore(null, true);
                    //TODO: load done
                }
            }

            @Override
            public void loadFail(String error) {
                if (mViewChat != null) {
                    mViewChat.showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void listenToNewMessage(String chatId) {
        mMessageRepo.getNewMessage(chatId, new MessageInterface.GetNewMessageCallback() {
            @Override
            public void getSuccess(Message messageObjects) {
                if (mViewChat != null) {
                    mViewChat.showNewMessage(messageObjects);
                }
            }

            @Override
            public void getFail(String message) {
                if (mViewChat != null) {
                    mViewChat.showErrorMessage(message);
                }
            }

        });
    }


    @Override
    public void sendMessage(final Chat chat, final String text, final ArrayList<String> mediaUri) {
        mMessageRepo.getNewMessageId(chat.getId(), new MessageInterface.SendMessageCallBack() {
            @Override
            public void getNewMessageIdSuccess(String messageId) {
                Message message = new Message(messageId);
                message.setStatus(Message.STATUS_SENDING);
                message.setCreator(ChatUtils.getUser().getId());
                message.setText(text);
                Map<String, Long> seenUsers = new HashMap<>();
                seenUsers.put(ChatUtils.getUser().getId(), (long) 1);
                message.setSeenUsers(seenUsers);

                if (mediaUri.size() > 0) {
                    Map<String, String> mediaMap = new HashMap<>();
                    for (int i = 0; i < mediaUri.size(); i++) {
                        mediaMap.put(mediaUri.get(i), "");
                    }
                    message.setMedia(mediaMap);
                }
                if (mViewChat != null) {
                    mViewChat.addSendingMessageToList(message);
                    mViewChat.resetUI();
                }
                mMessageRepo.sendMessage(chat, messageId, text, mediaUri, new MessageInterface.SendMessageCallBack() {
                    @Override
                    public void sendSuccess() {
                        //send success
                    }

                    @Override
                    public void sendFail(String message) {
                        if (mViewChat != null)
                            mViewChat.showErrorMessage(message);
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

    @Override
    public void cancelUpload() {
        mMessageRepo.cancelUpload();
    }
}
