package vn.huynh.whatsapp.contact_friend.contact.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.contact_friend.contact.ContactContract;
import vn.huynh.whatsapp.group.GroupContract;
import vn.huynh.whatsapp.model.ChatInterface;
import vn.huynh.whatsapp.model.ChatRepository;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.model.UserInterface;
import vn.huynh.whatsapp.model.UserRepository;
import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 4/12/2019.
 */

public class ContactPresenter implements ContactContract.Presenter {
    public static final String TAG = ContactPresenter.class.getSimpleName();
    private ChatInterface mChatRepo;
    private UserInterface mUserRepo;
    private ContactContract.View mViewContact;
    private GroupContract.View mViewGroup;
    private LoadContactAsyncTask mLoadContactAsyncTask;

    public ContactPresenter() {
        mChatRepo = new ChatRepository();
        mUserRepo = new UserRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof ContactContract.View)
            this.mViewContact = (ContactContract.View) view;
        else if (view instanceof GroupContract.View)
            this.mViewGroup = (GroupContract.View) view;
    }

    @Override
    public void detachView() {
        this.mViewContact = null;
        this.mViewGroup = null;
    }

    @Override
    public void loadListContact(Context context) {
        if (mViewContact != null)
            mViewContact.showLoadingIndicator();
        if (mLoadContactAsyncTask == null) {
            mLoadContactAsyncTask = new LoadContactAsyncTask(context);
            mLoadContactAsyncTask.execute();
        } else {
            mLoadContactAsyncTask.cancel(false);
            mLoadContactAsyncTask = new LoadContactAsyncTask(context);
            mLoadContactAsyncTask.execute();
        }
    }

    @Override
    public void loadListContactForGroup(Context context) {
        if (mViewGroup != null)
            mViewGroup.showLoadingIndicator();
        if (mLoadContactAsyncTask == null) {
            mLoadContactAsyncTask = new LoadContactAsyncTask(context);
            mLoadContactAsyncTask.execute();
        } else {
            mLoadContactAsyncTask.cancel(false);
            mLoadContactAsyncTask = new LoadContactAsyncTask(context);
            mLoadContactAsyncTask.execute();
        }
    }

    @Override
    public void checkSingleChatExist(final boolean isGroup, final String name, final List<User> users) {
        if (mViewContact != null)
            mViewContact.showLoadingIndicator();
        String singleChatId = ChatUtils.getSingleChatIdFomUsers(users);
        mChatRepo.checkSingleChatExist(singleChatId, new ChatInterface.CheckSingleChatCallback() {
            @Override
            public void exist(String chatId) {
                if (mViewContact != null) {
                    mViewContact.openChat(chatId);
                    mViewContact.hideLoadingIndicator();
                }
            }

            @Override
            public void notExist() {
                createChat(isGroup, name, users);
            }
        });
    }

    @Override
    public void createChat(boolean isGroup, String name, List<User> users) {
        if (mViewContact != null)
            mViewContact.showLoadingIndicator();
        mChatRepo.createChat(isGroup, name, users, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (mViewContact != null) {
                    mViewContact.openChat(chatId);
                    mViewContact.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (mViewContact != null) {
                    mViewContact.showErrorMessage(message);
                    mViewContact.hideLoadingIndicator();
                }
            }
        });
    }

    class LoadContactAsyncTask extends AsyncTask<Void, Void, List<User>> {
        private Context context;

        public LoadContactAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            if (this.isCancelled()) {
                return null;
            }
            List<User> contacts = new ArrayList<>();
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "UPPER(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
            try {
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    String phone = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phone = ChatUtils.formatPhone(phone, context);
                    User contact = new User("", name, phone);
                    contacts.add(contact);
                }
            } catch (NullPointerException e) {
                return contacts;
            }
            phones.close();
            return contacts;
        }

        @Override
        protected void onPostExecute(List<User> list) {
            super.onPostExecute(list);
            mUserRepo.loadContact(context, list, new UserInterface.LoadContactCallback() {
                @Override
                public void loadSuccess(User userObject) {
                    if (mViewContact != null) {
                        mViewContact.showListContact(userObject);
                        mViewContact.hideLoadingIndicator();
                    }
                    if (mViewGroup != null) {
                        mViewGroup.showListContact(userObject);
                    }
                }

                @Override
                public void loadFail(String message) {
                    if (mViewContact != null) {
                        mViewContact.hideLoadingIndicator();
                        mViewContact.showErrorIndicator();
                        mViewContact.showErrorMessage(message);
                    }
                    if (mViewGroup != null) {
                        mViewGroup.hideLoadingIndicator();
                        mViewGroup.showErrorIndicator();
                        mViewGroup.showErrorMessage(message);
                    }
                }
            });
        }
    }

    @Override
    public void searchFriend(String phoneNumber) {
        if (mViewContact != null) {
            mViewContact.showLoadingIndicator();
        }
        mUserRepo.searchFriend(phoneNumber, new UserInterface.SearchFriendCallback() {
            @Override
            public void onSearchSuccess(List<User> userList) {
                if (mViewContact != null) {
                    mViewContact.showSearchResult(userList);
                }
            }

            @Override
            public void onSearchFail(String error) {
                if (mViewContact != null) {
                    mViewContact.showErrorMessage(error);
                }
            }
        });
    }
}
