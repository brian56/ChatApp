package vn.huynh.whatsapp.chat.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.google.firebase.auth.FirebaseAuth;

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
import vn.huynh.whatsapp.utils.GlideLoader;

/**
 * Created by duong on 3/20/2019.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Message> messageList;
    private Chat chatObject;
    private static final int MY_MESSAGE = 0;
    private static final int THEIR_MESSAGE = 1;
    private Context context;
    private int maxColumn = 3;

    public MessageAdapter(ArrayList<Message> messageList, Chat chatObject, Context context) {
        this.messageList = messageList;
        this.chatObject = chatObject;
        this.context = context;
    }

    public void setChatObject(Chat chatObject) {
        this.chatObject = chatObject;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case THEIR_MESSAGE:
                View layoutView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_their_message, null, false);
                RecyclerView.LayoutParams lp2 = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView2.setLayoutParams(lp2);
                TheirMessageViewHolder rcv2 = new TheirMessageViewHolder(layoutView2);
                return rcv2;
            default:
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, null, false);
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutView.setLayoutParams(lp);
                MyMessageViewHolder rcv = new MyMessageViewHolder(layoutView);
                return rcv;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getCreator().equals(FirebaseAuth.getInstance().getUid()))
            return MY_MESSAGE;
        else
            return THEIR_MESSAGE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case MY_MESSAGE:
                if (messageList.get(holder.getAdapterPosition()).getStatus() == Message.STATUS_SENDING) {
                    ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.VISIBLE);
                    ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.GONE);
                } else if (messageList.get(holder.getAdapterPosition()).getStatus() == Message.STATUS_DELIVERED) {
                    if (holder.getAdapterPosition() == getItemCount() - 1) {
                        //last message
                        ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.GONE);
                        ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.VISIBLE);
                    } else {
                        ((MyMessageViewHolder) holder).loaderSending.setVisibility(View.GONE);
                        ((MyMessageViewHolder) holder).ivStatus.setVisibility(View.GONE);
                    }
                }

                if (TextUtils.isEmpty(messageList.get(holder.getAdapterPosition()).getText())) {
                    ((MyMessageViewHolder) holder).tvMessage.setVisibility(View.GONE);
                } else {
                    ((MyMessageViewHolder) holder).tvMessage.setVisibility(View.VISIBLE);
                    ((MyMessageViewHolder) holder).tvMessage.setText(messageList.get(holder.getAdapterPosition()).getText());
                }
                if (messageList.get(holder.getAdapterPosition()).getMedia() == null) {
                    ((MyMessageViewHolder) holder).rvMedia.setVisibility(View.GONE);
                } else {
                    ((MyMessageViewHolder) holder).rvMedia.setVisibility(View.VISIBLE);
                    List<String> mediaUrl = new ArrayList<>(messageList.get(holder.getAdapterPosition()).getMedia().values());

                    MediaAdapter mediaAdapter = new MediaAdapter(context, mediaUrl);
                    ((MyMessageViewHolder) holder).rvMedia.setHasFixedSize(true);
                    ((MyMessageViewHolder) holder).rvMedia.setLayoutManager(new GridLayoutManager(context, numberColumn(mediaAdapter.getItemCount())));
                    ((MyMessageViewHolder) holder).rvMedia.setAdapter(mediaAdapter);
                }
                break;
            case THEIR_MESSAGE:
                if (TextUtils.isEmpty(messageList.get(holder.getAdapterPosition()).getText())) {
                    ((TheirMessageViewHolder) holder).tvMessage.setVisibility(View.GONE);
                } else {
                    ((TheirMessageViewHolder) holder).tvMessage.setVisibility(View.VISIBLE);
                    ((TheirMessageViewHolder) holder).tvMessage.setText(messageList.get(holder.getAdapterPosition()).getText());
                }

                for (User userObject : chatObject.getUsers()) {
                    if (userObject.getId().equals(messageList.get(holder.getAdapterPosition()).getCreator())) {
                        ((TheirMessageViewHolder) holder).tvSender.setText(userObject.getName());
                        break;
                    }
                }
                if (messageList.get(holder.getAdapterPosition()).getMedia() == null) {
                    ((TheirMessageViewHolder) holder).rvMedia.setVisibility(View.GONE);
                } else {
                    ((TheirMessageViewHolder) holder).rvMedia.setVisibility(View.VISIBLE);

                    List<String> mediaUrl = new ArrayList<>(messageList.get(holder.getAdapterPosition()).getMedia().values());

                    MediaAdapter mediaAdapter = new MediaAdapter(context, mediaUrl);
                    ((TheirMessageViewHolder) holder).rvMedia.setHasFixedSize(true);
                    ((TheirMessageViewHolder) holder).rvMedia.setLayoutManager(new GridLayoutManager(context, numberColumn(mediaAdapter.getItemCount())));
                    ((TheirMessageViewHolder) holder).rvMedia.setAdapter(mediaAdapter);
//                    ((TheirMessageViewHolder)holder).btnViewMedia.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            new ImageViewer.Builder(v.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
//                                    .setStartPosition(0)
//                                    .show();
//                        }
//                    });
                }
                ((TheirMessageViewHolder) holder).setiImageLoader(new GlideLoader());
                ((TheirMessageViewHolder) holder).setUser(getUserObject(chatObject, messageList.get(holder.getAdapterPosition()).getCreator()));
                ((TheirMessageViewHolder) holder).getiImageLoader().loadImage(((TheirMessageViewHolder) holder).avatarView,
                        ((TheirMessageViewHolder) holder).getUser().getAvatar(),
                        ((TheirMessageViewHolder) holder).getUser().getName());
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
        if (messageList != null)
            return messageList.size();
        else
            return 0;
    }

    private int numberColumn(int itemCount) {
        if(itemCount <= maxColumn)
            return itemCount;
        else
            return maxColumn;
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
