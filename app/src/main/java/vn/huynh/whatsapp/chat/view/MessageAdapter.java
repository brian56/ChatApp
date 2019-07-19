package vn.huynh.whatsapp.chat.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;

import java.util.ArrayList;
import java.util.List;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.model.Chat;
import vn.huynh.whatsapp.model.Message;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.GlideLoader;

/**
 * Created by duong on 3/20/2019.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    private ArrayList<Message> mMessageList;
    private Chat mChatObject;
    private static final int MY_MESSAGE = 0;
    private static final int THEIR_MESSAGE = 1;
    private static final int LOADING_MESSAGE = -1;
    private static final int LAST_MESSAGE = -2;
    private Context mContext;
    private int mMaxColumnMediaList = 3;

    public MessageAdapter(ArrayList<Message> messageList, Chat chatObject, Context context) {
        this.mMessageList = messageList;
        this.mChatObject = chatObject;
        this.mContext = context;
    }

    public void setChatObject(Chat chatObject) {
        this.mChatObject = chatObject;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LAST_MESSAGE:
                View layoutView4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_last_message, null, false);
                RecyclerView.LayoutParams lp4 = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView4.setLayoutParams(lp4);
                return new LastMessageViewHolder(layoutView4);

            case LOADING_MESSAGE:
                View layoutView3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_more_message, null, false);
                RecyclerView.LayoutParams lp3 = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView3.setLayoutParams(lp3);
                return new LoadingMessageViewHolder(layoutView3);

            case THEIR_MESSAGE:
                View layoutView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_their_message, null, false);
                RecyclerView.LayoutParams lp2 = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView2.setLayoutParams(lp2);
                return new TheirMessageViewHolder(layoutView2);
            default:
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, null, false);
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView.setLayoutParams(lp);
                return new MyMessageViewHolder(layoutView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position) == null)
            return LOADING_MESSAGE;

        if (mMessageList.get(position).getType() == Message.TYPE_LAST_MESSAGE)
            return LAST_MESSAGE;

        if (mMessageList.get(position).getCreator().equals(ChatUtils.getUser().getId()))
            return MY_MESSAGE;
        else
            return THEIR_MESSAGE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case LAST_MESSAGE:
                break;

            case LOADING_MESSAGE:
                break;

            case MY_MESSAGE:
                if (holder.getAdapterPosition() > 0) {
                    //check previous item is not loading progress bar
                    if (mMessageList.get(holder.getAdapterPosition() - 1) != null
                            || (mMessageList.get(holder.getAdapterPosition() - 1) != null
                            && mMessageList.get(holder.getAdapterPosition() - 1).getType() != Message.TYPE_LAST_MESSAGE)) {
                        if (mMessageList.get(holder.getAdapterPosition()).getCreator().
                                equals(mMessageList.get(holder.getAdapterPosition() - 1).getCreator())) {
                            ((MyMessageViewHolder) holder).viewPaddingTop.setVisibility(View.GONE);
                        } else {
                            ((MyMessageViewHolder) holder).viewPaddingTop.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ((MyMessageViewHolder) holder).viewPaddingTop.setVisibility(View.GONE);
                    }
                }
                if (mMessageList.get(holder.getAdapterPosition()).getStatus() == Message.STATUS_SENDING) {
                    ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.VISIBLE);
                    ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.GONE);
                } else if (mMessageList.get(holder.getAdapterPosition()).getStatus() == Message.STATUS_DELIVERED) {
                    if (holder.getAdapterPosition() == getItemCount() - 1) {
                        //last message
                        ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.GONE);
                        ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.VISIBLE);
                    } else {
                        ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.GONE);
                        ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.GONE);
                    }
                }

                if (TextUtils.isEmpty(mMessageList.get(holder.getAdapterPosition()).getText())) {
                    ((MyMessageViewHolder) holder).tvMessage.setVisibility(View.GONE);
                } else {
                    ((MyMessageViewHolder) holder).tvMessage.setVisibility(View.VISIBLE);
                    ((MyMessageViewHolder) holder).tvMessage.setText(mMessageList.get(holder.getAdapterPosition()).getText());
                }
                if (mMessageList.get(holder.getAdapterPosition()).getMedia() == null) {
                    ((MyMessageViewHolder) holder).rvMedia.setVisibility(View.GONE);
                } else {
                    ((MyMessageViewHolder) holder).rvMedia.setVisibility(View.VISIBLE);
                    List<String> mediaUrl = new ArrayList<>(mMessageList.get(holder.getAdapterPosition()).getMedia().values());

                    MediaAdapter mediaAdapter = new MediaAdapter(mContext, mediaUrl, false);
                    ((MyMessageViewHolder) holder).rvMedia.setHasFixedSize(true);
                    ((MyMessageViewHolder) holder).rvMedia.setLayoutManager(new GridLayoutManager(mContext, numberColumnMediaList(mediaAdapter.getItemCount())));
                    ((MyMessageViewHolder) holder).rvMedia.setAdapter(mediaAdapter);
                }
                break;
            case THEIR_MESSAGE:
                ((TheirMessageViewHolder) holder).setiImageLoader(new GlideLoader());
                ((TheirMessageViewHolder) holder).setUser(getUserObject(mChatObject, mMessageList.get(holder.getAdapterPosition()).getCreator()));
                ((TheirMessageViewHolder) holder).getiImageLoader().loadImage(((TheirMessageViewHolder) holder).avatarView,
                        ((TheirMessageViewHolder) holder).getUser().getAvatar(),
                        ((TheirMessageViewHolder) holder).getUser().getName());

                if (holder.getAdapterPosition() > 0) {
                    //check previous item is not loading progress bar
                    if (mMessageList.get(holder.getAdapterPosition() - 1) != null
                            || (mMessageList.get(holder.getAdapterPosition() - 1) != null
                            && mMessageList.get(holder.getAdapterPosition() - 1).getType() == Message.TYPE_LAST_MESSAGE)) {
                        if (mMessageList.get(holder.getAdapterPosition()).getCreator().
                                equals(mMessageList.get(holder.getAdapterPosition() - 1).getCreator())) {
                            ((TheirMessageViewHolder) holder).tvSender.setVisibility(View.GONE);
                            ((TheirMessageViewHolder) holder).viewPaddingTop.setVisibility(View.GONE);
                            ((TheirMessageViewHolder) holder).avatarView.setVisibility(View.INVISIBLE);
                        } else {
                            ((TheirMessageViewHolder) holder).tvSender.setVisibility(View.VISIBLE);
                            ((TheirMessageViewHolder) holder).viewPaddingTop.setVisibility(View.VISIBLE);
                            ((TheirMessageViewHolder) holder).avatarView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ((TheirMessageViewHolder) holder).tvSender.setVisibility(View.VISIBLE);
                        ((TheirMessageViewHolder) holder).viewPaddingTop.setVisibility(View.VISIBLE);
                        ((TheirMessageViewHolder) holder).avatarView.setVisibility(View.VISIBLE);
                    }
                } else {
                    //first message
                    ((TheirMessageViewHolder) holder).tvSender.setVisibility(View.VISIBLE);
                    ((TheirMessageViewHolder) holder).viewPaddingTop.setVisibility(View.VISIBLE);
                    ((TheirMessageViewHolder) holder).avatarView.setVisibility(View.VISIBLE);
                }
                if (TextUtils.isEmpty(mMessageList.get(holder.getAdapterPosition()).getText())) {
                    ((TheirMessageViewHolder) holder).tvMessage.setVisibility(View.GONE);
                } else {
                    ((TheirMessageViewHolder) holder).tvMessage.setVisibility(View.VISIBLE);
                    ((TheirMessageViewHolder) holder).tvMessage.setText(mMessageList.get(holder.getAdapterPosition()).getText());
                }

                for (User userObject : mChatObject.getUsers()) {
                    if (userObject.getId().equals(mMessageList.get(holder.getAdapterPosition()).getCreator())) {
                        ((TheirMessageViewHolder) holder).tvSender.setText(userObject.getName());
                        break;
                    }
                }
                if (mMessageList.get(holder.getAdapterPosition()).getMedia() == null) {
                    ((TheirMessageViewHolder) holder).rvMedia.setVisibility(View.GONE);
                } else {
                    ((TheirMessageViewHolder) holder).rvMedia.setVisibility(View.VISIBLE);

                    List<String> mediaUrl = new ArrayList<>(mMessageList.get(holder.getAdapterPosition()).getMedia().values());

                    MediaAdapter mediaAdapter = new MediaAdapter(mContext, mediaUrl, false);
                    ((TheirMessageViewHolder) holder).rvMedia.setHasFixedSize(true);
                    ((TheirMessageViewHolder) holder).rvMedia.setLayoutManager(new GridLayoutManager(mContext, numberColumnMediaList(mediaAdapter.getItemCount())));
                    ((TheirMessageViewHolder) holder).rvMedia.setAdapter(mediaAdapter);
//                    ((TheirMessageViewHolder)holder).btnViewMedia.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            new ImageViewer.Builder(v.getContext(), mMessageList.get(holder.getAdapterPosition()).getMediaUrlList())
//                                    .setStartPosition(0)
//                                    .show();
//                        }
//                    });
                }
                break;
        }
    }

    private User getUserObject(Chat chat, String userId) {
        for (User user : chat.getUsers()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (mMessageList != null)
            return mMessageList.size();
        else
            return 0;
    }

    private int numberColumnMediaList(int itemCount) {
        if (itemCount <= mMaxColumnMediaList)
            return itemCount;
        else
            return mMaxColumnMediaList;
    }

    public class LoadingMessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.pb_loading)
        ProgressBar progressBar;

        public LoadingMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class LastMessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_last_message)
        TextView tvLastMessage;

        public LastMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class MyMessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_message)
        TextView tvMessage;
        @BindView(R.id.ll_layout)
        LinearLayout linearLayout;
        @BindView(R.id.rv_media)
        RecyclerView rvMedia;
        @BindView(R.id.iv_delivered)
        ImageView ivStatus;
        @BindView(R.id.view_padding_top)
        View viewPaddingTop;
        @BindView(R.id.loader_sending)
        TashieLoader loaderSending;

        public MyMessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public class TheirMessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_message)
        TextView tvMessage;
        @BindView(R.id.tv_sender)
        TextView tvSender;
        @BindView(R.id.ll_layout)
        LinearLayout linearLayout;
        @BindView(R.id.rv_media)
        RecyclerView rvMedia;
        @BindView(R.id.avatar)
        AvatarView avatarView;
        @BindView(R.id.view_padding_top)
        View viewPaddingTop;
        private IImageLoader iImageLoader;
        private User user;

        public TheirMessageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public IImageLoader getiImageLoader() {
            return iImageLoader;
        }

        public void setiImageLoader(IImageLoader iImageLoader) {
            this.iImageLoader = iImageLoader;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
