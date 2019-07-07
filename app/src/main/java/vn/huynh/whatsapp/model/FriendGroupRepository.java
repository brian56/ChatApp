package vn.huynh.whatsapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 5/18/2019.
 */

public class FriendGroupRepository implements FriendGroupInterface {
    private DatabaseReference dbRef;
    private DatabaseReference friendGroupDb;
    private ChildEventListener friendGroupChildEventListener;
    private ValueEventListener friendGroupValueEventListener;

    public FriendGroupRepository() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void removeListener() {
        if (friendGroupDb != null && friendGroupChildEventListener != null) {
            friendGroupDb.removeEventListener(friendGroupChildEventListener);
        }
    }

    @Override
    public void createFriendGroup(String name, List<User> userList, final CreateFriendGroupCallback callback) {
        friendGroupDb = dbRef.child(ChatUtils.getUser().getId()).push();
        final String friendGroupId = friendGroupDb.getKey();

        Map<String, Object> friendGroupMap = new HashMap<>();
        friendGroupMap.put("id", friendGroupId);
        friendGroupMap.put("name", name);
        friendGroupMap.put("createDate", ServerValue.TIMESTAMP);
        Map<String, Object> memberMap = new HashMap<>();
        for (User user : userList) {
            memberMap.put(user.getId(), ServerValue.TIMESTAMP);
        }
        friendGroupDb.setValue(friendGroupMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callback.createSuccess(friendGroupId);
                } else {
                    callback.createFail(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void removeFriendGroup(String id, RemoveFriendGroupCallback callBack) {

    }

    @Override
    public void addFriends(List<String> userIdList, String friendGroupId, AddFriendsCallback callBack) {

    }

    @Override
    public void removeFriends(List<String> userIdList, String friendGroupId, RemoveFriendsCallback callBack) {

    }
}
