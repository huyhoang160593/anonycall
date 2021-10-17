package com.example.anonycall.webRTC

import android.util.Log
import com.example.anonycall.models.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.webrtc.DataChannel
import java.nio.ByteBuffer

private const val TAG = "DataChannelObserver"
open class DataChannelObserver : DataChannel.Observer {

    override fun onBufferedAmountChange(p0: Long) {}

    override fun onStateChange() {
        Log.e(TAG,"State has change")
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        if(p0 != null){
            val data: ByteBuffer = p0.data
            val bytes = ByteArray(data.remaining())
            data.get(bytes)
            //Message Answer
            val message = String(bytes)
            EventBus.getDefault().post(MessageEvent(message))
        }
    }
}