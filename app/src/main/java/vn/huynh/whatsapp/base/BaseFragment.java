package vn.huynh.whatsapp.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Toast;

/**
 * Created by duong on 7/4/2019
 */

public class BaseFragment extends Fragment {
    protected static ParentActivityListener parentActivityListener;
    protected static NewNotificationCallback newNotificationCallback;

    protected void showLoadingSwipeLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }

    protected void hideLoadingSwipeLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    protected void showErrorMessage(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    protected void showHideListIndicator(View container, boolean show) {
        if (container != null) {
            if (show)
                container.setVisibility(View.VISIBLE);
            else
                container.setVisibility(View.GONE);
        }
    }

    protected void showHideListLoadingIndicator(View container, View indicator, boolean show) {
        if (container != null && indicator != null) {
            if (show) {
                container.setVisibility(View.VISIBLE);
                indicator.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
                indicator.setVisibility(View.GONE);
            }
        }
    }

    protected void showHideListEmptyIndicator(View container, View indicator, boolean show) {

        if (container != null && indicator != null) {
            if (show) {
                container.setVisibility(View.VISIBLE);
                indicator.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
                indicator.setVisibility(View.GONE);
            }
        }
    }

    protected void showHideListErrorIndicator(View container, View indicator, boolean show) {
        if (container != null && indicator != null) {
            if (show) {
                container.setVisibility(View.VISIBLE);
                indicator.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
                indicator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivityListener = null;
        newNotificationCallback = null;
    }

    public interface ParentActivityListener {
        boolean returnFromChildActivity();

        void setReturnFromChildActivity(boolean returnFromChildActivity);

        void showMessageNotification(boolean show);

        void showFriendNotification(boolean show);
    }

    public interface NewNotificationCallback {
        void newChatNotificationDot();

        void removeChatNotificationDot();

        void newGroupNotificationDot();

        void removeGroupNotificationDot();

        void newContactNotificationDot();

        void removeContactNotificationDot();

        void newSettingNotificationDot();

        void removeSettingNotification();

        void showHideFriendDot(int friendNotification);
    }
}
