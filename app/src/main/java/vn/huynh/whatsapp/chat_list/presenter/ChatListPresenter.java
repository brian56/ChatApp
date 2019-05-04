package vn.huynh.whatsapp.chat_list.presenter;

import java.util.List;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.chat_list.ChatListContract;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.ChatRepository;

/**
 * Created by duong on 4/2/2019.
 */

public class ChatListPresenter implements ChatListContract.Presenter {
    private static final String TAG = ChatListPresenter.class.getSimpleName();
    protected ChatListContract.View view;
    protected ChatInterface chatRepo;

    public ChatListPresenter() {
        chatRepo = new ChatRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (ChatListContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void removeChatListListener() {
        this.chatRepo.removeChatListListener();
    }

    @Override
    public void addChatListListener() {
        this.chatRepo.addChatListListener();
    }

    @Override
    public void loadChatList(boolean isGroup, final List<Chat> list) {
        if (view != null)
            view.showLoadingIndicator();
        chatRepo.getChatList(isGroup, new ChatInterface.ChatListCallback() {
            @Override
            public void loadSuccess(Chat chatObject) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    if (chatObject != null) {
////                        removeExistChat(list, chatObject);
                        int addPosition = 0;
                        if (list.size() > 0) {
                            if (list.size() == 1) {
                                if (list.get(0).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
//                                    list.add(0, chatObject);
                                    addPosition = 0;
                                } else {
//                                    list.add(chatObject);
                                    addPosition = list.size();
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
//                                        list.add(i, chatObject);
                                        addPosition = i;
                                        break;
                                    } else if (i == (list.size() - 1)) {
//                                        list.add(chatObject);
                                        addPosition = list.size();
                                        break;
                                    }
                                }
                            }
                        } else {
//                            list.add(chatObject);
                            addPosition = list.size();
                        }
//                        list.add(0, chatObject);
                        view.showChatList(chatObject, addPosition);
                    } else {
                        //not a group chat, check empty list
                        view.showChatList(null, -1);
                    }
                }
            }

            @Override
            public void updateChatStatus(Chat chatObject) {
                if (view != null) {
                    if (chatObject != null) {
                        view.updateChatListStatus(chatObject);
                    }
                    view.hideLoadingIndicator();
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

            @Override
            public void removeSuccess(Chat chatObject) {

            }

            @Override
            public void removeFail(String message) {

            }
        });
    }

    public static void updateChat(List<Chat> list, Chat chatObject) {
        if (chatObject == null || list == null || list.size() == 0) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(chatObject.getId())) {
                list.get(i).cloneChat(chatObject);
                break;
            }
        }
    }
}
