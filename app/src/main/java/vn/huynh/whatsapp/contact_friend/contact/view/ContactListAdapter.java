package vn.huynh.whatsapp.contact_friend.contact.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.model.Friend;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.GlideLoader;
import vn.huynh.whatsapp.utils.MyApp;

/**
 * Created by duong on 3/20/2019.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.UserListViewHolder> {
    private static final String TAG = ContactListAdapter.class.getSimpleName();
    private ArrayList<User> mUserList;
    private boolean mContactClickable = true;
    private boolean mShowFriendStatus = false;
    private boolean mShowCheckbox = false;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public ContactListAdapter(Context context, ArrayList<User> userList, boolean contactClickable, boolean showCheckbox, boolean showFriendStatus) {
        this.mContext = context;
        this.mUserList = userList;
        this.mContactClickable = contactClickable;
        this.mShowCheckbox = showCheckbox;
        this.mShowFriendStatus = showFriendStatus;
    }

    public ContactListAdapter(Context context, ArrayList<User> userList, boolean contactClickable, boolean showCheckbox,
                              boolean showFriendStatus, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mUserList = userList;
        this.mShowFriendStatus = showFriendStatus;
        this.mShowCheckbox = showCheckbox;
        this.mContactClickable = contactClickable;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        return new UserListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, int position) {
        holder.tvName.setText(mUserList.get(position).getName());
        holder.tvPhone.setText(mUserList.get(position).getPhoneNumber());
        holder.mIImageLoader = new GlideLoader();
        holder.mIImageLoader.loadImage(holder.avatarView, mUserList.get(holder.getAdapterPosition()).getAvatar(), mUserList.get(holder.getAdapterPosition()).getName());
        if (mShowCheckbox) {
            holder.cbAdd.setVisibility(View.VISIBLE);
            holder.cbAdd.setChecked(mUserList.get(holder.getAdapterPosition()).getSelected());
            holder.cbAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mUserList.get(holder.getAdapterPosition()).setSelected(isChecked);
                }
            });
        } else {
            holder.cbAdd.setVisibility(View.GONE);
        }
        if (mShowFriendStatus) {
            holder.cbAdd.setVisibility(View.GONE);
            holder.ivAddFriend.setVisibility(View.VISIBLE);
            switch (mUserList.get(holder.getAdapterPosition()).getFriendStatus()) {
                case Friend.STATUS_ACCEPT:
                case Friend.STATUS_WAS_ACCEPTED:
                    holder.ivAddFriend.setImageResource(R.drawable.ic_supervisor_account_black_24dp);
                    holder.ivAddFriend.setColorFilter(R.color.colorAccent_1, PorterDuff.Mode.SRC_IN);
                    holder.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, MyApp.resources.getString(R.string.you_were_friend,
                                    mUserList.get(holder.getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                case Friend.STATUS_REQUEST:
                    holder.ivAddFriend.setImageResource(R.drawable.ic_watch_later_black_24dp);
                    holder.ivAddFriend.setColorFilter(R.color.colorAccent_1, PorterDuff.Mode.SRC_IN);
                    holder.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, MyApp.resources.getString(R.string.you_have_sent_friend_request_to_this,
                                    mUserList.get(holder.getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                case Friend.STATUS_WAS_REQUESTED:
                    holder.ivAddFriend.setImageResource(R.drawable.ic_mail_black_24dp);
                    holder.ivAddFriend.setColorFilter(R.color.colorAccent_1, PorterDuff.Mode.SRC_IN);
                    holder.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, MyApp.resources.getString(R.string.sent_you_a_friend_request_please_confirm,
                                    mUserList.get(holder.getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
                        }
                    });
                    break;

                case Friend.STATUS_BLOCK:
                case Friend.STATUS_WAS_BLOCKED:
                    holder.ivAddFriend.setVisibility(View.GONE);
                    break;

                default:
                    if (mShowCheckbox) {
                        holder.cbAdd.setVisibility(View.VISIBLE);
                        holder.ivAddFriend.setVisibility(View.GONE);
                    } else {
                        holder.ivAddFriend.setImageResource(R.drawable.ic_person_add_grey_24dp);
                        holder.ivAddFriend.setColorFilter(R.color.colorAccent, PorterDuff.Mode.SRC_IN);
                        holder.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnItemClickListener != null) {
                                    mOnItemClickListener.onAddFriend(mUserList.get(holder.getAdapterPosition()));
                                }
                            }
                        });
                    }
                    break;
            }
        } else {
            holder.ivAddFriend.setVisibility(View.GONE);
        }
        if (mContactClickable) {
            if (mUserList.get(holder.getAdapterPosition()).getRegisteredUser()) {
                //registered user
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = mUserList.get(holder.getAdapterPosition());
                        mOnItemClickListener.onChat(user);
                    }
                });
            } else {
                //not registered user
                holder.ivAddFriend.setVisibility(View.GONE);
            }
        } else {
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mShowCheckbox)
                        holder.cbAdd.performClick();
                }
            });
        }

        if (mUserList.get(holder.getAdapterPosition()).getRegisteredUser()) {
            holder.setAlpha(1);
        } else {
            holder.setAlpha(0.4f);
        }
    }

    @Override
    public int getItemCount() {
        if (mUserList != null)
            return mUserList.size();
        else
            return 0;
    }

//    private void createChat(final int position) {
//        final String key = ChatUtils.getChatId(FirebaseAuth.getInstance().getUid(), mUserList.get(position).getId());
//        //String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//
//        HashMap newChatMap = new HashMap();
//        newChatMap.put("id", key);
//        newChatMap.put("group", false);
//        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);
//        newChatMap.put("users/" + mUserList.get(position).getId(), true);
//
//        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
//        chatInfoDb.updateChildren(newChatMap, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                if (databaseError == null) {
//                    DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");
//                    userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
//                    userDb.child(mUserList.get(position).getUid()).child("chat").child(key).setValue(true);
//
//                    Intent intent = new Intent(context, ChatActivity.class);
//                    intent.putExtra("chatId", key);
//                    context.startActivity(intent);
//                } else {
//                    Log.w(ContactListAdapter.class.getSimpleName(), "onComplete: fail", databaseError.toException());
//                }
//            }
//
//        });
//    }


    public class UserListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_phone)
        TextView tvPhone;
        @BindView(R.id.ll_layout)
        LinearLayout linearLayout;
        @BindView(R.id.cb_add)
        CheckBox cbAdd;
        @BindView(R.id.avatar)
        AvatarView avatarView;
        @BindView(R.id.iv_friend_status)
        ImageView ivAddFriend;

        public IImageLoader mIImageLoader;

        public UserListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setAlpha(float alpha) {
            linearLayout.setAlpha(alpha);
        }
    }

    interface OnItemClickListener {
        void onInvite(User user);

        void onAddFriend(User user);

        void onChat(User user);
    }
}
