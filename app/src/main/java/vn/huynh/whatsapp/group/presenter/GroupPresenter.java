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
    public void addListener() {
        this.chatModelInterface.addListener();
    }

    @Override
    public void loadListGroup(final List<Chat> list) {
        if(view != null)
            view.showLoadingIndicator();
        chatModelInterface.getChatList(true, new ChatInterface.ChatListCallBack() {
            @Override
            public void loadSuccess(Chat chatObject) {
                if (view != null) {
                    if (chatObject != null) {
                        int addPosition = 0;
                        if (list.size() > 0) {
                            if (list.size() == 1) {
                                if (list.get(0).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
                                    list.add(0, chatObject);
                                    addPosition = 0;
                                } else {
                                    list.add(chatObject);
                                    addPosition = list.size() - 1;
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
                                        list.add(i, chatObject);
                                        addPosition = i;
                                        break;
                                    } else if (i == (list.size() - 1)) {
                                        list.add(chatObject);
                                        addPosition = list.size() - 1;
                                        break;
                                    }
                                }
                            }
                        } else {
                            list.add(chatObject);
                            addPosition = list.size() - 1;
                        }
                        view.showListGroup(addPosition);
                    }
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void updateChatStatus(Chat chatObject) {
                if (view != null) {
                    if (chatObject != null) {
//                        if(list.size() > 0) {
//                            int i = list.indexOf(chatObject);
//                            if(i > 0) {
//                                Collections.swap(list, i, 0);
//                            }
//                        }
                        view.updateListGroupStatus(chatObject);
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
}
