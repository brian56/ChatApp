package vn.huynh.whatsapp.chat.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseActivity;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.chat.presenter.ChatPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.services.NewMessageService;
import vn.huynh.whatsapp.services.UpdateOnlineStatusService;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.MyApp;

public class ChatActivity extends BaseActivity implements ChatContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rv_chat)
    RecyclerView rvChat;
    @BindView(R.id.edt_message)
    EditText edtMessage;
    @BindView(R.id.btn_send)
    ImageButton btnSend;
    @BindView(R.id.btn_add_media)
    ImageButton btnAddMedia;
    @BindView(R.id.rv_media)
    RecyclerView rvMedia;
    @BindView(R.id.ll_indicator)
    LinearLayout llIndicator;
    @BindView(R.id.loader)
    CircularDotsLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;
    @BindView(R.id.btn_new_message)
    Button btnScrollToNewMessage;

    private static final String TAG = ChatActivity.class.getSimpleName();
    private MessageAdapter mMessageAdapter;
    private LinearLayoutManager mMessageLayoutManager;
    private ArrayList<Message> mMessageArrayList;
    private static final int PICK_IMAGE_INTENT = 1;
    private Chat mChatObject;

    private MediaAdapter mMediaAdapter;
    private RecyclerView.LayoutManager mMediaLayoutManager;
    private ArrayList<String> mMediaUriList;
    private String mMessage = "";
    private int currentPosition = 0;
    //    private boolean firstStart = true;
    private boolean mIsReturnFromGallery = false;
    private static boolean sIsVisible = false;
    private String mChatId;
    private String mChatName;

    private ChatContract.Presenter mChatPresenter;
    private boolean mIsLoadingMoreMessage = false;
    private boolean mIsLoadAllMessageDone = false;
    private RecyclerView.OnScrollListener mOnScrollLoadMoreListener;

//    private static String KEY_CHAT_ID = "KEY_CHAT_ID";
//    private static String KEY_CHAT_NAME = "KEY_CHAT_NAME";
//    private static String KEY_MESSAGE_LIST = "KEY_MESSAGE_LIST";
//    private static String KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(MyApp.resources.getString(R.string.menu_chat));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mChatObject = bundle.getParcelable(Constant.EXTRA_CHAT_OBJECT);
            mChatId = bundle.getString(Constant.EXTRA_CHAT_ID);
            mChatName = bundle.getString(Constant.EXTRA_CHAT_NAME);
            if (!TextUtils.isEmpty(mChatName)) {
                getSupportActionBar().setTitle(mChatName);
            }
            if (mChatObject != null) {
                mChatId = mChatObject.getId();
                getSupportActionBar().setTitle(mChatObject.getChatName());
            }
            LogManagerUtils.d(TAG, mChatId);
            ChatUtils.setCurrentChatId(mChatId);
            initData();
            setupPresenter(this, mChatObject, mChatId);
            setEvents();
            toggleChatInput(false);
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mChatId = bundle.getString(Constant.EXTRA_CHAT_ID);
                mChatName = bundle.getString(Constant.EXTRA_CHAT_NAME);
                if (!TextUtils.isEmpty(mChatName)) {
                    getSupportActionBar().setTitle(mChatName);
                }
                if (!TextUtils.isEmpty(ChatUtils.getCurrentChatId()) && !mChatId.equals(ChatUtils.getCurrentChatId())) {
                    mChatObject = null;
                    ChatUtils.setCurrentChatId(mChatId);
                    setupPresenter(this, mChatObject, mChatId);
                } else {
                    ChatUtils.setCurrentChatId(mChatId);
                }
                LogManagerUtils.d(TAG, mChatId);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        sIsVisible = true;
        ChatUtils.setCurrentChatId(mChatId);
        if (mIsReturnFromGallery) {
            mIsReturnFromGallery = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mIsReturnFromGallery) {
            sIsVisible = false;
        } else {
            sIsVisible = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsVisible = false;
        stopService(new Intent(this, UpdateOnlineStatusService.class));
        ChatUtils.setCurrentChatId("");

        mChatPresenter.detachView();
        mChatPresenter.removeMessageListener();
        mChatPresenter.removeChatDetailListener();
    }

    public static boolean checkVisible() {
        return sIsVisible;
    }

    @Override
    public void resetData() {
        mMessageArrayList.clear();
        mMessageAdapter.notifyDataSetChanged();
        mIsLoadingMoreMessage = false;
        mIsLoadAllMessageDone = false;
    }

    @Override
    public void setupPresenter(ChatContract.View view, Chat chat, String chatId) {
        mChatPresenter = new ChatPresenter();
        mChatPresenter.attachView(view);

        LogManagerUtils.d(TAG, chatId);

        if (chat != null) {
            showChatDetail(chat);
        } else {
            mChatPresenter.loadChatDetail(chatId);
        }
    }

    @Override
    public void setEvents() {
        btnScrollToNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
                btnScrollToNewMessage.setVisibility(View.GONE);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage();
            }
        });

        btnAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        llIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
                mChatPresenter.loadMessage(mChatObject.getId());
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
            NewMessageService mNewMessageService = binder.getService();
            LogManagerUtils.d(TAG, "setShowMessageNotification()");
            mNewMessageService.setShowMessageNotification(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    
    @Override
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mIsReturnFromGallery = true;
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.title_select_pictures)), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data.getClipData() == null) {
                    mMediaUriList.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mMediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void initData() {
        initializeMessageList();
        initializeMediaList();
        initScrollListener();
    }

    private void initializeMessageList() {
        rvChat.setNestedScrollingEnabled(false);
        rvChat.setHasFixedSize(false);
        mIsLoadingMoreMessage = false;
        mIsLoadAllMessageDone = false;
        mMessageArrayList = new ArrayList<>();
        mMessageLayoutManager = new LinearLayoutManager(getApplicationContext());
        mMessageLayoutManager.setStackFromEnd(true);
        mMessageLayoutManager.setSmoothScrollbarEnabled(true);
        rvChat.setLayoutManager(mMessageLayoutManager);
        mMessageAdapter = new MessageAdapter(mMessageArrayList, mChatObject, ChatActivity.this);
        rvChat.setAdapter(mMessageAdapter);
    }

    private void initializeMediaList() {
        rvMedia.setNestedScrollingEnabled(false);
        rvMedia.setHasFixedSize(false);
        mMediaUriList = new ArrayList<>();
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false);
        rvMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mMediaUriList, true);
        rvMedia.setAdapter(mMediaAdapter);
    }

    private void initScrollListener() {
        mOnScrollLoadMoreListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mMessageLayoutManager.findLastCompletelyVisibleItemPosition() == mMessageArrayList.size() - 1) {
                    btnScrollToNewMessage.setVisibility(View.GONE);
                }

                if (!mIsLoadingMoreMessage && !mIsLoadAllMessageDone) {
                    LogManagerUtils.d(TAG, "last visible item: " + mMessageLayoutManager.findFirstVisibleItemPosition());
                    if (mMessageLayoutManager != null && mMessageLayoutManager.findFirstVisibleItemPosition() < 15) {
                        //top of message list!
                        loadMoreMessage();
                    }
                }
            }
        };
        rvChat.addOnScrollListener(mOnScrollLoadMoreListener);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mMessage", edtMessage.getText().toString().trim());
        if (mMediaUriList != null && mMediaUriList.size() > 0)
            outState.putStringArrayList("mediaList", mMediaUriList);
//        if(mMessageArrayList != null && !mMessageArrayList.isEmpty()) {
//            outState.putParcelableArrayList(KEY_MESSAGE_LIST, mMessageArrayList);
//            outState.putInt(KEY_CURRENT_POSITION, mMessageLayoutManager.findFirstCompletelyVisibleItemPosition());
//        }
//        outState.putString(KEY_CHAT_ID, mChatId);
//        outState.putString(KEY_CHAT_NAME, mChatName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        edtMessage.setText(savedInstanceState.getString("mMessage"));
        mMediaUriList = savedInstanceState.getStringArrayList("mediaList");
        mMediaAdapter.notifyDataSetChanged();
//        mMessageArrayList = savedInstanceState.getParcelableArrayList(KEY_MESSAGE_LIST);
//        currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
//        mMessageAdapter.notifyDataSetChanged();
//        mMessageLayoutManager.scrollToPositionWithOffset(currentPosition,0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void showLoadingIndicator() {
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListErrorIndicator(llIndicator, llError, false);
        showHideListLoadingIndicator(llIndicator, loader, true);
    }

    @Override
    public void hideLoadingIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
    }

    @Override
    public void showEmptyDataIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListEmptyIndicator(llIndicator, llEmptyData, true);
    }

    @Override
    public void showErrorIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, false);
        showHideListErrorIndicator(llIndicator, llError, true);
    }

    @Override
    public void showChatDetail(Chat object) {
        if (object != null) {
            mChatObject = object;
            mMessageAdapter.setChatObject(mChatObject);
            getSupportActionBar().setTitle(mChatObject.getChatName());
            resetData();
            mChatPresenter.resetNumberUnread(mChatObject.getId(), true);
        }
    }

    @Override
    public void loadMessage() {
        mChatPresenter.loadMessage(mChatObject.getId());
    }

    @Override
    public void loadMoreMessage() {
        if (mMessageArrayList.size() > 0 && mMessageArrayList.get(0) != null) {
            mMessageArrayList.add(0, null);
            mMessageAdapter.notifyItemInserted(0);
        }
        if (mChatPresenter != null) {
            mIsLoadingMoreMessage = true;
            mChatPresenter.loadMessageMore(mChatId);
        }
    }

    @Override
    public void showMessageList(List<Message> messages, boolean isDone) {
//        mChatPresenter.resetNumberUnread(mChatId, false);
        mIsLoadingMoreMessage = false;
        mIsLoadAllMessageDone = isDone;
        if (messages != null && !messages.isEmpty()) {
            mMessageArrayList.addAll(messages);
            mMessageAdapter.notifyDataSetChanged();
            if (isDone) {
                Message last = new Message();
                last.setType(Message.TYPE_LAST_MESSAGE);
                mMessageArrayList.add(0, last);
                mMessageAdapter.notifyItemInserted(0);
            }
            mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
        }
        toggleChatInput(true);
    }

    @Override
    public void showMessageListLoadMore(List<Message> messages, boolean isDone) {
        mIsLoadingMoreMessage = false;
        mIsLoadAllMessageDone = isDone;
        mMessageArrayList.remove(0);
        mMessageAdapter.notifyItemRemoved(0);
//        postDelayNotifyItem(rvChat, mMessageAdapter, 0, false, true, false);
        if (messages != null && !messages.isEmpty()) {
            boolean notifyChange = true;
            for (int i = messages.size() - 1; i >= 0; i--) {
                mMessageArrayList.add(0, messages.get(i));
                mMessageAdapter.notifyItemInserted(0);
                if (notifyChange) {
                    mMessageAdapter.notifyItemChanged(1);
                    notifyChange = false;
                }
            }
        }
        if (isDone) {
//            rvChat.removeOnScrollListener(mOnScrollLoadMoreListener);
            Message last = new Message();
            last.setType(Message.TYPE_LAST_MESSAGE);
            mMessageArrayList.add(0, last);
            mMessageAdapter.notifyItemInserted(0);
            mMessageAdapter.notifyItemChanged(1);
        }
    }

    @Override
    public void showNewMessage(Message messageObject) {
        mChatPresenter.resetNumberUnread(mChatId, false);
        if (messageObject != null) {
            showHideListIndicator(llIndicator, false);
            if (messageObject.getCreator().equals(ChatUtils.getUser().getId())) {
                //my mMessage
                for (int i = mMessageArrayList.size() - 1; i >= 0; i--) {
                    if (mMessageArrayList.get(i) != null && mMessageArrayList.get(i).getType() != Message.TYPE_LAST_MESSAGE) {
                        if (mMessageArrayList.get(i).getId().equals(messageObject.getId())) {
                            //update mMessage that I have just sent
                            Message.copyMessageObject(mMessageArrayList.get(i), messageObject);
                            mMessageAdapter.notifyItemChanged(i);
                            if (i > 0) {
                                mMessageAdapter.notifyItemChanged(i - 1);
                            }
                            mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
                            return;
                        }
                    }
                }
                //old mMessage, just add it to the lists
                mMessageArrayList.add(messageObject);
                if (mMessageArrayList.size() == 0) {
                    mMessageAdapter.notifyItemInserted(0);
                } else {
                    mMessageAdapter.notifyItemInserted(mMessageArrayList.size() - 1);
                }

                if (mMessageArrayList.size() > 1) {
                    //hide the check icon on the previous mMessage
                    mMediaAdapter.notifyItemChanged(mMessageArrayList.size() - 2);
                }

                mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
            } else {
                //their mMessage
                if (mMessageLayoutManager.findLastVisibleItemPosition() == mMessageArrayList.size() - 1) {
                    btnScrollToNewMessage.setVisibility(View.GONE);
                } else {
                    btnScrollToNewMessage.setVisibility(View.VISIBLE);
                }

                mMessageArrayList.add(messageObject);
                mMessageAdapter.notifyItemInserted(mMessageArrayList.size() - 1);

                if (mMessageArrayList.size() > 1) {
                    //hide the check icon in my last mMessage
                    mMessageAdapter.notifyItemChanged(mMessageArrayList.size() - 2);
                }
                if (btnScrollToNewMessage.getVisibility() == View.GONE) {
                    mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
                }
            }
        }
    }

    @Override
    public void showErrorMessage(String error) {
        mIsLoadingMoreMessage = false;
        mIsLoadAllMessageDone = false;
    }

    @Override
    public void addSendingMessageToList(Message messageObject) {
        if (messageObject != null) {
            mMessageArrayList.add(messageObject);
            if (mMessageArrayList.size() > 1)
                mMediaAdapter.notifyItemChanged(mMessageArrayList.size() - 2);
            if (mMessageArrayList.size() == 0)
                mMessageAdapter.notifyItemInserted(0);
            else {
                mMessageAdapter.notifyItemInserted(mMessageArrayList.size() - 1);
            }
            mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
        }
    }

    @Override
    public void newMessage() {
        mMessage = edtMessage.getText().toString().trim();
        if (!mMessage.isEmpty() || (mMediaUriList != null && !mMediaUriList.isEmpty())) {
            showHideListIndicator(llIndicator, false);
            ArrayList<String> mediaArrayList = new ArrayList<>();
            if (mMediaUriList != null) {
                mediaArrayList.addAll(mMediaUriList);
            }
            mChatPresenter.sendMessage(mChatObject, mMessage, mediaArrayList);
        }
    }

    @Override
    public void resetUI() {
        edtMessage.setText(null);
        if (mMediaUriList != null)
            mMediaUriList.clear();
        if (mMediaAdapter != null)
            mMediaAdapter.notifyDataSetChanged();
        if (mMessageArrayList != null && mMessageArrayList.size() > 0)
            mMessageLayoutManager.scrollToPositionWithOffset(mMessageArrayList.size() - 1, 0);
    }

    @Override
    public void toggleChatInput(boolean enable) {
        edtMessage.setEnabled(enable);
        btnAddMedia.setEnabled(enable);
        btnSend.setEnabled(enable);
    }
}
