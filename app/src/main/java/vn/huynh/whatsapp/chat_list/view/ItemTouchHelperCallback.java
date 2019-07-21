package vn.huynh.whatsapp.chat_list.view;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.loopeer.itemtouchhelperextension.ItemTouchHelperExtension;

/**
 * Created by duong on 7/21/2019.
 */

public class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ChatListAdapter.ItemNoSwipeViewHolder) {
            return 0;
        }
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        ChatListAdapter adapter = (ChatListAdapter) recyclerView.getAdapter();
        adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (dY != 0 && dX == 0)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        ChatListAdapter.ChatListBaseViewHolder holder = (ChatListAdapter.ChatListBaseViewHolder) viewHolder;
        if (viewHolder instanceof ChatListAdapter.ItemSwipeWithActionWidthNoSpringViewHolder) {
            if (dX < -holder.actionContainer.getWidth()) {
                dX = -holder.actionContainer.getWidth();
            }
            holder.mainContainer.setTranslationX(dX);
            return;
        }
        if (viewHolder instanceof ChatListAdapter.ChatListBaseViewHolder)
            holder.mainContainer.setTranslationX(dX);
    }
}
