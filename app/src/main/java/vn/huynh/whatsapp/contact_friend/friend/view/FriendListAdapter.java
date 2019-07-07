package vn.huynh.whatsapp.contact_friend.friend.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.custom_views.sticky_header.stickyData.HeaderData;
import vn.huynh.whatsapp.custom_views.sticky_header.stickyView.StickHeaderRecyclerViewAdapter;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.utils.GlideLoader;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 3/20/2019.
 */

public class FriendListAdapter extends StickHeaderRecyclerViewAdapter<Friend, FriendListAdapter.HeaderDataImpl> {
    private static final String TAG = FriendListAdapter.class.getSimpleName();
    private Context context;
    private ItemFriendMenuClickListener itemFriendMenuClickListener;

    public FriendListAdapter(Context context,
                             ItemFriendMenuClickListener itemFriendMenuClickListener) {
        this.context = context;
        this.itemFriendMenuClickListener = itemFriendMenuClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            //item
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, null, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutView.setLayoutParams(lp);
            return new FriendListViewHolder(layoutView);
        } else {
            //header
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_header, null, false);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(0, 0);
            layoutView.setLayoutParams(lp);
            return new HeaderViewHolder(layoutView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FriendListViewHolder) {
            ((FriendListViewHolder) holder).bindData(position);
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bindData(position);
        }
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        LinearLayout linearLayout = header.findViewById(R.id.ll_layout_friend_header);
        TextView tvHeader = header.findViewById(R.id.tv_header);
        if (getDataInPosition(headerPosition) instanceof HeaderData) {
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setVisibility(View.VISIBLE);
            switch (((HeaderData) getDataInPosition(headerPosition)).getHeaderType()) {
                case Friend.STATUS_WAS_REQUESTED:
                    tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.black));
                    tvHeader.setText(MyApp.resources.getString(R.string.header_friend_request));
                    break;
                case Friend.STATUS_ACCEPT:
                    tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.blue));
                    tvHeader.setText(MyApp.resources.getString(R.string.header_friend));
                    break;
                case Friend.STATUS_REQUEST:
                    tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.green));
                    tvHeader.setText(MyApp.resources.getString(R.string.header_friend_my_request));
                    break;
                case Friend.STATUS_BLOCK:
                    tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.purple));
                    tvHeader.setText(MyApp.resources.getString(R.string.header_friend_block));
                    break;
            }
        }
    }

    public void setHeaderAndData(@NonNull Friend data, @Nullable HeaderData header) {
        //check if data exist > update createDate only
        switch (header.getHeaderType()) {
            case Friend.STATUS_WAS_REQUESTED:
                setHeaderAndData(data, header, 0);
                break;
            case Friend.STATUS_ACCEPT:
            case Friend.STATUS_WAS_ACCEPTED:
                if (getHeaderPositionByHeaderType(Friend.STATUS_REQUEST) >= 0) {
                    setHeaderAndData(data, header, getHeaderPositionByHeaderType(Friend.STATUS_REQUEST));
                } else {
                    setHeaderAndData(data, header, getItemCount());
                }
                break;
            case Friend.STATUS_REQUEST:
                if (getHeaderPositionByHeaderType(Friend.STATUS_BLOCK) >= 0) {
                    setHeaderAndData(data, header, getHeaderPositionByHeaderType(Friend.STATUS_REQUEST));
                } else {
                    setHeaderAndData(data, header, getItemCount());
                }
                break;
            case Friend.STATUS_BLOCK:
                setHeaderAndData(data, header, getItemCount());
                break;
        }
    }

    public void refreshHeaderVisible() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getListData().get(i) instanceof HeaderData) {
                if (i + 1 < getItemCount()) {
                    if (!(getListData().get(i + 1) instanceof Friend)) {
                        getListData().remove(i);
                        notifyItemRemoved(i);
                    }
                } else {
                    getListData().remove(i);
                    notifyItemRemoved(i);
                }
            } else {
                continue;
            }
        }
    }

    public void removeData(Friend friend) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getListData().get(i) instanceof Friend) {
                if (((Friend) getListData().get(i)).getUserId().equals(friend.getUserId())) {
                    getListData().remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
        refreshHeaderVisible();
    }

    public int countItemByStatus(int status) {
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            if (getListData().get(i) instanceof Friend) {
                if (((Friend) getListData().get(i)).getStatus() == status) {
                    count++;
                }
            }
        }
        return count;
    }

    public class FriendListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_phone)
        TextView tvPhone;
        @BindView(R.id.ll_layout)
        LinearLayout linearLayout;
        @BindView(R.id.iv_more_action)
        ImageView ivMoreAction;
        @BindView(R.id.avatar)
        AvatarView avatarView;

        public IImageLoader iImageLoader;

        public FriendListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final int position) {
            tvName.setText(getDataInPosition(position).getName());
            tvPhone.setText(getDataInPosition(position).getPhoneNumber());
            ivMoreAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, ivMoreAction);
                    if (getDataInPosition(position).getStatus() == Friend.STATUS_WAS_REQUESTED) {
                        popup.inflate(R.menu.menu_friend_was_requested);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_accept:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onAccept(getDataInPosition(position));
                                        return true;
                                    case R.id.menu_reject:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onReject(getDataInPosition(position));
                                        return true;
                                    case R.id.menu_block:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onBlock(getDataInPosition(position));
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                    if (getDataInPosition(position).getStatus() == Friend.STATUS_ACCEPT
                            || getDataInPosition(position).getStatus() == Friend.STATUS_WAS_ACCEPTED) {
                        popup.inflate(R.menu.menu_friend);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_unfriend:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onUnfriend(getDataInPosition(position));
                                        return true;
                                    case R.id.menu_block:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onBlock(getDataInPosition(position));
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                    if (getDataInPosition(position).getStatus() == Friend.STATUS_REQUEST) {
                        popup.inflate(R.menu.menu_friend_my_request);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_cancel:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onCancel(getDataInPosition(position));
                                        return true;
                                    case R.id.menu_block:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onBlock(getDataInPosition(position));
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                    if (getDataInPosition(position).getStatus() == Friend.STATUS_BLOCK) {
                        popup.inflate(R.menu.menu_friend_my_block);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_unblock:
                                        //handle menu1 click
                                        itemFriendMenuClickListener.onUnblock(getDataInPosition(position));
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                    popup.show();
                }
            });
            iImageLoader = new GlideLoader();
            iImageLoader.loadImage(avatarView, getDataInPosition(position).getAvatar(), getDataInPosition(position).getName());
        }
    }

    interface ItemFriendMenuClickListener {
        void onAccept(Friend friend);

        void onReject(Friend friend);

        void onBlock(Friend friend);

        void onUnfriend(Friend friend);

        void onCancel(Friend friend);

        void onUnblock(Friend friend);

    }

    public static class HeaderDataImpl implements HeaderData {
        private int headerType;
        @LayoutRes
        private final int layoutResource;

        public HeaderDataImpl(int headerType, @LayoutRes int layoutResource) {
            this.layoutResource = layoutResource;
            this.headerType = headerType;
        }

        public HeaderDataImpl(int headerType) {
            this.layoutResource = R.layout.item_friend_header;
            this.headerType = headerType;
        }

        @LayoutRes
        @Override
        public int getHeaderLayout() {
            //return layout of yourHeader
            return layoutResource;
        }

        @Override
        public int getHeaderType() {
            return headerType;
        }

    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ll_layout_friend_header)
        LinearLayout linearLayout;
        @BindView(R.id.tv_header)
        TextView tvHeader;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(int position) {
            if (getDataInPosition(position) instanceof HeaderData) {
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.setVisibility(View.VISIBLE);
                switch (((HeaderData) getDataInPosition(position)).getHeaderType()) {
                    case Friend.STATUS_WAS_REQUESTED:
                        tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.black));
                        tvHeader.setText(MyApp.resources.getString(R.string.header_friend_request));
                        break;
                    case Friend.STATUS_ACCEPT:
                        tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.blue));
                        tvHeader.setText(MyApp.resources.getString(R.string.header_friend));
                        break;
                    case Friend.STATUS_REQUEST:
                        tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.green));
                        tvHeader.setText(MyApp.resources.getString(R.string.header_friend_my_request));
                        break;
                    case Friend.STATUS_BLOCK:
                        tvHeader.setBackgroundColor(MyApp.resources.getColor(R.color.purple));
                        tvHeader.setText(MyApp.resources.getString(R.string.header_friend_block));
                        break;
                }
            }
        }
    }
}
