package com.example.anonycall.webRTC

import android.util.Log
import org.webrtc.DataChannel

private const val TAG = "DataChannelObserver"
open class DataChannelObserver : DataChannel.Observer {
    override fun onBufferedAmountChange(p0: Long) {}

    override fun onStateChange() {
        Log.e(TAG,"State has change")
    }

    override fun onMessage(p0: DataChannel.Buffer?) {}
}