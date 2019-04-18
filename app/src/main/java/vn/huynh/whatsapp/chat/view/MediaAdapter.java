package vn.huynh.whatsapp.chat.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.huynh.whatsapp.R;

/**
 * Created by duong on 3/21/2019.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private List<String> mediaList;
    private Context context;

    public MediaAdapter(Context context, List<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        MediaViewHolder viewHolder = new MediaViewHolder(layoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MediaViewHolder holder, final int position) {
        holder.loader.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(Uri.parse(mediaList.get(position)))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.loader.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.loader.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.ivMedia);
        holder.ivMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageViewer.Builder(v.getContext(), mediaList)
                        .setStartPosition(position)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mediaList == null)
            return 0;
        else
            return mediaList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_media)
        ImageView ivMedia;
        @BindView(R.id.loader_image)
        TashieLoader loader;

        public MediaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
