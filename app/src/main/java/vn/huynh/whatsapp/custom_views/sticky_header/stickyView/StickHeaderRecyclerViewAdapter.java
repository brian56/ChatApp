package vn.huynh.whatsapp.custom_views.sticky_header.stickyView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.huynh.whatsapp.custom_views.sticky_header.stickyData.HeaderData;
import vn.huynh.whatsapp.custom_views.sticky_header.stickyData.StickyMainData;

public abstract class StickHeaderRecyclerViewAdapter<D extends StickyMainData, H extends HeaderData> extends RecyclerView.Adapter implements StickHeaderItemDecoration.StickyHeaderInterface {
    private static final String TAG = StickHeaderRecyclerViewAdapter.class.getSimpleName();
    private List<StickyMainData> mData;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        StickHeaderItemDecoration stickHeaderDecoration = new StickHeaderItemDecoration(this);
        recyclerView.addItemDecoration(stickHeaderDecoration);
    }

    @Override
    public final int getItemViewType(int position) {
        if (mData.get(position) instanceof HeaderData) {
            return ((HeaderData) mData.get(position)).getHeaderType();
        }
        return getViewType(position);
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return itemPosition < getItemCount()
                && mData.get(itemPosition) instanceof HeaderData;
    }

    @Override
    public int getItemCount() {
        if (mData != null)
            return mData.size();
        else
            return 0;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return ((HeaderData) mData.get(headerPosition)).getHeaderLayout();
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    public List<StickyMainData> getListData() {
        if (mData != null) {
            return mData;
        } else {
            mData = new ArrayList<>();
            return mData;
        }
    }

    public int getNumberHeader() {
        int numberHeader = 0;
        if (mData == null)
            return numberHeader;
        for (StickyMainData s : mData) {
            if (s instanceof HeaderData) {
                numberHeader++;
            }
        }
        return numberHeader;
    }

    public void setHeaderAndData(@NonNull List<D> datas, @Nullable HeaderData header) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (header != null && !mData.contains(header)) {
            mData.add(header);
        }
        int position = getHeaderPositionByHeaderType(header.getHeaderType()) + 1;
        mData.addAll(position, datas);
        this.notifyItemRangeInserted(position, datas.size());
    }

    public void setHeaderAndData(@NonNull D data, @Nullable HeaderData header, int headerPosition) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        if (header != null && !mData.contains(header)) {
            mData.add(headerPosition, header);
            notifyItemInserted(headerPosition);
            notifyItemRangeChanged(headerPosition, getItemCount());
        }
        int positionHeader = getHeaderPositionByHeaderType(header.getHeaderType());
        int position = positionHeader + 1;
        mData.add(position, data);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public int getHeaderPositionByHeaderType(int headerType) {
        int headerPosition = -1;
        for (int i = 0; i < getItemCount(); i++) {
            if (mData.get(i) instanceof HeaderData) {
                if (((HeaderData) mData.get(i)).getHeaderType() == headerType)
                    return i;
            }
        }
        return headerPosition;
    }

    protected int getViewType(int pos) {
        return 0;
    }

    protected D getDataInPosition(int position) {
        return (D) mData.get(position);
    }

    protected H getHeaderDataInPosition(int position) {
        return (H) mData.get(position);
    }

    public void clearData() {
        if (mData != null) {
            mData.clear();
            this.notifyDataSetChanged();
        }
    }
}
