package vn.huynh.whatsapp.chat_list.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.utils.GlideLoader;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 3/20/2019.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    private ArrayList<Chat> chatList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ChatListAdapter(ArrayList<Chat> chatList, Context context, OnItemClickListener onItemClickListener) {
        this.chatList = chatList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public void setChatList(ArrayList<Chat> chatList) {
        this.chatList = chatList;
    }

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        return new ChatListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ChatListViewHolder holder, final int position) {
        Log.d("Chat group", chatList.get(holder.getAdapterPosition()).getChatName());
        holder.tvTitle.setText(chatList.get(holder.getAdapterPosition()).getChatName());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClick(chatList.get(holder.getAdapterPosition()));
            }
        });
        if (chatList.get(holder.getAdapterPosition()).getLastMessageSent() == null) {
            holder.tvLastMessage.setVisibility(View.GONE);
        } else {
            holder.tvLastMessage.setVisibility(View.VISIBLE);
            if (chatList.get(holder.getAdapterPosition()).getLastMessageSent().getType() == Message.TYPE_TEXT) {
                //sent text
                holder.tvLastMessage.setText(chatList.get(holder.getAdapterPosition()).getLastMessageSent().getText());
                holder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                //sent media
                holder.tvLastMessage.setText(MyApp.resources.getString(R.string.message_sent_media));
                holder.tvLastMessage.setCompoundDrawablePadding(8 * (int) MyApp.resources.getDisplayMetrics().density);
                holder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_photo_size_select_actual_black_24dp, 0);
            }
        }
        if (!chatList.get(holder.getAdapterPosition()).isGroup()) {
            holder.iImageLoader = new GlideLoader();
            holder.iImageLoader.loadImage(holder.avatarView, chatList.get(holder.getAdapterPosition()).getSingleChatAvatar(), chatList.get(holder.getAdapterPosition()).getChatName());
        } else {
            holder.avatarView.setImageResource(R.drawable.ic_google_groups);
        }
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

    public interface OnItemClickListener {
        void onClick(Chat chat);
    }
}
