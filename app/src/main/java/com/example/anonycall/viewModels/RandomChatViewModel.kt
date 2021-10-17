package com.example.anonycall.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.anonycall.models.MessageEvent
import com.example.anonycall.services.RandomCallService
import com.example.anonycall.utils.Constants
import com.example.anonycall.utils.Constants.RANDOM_CHAT_COLLECTION
import com.example.anonycall.webRTC.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.nio.ByteBuffer

private const val TAG = "RandomChatViewModel"
class RandomChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _meetingId = MutableLiveData("")
    val meetingId: LiveData<String> = _meetingId

    private val _closeFragment = MutableLiveData(false)
    val closeFragment: LiveData<Boolean> = _closeFragment

    private var _isJoin = false

    private lateinit var rtcChatClient: RTCChatClient

    private lateinit var signallingClient: SignalingClientNew

    private val sdpObserver = object : AppSdpObserver(){}

    private lateinit var dataChannel: DataChannel

    private val vmDataChannelObserver: DataChannelObserver

    init {
        getMeetingId()
        vmDataChannelObserver = createDataChannelObserver()
    }

    fun createRTCChatClient() {
        Constants.isCallEnded = false
        rtcChatClient = RTCChatClient(getApplication(),object : PeerConnectionObserver(){
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                Log.e(TAG,"Ice Candidate: $p0")
                signallingClient.sendIceCandidate(p0, _isJoin)
                rtcChatClient.addIceCandidate(p0)
            }

            override fun onDataChannel(p0: DataChannel?) {
                super.onDataChannel(p0)
                Log.e(TAG,"Data channel on Receive: $p0")
                dataChannel = p0!!
                dataChannel.registerObserver(createDataChannelObserver())
            }
        })

        if (_meetingId.value != null){
            dataChannel = rtcChatClient.createDataChannel()!!
            dataChannel.registerObserver(vmDataChannelObserver)
            signallingClient =  SignalingClientNew(_meetingId.value!!,createSignallingClientListener(),RANDOM_CHAT_COLLECTION)
            if (!_isJoin)
                rtcChatClient.call(sdpObserver, _meetingId.value!!)
        }
    }

    fun endCall() {
        if (!Constants.isCallEnded) {
            Constants.isCallEnded = true
            //Make something to end the call
            sendMessage("Thông báo: Người dùng ngắt kết nối!!")

            if(_meetingId.value!!.isNotBlank())
                rtcChatClient.endCall(_meetingId.value!!)
        }
    }
    fun sendMessage(message: String) = rtcChatClient.sendMessage(dataChannel,message)

    private fun getMeetingId() = viewModelScope.launch {
        val result = RandomCallService.getFirstOfferCall(RANDOM_CHAT_COLLECTION)
        if(result == null) {
            _meetingId.value = RandomCallService.createCallId(RANDOM_CHAT_COLLECTION)
        } else {
            _isJoin = true
            _meetingId.value = result
        }
        Log.e(TAG,"finish getMeetingId with ${_meetingId.value} and $_isJoin")
    }

    private fun createDataChannelObserver() = object : DataChannelObserver() {
        override fun onStateChange() {
            super.onStateChange()
            val msg = "Liên kết hai người đã được khởi tạo, hãy thử nhắn tin cho đối phương..."
            rtcChatClient.sendMessage(dataChannel,msg)
        }
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
//            binding.endCallButton.isClickable = true
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcChatClient.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
            if(_meetingId.value!!.isNotBlank())
                rtcChatClient.answer(sdpObserver,_meetingId.value!!)
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcChatClient.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcChatClient.addIceCandidate(iceCandidate)
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true

                //Make something to end the call
                sendMessage("Thông báo: Người dùng ngắt kết nối!!")
                _closeFragment.value = true
                dataChannel.close()
                if(_meetingId.value!!.isNotBlank())
                    rtcChatClient.endCall(_meetingId.value!!)
            }
        }
    }
}