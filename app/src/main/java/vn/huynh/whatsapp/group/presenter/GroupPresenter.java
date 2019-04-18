package vn.huynh.whatsapp.group.presenter;

import java.util.List;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.ChatRepository;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 4/7/2019.
 */

public class GroupPresenter implements GroupContract.Presenter {
    private GroupContract.View view;
    private ChatInterface chatModelInterface;

    public GroupPresenter() {
        chatModelInterface = new ChatRepository();
    }


    @Override
    public void attachView(BaseView view) {
        this.view = (GroupContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.chatModelInterface.removeListener();
    }

    @Override
    public void removeListener() {
        this.chatModelInterface.removeListener();
    }

    @Override
    public void loadListGroup(final List<Chat> list) {
        list.clear();
        if(view != null)
            view.showLoadingIndicator();
        chatModelInterface.getChatList(true, new ChatInterface.ChatListCallBack() {
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
                        view.showListGroup(list);
                    }
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if(view != null) {
                    view.hideLoadingIndicator();
                    view.showListGroupEmpty();
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

    @Override
    public void createGroupChat(String groupName, List<User> list) {
        view.showLoadingIndicator();
        chatModelInterface.createChat(true, groupName, list, new ChatInterface.CreateChatCallBack() {
            @Override
            public void createSuccess(String chatId) {
                if (view != null) {
                    view.openChat(chatId);
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (view != null) {
                    view.showErrorMessage(message);
                    view.hideLoadingIndicator();
                }
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
