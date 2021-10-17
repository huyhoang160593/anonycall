package com.example.anonycall.webRTC

import android.app.Application
import android.util.Log
import com.example.anonycall.models.Answer
import com.example.anonycall.models.Offer
import com.example.anonycall.services.RandomCallService
import com.example.anonycall.utils.Constants.RANDOM_CHAT_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import org.webrtc.*
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import org.webrtc.MediaConstraints

private const val TAG = "RTCChatClient"
class RTCChatClient(
    context: Application,
    observer: PeerConnection.Observer,
) {
    var remoteSessionDescription : SessionDescription? = null

    private val database = FirebaseFirestore.getInstance()

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.1.google.com:19302")
            .createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.1.google.com:19302")
            .createIceServer()
    )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-IntelVP8/Enabled")
//            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }


    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
//                disableEncryption = true -- The cause of our mayhem, without you we finally can create DataChannel
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) : PeerConnection?{
        val rtcConfig = PeerConnection.RTCConfiguration(iceServer)
        // Using rtcConfig instead of regular server to have the null MediaConstraints in contruction
        return peerConnectionFactory.createPeerConnection(
            rtcConfig,
            observer
        )
    }

    private fun PeerConnection.chatCall(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
            optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        val newOffer = Offer(desc!!.description,desc.type)

                        RandomCallService.addOffer(
                            collection = RANDOM_CHAT_COLLECTION,
                            meetingId = meetingID,
                            offer = newOffer
                        )
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailure: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
            }
        }, constraints)
    }

    private fun PeerConnection.chatAnswer(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"))
            optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {

                val newAnswer = Answer(desc!!.description,desc.type)
                    RandomCallService.addAnswer(
                        collection = RANDOM_CHAT_COLLECTION,
                        meetingId = meetingID,
                        answer = newAnswer
                    )
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailureLocal: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailureRemote: $p0")
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, meetingID: String) = peerConnection?.chatCall(sdpObserver, meetingID)

    fun answer(sdpObserver: SdpObserver, meetingID: String) = peerConnection?.chatAnswer(sdpObserver, meetingID)

    fun createDataChannel() = peerConnection?.createDataChannel("message",DataChannel.Init())

    fun sendMessage(dataChannel: DataChannel, msg: String) {
        Log.d(TAG,"message: $msg")
        val buffer: ByteBuffer = ByteBuffer.wrap(msg.toByteArray())
        val sent = dataChannel.send(DataChannel.Buffer(buffer, false))
        Log.d(TAG, "MessageSend -> $sent")
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(TAG, "onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(meetingID: String) {
        database.collection(RANDOM_CHAT_COLLECTION).document(meetingID).collection("candidates")
            .get().addOnSuccessListener {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for ( dataSnapshot in it) {
                    if (dataSnapshot.contains("type") && dataSnapshot["type"]=="offerCandidate") {
                        iceCandidateArray.add(
                            IceCandidate(
                                dataSnapshot["sdpMid"].toString(),
                                Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long),
                                dataSnapshot["sdp"].toString()
                            )
                        )
                    } else if (dataSnapshot.contains("type") && dataSnapshot["type"]=="answerCandidate") {
                        iceCandidateArray.add(
                            IceCandidate(
                                dataSnapshot["sdpMid"].toString(),
                                Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long),
                                dataSnapshot["sdp"].toString()
                            )
                        )
                    }
                }
                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
            }

        RandomCallService.endCall(RANDOM_CHAT_COLLECTION,meetingID)

        peerConnection?.close()
    }
}