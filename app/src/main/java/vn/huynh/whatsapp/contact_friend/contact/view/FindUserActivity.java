package vn.huynh.whatsapp.contact_friend.contact.view;

import android.support.v7.app.AppCompatActivity;

public class FindUserActivity extends AppCompatActivity {

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
//    @BindView(R.id.rv_user_list)
//    RecyclerView rvUserList;
//    @BindView(R.id.btn_create_chat_room)
//    Button btnCreateChatRoom;
//
//    private RecyclerView.Adapter userListAdapter;
//    private RecyclerView.LayoutManager userListLayoutManager;
//    private ArrayList<UserObject> userList, contactList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_find_user);
//        ButterKnife.bind(this);
//
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setTitle("Find user");
//
//        setEvents();
//        initializeRecyclerView();
//        getContactList();
//    }
//
//
//    private void setEvents() {
//        btnCreateChatRoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createChat();
//            }
//        });
//    }
//
//    private void createChat() {
//        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//
//        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");
//        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
//
//        HashMap newChatMap = new HashMap();
//        newChatMap.put("id", key);
//        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);
//        boolean valid = false;
//        for (UserObject user : userList) {
//            if (user.getSelected()) {
//                valid = true;
//                newChatMap.put("users/" + user.getUid(), true);
//                userDb.child(user.getUid()).child("chat").child(key).setValue(true);
//            }
//        }
//        if (valid) {
//            chatInfoDb.updateChildren(newChatMap);
//            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
//        }
//    }
//
//    private void initializeRecyclerView() {
//        userList = new ArrayList<>();
//        contactList = new ArrayList<>();
//
//        rvUserList.setNestedScrollingEnabled(false);
//        rvUserList.setHasFixedSize(false);
//        userListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
//        rvUserList.setLayoutManager(userListLayoutManager);
//        userListAdapter = new ContactListAdapter(this, userList, true);
//        rvUserList.setAdapter(userListAdapter);
//
//    }
//
//    private void getContactList() {
//        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
//        while (phones.moveToNext()) {
//            String name = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
//            String phone = phones.getString(phones.getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER)));
//            phone = formatPhone(phone);
//            UserObject contact = new UserObject("", name, phone);
//            contactList.add(contact);
//            getUserDetail(contact);
//        }
//    }
//
//    private void getUserDetail(UserObject contact) {
//        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
//        Query query = userDB.orderByChild("phone").equalTo(contact.getPhone());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String phone = "";
//                    String name = "";
//                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
//                        if (childSnapshot.child("phone").getValue() != null) {
//                            phone = childSnapshot.child("phone").getValue().toString();
//                        }
//                        if (childSnapshot.child("name").getValue() != null) {
//                            name = childSnapshot.child("name").getValue().toString();
//                        }
//
//                        UserObject user = new UserObject(childSnapshot.getKey(), name, phone);
//                        if (name.equalsIgnoreCase(phone)) {
//                            for (UserObject contact : contactList) {
//                                if (formatPhone(contact.getPhone()).equalsIgnoreCase(user.getPhone())) {
//                                    user.setName(contact.getName());
//                                }
//                            }
//                        }
//                        userList.add(user);
//                        userListAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private String getPhoneFromCountryISO() {
//        String iso = null;
//        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
//        if (telephonyManager.getNetworkCountryIso() != null) {
//            if (!telephonyManager.getNetworkCountryIso().toString().equalsIgnoreCase("")) {
//                iso = telephonyManager.getNetworkCountryIso().toString();
//            }
//        }
//        return IsoToPhone.getPhone(iso);
//    }
//
//    private String formatPhone(String phone) {
//        if (phone != null) {
//            phone = phone.replace(" ", "");
//            phone = phone.replace("-", "");
//            phone = phone.replace("(", "");
//            phone = phone.replace(")", "");
//            if (!String.valueOf(phone.charAt(0)).equalsIgnoreCase("+")) {
//                phone = getPhoneFromCountryISO() + phone;
//            }
//        }
//        return phone;
//    }
}
