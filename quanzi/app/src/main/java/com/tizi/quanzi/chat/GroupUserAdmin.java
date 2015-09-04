package com.tizi.quanzi.chat;

import android.content.Context;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.tizi.quanzi.app.App;
import com.tizi.quanzi.dataStatic.GroupList;
import com.tizi.quanzi.dataStatic.MyUserInfo;
import com.tizi.quanzi.gson.GroupUserInfo;
import com.tizi.quanzi.log.Log;
import com.tizi.quanzi.model.GroupClass;
import com.tizi.quanzi.network.UserManageInGroup;
import com.tizi.quanzi.tool.StaticField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qixingchen on 15/9/3.
 * 管理群用户
 */
public class GroupUserAdmin {
    private Context mContext;
    private OnResult onResult;
    private final static String TAG = GroupUserAdmin.class.getSimpleName();

    public static GroupUserAdmin getInstance(Context context) {
        return new GroupUserAdmin(context);
    }

    private GroupUserAdmin(Context context) {
        this.mContext = context;
    }

    public GroupUserAdmin setOnResult(OnResult onResult) {
        this.onResult = onResult;
        return this;
    }

    public void addMember(String convID, final String groupID, String userID) {
        Map<String, Object> attr = SendMessage.setMessAttr(groupID, StaticField.ChatBothUserType.twoPerson);
        attr = SendMessage.setGroupManageSysMessAttr(attr, convID,
                StaticField.SystemMessAttrName.systemFlag.invitation, "");

        List<String> clientIds = new ArrayList<>();
        clientIds.add(App.getUserID());
        clientIds.add(userID);

        Map<String, Object> chatAttr = new HashMap<String, Object>();
        attr.put("type", StaticField.ChatBothUserType.twoPerson);
        final Map<String, Object> finalAttr = attr;
        App.getImClient().createConversation(clientIds, chatAttr, new AVIMConversationCreatedCallback() {
            @Override
            public void done(AVIMConversation avimConversation, AVException e) {
                if (e != null) {
                    if (onResult != null) {
                        onResult.error(e.getMessage());
                    }
                    return;
                }
                SendMessage.getInstance().setSendOK(
                        new SendMessage.SendOK() {
                            @Override
                            public void sendOK(AVIMTypedMessage Message, String CONVERSATION_ID) {
                                if (onResult != null) {
                                    onResult.OK();
                                }
                            }

                            @Override
                            public void sendError(String errorMessage, String CONVERSATION_ID) {
                                if (onResult != null) {
                                    onResult.error(errorMessage);
                                }
                            }
                        }
                ).sendTextMessage(avimConversation.getConversationId(),
                        MyUserInfo.getInstance().getUserInfo().getUserName() + "邀请你来加入圈子" +
                                GroupList.getInstance().getGroup(groupID).groupName
                        , finalAttr);
            }
        });


    }

    public void deleteMember(String convID, String groupID, String userID) {
        //发送系统通知
        Map<String, Object> attr = SendMessage.setMessAttr(groupID, StaticField.ChatBothUserType.twoPerson);
        attr = SendMessage.setGroupManageSysMessAttr(attr, convID,
                StaticField.SystemMessAttrName.systemFlag.kicked, "");
        SendMessage.getInstance().setSendOK(
                new SendMessage.SendOK() {
                    @Override
                    public void sendOK(AVIMTypedMessage Message, String CONVERSATION_ID) {
                        Log.i(TAG, "已发送删除通知");
                    }

                    @Override
                    public void sendError(String errorMessage, String CONVERSATION_ID) {
                        Toast.makeText(mContext, "发送删除通知失败", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "发送删除通知失败" + errorMessage);
                    }
                }
        ).sendTextMessage(convID, "你被踢出了OAQ", attr);

        //LeanCloud删除
        List<String> userIds = new ArrayList<>();
        userIds.add(userID);
        AVIMConversation conversation = App.getImClient().getConversation(convID);
        conversation.kickMembers(userIds, new AVIMConversationCallback() {
            @Override
            public void done(AVException e) {
                if (e != null) {
                    Toast.makeText(mContext, "删除失败", Toast.LENGTH_LONG).show();
                } else {
                    Log.w(TAG, "LC删除好友成功");
                }
            }
        });


        //后台删除
        UserManageInGroup.getInstance().setManageGroupListener(
                new UserManageInGroup.ManageGroupListener() {
                    @Override
                    public void onOK(GroupUserInfo groupUserInfo) {
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(mContext, "删除失败", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "后台删除好友成功");
                    }

                }).deleteUser(groupID, userID);
    }

    public void deleteGroup(String convID, String groupID) {

    }

    public void acceptToJoinGroup(boolean isAccept, final String convID, final String groupID) {
        if (!isAccept) {
            return;
        }
        UserManageInGroup.getInstance().setManageGroupListener(
                new UserManageInGroup.ManageGroupListener() {
                    @Override
                    public void onOK(GroupUserInfo groupUserInfo) {
                        GroupClass groupClass = GroupClass.getGroupByGroupUserInfo(groupUserInfo, groupID, convID);
                        GroupList.getInstance().addGroup(groupClass);
                        List<String> clientIds = new ArrayList<>();
                        clientIds.add(App.getUserID());
                        App.getImClient().getConversation(convID).addMembers(clientIds, new AVIMConversationCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e != null) {
                                    Log.w(TAG, "LC addMembers err:" + e.getMessage());
                                    if (onResult != null) {
                                        onResult.error("LC addMembers err:" + e.getMessage());
                                    }
                                } else {
                                    if (onResult != null) {
                                        onResult.OK();
                                    }
                                }
                            }
                        });

                    }

                    @Override
                    public void onError() {
                        if (onResult != null) {
                            onResult.error("");
                        }
                    }
                }
        ).acceptJoinGroup(groupID, App.getUserID());

    }

    public interface OnResult {
        void OK();

        void error(String errorMessage);
    }

}