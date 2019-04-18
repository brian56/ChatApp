package vn.huynh.whatsapp.utils;

import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.ImageLoaderBase;
import agency.tango.android.avatarview.views.AvatarView;

/**
 * Created by duong on 4/17/2019.
 */

public class GlideLoader extends  ImageLoaderBase {

    public GlideLoader() {
        super();
    }

    public GlideLoader(String defaultPlaceholderString) {
        super(defaultPlaceholderString);
    }

    @Override
    public void loadImage(@NonNull AvatarView avatarView, @NonNull AvatarPlaceholder avatarPlaceholder, String avatarUrl) {
        Glide.with(avatarView.getContext())
                .load(avatarUrl)
                .placeholder(avatarPlaceholder)
                .into(avatarView);
    }
}
