package vn.huynh.whatsapp.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
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
    Button btnNewMessage;

    private static final String TAG = ChatActivity.class.getSimpleName();
    private MessageAdapter messageAdapter;
    private LinearLayoutManager messageLayoutManager;
    private ArrayList<Message> messageList;
    private static final int PICK_IMAGE_INTENT = 1;
    private Chat chatObject;

    private MediaAdapter mediaAdapter;
    private RecyclerView.LayoutManager mediaLayoutManager;
    private ArrayList<String> mediaUriList;
    private String message = "";
    private int currentPosition = 0;
    //    private boolean firstStart = true;
    private boolean returnFromGallery = false;
    private static boolean isVisible = false;
    private String chatId;
    private String chatName;

    private ChatContract.Presenter chatPresenter;
    private long totalMessage = 0;
    private long messageCount = 0;
    private boolean isLoadingMore = false;
    private boolean isLoadedAllMessage = false;
    private RecyclerView.OnScrollListener onScrollLoadMoreListener;
    private RecyclerView.OnScrollListener onScrollNewMessageListener;

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
            chatObject = bundle.getParcelable(Constant.EXTRA_CHAT_OBJECT);
            chatId = bundle.getString(Constant.EXTRA_CHAT_ID);
            chatName = bundle.getString(Constant.EXTRA_CHAT_NAME);
            if (!TextUtils.isEmpty(chatName)) {
                getSupportActionBar().setTitle(chatName);
            }
            if (chatObject != null) {
                chatId = chatObject.getId();
                getSupportActionBar().setTitle(chatObject.getChatName());
            }
            Log.d("ChatActivity", chatId);
            ChatUtils.setCurrentChatId(chatId);
            initializeMessageList();
            initializeMediaList();
            setupPresenter(this, chatObject, chatId);
            setEvents();
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();

        chatId = bundle.getString(Constant.EXTRA_CHAT_ID);
        chatName = bundle.getString(Constant.EXTRA_CHAT_NAME);
        if (!TextUtils.isEmpty(chatName)) {
            getSupportActionBar().setTitle(chatName);
        }
        if (!TextUtils.isEmpty(ChatUtils.getCurrentChatId()) && !chatId.equals(ChatUtils.getCurrentChatId())) {
            chatObject = null;
            ChatUtils.setCurrentChatId(chatId);
            setupPresenter(this, chatObject, chatId);
        } else {
            ChatUtils.setCurrentChatId(chatId);
        }
        Log.d("ChatActivity", chatId);
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

        isVisible = true;
        ChatUtils.setCurrentChatId(chatId);
//        if (!firstStart && !returnFromGallery) {
//            resetDataBeforeReload();
//            if (chatObject != null) {
//                resetDataBeforeReload();
//                chatPresenter.loadMessage(chatId);
//            } else {
//                chatPresenter.loadChatDetail(chatId);
//            }
//        }
        if (returnFromGallery) {
            returnFromGallery = false;
        }
        chatPresenter.resetNumberUnread(chatId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatPresenter.resetNumberUnread(chatId);
        isVisible = false;
//        firstStart = false;
        if (!returnFromGallery) {
            isVisible = false;
//            chatPresenter.removeMessageListener();
        }
    }

    public static boolean checkVisible() {
        return isVisible;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatUtils.setCurrentChatId("");
        chatPresenter.detachView();
        chatPresenter.removeMessageListener();
        chatPresenter.removeChatDetailListener();
    }

    private void resetDataBeforeReload() {
        messageList.clear();
        messageAdapter.notifyDataSetChanged();
        isLoadingMore = false;
        isLoadedAllMessage = false;
        totalMessage = 0;
        messageCount = 0;
    }

    private void setupPresenter(ChatContract.View view, Chat chat, String chatId) {
        chatPresenter = new ChatPresenter();
        chatPresenter.attachView(view);

        Log.d("ChatActivity", chatId);

        if (chat != null) {
            showChatDetail(chat);
        } else {
            chatPresenter.loadChatDetail(chatId);
        }
    }

    private void setEvents() {
        btnNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                btnNewMessage.setVisibility(View.GONE);
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
                resetDataBeforeReload();
                chatPresenter.loadMessage(chatObject.getId());
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        returnFromGallery = true;
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.title_select_pictures)), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data.getClipData() == null) {
                    mediaUriList.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                mediaAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initializeMessageList() {
        rvChat.setNestedScrollingEnabled(false);
        rvChat.setHasFixedSize(false);
        isLoadingMore = false;
        isLoadedAllMessage = false;
        messageList = new ArrayList<>();
        messageLayoutManager = new LinearLayoutManager(getApplicationContext());
        messageLayoutManager.setStackFromEnd(true);
        messageLayoutManager.setSmoothScrollbarEnabled(true);
        rvChat.setLayoutManager(messageLayoutManager);
        messageAdapter = new MessageAdapter(messageList, chatObject, ChatActivity.this);
        rvChat.setAdapter(messageAdapter);
        initScrollListener();
    }

    private void initializeMediaList() {
        rvMedia.setNestedScrollingEnabled(false);
        rvMedia.setHasFixedSize(false);
        mediaUriList = new ArrayList<>();
        mediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false);
        rvMedia.setLayoutManager(mediaLayoutManager);
        mediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList, true);
        rvMedia.setAdapter(mediaAdapter);
    }

    private void initScrollListener() {
        onScrollLoadMoreListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (messageLayoutManager.findLastCompletelyVisibleItemPosition() == messageList.size() - 1) {
                    btnNewMessage.setVisibility(View.GONE);
                }

                if (!isLoadingMore && !isLoadedAllMessage) {
                    Log.d(TAG, "last visible item: " + messageLayoutManager.findFirstVisibleItemPosition());
                    if (messageLayoutManager != null && messageLayoutManager.findFirstVisibleItemPosition() < 8) {
                        //bottom of list!
                        isLoadingMore = true;
                        loadMoreMessage();
                    }
                }
            }
        };
        rvChat.addOnScrollListener(onScrollLoadMoreListener);

    }

    private void loadMoreMessage() {
        if (messageList.get(0) != null) {
            messageList.add(0, null);
            messageAdapter.notifyItemInserted(0);
//            postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
        }
        if (chatPresenter != null)
            chatPresenter.loadMessageMore(chatId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("message", edtMessage.getText().toString().trim());
        if (mediaUriList != null && mediaUriList.size() > 0)
            outState.putStringArrayList("mediaList", mediaUriList);
//        if(messageList != null && !messageList.isEmpty()) {
//            outState.putParcelableArrayList(KEY_MESSAGE_LIST, messageList);
//            outState.putInt(KEY_CURRENT_POSITION, messageLayoutManager.findFirstCompletelyVisibleItemPosition());
//        }
//        outState.putString(KEY_CHAT_ID, chatId);
//        outState.putString(KEY_CHAT_NAME, chatName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        edtMessage.setText(savedInstanceState.getString("message"));
        mediaUriList = savedInstanceState.getStringArrayList("mediaList");
        mediaAdapter.notifyDataSetChanged();
//        messageList = savedInstanceState.getParcelableArrayList(KEY_MESSAGE_LIST);
//        currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
//        messageAdapter.notifyDataSetChanged();
//        messageLayoutManager.scrollToPositionWithOffset(currentPosition,0);
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
            chatObject = object;
            messageAdapter.setChatObject(chatObject);
            getSupportActionBar().setTitle(chatObject.getChatName());
            resetDataBeforeReload();
            chatPresenter.loadMessage(chatObject.getId());
        }
    }

    @Override
    public void showMessageList(List<Message> messages, boolean isDone) {
        chatPresenter.resetNumberUnread(chatId);
        isLoadingMore = false;
        isLoadedAllMessage = isDone;
        if (messages != null && !messages.isEmpty()) {
            messageList.addAll(messages);
            messageAdapter.notifyDataSetChanged();
            if (isDone) {
//                rvChat.removeOnScrollListener(onScrollLoadMoreListener);
                Message last = new Message();
                last.setType(Message.TYPE_LAST_MESSAGE);
                messageList.add(0, last);
                messageAdapter.notifyItemInserted(0);
//                postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
            }
            messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
        }
    }

    @Override
    public void showMessageListLoadMore(List<Message> messages, boolean isDone) {
        isLoadingMore = false;
        isLoadedAllMessage = isDone;
        messageList.remove(0);
        messageAdapter.notifyItemRemoved(0);
//        postDelayNotifyItem(rvChat, messageAdapter, 0, false, true, false);
        if (messages != null && !messages.isEmpty()) {
            boolean notifyChange = true;
            for (int i = messages.size() - 1; i >= 0; i--) {
                messageList.add(0, messages.get(i));
                messageAdapter.notifyItemInserted(0);
                if (notifyChange) {
                    messageAdapter.notifyItemChanged(1);
                    notifyChange = false;
                }
//                postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
            }
//            messageAdapter.notifyItemRangeInserted(0, messages.size());
        }
        if (isDone) {
//            rvChat.removeOnScrollListener(onScrollLoadMoreListener);
            Message last = new Message();
            last.setType(Message.TYPE_LAST_MESSAGE);
            messageList.add(0, last);
            messageAdapter.notifyItemInserted(0);
            messageAdapter.notifyItemChanged(1);
//            postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
        }
    }

    @Override
    public void showMessage(Message messageObject) {
        chatPresenter.resetNumberUnread(chatId);
        if (messageObject != null) {
            showHideListIndicator(llIndicator, false);
            if (messageObject.getCreator().equals(ChatUtils.getCurrentUserId())) {
                //my message
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (messageList.get(i) != null && messageList.get(i).getType() != Message.TYPE_LAST_MESSAGE) {
                        if (messageList.get(i).getId().equals(messageObject.getId())) {
                            //update message that I have just sent
                            Message.copyMessageObject(messageList.get(i), messageObject);
                            messageAdapter.notifyItemChanged(i);
//                           postDelayNotifyItem(rvChat, messageAdapter, i, false, false, true);
                            if (i > 0) {
                                messageAdapter.notifyItemChanged(i - 1);
//                            postDelayNotifyItem(rvChat, messageAdapter, i-1, false, false, true);
                            }
                            messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                            return;
                        }
                    }
                }
                //old message, just add it to the lists
                messageList.add(messageObject);
                if (messageList.size() == 0) {
                    messageAdapter.notifyItemInserted(0);
//                    postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
                } else {
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
//                    postDelayNotifyItem(rvChat, messageAdapter, messageList.size() - 1, true, false, false);
                }

                if (messageList.size() > 1) {
                    //hide the check icon on the previous message
                    mediaAdapter.notifyItemChanged(messageList.size() - 2);
//                    postDelayNotifyItem(rvChat, messageAdapter, messageList.size() - 2, false, false, true);
                }

                messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
            } else {
                //their message
                if (messageLayoutManager.findLastVisibleItemPosition() == messageList.size() - 1) {
                    btnNewMessage.setVisibility(View.GONE);
                } else {
                    btnNewMessage.setVisibility(View.VISIBLE);
                }

                messageList.add(messageObject);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
//                postDelayNotifyItem(rvChat, messageAdapter, messageList.size() - 1, true, false, false);

                if (messageList.size() > 1) {
                    //hide the check icon in my last message
                    messageAdapter.notifyItemChanged(messageList.size() - 2);
//                   postDelayNotifyItem(rvChat, messageAdapter, i, false, false, true);
                }
                if (btnNewMessage.getVisibility() == View.GONE) {
                    messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                }
            }
        }
    }

    @Override
    public void showErrorMessage(String error) {
        isLoadingMore = false;
        isLoadedAllMessage = false;
    }

    @Override
    public void addSendingMessageToList(Message messageObject) {
        if (messageObject != null) {
            messageList.add(messageObject);
            if (messageList.size() > 1)
                mediaAdapter.notifyItemChanged(messageList.size() - 2);
//                postDelayNotifyItem(rvChat, messageAdapter, messageList.size() - 2, false, false, true);
            if (messageList.size() == 0)
                messageAdapter.notifyItemInserted(0);
//                postDelayNotifyItem(rvChat, messageAdapter, 0, true, false, false);
            else {
                messageAdapter.notifyItemInserted(messageList.size() - 1);
//                postDelayNotifyItem(rvChat, messageAdapter, messageList.size() - 1, true, false, false);
            }
            messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
        }
    }

    @Override
    public void newMessage() {
        message = edtMessage.getText().toString().trim();
        if (!message.isEmpty() || (mediaUriList != null && !mediaUriList.isEmpty())) {
            showHideListIndicator(llIndicator, false);
            ArrayList<String> mediaArrayList = new ArrayList<>();
            if (mediaUriList != null) {
                mediaArrayList.addAll(mediaUriList);
            }
            chatPresenter.sendMessage(chatObject, message, mediaArrayList);
        }
    }

   /* @Override
    public void updateMessageStatus(Message message) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if(messageList.get(i).getId().equals(message.getId())) {
                Message.copyMessageObject(messageList.get(i), message);
                messageAdapter.notifyItemChanged(i);
                return;
            }
        }
    }*/

    @Override
    public void resetUI() {
        edtMessage.setText(null);
        mediaUriList.clear();
        mediaAdapter.notifyDataSetChanged();
        if (messageList.size() > 0)
            messageLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
    }

    private void postDelayNotifyItem(RecyclerView recyclerView, final MessageAdapter adapter, final int position,
                                     boolean insert, boolean remove, boolean change) {
        if (insert) {
            recyclerView.post(new Runnable() {
                public void run() {
                    // There is no need to use notifyDataSetChanged()
                    adapter.notifyItemInserted(position);
                }
            });
        }
        if (remove) {
            recyclerView.post(new Runnable() {
                public void run() {
                    // There is no need to use notifyDataSetChanged()
                    adapter.notifyItemRemoved(position);
                }
            });
        }
        if (change) {
            recyclerView.post(new Runnable() {
                public void run() {
                    // There is no need to use notifyDataSetChanged()
                    adapter.notifyItemChanged(position);
                }
            });
        }
    }
}
