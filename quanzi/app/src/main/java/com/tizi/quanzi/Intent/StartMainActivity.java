package com.tizi.quanzi.Intent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.tizi.quanzi.gson.Login;
import com.tizi.quanzi.model.GroupClass;
import com.tizi.quanzi.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qixingchen on 15/7/21.
 */
public class StartMainActivity {

    public static void startByLoginGroup(List<Login.GroupEntity> groupEntityList, Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        //todo use getGroupByEntity

        Bundle bundle = new Bundle();

        ArrayList<GroupClass> groupClassArrayList = new ArrayList<>();
        for (Login.GroupEntity groupEntity : groupEntityList) {
            groupClassArrayList.add(getGroupClassFromLoginGroup(groupEntity));
        }


        bundle.putParcelableArrayList("group", groupClassArrayList);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

//        int length = groupEntityList.size();
//
//        String[] ids = new String[length];
//        String[] icons = new String[length];
//        String[] groupNames = new String[length];
//        String[] types = new String[length];
//
//        for (int i = 0; i < length; i++) {
//            ids[i] = groupEntityList.get(i).getId();
//            icons[i] = groupEntityList.get(i).getIcon();
//            groupNames[i] = groupEntityList.get(i).getGroupName();
//            types[i] = groupEntityList.get(i).getType();
//        }
//        intent.putExtra("groupids", ids);
//        intent.putExtra("groupicons", icons);
//        intent.putExtra("groupgroupNames", groupNames);
//        intent.putExtra("grouptypes", types);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
    }

    private static GroupClass getGroupClassFromLoginGroup(Login.GroupEntity groupEntity) {
        GroupClass groupClass = new GroupClass();
        groupClass.groupFace = Uri.parse(groupEntity.getIcon());
        groupClass.groupID = groupEntity.getId();
        groupClass.groupName = groupEntity.getGroupName();
        groupClass.groupType = groupEntity.getType();
        return  groupClass;
    }

}