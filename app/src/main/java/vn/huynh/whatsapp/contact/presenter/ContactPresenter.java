package vn.huynh.whatsapp.contact.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.base.BaseView;
import vn.huynh.whatsapp.contact.ContactContract;
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
    private ChatInterface chatRepo;
    private UserInterface userRepo;
    private ContactContract.View viewContact;
    private GroupContract.View viewGroup;
    private LoadContactAsyncTask loadContactAsyncTask;

    public ContactPresenter() {
        chatRepo = new ChatRepository();
        userRepo = new UserRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof ContactContract.View)
            this.viewContact = (ContactContract.View) view;
        else if (view instanceof GroupContract.View)
            this.viewGroup = (GroupContract.View) view;
    }

    @Override
    public void detachView() {
        this.viewContact = null;
        this.viewGroup = null;
    }

    @Override
    public void loadListContact(Context context) {
        if (viewContact != null)
            viewContact.showLoadingIndicator();
        if (loadContactAsyncTask == null) {
            loadContactAsyncTask = new LoadContactAsyncTask(context);
            loadContactAsyncTask.execute();
        } else {
            loadContactAsyncTask.cancel(false);
            loadContactAsyncTask = new LoadContactAsyncTask(context);
            loadContactAsyncTask.execute();
        }
    }

    @Override
    public void loadListContactForGroup(Context context) {
        if (viewGroup != null)
            viewGroup.showLoadingIndicator();
//        loadContactAsyncTask = new LoadContactAsyncTask(context);
//        loadContactAsyncTask.execute();
        if (loadContactAsyncTask == null) {
            loadContactAsyncTask = new LoadContactAsyncTask(context);
            loadContactAsyncTask.execute();
        } else {
            loadContactAsyncTask.cancel(false);
            loadContactAsyncTask = new LoadContactAsyncTask(context);
            loadContactAsyncTask.execute();
        }
    }

    @Override
    public void checkSingleChatExist(final boolean isGroup, final String name, final List<User> users) {
        if (viewContact != null)
            viewContact.showLoadingIndicator();
        String singleChatId = ChatUtils.getSingleChatIdFomUsers(users);
        chatRepo.checkSingleChatExist(singleChatId, new ChatInterface.CheckSingleChatCallback() {
            @Override
            public void exist(String chatId) {
                if (viewContact != null) {
                    viewContact.openChat(chatId);
                    viewContact.hideLoadingIndicator();
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
        if (viewContact != null)
            viewContact.showLoadingIndicator();
        chatRepo.createChat(isGroup, name, users, new ChatInterface.CreateChatCallback() {
            @Override
            public void createSuccess(String chatId) {
                if (viewContact != null) {
                    viewContact.openChat(chatId);
                    viewContact.hideLoadingIndicator();
                }
            }

            @Override
            public void createFail(String message) {
                if (viewContact != null) {
                    viewContact.showErrorMessage(message);
                    viewContact.hideLoadingIndicator();
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
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
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
            userRepo.loadContact(context, list, new UserInterface.LoadContactCallBack() {
                @Override
                public void loadSuccess(User userObject) {
                    if (viewContact != null) {
                        viewContact.showListContact(userObject);
                        viewContact.hideLoadingIndicator();
                    }
                    if (viewGroup != null) {
                        viewGroup.showListContact(userObject);
                        viewGroup.hideLoadingIndicator();
                    }
                }

                @Override
                public void loadFail(String message) {
                    if (viewContact != null) {
                        viewContact.hideLoadingIndicator();
                        viewContact.showErrorIndicator();
                        viewContact.showErrorMessage(message);
                    }
                    if (viewGroup != null) {
                        viewGroup.hideLoadingIndicator();
                        viewGroup.showErrorIndicator();
                        viewGroup.showErrorMessage(message);
                    }
                }
            });
        }
    }
}
