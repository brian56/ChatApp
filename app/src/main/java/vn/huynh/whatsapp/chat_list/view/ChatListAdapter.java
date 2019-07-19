package vn.huynh.whatsapp.chat_list.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.DateUtils;
import vn.huynh.whatsapp.utils.GlideLoader;
import vn.huynh.whatsapp.utils.LogManagerUtils;
import vn.huynh.whatsapp.utils.MyApp;
import vn.huynh.whatsapp.utils.VNCharacterUtils;

/**
 * Created by duong on 3/20/2019.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> implements Filterable {
    private static final String TAG = ChatListAdapter.class.getSimpleName();
    private ArrayList<Chat> mChatList;
    private ArrayList<Chat> mChatListFilter;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private ChatAdapterListener mChatAdapterListener;

    public ChatListAdapter(ArrayList<Chat> chatList, Context context, OnItemClickListener onItemClickListener,
                           ChatAdapterListener listener) {
        this.mChatList = chatList;
        this.mChatListFilter = chatList;
        this.mContext = context;
        this.mOnItemClickListener = onItemClickListener;
        this.mChatAdapterListener = listener;
    }

    public void setChatList(ArrayList<Chat> chatList) {
        this.mChatList = chatList;
        this.mChatListFilter = chatList;
        notifyDataSetChanged();
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
        LogManagerUtils.d(TAG, "chat name: " + mChatListFilter.get(holder.getAdapterPosition()).getChatName());
        final Chat itemChat = mChatListFilter.get(holder.getAdapterPosition());
        holder.tvTitle.setText(itemChat.getChatName());
        if (itemChat.getNumberUnread() != null
                && itemChat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
            Long num = itemChat.getNumberUnread().get(ChatUtils.getUser().getId());
            holder.tvUnread.setVisibility(View.VISIBLE);
            if (num > 99)
                holder.tvUnread.setText(MyApp.resources.getString(R.string.label_number_unread_message_max));
            else
                holder.tvUnread.setText(String.valueOf(num));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(holder.getAdapterPosition(), itemChat);
            }
        });
        if (itemChat.getLastMessageSent() == null) {
            holder.tvLastMessage.setVisibility(View.GONE);
        } else {
            holder.tvLastMessage.setVisibility(View.VISIBLE);
            if (itemChat.getLastMessageSent().getType() == Message.TYPE_TEXT) {
                //sent text
                holder.tvLastMessage.setText(itemChat.getLastMessageSent().getText());
                holder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                //sent media
                holder.tvLastMessage.setText(MyApp.resources.getString(R.string.message_sent_media));
                holder.tvLastMessage.setCompoundDrawablePadding(8 * (int) MyApp.resources.getDisplayMetrics().density);
                holder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_photo_size_select_actual_grey_24dp, 0);
            }
        }
        if (!itemChat.isGroup()) {
            holder.ivGroup.setVisibility(View.GONE);
            holder.avatarView.setVisibility(View.VISIBLE);
            holder.mIImageLoader = new GlideLoader();
            holder.mIImageLoader.loadImage(holder.avatarView, itemChat.getSingleChatAvatar(), itemChat.getChatName());
        } else {
            holder.ivGroup.setVisibility(View.VISIBLE);
            holder.avatarView.setVisibility(View.GONE);
        }
        String time = DateUtils.formatTimeWithMarker(itemChat.getLastMessageDateInLong());
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        if (mChatListFilter != null)
            return mChatListFilter.size();
        else
            return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                charString = VNCharacterUtils.removeAccent(charString.toLowerCase());
                ArrayList<Chat> filteredList = null;
                if (TextUtils.isEmpty(charString.trim())) {
                    filteredList = mChatList;
                } else {
                    filteredList = new ArrayList<>();
                    for (Chat row : mChatList) {
                        if (VNCharacterUtils.removeAccent(row.getChatName().toLowerCase()).contains(charString.trim())) {
                            filteredList.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mChatListFilter = (ArrayList<Chat>) filterResults.values;
                notifyDataSetChanged();
                if (mChatAdapterListener != null) {
                    mChatAdapterListener.onFilter(mChatListFilter.size() > 0 ? false : true);
                }
            }
        };
    }

    public interface ChatAdapterListener {
        void onFilter(boolean isEmptyResult);
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
        @BindView(R.id.iv_group)
        ImageView ivGroup;
        @BindView(R.id.tv_unread)
        TextView tvUnread;
        @BindView(R.id.tv_time)
        TextView tvTime;

        public IImageLoader mIImageLoader;

        public ChatListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position, Chat chat);
    }
}
