package com.yibaiqi.face.recognition.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * Created by @author xiaofu on 2019/3/5.
 */
public class RongViewModel extends ViewModel {

    private MutableLiveData<Boolean> connectStatus = new MutableLiveData<>();
    private MutableLiveData<Message> rongMessage = new MutableLiveData<>();

    public MutableLiveData<Message> getRongMessage() {
        return rongMessage;
    }

    public MutableLiveData<Boolean> getConnectStatus() {
        return connectStatus;
    }

    /**
     * 融云连接方法
     */
    public void connect(String token) {

        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {

            /**
             * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             *                            2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            @Override
            public void onTokenIncorrect() {
                System.out.println("--->>>>>>onTokenIncorrect");

            }

            /**
             * 连接融云成功
             * @param uid 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String uid) {
                System.out.println("--->>>>>>uid=" + uid);
                connectStatus.setValue(true);
            }

            /**
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                System.out.println("--->>>>>>onError:" + errorCode);

            }
        });
    }

    /**
     * 融云消息监听
     * 如果Activity被释放回收无法收到回调事件
     *
     * @param message 收到的消息实体
     * @param left    剩余未拉取消息数目
     */
//    public void registerMessage() {
//        RongIMClient.setOnReceiveMessageListener((message, left) -> {
//            rongMessage.setValue(message);
//            System.out.println("--->>>>>>消息：" + message);
//            System.out.println("--->>>>>>未拉取：" + left);
//            return false;
//        });
//    }

    public void sendTestMsg() {
        TextMessage myTextMessage = TextMessage.obtain("我是消息内容");
        Message myMessage = Message.obtain("1", Conversation.ConversationType.PRIVATE, myTextMessage);
        RongIMClient.getInstance().sendMessage(myMessage, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {
                //消息本地数据库存储成功的回调
                System.out.println("--->>>>>>消息本地数据库存储成功的回调");
            }

            @Override
            public void onSuccess(Message message) {
                //消息通过网络发送成功的回调
                System.out.println("--->>>>>>消息通过网络发送成功的回调");
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                //消息发送失败的回调
                System.out.println("--->>>>>>消息发送失败的回调："+errorCode);
            }
        });
    }
}
