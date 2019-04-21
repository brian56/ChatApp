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
import vn.huynh.whatsapp.utils.Utils;

/**
 * Created by duong on 4/12/2019.
 */

public class ContactPresenter implements ContactContract.Presenter {
    private ChatInterface chatModelInterface;
    private UserInterface userModelInterface;
    private ContactContract.View view;
    private GroupContract.View viewGroup;

    public ContactPresenter() {
        chatModelInterface = new ChatRepository();
        userModelInterface = new UserRepository();
    }

    @Override
    public void attachView(BaseView view) {
        if (view instanceof ContactContract.View)
            this.view = (ContactContract.View) view;
        else if (view instanceof GroupContract.View)
            this.viewGroup = (GroupContract.View) view;
    }

    @Override
    public void detachView() {
        this.view = null;
        this.viewGroup = null;
        this.chatModelInterface.removeListener();
        this.userModelInterface.removeListener();
    }

    @Override
    public void removeListener() {
        this.chatModelInterface.removeListener();
        this.userModelInterface.removeListener();
    }

    @Override
    public void addListener() {
        this.chatModelInterface.addListener();
        this.userModelInterface.addListener();
    }

    @Override
    public void loadListContact(Context context) {
        view.showLoadingIndicator();
        LoadContactAsyncTask loadContactAsyncTask = new LoadContactAsyncTask(context);
        loadContactAsyncTask.execute();
    }

    @Override
    public void loadListContactForGroup(Context context) {
        viewGroup.showLoadingIndicator();
        LoadContactAsyncTask loadContactAsyncTask = new LoadContactAsyncTask(context);
        loadContactAsyncTask.execute();
    }

    @Override
    public void checkSingleChatExist(final boolean isGroup, final String name, final List<User> users) {
        if(view != null)
            view.showLoadingIndicator();
        String singleChatId = Utils.getSingleChatIdFomUsers(users);
        chatModelInterface.checkSingleChatExist(singleChatId, new ChatInterface.CheckSingleChatCallBack() {
            @Override
            public void exist(String chatId) {
                if(view != null) {
                    view.openChat(chatId);
                    view.hideLoadingIndicator();
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
        if(view != null)
            view.showLoadingIndicator();
        chatModelInterface.createChat(isGroup, name, users, new ChatInterface.CreateChatCallBack() {
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

    class LoadContactAsyncTask extends AsyncTask<Void, Void, List<User>> {
        private Context context;

        public LoadContactAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            List<User> contacts = new ArrayList<>();
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            try {
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    String phone = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phone = Utils.formatPhone(phone, context);
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
            userModelInterface.loadContact(context, list, new UserInterface.LoadContactCallBack() {
                @Override
                public void loadSuccess(User userObject) {
                    if (view != null) {
                        view.showListContact(userObject);
                        view.hideLoadingIndicator();
                    }
                    if (viewGroup != null) {
                        viewGroup.showListContact(userObject);
                        viewGroup.hideLoadingIndicator();
                    }
                }

                @Override
                public void loadFail(String message) {
                    if (view != null) {
                        view.showErrorMessage(message);
                        view.hideLoadingIndicator();
                    }
                    if (viewGroup != null) {
                        viewGroup.showErrorMessage(message);
                        viewGroup.hideLoadingIndicator();
                    }
                }
            });
        }
    }
}
