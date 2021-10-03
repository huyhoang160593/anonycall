package com.example.anonycall.webRTC

import android.util.Log
import com.example.anonycall.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlin.coroutines.CoroutineContext

private const val TAG = "SignallingClient"

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class SignalingClient(
    private val meetingId: String,
    private val listener: SignalingClientListener
): CoroutineScope{

    private val job = Job()
    private val db = Firebase.firestore
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    var SDPType : String? = null

    @ObsoleteCoroutinesApi
    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    private fun connect() = launch {
        db.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }
//        val sendData = sendChannel.send("")
        try {
            db.collection("calls")
                .document(meetingId)
                .addSnapshotListener { snapshot, error ->
                    if(error != null){
                        Log.w(TAG, "listen:error", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        if (data?.containsKey("type")!!){
                            when(data.getValue("type").toString()){
                                "OFFER" -> {
                                   listener.onOfferReceived(SessionDescription(
                                       SessionDescription.Type.OFFER,data["sdp"].toString()
                                   ))
                                   SDPType = "Offer"
                                }
                                "ANSWER" -> {
                                    listener.onAnswerReceived(SessionDescription(
                                        SessionDescription.Type.ANSWER,data["sdp"].toString()
                                    ))
                                    SDPType = "Answer"
                                }
                                "END_CALL" -> {
                                    if(!Constants.isInitiatedNow){
                                        listener.onCallEnded()
                                        SDPType = "End Call"
                                    }
                                }

                            }
                        }
                        Log.d(TAG, "Current data: ${snapshot.data} and the SDPType is: $SDPType")
                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }
            db.collection("calls")
                .document(meetingId)
                .collection("candidates")
                .addSnapshotListener { querySnapshot, error ->
                    if(error != null){
                        Log.w(TAG,"listen:error",error)
                        return@addSnapshotListener
                    }

                    if(querySnapshot!= null && !querySnapshot.isEmpty){
                        for(document in querySnapshot){
                            val data = document.data
                            if (SDPType == "Offer" && data.containsKey("type") && data["type"] =="offerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString())
                                )
                            } else if (SDPType == "Answer" && data.containsKey("type") && data["type"] =="answerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString()))
                            }
                            Log.w(TAG, "candidateQuery: $document" )
                        }
                    }
                }
        } catch (exception: Exception) {
            Log.e(TAG, "connectException: $exception")
        }
    }

    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        val type = when {
            isJoin -> "answerCandidate"
            else -> "offerCandidate"
        }
        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )
        db.collection("calls")
            .document(meetingId).collection("candidates").document(type)
            .set(candidateConstant as Map<*, *>)
            .addOnSuccessListener {
                Log.w(TAG, "sendIceCandidate: Success" )
            }
            .addOnFailureListener {
                Log.e(TAG, "sendIceCandidate: Error $it" )
            }
    }

    fun destroy(){
        job.complete()
    }
}