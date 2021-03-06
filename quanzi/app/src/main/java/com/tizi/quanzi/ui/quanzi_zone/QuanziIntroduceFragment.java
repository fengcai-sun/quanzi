package com.tizi.quanzi.ui.quanzi_zone;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tizi.quanzi.R;
import com.tizi.quanzi.adapter.DynsAdapter;
import com.tizi.quanzi.adapter.GroupUserAdapter;
import com.tizi.quanzi.app.AppStaticValue;
import com.tizi.quanzi.dataStatic.BoomGroupList;
import com.tizi.quanzi.dataStatic.GroupList;
import com.tizi.quanzi.gson.AllTags;
import com.tizi.quanzi.gson.Dyns;
import com.tizi.quanzi.gson.GroupAllInfo;
import com.tizi.quanzi.log.Log;
import com.tizi.quanzi.model.BoomGroupClass;
import com.tizi.quanzi.model.GroupClass;
import com.tizi.quanzi.network.DynamicAct;
import com.tizi.quanzi.network.GroupSetting;
import com.tizi.quanzi.network.RetrofitNetworkAbs;
import com.tizi.quanzi.tool.GetThumbnailsUri;
import com.tizi.quanzi.tool.RequreForImage;
import com.tizi.quanzi.tool.StaticField;
import com.tizi.quanzi.tool.Tool;
import com.tizi.quanzi.ui.BaseFragment;
import com.tizi.quanzi.ui.dyns.DynsActivity;
import com.tizi.quanzi.widget.AutoGridfitLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.next.tagview.TagCloudView;

/**
 * A placeholder fragment containing a simple view.
 * 群空间，介绍页
 */
public class QuanziIntroduceFragment extends BaseFragment {

    private ImageView groupFaceImageView, zoneBackgroundImageView;
    private TextView zoneSignTextview;
    private TagCloudView quanziTagView;
    private RecyclerView groupUsersRecyclerView, groupDynsRecyclerView;
    private GroupUserAdapter groupUserAdapter;
    private DynsAdapter dynsAdapter;
    private GroupAllInfo groupAllInfo;
    private boolean hasMoreToGet = true;
    private int lastIndex = 0;

    private View groupHeadView, userView;

    private RequreForImage requreForImage;


    public QuanziIntroduceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quanzi_introduce, container, false);
    }

    @Override
    protected void findViews(View view) {

        groupHeadView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_head, (ViewGroup) view, false);

        zoneBackgroundImageView = (ImageView) groupHeadView.findViewById(R.id.zoneBackground);
        groupFaceImageView = (ImageView) groupHeadView.findViewById(R.id.gruop_face);
        zoneSignTextview = (TextView) groupHeadView.findViewById(R.id.zoneSign);
        quanziTagView = (TagCloudView) groupHeadView.findViewById(R.id.group_tag_view);

        userView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_group_users, (ViewGroup) view, false);
        groupUsersRecyclerView = (RecyclerView) userView.findViewById(R.id.group_users_item_recycler_view);

        groupDynsRecyclerView = (RecyclerView) view.findViewById(R.id.group_dyns_item_recycler_view);


    }

    @Override
    protected void initViewsAndSetEvent() {

        /*用户*/
        groupUserAdapter = new GroupUserAdapter(mActivity, null, false, null, null);

        groupUsersRecyclerView.setAdapter(groupUserAdapter);
        groupUsersRecyclerView.setLayoutManager(new AutoGridfitLayoutManager(mContext, 72));
        groupFaceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requreForImage = RequreForImage.getInstance(mActivity);
                requreForImage.showDialogAndCallIntent("选择圈子照片",
                        StaticField.PermissionRequestCode.QuanziIntroduceFragment_group_face);

            }
        });
        groupFaceImageView.setEnabled(false);
        /*动态*/
        dynsAdapter = new DynsAdapter(null, null, mActivity, false);
        dynsAdapter.setHeadViews(groupHeadView, userView);
        lastIndex = 0;
        dynsAdapter.setNeedMore(new DynsAdapter.NeedMore() {
            @Override
            public void needMore() {
                if (hasMoreToGet) {
                    quaryMore(groupAllInfo.group.id, lastIndex);
                    lastIndex += StaticField.Limit.DynamicLimit;
                }
            }
        });
        groupDynsRecyclerView.setAdapter(dynsAdapter);
        groupDynsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        groupDynsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        showGroupInfo();
    }

    /**
     * 设置群的信息
     */
    public void setGroupInfo(GroupAllInfo groupAllInfo) {
        this.groupAllInfo = groupAllInfo;

        boolean isCreate = groupAllInfo.group.createUser.compareTo(AppStaticValue.getUserID()) == 0;
        if (groupUserAdapter != null) {
            groupUserAdapter.setCreaterID(groupAllInfo.group.createUser);
            groupUserAdapter.setMemlist(groupAllInfo.memlist);
            groupUserAdapter.setIsCreater(isCreate);
            groupUserAdapter.setGroupID(groupAllInfo.group.id);
        }
        for (GroupAllInfo.MemberEntity member : groupAllInfo.memlist) {
            if (member.id.compareTo(AppStaticValue.getUserID()) == 0) {
                addSettingMenu();
                groupUserAdapter.setIsMember(true);
                break;
            }
        }
        showGroupInfo();
    }

    private void showGroupInfo() {
        if (groupAllInfo != null) {

            /*判断是否可以显示用户*/
            boolean showUsers = false;
            boolean isMember = false;

            if (GroupList.getInstance().getGroup(groupAllInfo.group.id) != null) {
                showUsers = true;
                isMember = true;
                ((GroupClass) (GroupList.getInstance().getGroup(groupAllInfo.group.id))).memlist = groupAllInfo.memlist;
            }
            if (isMember) {
                groupFaceImageView.setEnabled(true);
            }
            if (!showUsers) {
                for (BoomGroupClass boomGroupClass : BoomGroupList.getInstance().getGroupList()) {
                    if (boomGroupClass.groupId1.equals(groupAllInfo.group.id)) {
                        showUsers = true;
                        boomGroupClass.groupMenber1 = groupAllInfo.memlist;
                        break;
                    }

                    if (boomGroupClass.groupId2.equals(groupAllInfo.group.id)) {
                        showUsers = true;
                        boomGroupClass.groupMenber2 = groupAllInfo.memlist;
                        break;
                    }
                }
            }
            dynsAdapter.setShowUser(showUsers);
            final boolean finalShowUsers = showUsers;
            dynsAdapter.setOnclick(new DynsAdapter.Onclick() {
                @Override
                public void click(Dyns.DynsEntity dyn) {
                    Intent dynIntent = new Intent(mContext, DynsActivity.class);
                    dynIntent.putExtra("dynID", dyn.dynid);
                    dynIntent.putExtra("isUser", false);
                    dynIntent.putExtra("showUser", finalShowUsers);
                    mActivity.startActivityForResult(dynIntent, StaticField.PermissionRequestCode.GroupDynInfo_QuestCode);
                }
            });

            /*用户*/
            if (showUsers) {
                groupUsersRecyclerView.setVisibility(View.VISIBLE);
            } else {
                groupUsersRecyclerView.setVisibility(View.GONE);
            }

            //noinspection ConstantConditions
            ((AppCompatActivity) (mActivity)).getSupportActionBar().setTitle(groupAllInfo.group.groupName);
            Picasso.with(mActivity).load(groupAllInfo.group.icon)
                    .fit()
                    .into(groupFaceImageView);

            Picasso.with(mActivity).load(groupAllInfo.group.icon)
                    .fit()
                    .into(zoneBackgroundImageView);

            if (groupAllInfo.group.notice != null) {
                zoneSignTextview.setText(String.format("公告：%s", groupAllInfo.group.notice));
            }
            List<String> tagsString = new ArrayList<>();
            for (AllTags.TagsEntity tag : groupAllInfo.tagList) {
                tagsString.add(tag.tagName);
            }
            quanziTagView.setTags(tagsString);
            quaryMore(groupAllInfo.group.id, lastIndex);
            lastIndex += StaticField.Limit.DynamicLimit;
        }
        if (groupAllInfo != null && groupUsersRecyclerView.getVisibility() == View.VISIBLE) {
            int size = Math.min(groupAllInfo.memlist.size() + 1, 10);
            int wei = (int) (mContext.getResources().getDisplayMetrics().widthPixels
                    - 8 * GetThumbnailsUri.getDpi(mActivity) * 2);
            int hei = (int) (76 * GetThumbnailsUri.getDpi(mActivity)
                    * (((size - 1) / ((Tool.getSrceenWidthDP() - 16) / 72)) + 1));
            groupUsersRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(wei, hei));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (groupAllInfo != null) {
            for (GroupAllInfo.MemberEntity member : groupAllInfo.memlist) {
                if (member.id.compareTo(AppStaticValue.getUserID()) == 0) {
                    addSettingMenu();
                    groupUserAdapter.setIsMember(true);
                    break;
                }
            }
        }
        ((QuanziZoneActivity) mActivity).callForUpdateGroupInfo();
    }

    private void quaryMore(String groupID, int lastIndex) {
        Log.i(TAG, "查询群动态 lastIndex=" + lastIndex);
        DynamicAct.getNewInstance(false).setNetworkListener(new RetrofitNetworkAbs.NetworkListener() {
            @Override
            public void onOK(Object ts) {
                Dyns dyns = (Dyns) ts;
                dynsAdapter.addItems(dyns.dyns);
                if (dyns.dyns.size() != StaticField.Limit.DynamicLimit) {
                    hasMoreToGet = false;
                }
            }

            @Override
            public void onError(String Message) {

            }
        }).getDynamic(true, groupID, lastIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case StaticField.PermissionRequestCode.QuanziIntroduceFragment_group_face:
                if (resultCode != Activity.RESULT_OK) {
                    requreForImage = null;
                    return;
                }
                String filepath = requreForImage.getFilePathFromIntentMaybeCamera(data);
                requreForImage.startPhotoCrop(Uri.fromFile(new File(filepath)), 1, 1,
                        StaticField.PermissionRequestCode.QuanziIntroduceFragment_group_face_crop);
                break;
            case StaticField.PermissionRequestCode.QuanziIntroduceFragment_group_face_crop:
                if (resultCode != Activity.RESULT_OK) {
                    requreForImage = null;
                    return;
                }
                savePhotoToLCAndSetGroupImage(requreForImage.getCropImage().getPath());
                requreForImage = null;
                break;
            case StaticField.PermissionRequestCode.GroupDynInfo_QuestCode:
                if (resultCode == Activity.RESULT_OK) {
                    String dynID = data.getStringExtra("dynID");
                    dynsAdapter.deleteItem(dynID);
                }
                break;
        }

    }

    /**
     * 将图片储存到LeanCloud
     *
     * @param filepath 图片地址
     */
    private void savePhotoToLCAndSetGroupImage(String filepath) {
        AVFile file = null;
        try {
            file = AVFile.withAbsoluteLocalPath(AppStaticValue.getUserID() + "face.jpg",
                    filepath);
            final AVFile finalFile = file;
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e != null) {
                        //上传失败
                    } else {
                        String photoUri = finalFile.getUrl();

                        Picasso.with(mContext).load(photoUri)
                                .resizeDimen(R.dimen.group_introduce_face_size, R.dimen.group_introduce_face_size)
                                .into(groupFaceImageView);

                        Picasso.with(mContext).load(photoUri)
                                .resize(GetThumbnailsUri.getPXs(mActivity, 360),
                                        GetThumbnailsUri.getPXs(mActivity, 280))
                                .into(zoneBackgroundImageView);
                        //通知后台更改
                        GroupSetting.getNewInstance().changeIcon(groupAllInfo.group.id, photoUri);
                        //本地群列表更改
                        GroupClass groupClass = (GroupClass) GroupList.getInstance().getGroup(groupAllInfo.group.id);
                        if (groupClass != null) {
                            groupClass.setFace(photoUri);
                            GroupList.getInstance().updateGroup(groupClass);
                        }

                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addSettingMenu() {
        menuAdded(R.id.action_group_settings, "圈子设置", MenuItem.SHOW_AS_ACTION_IF_ROOM, R.drawable.ic_group_settings);

    }


}
