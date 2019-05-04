package vn.huynh.whatsapp.group.presenter;

import java.util.List;

import vn.huynh.whatsapp.chat_list.presenter.ChatListPresenter;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 4/7/2019.
 */

public class GroupPresenter extends ChatListPresenter implements GroupContract.Presenter {
    private static final String TAG = GroupPresenter.class.getSimpleName();
    public GroupPresenter() {
        super();
    }


//    @Override
//    public void attachView(BaseView view) {
//        this.view = (GroupContract.View) view;
//    }
//
//    @Override
//    public void detachView() {
//        this.view = null;
//    }


//    @Override
//    public void loadListGroup(final List<Chat> list) {
//        if(view != null)
//            view.showLoadingSwipeLayout();
//        chatRepo.getChatList(true, new ChatInterface.ChatListCallback() {
//            @Override
//            public void loadSuccess(Chat chatObject) {
//                if (view != null) {
//                    if (chatObject != null) {
//                        int addPosition = 0;
//                        if (list.size() > 0) {
//                            if (list.size() == 1) {
//                                if (list.get(0).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
//                                    list.add(0, chatObject);
//                                    addPosition = 0;
//                                } else {
//                                    list.add(chatObject);
//                                    addPosition = list.size() - 1;
//                                }
//                            } else {
//                                for (int i = 0; i < list.size(); i++) {
//                                    if (list.get(i).getLastMessageDateInLong() < chatObject.getLastMessageDateInLong()) {
//                                        list.add(i, chatObject);
//                                        addPosition = i;
//                                        break;
//                                    } else if (i == (list.size() - 1)) {
//                                        list.add(chatObject);
//                                        addPosition = list.size() - 1;
//                                        break;
//                                    }
//                                }
//                            }
//                        } else {
//                            list.add(chatObject);
//                            addPosition = list.size() - 1;
//                        }
//                        view.showListGroup(addPosition);
//                    }
//                    view.hideLoadingSwipeLayout();
//                }
//            }
//
//            @Override
//            public void updateChatStatus(Chat chatObject) {
//                if (view != null) {
//                    if (chatObject != null) {
////                        if(list.size() > 0) {
////                            int i = list.indexOf(chatObject);
////                            if(i > 0) {
////                                Collections.swap(list, i, 0);
////                            }
////                        }
//                        view.updateListGroupStatus(chatObject);
//                    }
//                    view.hideLoadingSwipeLayout();
//                }
//            }
//
//            @Override
//            public void loadSuccessEmptyData() {
//                if(view != null) {
//                    view.hideLoadingSwipeLayout();
//                    view.showListGroupEmpty();
//                }
//            }
//
//            @Override
//            public void loadFail(String message) {
//                if (view != null) {
//                    view.showErrorMessage(message);
//                    view.hideLoadingSwipeLayout();
//                }
//            }
//
//            @Override
//            public void removeSuccess(Chat chatObject) {
//
//            }
//
//            @Override
//            public void removeFail(String message) {
//
//            }
//        });
//    }

    @Override
    public void createGroupChat(String groupName, List<User> list) {
        if (view != null) {
            view.showLoadingIndicator();
        }
        chatRepo.createChat(true, groupName, list, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (view != null) {
                    ((GroupContract.View) view).openChat(chatId);
                    view.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (view != null) {
                    view.hideLoadingIndicator();
                    view.showErrorMessage(message);
                }
            }
        });
    }
}
