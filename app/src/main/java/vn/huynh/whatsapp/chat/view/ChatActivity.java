package vn.huynh.whatsapp.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.base.BaseActivity;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.chat.presenter.ChatPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.services.NewMessageService;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

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
    TashieLoader loader;
    @BindView(R.id.ll_empty_data)
    LinearLayout llEmptyData;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    private static final String TAG = ChatActivity.class.getSimpleName();
    private MessageAdapter messageAdapter;
    private LinearLayoutManager chatLayoutManager;
    private ArrayList<Message> messageList;
    private static final int PICK_IMAGE_INTENT = 1;
    private Chat chatObject;

    private MediaAdapter mediaAdapter;
    private RecyclerView.LayoutManager mediaLayoutManager;
    private ArrayList<String> mediaUriList;
    private String message = "";
    private int currentPosition = 0;
    private boolean firstStart = true;
    private boolean returnFromGallery = false;
    private static boolean isVisible = false;
    private String chatId;
    private boolean isBound = false;

    private NewMessageService newMessageService;

    private ChatContract.Presenter chatPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chat");

        Bundle bundle = getIntent().getExtras();

        chatObject = bundle.getParcelable(Constant.EXTRA_CHAT_OBJECT);
        chatId = bundle.getString(Constant.EXTRA_CHAT_ID);
        if (chatObject != null) {
            chatId = chatObject.getId();
        }
        Log.d("ChatActivity", chatId);
        ChatUtils.setCurrentChatId(chatId);
        initializeMessageList();
        initializeMediaList();
        setupPresenter(this, chatObject, chatId);
        setEvents();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();

        chatId = bundle.getString(Constant.EXTRA_CHAT_ID);
        if (!chatId.equals(ChatUtils.getCurrentChatId())) {
            chatObject = null;
            ChatUtils.setCurrentChatId(chatId);
            setupPresenter(this, chatObject, chatId);
        }
        Log.d("ChatActivity", chatId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            NewMessageService.LocalBinder binder = (NewMessageService.LocalBinder) service;
//            newMessageService = binder.getService();
//            Log.d("Noti_ChatActivity", "setShowNotification()");
//            newMessageService.setShowNotification(true);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//    };

    @Override
    protected void onStart() {
        super.onStart();
//        Intent intent2 = new Intent(this, NewMessageService.class);
//        startService(intent2);
//        bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
//        isBound = true;

        isVisible = true;
        ChatUtils.setCurrentChatId(chatId);
        if (!firstStart && !returnFromGallery) {
            messageList.clear();
            messageAdapter.notifyDataSetChanged();
            if (chatObject != null) {
                chatPresenter.loadChatMessage(chatId);
            } else {
                chatPresenter.loadChatDetail(chatId);
            }
        }
        if (returnFromGallery) {
            returnFromGallery = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (newMessageService != null) {
//            Log.d("Noti_ChatActivity", "setShowNotification()");
//            newMessageService.setShowNotification(true);
//        }
        isVisible = false;
        firstStart = false;
        if (!returnFromGallery) {
            isVisible = false;
            chatPresenter.removeMessageListener();
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
//        if (isBound) {
//            newMessageService.removeListener();
//            unbindService(serviceConnection);
//            isBound = false;
//        }
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
                messageList.clear();
                messageAdapter.notifyDataSetChanged();
                chatPresenter.loadChatMessage(chatObject.getId());
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
        messageList = new ArrayList<>();
        chatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        chatLayoutManager.setStackFromEnd(false);
        chatLayoutManager.setSmoothScrollbarEnabled(true);
        rvChat.setLayoutManager(chatLayoutManager);
        messageAdapter = new MessageAdapter(messageList, chatObject, ChatActivity.this);
        rvChat.setAdapter(messageAdapter);
    }

    private void initializeMediaList() {
        rvMedia.setNestedScrollingEnabled(false);
        rvMedia.setHasFixedSize(false);
        mediaUriList = new ArrayList<>();
        mediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false);
        rvMedia.setLayoutManager(mediaLayoutManager);
        mediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        rvMedia.setAdapter(mediaAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("message", edtMessage.getText().toString().trim());
        if (mediaUriList != null && mediaUriList.size() > 0)
            outState.putStringArrayList("mediaList", mediaUriList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        edtMessage.setText(savedInstanceState.getString("message"));
        mediaUriList = savedInstanceState.getStringArrayList("mediaList");
        mediaAdapter.notifyDataSetChanged();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void showLoadingIndicator() {
        showHideListLoadingIndicator(llIndicator, loader, true);
        showHideListEmptyIndicator(llIndicator, llEmptyData, false);
        showHideListErrorIndicator(llIndicator, llError, false);
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
            messageList.clear();
            messageAdapter.notifyDataSetChanged();
            chatPresenter.loadChatMessage(chatObject.getId());
        }
    }

    @Override
    public void addSendingMessageToList(Message messageObject) {
        if (messageObject != null) {
            messageList.add(messageObject);
            if (messageList.size() == 0)
                messageAdapter.notifyItemInserted(0);
            else {
                messageAdapter.notifyItemInserted(messageList.size() - 1);
            }
            if(messageList.size() > 1)
                mediaAdapter.notifyItemChanged(messageList.size() -2);
            chatLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
        }
    }

    @Override
    public void showMessageList(Message messageObject) {
        if (messageObject != null) {
            showHideListIndicator(llIndicator, false);
            if (messageObject.getCreator().equals(ChatUtils.getCurrentUserId())) {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (messageList.get(i).getId().equals(messageObject.getId())) {
                        Message.copyMessageObject(messageList.get(i), messageObject);
                        messageAdapter.notifyItemChanged(i);
                        if (i > 0)
                            messageAdapter.notifyItemChanged(i - 1);
                        chatLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                        return;
                    }
                }
                messageList.add(messageObject);
                if (messageList.size() == 0)
                    messageAdapter.notifyItemInserted(0);
                else {
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                }
                if(messageList.size() > 1)
                    mediaAdapter.notifyItemChanged(messageList.size() -2);
                chatLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
            } else {
                messageList.add(messageObject);
                messageAdapter.notifyItemInserted(messageList.size() - 1);

                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (messageList.get(i).getCreator().equals(ChatUtils.getCurrentUserId())) {
                        messageAdapter.notifyItemChanged(i);
                        break;
                    }
                }
                chatLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
            }
        }
    }

    @Override
    public void showErrorMessage(String error) {

    }

    @Override
    public void newMessage() {
        message = edtMessage.getText().toString().trim();
        if (!message.isEmpty() || mediaUriList != null) {
            showHideListIndicator(llIndicator, false);
            ArrayList<String> mediaArrayList = new ArrayList<>();
            mediaArrayList.addAll(mediaUriList);
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
            chatLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
    }
}
