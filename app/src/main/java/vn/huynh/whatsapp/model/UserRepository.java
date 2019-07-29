package vn.huynh.whatsapp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;
import vn.huynh.whatsapp.utils.LogManagerUtils;

/**
 * Created by duong on 4/15/2019.
 */

public class UserRepository implements UserInterface {
    private static final String TAG = UserRepository.class.getSimpleName();
    private DatabaseReference mDbRef;

    private DatabaseReference mUserFriendDb;
    private ValueEventListener mUserFriendValueListener;

    public UserRepository() {
        mDbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void removeListener() {
        if (mUserFriendDb != null && mUserFriendValueListener != null) {
            mUserFriendDb.removeEventListener(mUserFriendValueListener);
        }
    }

    @Override
    public void isLoggedIn(final CheckLoginCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && ChatUtils.getUser() != null) {
            final DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER).
                    child(ChatUtils.getUser().getId());
            userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final User user = dataSnapshot.getValue(User.class);
                        Map<String, Object> map = new HashMap<>();
                        map.put(Constant.FB_KEY_LAST_ONLINE, ServerValue.TIMESTAMP);
                        userDb.updateChildren(map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (callback != null)
                                    callback.alreadyLoggedIn(user);
                            }
                        });
                    } else {
                        ChatUtils.clearUser();
                        if (callback != null)
                            callback.noLoggedIn();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (callback != null)
                        callback.noLoggedIn();
                }
            });
        } else {
            if (callback != null)
                callback.noLoggedIn();
        }
    }

    @Override
    public Task getUserData(final String userId) {
        final TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
        Task dbTask = dbSource.getTask();
        DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER).child(userId);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dbSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
            }
        };
        userDb.addListenerForSingleValueEvent(userValueEventListener);
        return dbTask;
    }

    @Override
    public void getCurrentUserData(String userId, final LoadContactCallback callback) {
        DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER).child(userId);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    LogManagerUtils.d(TAG, user.getName());
                    if (callback != null) {
                        callback.loadSuccess(user);
                    }
                } else {
                    if (callback != null)
                        callback.loadFail("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };
        userDb.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void checkPhoneNumberExist(String phoneNumber, final CheckPhoneNumberExistCallback callback) {
        DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER);
        Query query = userDb.orderByChild(Constant.FB_KEY_PHONE_NUMBER).equalTo(phoneNumber);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        if (callback != null)
                            callback.exist();
                    } else {
                        if (callback != null)
                            callback.notExist();
                    }
                } else {
                    if (callback != null)
                        callback.notExist();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.notExist();
            }
        };
        query.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void loadContact(Context context, List<User> contacts, final LoadContactCallback callback) {
        for (int i = 0; i < contacts.size(); i++) {
            getContactData(contacts.get(i), callback);
        }
    }

    @Override
    public void getContactData(final User contact, final LoadContactCallback callback) {
        DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER);
        Query query = userDb.orderByChild(Constant.FB_KEY_PHONE_NUMBER).equalTo(contact.getPhoneNumber());
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User tempUser;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        tempUser = childSnapshot.getValue(User.class);
                        tempUser.setRegisteredUser(true);
                        if (tempUser.getId().equals(ChatUtils.getUser().getId())) {
                            if (callback != null)
                                callback.loadSuccess(null);
                        } else {
                            if (tempUser.getName().equalsIgnoreCase(tempUser.getPhoneNumber())) {
                                tempUser.setName(contact.getName());
                            }
                            contact.cloneUser(tempUser);
                            //get the friend status
                            DatabaseReference friendDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                                    child(ChatUtils.getUser().getId()).child(tempUser.getId());
                            friendDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        contact.setFriendStatus(dataSnapshot.child(Constant.FB_KEY_STATUS).
                                                getValue(Integer.class));
                                    }
                                    if (callback != null)
                                        callback.loadSuccess(contact);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if (callback != null)
                                        callback.loadFail(databaseError.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    contact.setRegisteredUser(false);
                    if (callback != null)
                        callback.loadSuccess(contact);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.loadFail(databaseError.getMessage());
            }
        };
        query.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void createUser(final String userId, final String phoneNumber, final String name,
                           final CreateUserCallback callback) {
        final DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER).child(userId);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put(Constant.FB_KEY_PHONE_NUMBER, phoneNumber);
                userMap.put(Constant.FB_KEY_ID, userId);
                userMap.put(Constant.FB_KEY_NAME, name);
                userMap.put(Constant.FB_KEY_STATUS, User.STATUS_ONLINE);
                userMap.put(Constant.FB_KEY_LAST_ONLINE, ServerValue.TIMESTAMP);
                userMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                userDb.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        ChatUtils.setUser(user);
                                        if (callback != null)
                                            callback.createSuccess();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if (callback != null)
                                        callback.createFail(databaseError.getMessage());
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.createFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.createFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void updateUser(String userId, String phoneNumber, String name) {

    }

    @Override
    public void searchFriend(String phoneNumber, final SearchFriendCallback callback) {
        final ArrayList<User> result = new ArrayList<>();
        DatabaseReference userDb = mDbRef.child(Constant.FB_KEY_USER);
        Query query = userDb.orderByChild(Constant.FB_KEY_PHONE_NUMBER).startAt(phoneNumber)
                .endAt(phoneNumber + "\uf8ff");
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        user = childSnapshot.getValue(User.class);
                        user.setId(childSnapshot.getKey());
                        if (user.getId().equals(ChatUtils.getUser().getId())) {
                            if (callback != null)
                                callback.onSearchSuccess(result);
                        } else {
                            result.add(user);
                            break;
                        }
                    }
                    if (result.size() > 0 && result.get(0) != null) {
                        DatabaseReference friend = mDbRef.child(Constant.FB_KEY_FRIEND).
                                child(ChatUtils.getUser().getId()).child(result.get(0).getId());
                        friend.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String status = dataSnapshot.child(Constant.FB_KEY_STATUS).getValue().toString();
                                    int s = Integer.valueOf(status);
                                    result.get(0).setFriendStatus(s);
                                    if (callback != null)
                                        callback.onSearchSuccess(result);
                                } else {
                                    if (callback != null)
                                        callback.onSearchSuccess(result);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                if (callback != null)
                                    callback.onSearchFail(databaseError.getMessage());
                            }
                        });
                    } else {
                        if (callback != null)
                            callback.onSearchSuccess(result);
                    }
                } else {
                    if (callback != null)
                        callback.onSearchSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onSearchFail(databaseError.getMessage());
            }
        };
        query.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void listenerForUserFriendNotification(final FriendCallback callback) {
        mUserFriendDb = mDbRef.child(Constant.FB_KEY_USER).child(ChatUtils.getUser().getId())
                .child(Constant.FB_KEY_FRIEND_NOTIFICATION);
        mUserFriendValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    if (callback != null)
                        callback.onFriendNotification(dataSnapshot.getValue(Integer.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mUserFriendDb.addValueEventListener(mUserFriendValueListener);
    }
}
