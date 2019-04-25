package vn.huynh.whatsapp.base;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by duong on 4/24/2019.
 */

public class BaseActivity extends AppCompatActivity {

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
}
