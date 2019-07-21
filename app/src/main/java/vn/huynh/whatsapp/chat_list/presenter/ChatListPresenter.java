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
    protected ChatListContract.View mChatListview;
    protected ChatInterface mChatRepo;

    public ChatListPresenter() {
        mChatRepo = new ChatRepository();
    }

    @Override
    public void attachView(BaseView view) {
        this.mChatListview = (ChatListContract.View) view;
    }

    @Override
    public void detachView() {
        this.mChatListview = null;
    }

    @Override
    public void removeChatListListener() {
        this.mChatRepo.removeChatListListener();
    }

    @Override
    public void addChatListListener() {
        this.mChatRepo.addChatListListener();
    }

    @Override
    public void loadChatList(boolean isGroup, final List<Chat> list) {
        if (mChatListview != null)
            mChatListview.showLoadingIndicator();
        mChatRepo.getChatList(isGroup, new ChatInterface.ChatListCallback() {
            @Override
            public void loadSuccess(Chat chatObject) {
                if (mChatListview != null) {
                    if (chatObject != null) {
                        int addPosition = 0;
                        if (list != null && list.size() > 0) {
                            for (int i = 0; i < list.size(); i++) {
                                if (chatObject.getLastMessageDateInLong() < list.get(i).getLastMessageDateInLong()) {
                                    addPosition = i + 1;
                                }
                            }
                        }
                        mChatListview.showChatList(chatObject, addPosition);
                    } else {
                        //not a group chat, check empty list
                        mChatListview.showChatList(null, -1);
                    }
                }
            }

            @Override
            public void updateChatStatus(Chat chatObject, boolean hasNewMessage) {
                if (mChatListview != null) {
                    if (chatObject != null) {
                        mChatListview.updateChatStatus(chatObject, hasNewMessage);
                    }
                }
            }

            @Override
            public void loadSuccessEmptyData() {
                if (mChatListview != null) {
                    mChatListview.hideLoadingIndicator();
                    mChatListview.showEmptyDataIndicator();
                }
            }

            @Override
            public void loadFail(String message) {
                if (mChatListview != null) {
                    mChatListview.hideLoadingIndicator();
                    mChatListview.showErrorIndicator();
                    mChatListview.showErrorMessage(message);
                }
            }

            @Override
            public void getChatCount(long count) {
                if (mChatListview != null) {
                    mChatListview.setChatCount(count);
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
}
