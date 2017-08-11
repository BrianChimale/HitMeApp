package com.chimale.hitmeapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HP PAVILION on 8/9/2017.
 */

public class UsersViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public UsersViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setName(String name){
        TextView userNameView = (TextView) mView.findViewById(R.id.user_single_profile);
        userNameView.setText(name);
    }

    public void setStatus(String status){
        TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
        userStatusView.setText(status);
    }

    public void setImage(String thumbnail, Context context){
        CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
        if (!thumbnail.equals("default")) {
            Picasso.with(context).load(thumbnail).placeholder(R.drawable.default_male_avatar).into(userImageView);
        }
    }

}
