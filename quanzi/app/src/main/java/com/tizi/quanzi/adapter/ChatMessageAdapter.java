package com.tizi.quanzi.adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tizi.chatlibrary.action.MessageManage;
import com.tizi.chatlibrary.model.message.ChatMessage;
import com.tizi.chatlibrary.model.message.ImageChatMessage;
import com.tizi.chatlibrary.model.message.VoiceChatMessage;
import com.tizi.quanzi.Intent.StartGalleryActivity;
import com.tizi.quanzi.R;
import com.tizi.quanzi.chat.VoicePlayAsync;
import com.tizi.quanzi.database.DBAct;
import com.tizi.quanzi.gson.OtherUserInfo;
import com.tizi.quanzi.network.FindUser;
import com.tizi.quanzi.network.RetrofitNetworkAbs;
import com.tizi.quanzi.tool.FriendTime;
import com.tizi.quanzi.tool.GetThumbnailsUri;
import com.tizi.quanzi.tool.StaticField;
import com.tizi.quanzi.tool.Tool;
import com.tizi.quanzi.ui.user_zone.UserZoneActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by qixingchen on 15/7/20.
 * 聊天记录Adapter
 */

public class ChatMessageAdapter extends RecyclerViewAdapterAbs {

    public SortedList<ChatMessage> chatMessageList;
    private Context mContext;
    private VoicePlayAsync voicePlayAsync;
    private OnResend onResend;

    /**
     * @param chatMessageList 聊天记录List
     * @param context         上下文
     *
     * @see ChatMessage
     */
    public ChatMessageAdapter(List<ChatMessage> chatMessageList, Context context) {
        this.chatMessageList = new SortedList<>(ChatMessage.class, new SortedList.Callback<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return (int) (o1.getCreateTime() - o2.getCreateTime());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(ChatMessage oldItem, ChatMessage newItem) {
                return oldItem.getStatus() == newItem.getStatus() && oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(ChatMessage item1, ChatMessage item2) {
                return item1.getMessID().compareTo(item2.getMessID()) == 0;
            }
        });
        if (chatMessageList != null) {
            this.chatMessageList.beginBatchedUpdates();
            this.chatMessageList.addAll(chatMessageList);
            this.chatMessageList.endBatchedUpdates();
        }
        this.mContext = context;
    }

    /**
     * @param position 记录位置
     *
     * @return 记录类型
     *
     * @see com.tizi.quanzi.tool.StaticField.ChatFrom
     */
    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).getMessageType() == ChatMessage.MESSAGE_TYPE_NOTIFI) {
            return StaticField.ChatFrom.NOTIFI;
        }
        return chatMessageList.get(position).getFrom();
    }

    /**
     * @param parent   需要创建ViewHolder的 ViewGroup
     * @param viewType 记录类型
     *
     * @return ViewHolder
     *
     * @see com.tizi.quanzi.tool.StaticField.ChatFrom
     * @see ChatMessAbsViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        RecyclerView.ViewHolder vh;
        switch (viewType) {
            case StaticField.ChatFrom.OTHER:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_ohter_chat, parent, false);
                vh = new OtherMessViewHolder(v);
                break;
            case StaticField.ChatFrom.ME:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_my_chat, parent, false);
                vh = new MyMessViewHolder(v);
                break;
            case StaticField.ChatFrom.GROUP:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_group_friend_chat, parent, false);
                vh = new OtherMessViewHolder(v);
                break;
            case StaticField.ChatFrom.NOTIFI:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_notifi, parent, false);
                vh = new NotifiViewHolder(v);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_ohter_chat, parent, false);
                vh = new OtherMessViewHolder(v);
        }

        return vh;
    }

    /**
     * 发生绑定时，为viewHolder的元素赋值
     *
     * @param holder   被绑定的ViewHolder
     * @param position 列表位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (ChatMessAbsViewHolder.class.isInstance(holder)) {
            onBindChatMessViewHolder((ChatMessAbsViewHolder) holder, position);
        }
        if (NotifiViewHolder.class.isInstance(holder)) {
            onBindNotifiViewHolder((NotifiViewHolder) holder, position);
        }

        /*设置已读*/
        ChatMessage chatMessage = chatMessageList.get(position);
        if (!chatMessage.isRead()) {
            MessageManage.setMessageRead(chatMessage, true);
        }
        if (position == chatMessageList.size() - 1) {
            MessageManage.setAllMessageASRead(chatMessage.getConversationId());
        }
    }

    private void onBindNotifiViewHolder(NotifiViewHolder holder, int position) {
        holder.notifiTextView.setText(chatMessageList.get(position).getChatText());
        String time = FriendTime.FriendlyDate(chatMessageList.get(position).getCreateTime());
        holder.chatTime.setText(time);
    }

    /**
     * 发生绑定时，为viewHolder的元素赋值
     *
     * @param holder   被绑定的ViewHolder
     * @param position 列表位置
     */
    private void onBindChatMessViewHolder(final ChatMessAbsViewHolder holder, final int position) {
        ChatMessage tempchatMessage = chatMessageList.get(position);
        final ChatMessage chatMessage = tempchatMessage;

        holder.setAllAdditionVisibilityGone();

        /*长按事件*/
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String[] items;
                switch (chatMessage.getMessageType()) {
                    case StaticField.ChatContantType.TEXT:
                        items = new String[]{"删除", "复制"};
                        break;
                    default:
                        items = new String[]{"删除"};
                }
                new AlertDialog.Builder(mContext)
                        .setItems(items, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                MessageManage.deleteMessage(chatMessage);
                                                chatMessageList.remove(chatMessage);
                                                break;
                                            case 1:

                                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", chatMessage.getChatText());
                                                clipboard.setPrimaryClip(clip);
                                                break;
                                        }
                                    }
                                }

                        ).show();
                return false;
            }
        });

        if (!TextUtils.isEmpty(chatMessage.getChatText())) {
            holder.chatMessTextView.setText(chatMessage.getChatText());
            holder.chatMessTextView.setVisibility(View.VISIBLE);
        }
        /*根据不同消息类型确定元素是否显示*/
        switch (chatMessage.getMessageType()) {
            case ChatMessage.MESSAGE_TYPE_IMAGE:
                final ImageChatMessage imageChatMessage = (ImageChatMessage) chatMessage;
                int[] imagePix = Tool.getImagePixel(mContext,
                        imageChatMessage.getImageHeight(), imageChatMessage.getImageWeight());
                holder.contantImageView.getLayoutParams().height = imagePix[0];
                holder.contantImageView.getLayoutParams().width = imagePix[1];
                if (TextUtils.isEmpty(imageChatMessage.getLocalPath()) ||
                        !new File(imageChatMessage.getLocalPath()).exists()) {
                    Picasso.with(mContext)
                            .load(GetThumbnailsUri.getUriLink(imageChatMessage.getImageUrl(), imagePix[0], imagePix[1]))
                            .placeholder(R.drawable.ic_photo_loading)
                            .resize(imagePix[1], imagePix[0])
                            .into(holder.contantImageView);
                } else {
                    Picasso.with(mContext)
                            .load("file://" + imageChatMessage.getLocalPath())
                            .placeholder(R.drawable.ic_photo_loading)
                            .resize(imagePix[1], imagePix[0])
                            .into(holder.contantImageView);
                }

                holder.contantImageView.setVisibility(View.VISIBLE);
                holder.contantImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> image = new ArrayList<>(
                                DBAct.getInstance().quaryPhotoMess(chatMessage.getConversationId()));
                        int index = image.indexOf(imageChatMessage.getImageUrl());
                        if (index == -1) {
                            index = image.indexOf("file://" + imageChatMessage.getLocalPath());
                        }
                        StartGalleryActivity.startByStringList(image, index, mContext);
                    }
                });
                break;
            case ChatMessage.MESSAGE_TYPE_VOICE:
                holder.videoPlayButton.setVisibility(View.VISIBLE);
                holder.audioProgressBar.setVisibility(View.VISIBLE);
                holder.voiceDuration.setVisibility(View.VISIBLE);
                if (((VoiceChatMessage) chatMessage).getVoiceDuration() == 0) {
                    holder.voiceDuration.setVisibility(View.GONE);
                }
                holder.voiceDuration.setText(String.format(Locale.getDefault(), "%d s", (int)
                        ((VoiceChatMessage) chatMessage).getVoiceDuration() + 1));
                break;
            case ChatMessage.MESSAGE_TYPE_VEDIO:
                holder.videoPlayButton.setVisibility(View.VISIBLE);
                holder.audioProgressBar.setVisibility(View.VISIBLE);
                holder.voiceDuration.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        /*设置头像*/
        Picasso.with(mContext).load(GetThumbnailsUri.maxDPHeiAndWei(
                chatMessage.getSenderIcon(), 48, 48, mContext)).fit().into(holder.userFaceImageView);

        String time = FriendTime.FriendlyDate(chatMessage.getCreateTime());
        holder.chatTime.setText(time);
        if (holder.chatUserName != null) {
            holder.chatUserName.setText(chatMessage.getSenderName());
        }
        if (!chatMessage.isRead()) {
            MessageManage.setMessageRead(chatMessage, true);
        }

        /*播放按钮*/
        holder.videoPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voicePlayAsync == null) {
                    voicePlayAsync = new VoicePlayAsync();
                } else {
                    voicePlayAsync.holder.audioProgressBar.setProgress(0);
                    voicePlayAsync.cancel(true);
                    voicePlayAsync = new VoicePlayAsync();
                }

                voicePlayAsync.setContext(mContext)
                        .setChatMessage((VoiceChatMessage) chatMessage)
                        .setHolder(holder)
                        .execute(0);
            }
        });

        /*点击用户头像进入个人空间*/
        holder.userFaceImageView.setOnClickListener(new View.OnClickListener() {
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
                }).findUserByID(chatMessage.getSenderID());
            }
        });

        /*自己发送的*/
        if (holder instanceof MyMessViewHolder) {
            ((MyMessViewHolder) holder).reSendButton.setVisibility(View.GONE);
            ((MyMessViewHolder) holder).progressBar.setVisibility(View.GONE);
            if (chatMessage.getStatus() == ChatMessage.STATUS_SENDING) {
                ((MyMessViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            } else if (chatMessage.getStatus() == ChatMessage.STATUS_FAILED) {
                ((MyMessViewHolder) holder).reSendButton.setVisibility(View.VISIBLE);
                ((MyMessViewHolder) holder).reSendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onResend != null) {
                            if (onResend.onResend(chatMessage)) {
                                chatMessageList.remove(chatMessage);
                                MessageManage.deleteMessage(chatMessage);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * @return 聊天记录数
     */
    @Override
    public int getItemCount() {
        return chatMessageList == null ? 0 : chatMessageList.size();
    }

    /**
     * 新增或更新消息
     * 将按照 create_time 插入正确的位置
     *
     * @param chatMessage 需要新增或更新的消息
     */
    public void addOrUpdateMessage(ChatMessage chatMessage) {
        chatMessageList.add(chatMessage);
    }

    /**
     * 新增或更新一堆消息
     * 将按照 create_time 插入正确的位置
     *
     * @param chatMessages 需要新增或更新的一堆消息
     */
    public void addOrUpdateMessages(List<ChatMessage> chatMessages) {
        if (chatMessages == null) {
            return;
        }
        chatMessageList.beginBatchedUpdates();
        chatMessageList.addAll(chatMessages);
        chatMessageList.endBatchedUpdates();
    }

    public void updateTempMess(String messID, ChatMessage chatMessage) {
        for (int i = chatMessageList.size() - 1; i >= 0; i--) {
            if (chatMessageList.get(i).getMessID().equals(messID)) {
                chatMessageList.updateItemAt(i, chatMessage);
                break;
            }
        }
    }

    /**
     * @return 返回最后未读的位置
     */
    public int lastReadPosition() {
        int length = chatMessageList.size();
        int position = 0;
        for (int i = length - 1; i >= 0; i--) {
            if (chatMessageList.get(i).isRead()) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void setOnResend(OnResend onResend) {
        this.onResend = onResend;
    }

    public interface OnResend {
        boolean onResend(ChatMessage chatMessage);
    }

    /**
     * 实现BaseViewHolder
     */
    public static class OtherMessViewHolder extends ChatMessAbsViewHolder {
        public OtherMessViewHolder(View v) {
            super(v);
            findViewByID(v, StaticField.ChatFrom.OTHER);
        }
    }

    /**
     * 实现BaseViewHolder
     */
    public static class MyMessViewHolder extends ChatMessAbsViewHolder {
        private ProgressBar progressBar;
        private ImageButton reSendButton;

        public MyMessViewHolder(View v) {
            super(v);
            findViewByID(v, StaticField.ChatFrom.ME);
            progressBar = (ProgressBar) v.findViewById(R.id.sending_progressbar);
            reSendButton = (ImageButton) v.findViewById(R.id.resend_button);
        }
    }

    /**
     * 消息通知
     */
    public static class NotifiViewHolder extends RecyclerView.ViewHolder {

        private TextView notifiTextView, chatTime;

        public NotifiViewHolder(View itemView) {
            super(itemView);
            notifiTextView = (TextView) itemView.findViewById(R.id.notifi_text_view);
            chatTime = (TextView) itemView.findViewById(R.id.chat_message_time);
        }
    }
}
