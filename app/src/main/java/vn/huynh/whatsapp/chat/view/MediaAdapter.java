package vn.huynh.whatsapp.chat.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;

/**
 * Created by duong on 3/21/2019.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private static final String TAG = MediaAdapter.class.getSimpleName();

    private List<String> mMediaList;
    private Context mContext;
    private boolean mShowRemoveButton = false;

    public MediaAdapter(Context context, List<String> mediaList, boolean showRemoveButton) {
        this.mContext = context;
        this.mMediaList = mediaList;
        this.mShowRemoveButton = showRemoveButton;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        return new MediaViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final MediaViewHolder holder, int position) {
        holder.loader.setVisibility(View.VISIBLE);
        if (mShowRemoveButton) {
            holder.ivRemove.setVisibility(View.VISIBLE);
            holder.ivRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMediaList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });
        } else {
            holder.ivRemove.setVisibility(View.GONE);
        }
        try {
            Glide.with(mContext)
                    .load(Uri.parse(mMediaList.get(holder.getAdapterPosition())))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (!TextUtils.isEmpty(mMediaList.get(holder.getAdapterPosition())))
                                holder.loader.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.loader.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(holder.ivMedia);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        holder.ivMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageViewer.Builder(v.getContext(), mMediaList)
                        .setStartPosition(holder.getAdapterPosition())
                        .hideStatusBar(false)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mMediaList == null)
            return 0;
        else
            return mMediaList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_media)
        ImageView ivMedia;
        @BindView(R.id.loader_image)
        CircularDotsLoader loader;
        @BindView(R.id.iv_remove)
        ImageView ivRemove;

        public MediaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
