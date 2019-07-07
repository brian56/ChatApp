package vn.huynh.whatsapp.custom_views.sticky_header.stickyData;

import android.support.annotation.LayoutRes;

public interface HeaderData extends StickyMainData {
    @LayoutRes
    int getHeaderLayout();

    int getHeaderType();

}
