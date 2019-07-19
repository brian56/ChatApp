package vn.huynh.whatsapp.contact_friend.contact.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.model.User;

/**
 * Created by duong on 5/22/2019.
 */

public class InviteDialog {
    private static final String TAG = InviteDialog.class.getSimpleName();
    @BindView(R.id.edt_invite_message)
    EditText edtInviteMessage;
    @BindView(R.id.btn_invite)
    Button btnInvite;

    private Context mContext;
    private Dialog mDialog;
    private User mUserFriend;
    private InviteListener mInviteListener;

    public InviteDialog(Context context) {
        this.mContext = context;
    }

    public interface InviteListener {
        void onInviteCompleteListener(User friend, String message);
    }

    public boolean isShowing() {
        return (mDialog != null) && mDialog.isShowing();
    }

    public void show(User friend, InviteListener inviteListener) {
        if (friend == null)
            return;
        this.mInviteListener = inviteListener;
        this.mUserFriend = friend;
        mDialog = new Dialog(mContext, R.style.Dialog);
        View view = View.inflate(mContext, R.layout.dialog_invite, null);
        mDialog.setTitle(mContext.getResources().getString(R.string.title_invite_friend));
        mDialog.setContentView(view);
        ButterKnife.bind(this, view);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initData();
        setupEvent();

        mDialog.show();
    }

    private void initData() {
        edtInviteMessage.setHint(mContext.getResources().getString(R.string.hint_invite_message, mUserFriend.getName()));
    }

    private void setupEvent() {
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    if (mInviteListener != null) {
                        if (edtInviteMessage.getText().toString().trim().isEmpty()) {
                            mInviteListener.onInviteCompleteListener(mUserFriend, edtInviteMessage.getHint().toString());
                        } else {
                            mInviteListener.onInviteCompleteListener(mUserFriend, edtInviteMessage.getText().toString().trim());
                        }
                    }
                    mDialog.dismiss();
                }
            }
        });
    }
}
