package vn.huynh.whatsapp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;

/**
 * Created by duong on 4/15/2019.
 */

public class UserRepository implements UserInterface {
    private static final String TAG = UserRepository.class.getSimpleName();
    private DatabaseReference dbRef;
    private DatabaseReference userDb;
    private ValueEventListener userValueEventListener;
    private Query query;

    public UserRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void removeListener() {
        if(userDb != null && userValueEventListener != null)
            userDb.removeEventListener(userValueEventListener);
        if(query != null && userValueEventListener != null)
            query.removeEventListener(userValueEventListener);
    }

    @Override
    public void isLoggedIn(final CheckLoginCallBack callBack) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !TextUtils.isEmpty(ChatUtils.getCurrentUserId())) {
            userDb = dbRef.child("user").child(ChatUtils.getCurrentUserId());
            userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("lastOnline", ServerValue.TIMESTAMP);
                        userDb.updateChildren(map);
                        callBack.alreadyLoggedIn();
                    } else {
                        ChatUtils.clearUser();
                        callBack.noLoggedIn();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            callBack.noLoggedIn();
        }
    }

    @Override
    public void loadContact(Context context, List<User> contacts, LoadContactCallBack callBack) {
        for (int i = 0; i < contacts.size(); i++) {
            getContactData(contacts.get(i), callBack);
        }
    }

    @Override
    public Task getUserData(final String userId) {
        final TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
        final Task dbTask = dbSource.getTask();
        final DatabaseReference userDb = dbRef.child("user").child(userId);
        final ValueEventListener userValueEventListener = new ValueEventListener() {
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

    /*@Override
    public void getUserData(final String userId, final Chat chat, final ChatInterface.ChatListCallBack callBack) {
        final DatabaseReference userDb = dbRef.child("user").child(userId);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setId(dataSnapshot.getKey());
                chat.addUser(user);
                Log.d(TAG, user.getName());
                if (callBack != null) {
                    callBack.loadSuccess(chat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userDb.addListenerForSingleValueEvent(userValueEventListener);
    }*/

    /*@Override
    public void getUserData(String userId, final Chat chat, final ChatInterface.ChatDetailCallBack callBack) {
        final DatabaseReference userDb = dbRef.child("user").child(userId);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setId(dataSnapshot.getKey());
                chat.addUser(user);
                Log.d(TAG, user.getName());
                if (callBack != null) {
                    callBack.loadSuccess(chat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userDb.addListenerForSingleValueEvent(userValueEventListener);
    }*/

    @Override
    public void getCurrentUserData(String userId, final LoadContactCallBack callBack) {
        final DatabaseReference userDb = dbRef.child("user").child(userId);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.d(TAG, user.getName());
                    if (callBack != null) {
                        callBack.loadSuccess(user);
                    }
                } else {
                    callBack.loadFail("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userDb.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void checkPhoneNumberExist(String phoneNumber, final CheckPhoneNumberExistCallBack callBack) {
        DatabaseReference userDb = dbRef.child("user");
        Query query = userDb.orderByChild("phoneNumber").equalTo(phoneNumber);
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        callBack.exist();
                    } else {
                        callBack.notExist();
                    }
                } else {
                    callBack.notExist();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void getContactData(final User contact, final LoadContactCallBack callBack) {
        DatabaseReference userDb = dbRef.child("user");
        Query query = userDb.orderByChild("phoneNumber").equalTo(contact.getPhoneNumber());
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        User user = childSnapshot.getValue(User.class);
                        user.setId(childSnapshot.getKey());

                        if (user.getName().equalsIgnoreCase(user.getPhoneNumber())) {
                            user.setName(contact.getName());
                        }
                        callBack.loadSuccess(user);
                    }
                } else {
                    callBack.loadSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(userValueEventListener);
    }

    @Override
    public void createUser(final String userId, final String phoneNumber, final String name, final CreateUserCallBack callBack) {
        final DatabaseReference userDb = dbRef.child("user").child(userId);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("phoneNumber", phoneNumber);
                userMap.put("id", userId);
                userMap.put("name", name);
                userMap.put("status", User.STATUS_ONLINE);
                userMap.put("lastOnline", ServerValue.TIMESTAMP);
                userMap.put("createDate", ServerValue.TIMESTAMP);
                userDb.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        ChatUtils.setUser(user);
                                        callBack.createSuccess();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            callBack.createFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void updateUser(String userId, String phoneNumber, String name) {

    }
}
