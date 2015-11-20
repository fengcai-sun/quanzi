package com.tizi.quanzi.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tizi.quanzi.Intent.StartGalleryActivity;
import com.tizi.quanzi.R;
import com.tizi.quanzi.gson.Dyns;
import com.tizi.quanzi.gson.OtherUserInfo;
import com.tizi.quanzi.network.FindUser;
import com.tizi.quanzi.network.RetrofitNetworkAbs;
import com.tizi.quanzi.tool.FriendTime;
import com.tizi.quanzi.tool.GetThumbnailsUri;
import com.tizi.quanzi.tool.StaticField;
import com.tizi.quanzi.ui.quanzi_zone.QuanziZoneActivity;
import com.tizi.quanzi.ui.user_zone.UserZoneActivity;

import java.util.ArrayList;

/**
 * Created by qixingchen on 15/11/16.
 * 显示动态信息
 */
public class DynItem {

    private ImageView weibo_avatar_ImageView;
    private TextView userNameTextView, contentTextView, dateTextView,
            attitudesTextView, commentsTextView;
    private ImageView[] weibo_pics_ImageView = new ImageView[3];
    private LinearLayout weibo_pics_linearLayout;

    private Dyns.DynsEntity dyn;

    private View rootView;
    private boolean showUser;
    private Context mContext;

    public DynItem(Dyns.DynsEntity dyn, View rootView, boolean showUser, Context mContext) {
        this.dyn = dyn;
        this.rootView = rootView;
        this.showUser = showUser;
        this.mContext = mContext;
        init();
    }

    private void init() {
        findViews();
        initViews();
        setViewEvent();
    }

    private void findViews() {
        weibo_avatar_ImageView = (ImageView) rootView.findViewById(R.id.weibo_avatar);
        userNameTextView = (TextView) rootView.findViewById(R.id.weibo_name);
        contentTextView = (TextView) rootView.findViewById(R.id.weibo_content);
        dateTextView = (TextView) rootView.findViewById(R.id.weibo_date);
        attitudesTextView = (TextView) rootView.findViewById(R.id.weibo_attitudes);
        commentsTextView = (TextView) rootView.findViewById(R.id.weibo_comments);
        weibo_pics_ImageView[0] = (ImageView) rootView.findViewById(R.id.weibo_pic0);
        weibo_pics_ImageView[1] = (ImageView) rootView.findViewById(R.id.weibo_pic1);
        weibo_pics_ImageView[2] = (ImageView) rootView.findViewById(R.id.weibo_pic2);
        //        weibo_pics_NetworkImageView[3] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic3);
        //        weibo_pics_NetworkImageView[4] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic4);
        //        weibo_pics_NetworkImageView[5] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic5);
        //        weibo_pics_NetworkImageView[6] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic6);
        //        weibo_pics_NetworkImageView[7] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic7);
        //        weibo_pics_NetworkImageView[8] = (NetworkImageView) rootView.findViewById(R.id.weibo_pic8);
        weibo_pics_linearLayout = (LinearLayout) rootView.findViewById(R.id.weibo_pics);
    }

    private void initViews() {
        /*头像显示和点击*/
        if (showUser) {
            Picasso.with(mContext).load(dyn.icon)
                    .resizeDimen(R.dimen.dyn_user_icon, R.dimen.dyn_user_icon)
                    .into(weibo_avatar_ImageView);
            userNameTextView.setText(dyn.nickName);
        } else {
            Picasso.with(mContext).load(dyn.groupIcon)
                    .resizeDimen(R.dimen.dyn_user_icon, R.dimen.dyn_user_icon)
                    .into(weibo_avatar_ImageView);
            userNameTextView.setText(dyn.groupName);
        }
        contentTextView.setText(dyn.content);
        dateTextView.setText(FriendTime.FriendlyDate(FriendTime.getTimeFromServerString(dyn.createTime)));
        attitudesTextView.setText(String.valueOf(dyn.zan));
        commentsTextView.setText(String.valueOf(dyn.commentNum));
        int picsNum = dyn.pics.size();
        if (picsNum > 3) {
            picsNum = 3;
        }
        for (int i = 0; i < picsNum; i++) {
            String thumUri = dyn.pics.get(i).url;
            thumUri = GetThumbnailsUri.maxHeiAndWei(thumUri,
                    mContext.getResources().getDimensionPixelSize(R.dimen.weibo_pic_hei),
                    mContext.getResources().getDimensionPixelSize(R.dimen.weibo_pic_hei));
            Picasso.with(mContext).load(thumUri)
                    .resizeDimen(R.dimen.weibo_pic_hei, R.dimen.weibo_pic_wei)
                    .into(weibo_pics_ImageView[i]);
            final int finalI = i;
            weibo_pics_ImageView[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StartGalleryActivity.startByStringList(getPicsInfo(), finalI, mContext);
                }
            });
        }
        setPicVisbility(picsNum);
    }

    private void setViewEvent() {
        if (showUser) {
            weibo_avatar_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FindUser.getNewInstance().setNetworkListener(new RetrofitNetworkAbs.NetworkListener() {
                        @Override
                        public void onOK(Object ts) {
                            OtherUserInfo otherUserInfo = (OtherUserInfo) ts;
                            Intent otherUser = new Intent(mContext, UserZoneActivity.class);
                            otherUser.putExtra(StaticField.IntentName.OtherUserInfo, (Parcelable) otherUserInfo);
                            mContext.startActivity(otherUser);
                        }

                        @Override
                        public void onError(String Message) {
                            Toast.makeText(mContext, "此用户已不存在", Toast.LENGTH_LONG).show();
                        }
                    }).findUserByID(dyn.createUser);
                }
            });
        } else {
            weibo_avatar_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, QuanziZoneActivity.class);
                    intent.putExtra("groupID", dyn.senderId);
                    mContext.startActivity(intent);
                }
            });
        }
        for (int i = 0; i < Math.min(3, dyn.pics.size()); i++) {
            final int finalI = i;
            weibo_pics_ImageView[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StartGalleryActivity.startByStringList(getPicsInfo(), finalI, mContext);
                }
            });
        }
    }

    /**
     * 将需要的图片设置为可见
     * 将多余的图片设置成不可见
     * 如果没有图片，则将 weibo_pics_linearLayout 也设置为不可见
     *
     * @param picsNum 图片数量
     */
    private void setPicVisbility(int picsNum) {
        if (picsNum == 0) {
            weibo_pics_linearLayout.setVisibility(View.GONE);
            return;
        }
        for (int i = 0; i < picsNum; i++) {
            weibo_pics_ImageView[i].setVisibility(View.VISIBLE);
        }
        for (int i = picsNum; i < 3; i++) {
            weibo_pics_ImageView[i].setVisibility(View.GONE);
            weibo_pics_ImageView[i].setOnClickListener(null);
        }
    }

    /**
     * 获取图片uri List
     *
     * @return pic uri List
     */
    private ArrayList<String> getPicsInfo() {
        ArrayList<String> pics = new ArrayList<>();
        for (Dyns.DynsEntity.PicsEntity picsEntity : dyn.pics) {
            pics.add(picsEntity.url);
        }
        return pics;
    }
}