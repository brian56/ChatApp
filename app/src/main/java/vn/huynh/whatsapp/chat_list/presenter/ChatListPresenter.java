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
    private ChatListContract.View view;
    private ChatInterface chatInterface;

    public ChatListPresenter() {
        chatInterface = new ChatRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.view = (ChatListContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.chatInterface.removeListener();
    }

    @Override
    public void removeListener() {
        this.chatInterface.removeListener();
    }

    @Override
    public void loadChatList(final List<Chat> list) {
        list.clear();
        if(view != null)
            view.showLoadingIndicator();
        chatInterface.getChatList(false, new ChatInterface.ChatListCallBack() {
            @Override
            public void loadSuccess(Chat chatObject) {
                if (view != null) {
                    if (chatObject != null) {
                        removeExistChat(list, chatObject);
                        if (list.size() > 0) {
                            if (list.size() == 1) {
                                if (list.get(0).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
                                    list.add(0, chatObject);
                                } else {
                                    list.add(chatObject);
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
                                        list.add(i, chatObject);
                                        break;
                                    } else if (i == (list.size() - 1)) {
                                        list.add(chatObject);
                                        break;
                                    }
                                }
                            }
                        } else {
                            list.add(chatObject);
                        }
                        view.showChatList(list);
                    }
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if(view != null) {
                    view.hideLoadingIndicator();
                    view.showChatListEmpty();
                }
            }

            @Override
            public void loadFail(String message) {
                if (view != null) {
                    view.showErrorMessage(message);
                    view.hideLoadingIndicator();
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

    private void removeExistChat(List<Chat> list, Chat chatObject) {
        if (chatObject == null || list == null || list.size() == 0) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(chatObject.getId())) {
                list.remove(i);
                break;
            }
        }
    }
}