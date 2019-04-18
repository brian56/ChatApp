package vn.huynh.whatsapp.chat_list.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.chat.view.ChatActivity;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.GlideLoader;

/**
 * Created by duong on 3/20/2019.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    private ArrayList<Chat> chatList;
    private Context context;

    public ChatListAdapter(ArrayList<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChatListViewHolder rcv = new ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final ChatListViewHolder holder, final int position) {
        holder.tvTitle.setText(chatList.get(holder.getAdapterPosition()).getChatName());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatObject", chatList.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
        if(TextUtils.isEmpty(chatList.get(holder.getAdapterPosition()).getLastMessage())) {
            holder.tvLastMessage.setVisibility(View.GONE);
        } else {
            holder.tvLastMessage.setVisibility(View.VISIBLE);
            holder.tvLastMessage.setText(chatList.get(holder.getAdapterPosition()).getLastMessage());
        }
        holder.iImageLoader = new GlideLoader();
        holder.iImageLoader.loadImage(holder.avatarView, chatList.get(holder.getAdapterPosition()).getChatAvatar(), chatList.get(holder.getAdapterPosition()).getChatName());
    }

    @Override
    public int getItemCount() {
        if (chatList != null)
            return chatList.size();
        else
            return 0;
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_last_message)
        TextView tvLastMessage;
        @BindView(R.id.ll_layout)
        LinearLayout linearLayout;
        @BindView(R.id.avatar)
        AvatarView avatarView;

        public IImageLoader iImageLoader;

        public ChatListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
