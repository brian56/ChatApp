package vn.huynh.whatsapp.contact_friend.contact.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.GlideLoader;

/**
 * Created by duong on 3/20/2019.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.UserListViewHolder> {
    private static final String TAG = ContactListAdapter.class.getSimpleName();
    private ArrayList<User> userList;
    private boolean contactClickable = true;
    private OnItemClickListener onItemClickListener;

    public ContactListAdapter(ArrayList<User> userList, boolean contactClickable) {
        this.userList = userList;
        this.contactClickable = contactClickable;
    }

    public ContactListAdapter(ArrayList<User> userList, boolean contactClickable, OnItemClickListener onItemClickListener) {
        this.userList = userList;
        this.contactClickable = contactClickable;
        this.onItemClickListener = onItemClickListener;
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
        holder.tvName.setText(userList.get(position).getName());
        holder.tvPhone.setText(userList.get(position).getPhoneNumber());
        holder.iImageLoader = new GlideLoader();
        holder.iImageLoader.loadImage(holder.avatarView, userList.get(holder.getAdapterPosition()).getAvatar(), userList.get(holder.getAdapterPosition()).getName());
        if (contactClickable) {
            holder.cbAdd.setVisibility(View.GONE);
            if (userList.get(holder.getAdapterPosition()).getRegisteredUser()) {
                //registered user
                holder.ivInvite.setVisibility(View.GONE);
                holder.ivAddFriend.setVisibility(View.VISIBLE);
                holder.ivAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = userList.get(holder.getAdapterPosition());
                        onItemClickListener.onAddFriend(user);
                    }
                });
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = userList.get(holder.getAdapterPosition());
                        onItemClickListener.onChat(user);
                    }
                });
            } else {
                //not registered user
                holder.ivAddFriend.setVisibility(View.GONE);
                holder.ivInvite.setVisibility(View.VISIBLE);
                holder.ivInvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = userList.get(holder.getAdapterPosition());
                        onItemClickListener.onInvite(user);
                    }
                });
            }
        } else {
            holder.cbAdd.setVisibility(View.VISIBLE);
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.cbAdd.performClick();
                }
            });
            holder.cbAdd.setChecked(userList.get(holder.getAdapterPosition()).getSelected());
            holder.cbAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userList.get(holder.getAdapterPosition()).setSelected(isChecked);
                }
            });
        }
        if (userList.get(holder.getAdapterPosition()).getRegisteredUser()) {
            holder.setAlpha(1);
        } else {
            holder.setAlpha(0.4f);
        }
    }

    @Override
    public int getItemCount() {
        if (userList != null)
            return userList.size();
        else
            return 0;
    }

//    private void createChat(final int position) {
//        final String key = ChatUtils.getChatId(FirebaseAuth.getInstance().getUid(), userList.get(position).getId());
//        //String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//
//        HashMap newChatMap = new HashMap();
//        newChatMap.put("id", key);
//        newChatMap.put("group", false);
//        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);
//        newChatMap.put("users/" + userList.get(position).getId(), true);
//
//        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
//        chatInfoDb.updateChildren(newChatMap, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                if (databaseError == null) {
//                    DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");
//                    userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
//                    userDb.child(userList.get(position).getUid()).child("chat").child(key).setValue(true);
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
        @BindView(R.id.iv_invite)
        ImageView ivInvite;
        @BindView(R.id.iv_add_friend)
        ImageView ivAddFriend;

        public IImageLoader iImageLoader;

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
