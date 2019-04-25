package vn.huynh.whatsapp.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OneSignal;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;
import vn.huynh.whatsapp.login.view.LoginActivity;
import vn.huynh.whatsapp.utils.PlayMusicService;

/**
 * Created by duong on 3/22/2019.
 */

public class SettingFragment extends Fragment {

    @BindView(R.id.btn_log_out)
    Button btnLogOut;
    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.btn_webview)
    Button btnWebView;
    Intent playMusicIntent;

    public static final String TAG = "SettingFragment";

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEvents();
    }

    private void setEvents() {
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                OneSignal.setSubscription(false);
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicIntent = new Intent(getActivity(), PlayMusicService.class);
                getActivity().startService(playMusicIntent);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(playMusicIntent);
            }
        });
        btnWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), WebViewActivity.class));
            }
        });
    }

    @Override
    public void onDestroy() {

        Log.d(SettingFragment.class.getSimpleName(), "setting fragment on destroy");
        try {
            getActivity().stopService(playMusicIntent);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
