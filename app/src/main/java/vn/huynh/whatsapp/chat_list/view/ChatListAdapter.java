package vn.huynh.whatsapp.chat_list.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopeer.itemtouchhelperextension.Extension;
import com.loopeer.itemtouchhelperextension.ItemTouchHelperExtension;

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
import vn.huynh.whatsapp.utils.MyApp;
import vn.huynh.whatsapp.utils.VNCharacterUtils;

/**
 * Created by duong on 3/20/2019.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListBaseViewHolder> implements Filterable {
    private static final String TAG = ChatListAdapter.class.getSimpleName();
    public static final int ITEM_TYPE_RECYCLER_WIDTH = 1000;
    public static final int ITEM_TYPE_ACTION_WIDTH = 1001;
    public static final int ITEM_TYPE_ACTION_WIDTH_NO_SPRING = 1002;
    public static final int ITEM_TYPE_NO_SWIPE = 1003;
    private ItemTouchHelperExtension mItemTouchHelperExtension;

    private ArrayList<Chat> mChatList;
    private ArrayList<Chat> mChatListFilter;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private OnActionItemClickListener mOnActionItemClickListener;
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

    public void setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
        this.mItemTouchHelperExtension = itemTouchHelperExtension;
    }

    public void setOnActionItemClickListener(OnActionItemClickListener actionItemClickListener) {
        this.mOnActionItemClickListener = actionItemClickListener;
    }

    @Override
    public ChatListBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

//        if (viewType == ITEM_TYPE_ACTION_WIDTH) return new ItemSwipeWithActionWidthViewHolder(layoutView);
//        if (viewType == ITEM_TYPE_NO_SWIPE) return new ItemNoSwipeViewHolder(layoutView);
//        return new ItemSwipeWithActionWidthNoSpringViewHolder(layoutView);
        return new ItemSwipeWithActionWidthNoSpringViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final ChatListBaseViewHolder holder, final int position) {
        holder.bind(holder.getAdapterPosition(), mChatListFilter.get(holder.getAdapterPosition()));

        if (holder instanceof ItemSwipeWithActionWidthNoSpringViewHolder) {
            ((ItemSwipeWithActionWidthNoSpringViewHolder) holder).showMuteAction(holder.isShowMute);

            ((ItemSwipeWithActionWidthNoSpringViewHolder) holder).tvMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnActionItemClickListener != null) {
                        mOnActionItemClickListener.onMute(holder.getAdapterPosition(), mChatListFilter.get(holder.getAdapterPosition()));
                    }
                }
            });
            ((ItemSwipeWithActionWidthNoSpringViewHolder) holder).tvUnMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnActionItemClickListener != null) {
                        mOnActionItemClickListener.onUnmute(holder.getAdapterPosition(), mChatListFilter.get(holder.getAdapterPosition()));
                    }
                }
            });
            ((ItemSwipeWithActionWidthNoSpringViewHolder) holder).tvMarkAsRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnActionItemClickListener != null) {
                        mOnActionItemClickListener.onMarkAsRead(holder.getAdapterPosition(), mChatListFilter.get(holder.getAdapterPosition()));
                    }
                }
            });
            ((ItemSwipeWithActionWidthNoSpringViewHolder) holder).tvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnActionItemClickListener != null) {
                        mOnActionItemClickListener.onBack();
                    }
                }
            });
        }
    }

    public void move(int from, int to) {
        Chat prev = mChatListFilter.remove(from);
        mChatListFilter.add(to > from ? to - 1 : to, prev);
        notifyItemMoved(from, to);
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


    public class ChatListBaseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_last_message)
        TextView tvLastMessage;
        @BindView(R.id.avatar)
        AvatarView avatarView;
        @BindView(R.id.iv_group)
        ImageView ivGroup;
        @BindView(R.id.tv_unread)
        TextView tvUnread;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.iv_notification)
        ImageView ivNotification;
        @BindView(R.id.ll_action_container)
        LinearLayout actionContainer;
        @BindView(R.id.ll_layout_main_container)
        LinearLayout mainContainer;

        public IImageLoader mIImageLoader;
        boolean isShowMute = true;

        public ChatListBaseViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(final int position, final Chat itemChat) {
            tvTitle.setText(itemChat.getChatName());

            mainContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemTouchHelperExtension.closeOpened();
                    mOnItemClickListener.onClick(position, itemChat);
                }
            });

            if (itemChat.getNotificationUserIds().get(ChatUtils.getUser().getId())) {
                tvUnread.setVisibility(View.VISIBLE);
                ivNotification.setVisibility(View.GONE);
                if (itemChat.getNumberUnread() != null
                        && itemChat.getNumberUnread().get(ChatUtils.getUser().getId()) > 0) {
                    Long num = itemChat.getNumberUnread().get(ChatUtils.getUser().getId());
                    tvUnread.setVisibility(View.VISIBLE);
                    if (num > 99)
                        tvUnread.setText(MyApp.resources.getString(R.string.label_number_unread_message_max));
                    else
                        tvUnread.setText(String.valueOf(num));
                } else {
                    tvUnread.setVisibility(View.GONE);
                }
            } else {
                tvUnread.setVisibility(View.GONE);
                ivNotification.setVisibility(View.VISIBLE);
            }

            if (itemChat.getLastMessageSent() == null) {
                tvLastMessage.setVisibility(View.GONE);
            } else {
                tvLastMessage.setVisibility(View.VISIBLE);
                if (itemChat.getLastMessageSent().getType() == Message.TYPE_TEXT) {
                    //sent text
                    tvLastMessage.setText(itemChat.getLastMessageSent().getText());
                    tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    //sent media
                    tvLastMessage.setText(MyApp.resources.getString(R.string.message_sent_media));
                    tvLastMessage.setCompoundDrawablePadding(8 * (int) MyApp.resources.getDisplayMetrics().density);
                    tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_photo_size_select_actual_grey_24dp, 0);
                }
            }
            if (!itemChat.isGroup()) {
                ivGroup.setVisibility(View.GONE);
                avatarView.setVisibility(View.VISIBLE);
                mIImageLoader = new GlideLoader();
                mIImageLoader.loadImage(avatarView, itemChat.getSingleChatAvatar(), itemChat.getChatName());
            } else {
                ivGroup.setVisibility(View.VISIBLE);
                avatarView.setVisibility(View.GONE);
            }
            String time = DateUtils.formatTimeWithMarker(itemChat.getLastMessageDateInLong());
            tvTime.setText(time);

            isShowMute = itemChat.getNotificationUserIds().get(ChatUtils.getUser().getId());
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mItemTouchHelperExtension.startDrag(ChatListBaseViewHolder.this);
                    }
                    return true;
                }
            });
        }


    }

    public class ItemSwipeWithActionWidthViewHolder extends ChatListBaseViewHolder implements Extension {
        @BindView(R.id.tv_action_mark_as_read)
        TextView tvMarkAsRead;
        @BindView(R.id.tv_action_mute)
        TextView tvMute;
        @BindView(R.id.tv_action_unmute)
        TextView tvUnMute;
        @BindView(R.id.tv_action_back)
        TextView tvBack;

        public ItemSwipeWithActionWidthViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void showMuteAction(boolean isShowMute) {
            if (isShowMute) {
                tvMute.setVisibility(View.VISIBLE);
                tvUnMute.setVisibility(View.GONE);
            } else {
                tvMute.setVisibility(View.GONE);
                tvUnMute.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public float getActionWidth() {
            return actionContainer.getWidth();
        }
    }

    public class ItemSwipeWithActionWidthNoSpringViewHolder extends ItemSwipeWithActionWidthViewHolder implements Extension {

        public ItemSwipeWithActionWidthNoSpringViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public float getActionWidth() {
            return actionContainer.getWidth();
        }
    }

    public class ItemNoSwipeViewHolder extends ChatListBaseViewHolder {

        public ItemNoSwipeViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position, Chat chat);
    }

    public interface OnActionItemClickListener {
        void onBack();

        void onMarkAsRead(int position, Chat chatItem);

        void onMute(int position, Chat chatItem);

        void onUnmute(int position, Chat chatItem);
    }
}
