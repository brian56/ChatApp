package vn.huynh.whatsapp.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

/**
 * Created by duong on 7/4/2019
 */

public class BaseFragment extends Fragment {
    protected void showLoadingIndicator(SwipeRefreshLayout swipeRefreshLayout) {
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }

    protected void hideLoadingIndicator(SwipeRefreshLayout swipeRefreshLayout) {
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    protected void showErrorMessage(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
