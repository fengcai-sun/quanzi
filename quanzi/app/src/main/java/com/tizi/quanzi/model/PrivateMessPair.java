package com.tizi.quanzi.model;

import com.tizi.quanzi.dataStatic.ConvGroupAbs;
import com.tizi.quanzi.gson.OtherUserInfo;
import com.tizi.quanzi.tool.StaticField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qixingchen on 15/9/3.
 * 私信对信息
 */
public class PrivateMessPair extends ConvGroupAbs implements Serializable {
    //如果是系统消息
    public SystemMessage systemMessage;

    /**
     * 从List<SystemMessage> 转换 List<PrivateMessPair>
     */
    public static List<PrivateMessPair> PriMessesFromSystemMesses(List<SystemMessage> systemMessages) {
        List<PrivateMessPair> privateMessPairs = new ArrayList<>();
        for (SystemMessage systemMessage : systemMessages) {
            privateMessPairs.add(PriMessFromSystemMess(systemMessage));
        }
        return privateMessPairs;
    }

    /**
     * 从SystemMessage 转换 PrivateMessPair
     */
    public static PrivateMessPair PriMessFromSystemMess(SystemMessage systemMessage) {
        PrivateMessPair privateMessPair = new PrivateMessPair();
        privateMessPair.Type = StaticField.PrivateMessOrSysMess.SysMess;
        privateMessPair.Name = systemMessage.getUser_name();
        privateMessPair.Face = systemMessage.getUser_icon();
        privateMessPair.ID = systemMessage.getId();
        privateMessPair.convId = systemMessage.getConvid();
        privateMessPair.systemMessage = systemMessage;
        privateMessPair.lastMess = systemMessage.getContent();
        privateMessPair.lastMessTime = systemMessage.getCreate_time();
        if (systemMessage.getStatus() == StaticField.SystemMessAttrName.statueCode.complete) {
            systemMessage.setIsread(true);
        }
        privateMessPair.UnreadCount = (systemMessage.isread()) ? 0 : 1;
        return privateMessPair;
    }

    public static PrivateMessPair newPrivatePair(ChatMessage chatMessage) {
        PrivateMessPair privateMessPair = new PrivateMessPair();
        privateMessPair.Name = chatMessage.userName;
        privateMessPair.Face = chatMessage.chatImage;
        privateMessPair.ID = chatMessage.sender;
        privateMessPair.Type = StaticField.PrivateMessOrSysMess.PrivateMess;
        privateMessPair.convId = chatMessage.ConversationId;
        privateMessPair.UnreadCount = 1;
        privateMessPair.lastMess = ChatMessage.getContentText(chatMessage);
        privateMessPair.lastMessTime = chatMessage.create_time;

        return privateMessPair;
    }

    /**
     * 新建私信列表
     *
     * @param otherUserInfo 对方信息
     * @param convID        convID
     *
     * @return PrivateMessPair
     */
    public static PrivateMessPair newPrivatePair(OtherUserInfo otherUserInfo, String convID) {
        PrivateMessPair privateMessPair = new PrivateMessPair();
        privateMessPair.Name = otherUserInfo.userName;
        privateMessPair.Face = otherUserInfo.icon;
        privateMessPair.ID = otherUserInfo.id;
        privateMessPair.Type = StaticField.PrivateMessOrSysMess.PrivateMess;
        privateMessPair.convId = convID;
        privateMessPair.UnreadCount = 0;
        privateMessPair.lastMess = "";
        privateMessPair.lastMessTime = 0;

        return privateMessPair;
    }

}
