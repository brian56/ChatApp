package vn.huynh.whatsapp.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.chat.ChatContract;
import vn.huynh.whatsapp.chat.presenter.ChatPresenter;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.utils.Utils;

public class ChatActivity extends AppCompatActivity implements ChatContract.View {

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

    private MessageAdapter messageAdapter;
    private LinearLayoutManager chatLayoutManager;
    private ArrayList<Message> messageList = new ArrayList<>();
    private static final int PICK_IMAGE_INTENT = 1;
    private Chat chatObject;
    private String chatId;

    private MediaAdapter mediaAdapter;
    private RecyclerView.LayoutManager mediaLayoutManager;
    private ArrayList<String> mediaUriList = new ArrayList<>();
    private String message = "";

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


        chatObject = (Chat) getIntent().getSerializableExtra("chatObject");
        chatId = getIntent().getStringExtra("chatId");
        if (chatObject != null) {
            chatId = chatObject.getId();
        }
        initializeMessageList();
        initializeMediaList();
        setupPresenter(this, chatObject, chatId);
        setEvents();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatPresenter.detachView();
    }

    private void setupPresenter(ChatContract.View view, Chat chat, String chatId) {
        chatPresenter = new ChatPresenter();
        chatPresenter.attachView(view);

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
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture(s)"), PICK_IMAGE_INTENT);
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
        chatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        chatLayoutManager.setStackFromEnd(true);
        chatLayoutManager.setSmoothScrollbarEnabled(true);
        rvChat.setLayoutManager(chatLayoutManager);
        messageAdapter = new MessageAdapter(messageList, chatObject, ChatActivity.this);
        rvChat.setAdapter(messageAdapter);
//        rvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                if (messageAdapter.getItemCount() > 0 && bottom < oldBottom)
//                    chatLayoutManager.smoothScrollToPosition(rvChat, null, messageAdapter.getItemCount() - 1);
//            }
//        });
    }

    private void initializeMediaList() {
        rvMedia.setNestedScrollingEnabled(false);
        rvMedia.setHasFixedSize(false);
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

    }

    @Override
    public void hideLoadingIndicator() {

    }

    @Override
    public void showChatDetail(Chat object) {
        if (object != null) {
            chatObject = object;
            messageAdapter.setChatObject(chatObject);
            getSupportActionBar().setTitle(chatObject.getChatName());
            messageList.clear();
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
            chatLayoutManager.scrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    public void showMessageList(Message messageObject) {
        if (messageObject != null) {
            if (messageObject.getCreator().equals(Utils.currentUserId())) {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (messageList.get(i).getId().equals(messageObject.getId())) {
                        Message.copyMessageObject(messageList.get(i), messageObject);
                        messageAdapter.notifyItemChanged(i);
                        if (i > 0)
                            messageAdapter.notifyItemChanged(i - 1);
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
                chatLayoutManager.scrollToPosition(messageList.size() - 1);
            } else {
                messageList.add(messageObject);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                if(messageList.size() > 1)
                    mediaAdapter.notifyItemChanged(messageList.size() -2);
                chatLayoutManager.scrollToPosition(messageList.size() - 1);
            }
        }
    }

    @Override
    public void showError(String error) {

    }

    @Override
    public void newMessage() {
        message = edtMessage.getText().toString().trim();
        if (!message.isEmpty() || !mediaUriList.isEmpty()) {
            ArrayList<String> medias = new ArrayList<>();
            medias.addAll(mediaUriList);
            chatPresenter.sendMessage(chatObject, message, medias);
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
            chatLayoutManager.scrollToPosition(messageList.size() - 1);
    }

//    private void sendMessage() {
//        String messageId = chatMessageDb.push().getKey();
//        final DatabaseReference newMessageDB = chatMessageDb.child(messageId);
//
//        final Map newMessageMap = new HashMap<>();
//        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
//        if (!message.isEmpty())
//            newMessageMap.put("text", message);
//
//        if (!mediaUriList.isEmpty()) {
//            for (final String mediaUri : mediaUriList) {
//                String mediaId = newMessageDB.child("media").push().getKey();
//                mediaIdList.add(mediaId);
//                final StorageReference filePath = FirebaseStorage.getInstance().getReference()
//                        .child("chat").child(chatObject.getChatId()).child(messageId).child(mediaId);
//                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                newMessageMap.put("/media/" + mediaIdList.get(totalUploadedMedia) + "/", uri.toString());
//                                totalUploadedMedia++;
//                                if (totalUploadedMedia == mediaUriList.size()) {
//                                    updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
//                                }
//
//                            }
//                        });
//                    }
//                });
//            }
//        } else {
//            if (!edtMessage.getText().toString().trim().isEmpty())
//                updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
//        }
//    }
//
//    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDB, Map newMessageMap) {
//        newMessageDB.updateChildren(newMessageMap);
//        totalUploadedMedia = 0;
//        edtMessage.setText(null);
//        mediaUriList.clear();
//        mediaIdList.clear();
//        mediaAdapter.notifyDataSetChanged();
//        if(messageList.size() > 0)
//            chatLayoutManager.scrollToPosition(messageList.size() - 1);
//
//        String message = "";
//        if (newMessageMap.get("text") != null) {
//            message = newMessageMap.get("text").toString();
//        } else {
//            message = "Sent media";
//        }
//        for (UserObject userObject : chatObject.getUserObjectArrayList()) {
//            if (!userObject.getUid().equals(FirebaseAuth.getInstance().getUid())) {
//                new SendNotification(message, "New message", userObject.getNotificationKey());
//            }
//        }
//    }

}
