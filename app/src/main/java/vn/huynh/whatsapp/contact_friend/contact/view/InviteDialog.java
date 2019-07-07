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
    @BindView(R.id.edt_invite_message)
    EditText edtInviteMessage;
    @BindView(R.id.btn_invite)
    Button btnInvite;

    private Context context;
    private Dialog dialog;
    private User friend;
    private InviteListener inviteListener;

    public InviteDialog(Context context) {
        this.context = context;
    }

    public interface InviteListener {
        void onInviteCompleteListener(User friend, String message);
    }

    public boolean isShowing() {
        return (dialog != null) && dialog.isShowing();
    }

    public void show(User friend, InviteListener inviteListener) {
        if (friend == null)
            return;
        this.inviteListener = inviteListener;
        this.friend = friend;
        dialog = new Dialog(context, R.style.Dialog);
        View view = View.inflate(context, R.layout.dialog_invite, null);
        dialog.setTitle(context.getResources().getString(R.string.title_invite_friend));
        dialog.setContentView(view);
        ButterKnife.bind(this, view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initData();
        setupEvent();

        dialog.show();
    }

    private void initData() {
        edtInviteMessage.setHint(context.getResources().getString(R.string.hint_invite_message, friend.getName()));
    }

    private void setupEvent() {
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    if (inviteListener != null) {
                        if (edtInviteMessage.getText().toString().trim().isEmpty()) {
                            inviteListener.onInviteCompleteListener(friend, edtInviteMessage.getHint().toString());
                        } else {
                            inviteListener.onInviteCompleteListener(friend, edtInviteMessage.getText().toString().trim());
                        }
                    }
                    dialog.dismiss();
                }
            }
        });
    }
}
