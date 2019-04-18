package vn.huynh.whatsapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.home.HomeActivity;

public class LoginActivityold extends AppCompatActivity {

    @BindView(R.id.edt_phone_number)
    EditText edtPhoneNumber;
    @BindView(R.id.edt_code)
    EditText edtCode;
    @BindView(R.id.edt_name)
    EditText edtName;
    @BindView(R.id.btn_verify)
    Button btnVerify;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initialize();
        setEvents();
    }

    private void initialize() {
        FirebaseApp.initializeApp(this);
        userIsLoggedIn();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                verificationId = "";
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                btnVerify.setText("Verify");
            }
        };
    }

    private void setEvents() {
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificationId != null) {
                    verifyPhoneNumberWithCode();
                } else if (checkInput()){
                    name = edtName.getText().toString().trim();
                    startPhoneNumberVerification();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide phone number and name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startPhoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                edtPhoneNumber.getText().toString().trim(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null) {
                        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phoneNumber", user.getPhoneNumber());
                                    userMap.put("name", name);
                                    userMap.put("regTime", ServerValue.TIMESTAMP);
                                    userDB.updateChildren(userMap);
                                }
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
            return;
        }
    }

    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, edtCode.getText().toString().trim());
        signInWithPhoneAuthCredential(credential);
    }

    private boolean checkInput() {
        return ! (edtPhoneNumber.getText().toString().trim().isEmpty() || edtName.getText().toString().trim().isEmpty());
    }
}
