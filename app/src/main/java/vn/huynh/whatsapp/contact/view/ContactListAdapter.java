package vn.huynh.whatsapp.contact.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import agency.tango.android.avatarview.IImageLoader;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.contact.ContactContract;
import vn.huynh.whatsapp.model.User;
import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.GlideLoader;

/**
 * Created by duong on 3/20/2019.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.UserListViewHolder> {
    private ArrayList<User> userList;
    private boolean contactClickable = true;
    private ContactContract.Presenter presenter;

    public ContactListAdapter(ArrayList<User> userList, boolean contactClickable) {
        this.userList = userList;
        this.contactClickable = contactClickable;
    }

    public ContactListAdapter(ArrayList<User> userList, boolean contactClickable, ContactContract.Presenter presenter) {
        this.userList = userList;
        this.contactClickable = contactClickable;
        this.presenter = presenter;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        UserListViewHolder rcv = new UserListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, int position) {
        holder.tvName.setText(userList.get(position).getName());
        holder.tvPhone.setText(userList.get(position).getPhoneNumber());
        holder.iImageLoader = new GlideLoader();
        holder.iImageLoader.loadImage(holder.avatarView, userList.get(holder.getAdapterPosition()).getAvatar(), userList.get(holder.getAdapterPosition()).getName());
        if (contactClickable) {
            holder.cbAdd.setVisibility(View.GONE);
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (presenter != null) {
                        List<User> list = new ArrayList<>();
                        list.add(new User(ChatUtils.currentUserId()));
                        list.add(userList.get(holder.getAdapterPosition()));
                        presenter.checkSingleChatExist(false, "", list);
//                        presenter.createChat(false, "", list);
                    }
                }
            });
        } else {
            holder.cbAdd.setVisibility(View.VISIBLE);
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.cbAdd.performClick();
                }
            });
            holder.cbAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userList.get(holder.getAdapterPosition()).setSelected(isChecked);
                }
            });
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

        public IImageLoader iImageLoader;

        public UserListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
