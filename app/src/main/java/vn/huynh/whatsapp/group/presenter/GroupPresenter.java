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

    @Override
    public void createGroupChat(String groupName, List<User> list) {
        if (mChatListview != null) {
            mChatListview.showLoadingIndicator();
        }
        mChatRepo.createChat(true, groupName, list, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (mChatListview != null) {
                    ((GroupContract.View) mChatListview).openChat(chatId);
                    mChatListview.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (mChatListview != null) {
                    mChatListview.hideLoadingIndicator();
                    mChatListview.showErrorMessage(message);
                }
            }
        });
    }
}
