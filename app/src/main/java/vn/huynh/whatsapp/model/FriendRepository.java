package vn.huynh.whatsapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import vn.huynh.whatsapp.utils.ChatUtils;
import vn.huynh.whatsapp.utils.Constant;

/**
 * Created by duong on 5/18/2019.
 */

public class FriendRepository implements FriendInterface {
    private DatabaseReference mDbRef;

    private Query mFriendListQuery;
    private ChildEventListener mFriendListChildEventListener;

    public FriendRepository() {
        this.mDbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void removeListener() {
        if (mFriendListQuery != null && mFriendListChildEventListener != null) {
            mFriendListQuery.removeEventListener(mFriendListChildEventListener);
        }
    }

    @Override
    public void createInvite(User user, String message, CreateInviteCallback callback) {
        //TODO: send invitation to friend to install app
    }

    @Override
    public void getAllFriend(int friendStatus, final GetFriendCallback callback) {
        removeListener();
        updateFriendNotification(ChatUtils.getUser().getId(), -1, false);
        if (friendStatus >= 0) {
            mFriendListQuery = mDbRef.child(Constant.FB_KEY_FRIEND).child(ChatUtils.getUser().getId()).
                    orderByChild(Constant.FB_KEY_STATUS).equalTo(friendStatus);
        } else {
            //get all
            mFriendListQuery = mDbRef.child(Constant.FB_KEY_FRIEND).child(ChatUtils.getUser().getId()).
                    orderByChild(Constant.FB_KEY_CREATE_DATE);
        }
        mFriendListChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (callback != null)
                        callback.onGetAllFriendSuccess(friend);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (callback != null)
                        callback.onFriendUpdate(friend);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (callback != null)
                        callback.onFriendRemove(friend);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mFriendListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    if (callback != null)
                        callback.onGetAllFriendSuccessEmptyData();
                } else {
                    if (callback != null)
                        callback.onGetFriendCount(dataSnapshot.getChildrenCount());
                }
                mFriendListQuery.addChildEventListener(mFriendListChildEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onGetAllFriendFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void createRequest(final User userFriend, final CreateRequestCallback callback) {
        final DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                child(userFriend.getId()).child(ChatUtils.getUser().getId());
        theirFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put(Constant.FB_KEY_USER_ID, ChatUtils.getUser().getId());
                friendMap.put(Constant.FB_KEY_PHONE_NUMBER, ChatUtils.getUser().getPhoneNumber());
                friendMap.put(Constant.FB_KEY_NAME, ChatUtils.getUser().getName());
                friendMap.put(Constant.FB_KEY_AVATAR, ChatUtils.getUser().getAvatar());
                friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_WAS_REQUESTED);
                friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                theirFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            updateFriendNotification(userFriend.getId(), Friend.STATUS_WAS_REQUESTED, true);

                            final DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                                    child(ChatUtils.getUser().getId()).child(userFriend.getId());
                            myFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final Map<String, Object> friendMap = new HashMap<>();
                                    friendMap.put(Constant.FB_KEY_USER_ID, userFriend.getId());
                                    friendMap.put(Constant.FB_KEY_PHONE_NUMBER, userFriend.getPhoneNumber());
                                    friendMap.put(Constant.FB_KEY_NAME, userFriend.getName());
                                    friendMap.put(Constant.FB_KEY_AVATAR, userFriend.getAvatar());
                                    friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_REQUEST);
                                    friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                                    myFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError == null) {
//                                                updateFriendNotification(ChatUtils.getUser().getId(), Friend.STATUS_WAS_REQUESTED);
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            Friend friend = dataSnapshot.getValue(Friend.class);
                                                            if (callback != null)
                                                                callback.onCreateRequestSuccess(friend);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        if (callback != null)
                                                            callback.onCreateRequestFail(databaseError.getMessage());
                                                    }
                                                });
                                            } else {
                                                if (callback != null)
                                                    callback.onCreateRequestFail(databaseError.getMessage());
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if (callback != null)
                                        callback.onCreateRequestFail(databaseError.getMessage());
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.onCreateRequestFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onCreateRequestFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void cancelRequest(final Friend friend, final CancelRequestCallback callback) {
        DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).child(friend.getUserId()).
                child(ChatUtils.getUser().getId());
        theirFriendRequestDb.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    updateFriendNotification(friend.getUserId(), Friend.STATUS_WAS_CANCELED, false);
                    DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                            child(ChatUtils.getUser().getId()).child(friend.getUserId());
                    myFriendRequestDb.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                if (callback != null)
                                    callback.onCancelSuccess(friend);
                            } else {
                                if (callback != null)
                                    callback.onCancelFail(databaseError.getMessage());
                            }
                        }
                    });
                } else {
                    if (callback != null)
                        callback.onCancelFail(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void acceptRequest(final Friend friend, final AcceptRequestCallback callback) {
        final DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                child(friend.getUserId()).child(ChatUtils.getUser().getId());
        theirFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_WAS_ACCEPTED);
                friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                theirFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            updateFriendNotification(friend.getUserId(), Friend.STATUS_WAS_ACCEPTED, true);

                            final DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                                    child(ChatUtils.getUser().getId()).child(friend.getUserId());
                            myFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Map<String, Object> friendMap = new HashMap<>();
                                    friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_ACCEPT);
                                    //if we update using ServerValue.TIMESTAMP, the onChildChanged event will fired twice
                                    //so in the fragment, we have to ignore the first event, and process the second event
                                    friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                                    myFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                updateFriendNotification(ChatUtils.getUser().getId(),
                                                        Friend.STATUS_ACCEPT, false);

                                                friend.setStatus(Friend.STATUS_ACCEPT);
                                                if (callback != null)
                                                    callback.onAcceptSuccess(friend);
                                            } else {
                                                if (callback != null)
                                                    callback.onAcceptFail(databaseError.getMessage());
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if (callback != null)
                                        callback.onAcceptFail(databaseError.getMessage());
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.onAcceptFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onAcceptFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void rejectRequest(final Friend friend, final RejectRequestCallback callback) {
        final DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                child(friend.getUserId()).child(ChatUtils.getUser().getId());
        theirFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_WAS_REJECTED);
                friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                theirFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            updateFriendNotification(friend.getUserId(), Friend.STATUS_WAS_REJECTED, true);

                            final DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                                    child(ChatUtils.getUser().getId()).child(friend.getUserId());
                            myFriendRequestDb.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    updateFriendNotification(ChatUtils.getUser().getId(),
                                            Friend.STATUS_REJECT, false);
                                    if (callback != null)
                                        callback.onRejectSuccess(friend);
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.onRejectFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onRejectFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void blockRequest(final Friend friend, final BlockRequestCallback callback) {
        final DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                child(friend.getUserId()).child(ChatUtils.getUser().getId());
        theirFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_WAS_BLOCKED);
                friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                theirFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            final DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                                    child(ChatUtils.getUser().getId()).child(friend.getUserId());
                            myFriendRequestDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Map<String, Object> friendMap = new HashMap<>();
                                    friendMap.put(Constant.FB_KEY_STATUS, Friend.STATUS_BLOCK);
                                    friendMap.put(Constant.FB_KEY_CREATE_DATE, ServerValue.TIMESTAMP);
                                    myFriendRequestDb.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                friend.setStatus(Friend.STATUS_BLOCK);
                                                if (callback != null)
                                                    callback.onBlockSuccess(friend);
                                            } else {
                                                if (callback != null)
                                                    callback.onBlockFail(databaseError.getMessage());
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if (callback != null)
                                        callback.onBlockFail(databaseError.getMessage());
                                }
                            });
                        } else {
                            if (callback != null)
                                callback.onBlockFail(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (callback != null)
                    callback.onBlockFail(databaseError.getMessage());
            }
        });
    }

    @Override
    public void removeFriend(final Friend friend, final RemoveFriendCallback callback) {
        final DatabaseReference theirFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                child(friend.getUserId()).child(ChatUtils.getUser().getId());
        theirFriendRequestDb.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    updateFriendNotification(friend.getUserId(), Friend.STATUS_WAS_UNFRIEND, true);
                    final DatabaseReference myFriendRequestDb = mDbRef.child(Constant.FB_KEY_FRIEND).
                            child(ChatUtils.getUser().getId()).child(friend.getUserId());
                    myFriendRequestDb.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                if (callback != null)
                                    callback.onRemoveFriendSuccess(friend);
                            }
                        }
                    });
                } else {
                    if (callback != null)
                        callback.onRemoveFail(databaseError.getMessage());
                }
            }
        });
    }

    @Override
    public void updateFriendNotification(final String userId, int friendStatus, boolean showNotify) {
        if (showNotify) {
            DatabaseReference userDB = mDbRef.child(Constant.FB_KEY_USER).child(userId).
                    child(Constant.FB_KEY_FRIEND_NOTIFICATION);
            userDB.setValue(1);
        } else {
            Query query = mDbRef.child(Constant.FB_KEY_FRIEND).child(userId).orderByChild(Constant.FB_KEY_STATUS).
                    equalTo(Friend.STATUS_WAS_REQUESTED);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference userDB = mDbRef.child(Constant.FB_KEY_USER).child(userId).
                            child(Constant.FB_KEY_FRIEND_NOTIFICATION);
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            userDB.setValue(1);
                        } else {
                            userDB.setValue(0);
                        }
                    } else {
                        userDB.setValue(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
