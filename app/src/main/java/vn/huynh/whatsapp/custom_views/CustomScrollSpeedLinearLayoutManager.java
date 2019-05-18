package vn.huynh.whatsapp.custom_views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by duong on 5/17/2019.
 */

public class CustomScrollSpeedLinearLayoutManager extends LinearLayoutManager {

    private float factor = 1;

    public CustomScrollSpeedLinearLayoutManager(Context context) {
        super(context);
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            private static final float SPEED = 3000f;// Change this value (default=25f)

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return SPEED / displayMetrics.densityDpi;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
