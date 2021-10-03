package com.example.anonycall.webRTC

import android.util.Log
import com.example.anonycall.models.Candidate
import com.example.anonycall.services.RandomCallService
import com.example.anonycall.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlin.coroutines.CoroutineContext

private const val TAG = "SignallingClientNew"

class SignalingClientNew(
    private val meetingId:String,
    private val signalingListener: SignalingClientListener
):CoroutineScope {

    companion object {
        const val OFFER = "Offer"
        const val ANSWER = "Answer"
        const val END_CALL = "End Call"
    }

    private val job = Job()
    private val database = Firebase.firestore

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private var currentSDPType : String? = null
    init {
        connect()
    }

    private fun connect() = launch {
        database.enableNetwork().addOnSuccessListener {
            signalingListener.onConnectionEstablished()
        }
        try {
            database.collection(Constants.RANDOM_CALLS_COLLECTION)
                .document(meetingId)
                .addSnapshotListener { snapshot, error ->
                    if(error != null){
                        Log.e(TAG,"listener:error", error)
                        return@addSnapshotListener
                    }
                    if(snapshot != null && snapshot.exists()){
                        val data = snapshot.data
                        if(data == null){
                            Log.w(TAG, "There is no data in this snapshot")
                        } else {
                            when(data["type"].toString()){
                                "OFFER" -> {
                                    signalingListener.onOfferReceived(SessionDescription(
                                        SessionDescription.Type.OFFER,data["sdp"].toString()
                                    ))
                                    currentSDPType = OFFER
                                }
                                "ANSWER" -> {
                                    signalingListener.onAnswerReceived(SessionDescription(
                                        SessionDescription.Type.ANSWER,data["sdp"].toString()
                                    ))
                                    currentSDPType = ANSWER
                                }
                                "END_CALL" -> {
                                    Log.e(TAG,"END_CALL event trigger")
                                    if(!Constants.isInitiatedNow){
                                        currentSDPType = END_CALL
                                        signalingListener.onCallEnded()
                                    }
                                }
                            }
                            Log.e(TAG, "Current data: ${snapshot.data} and the SDPType is: $currentSDPType")
                        }
                    }
                }
            database.collection("random_calls")
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
                            when {
                                currentSDPType == OFFER && data["type"] == "offerCandidate" -> {
                                    signalingListener.onIceCandidateReceived(
                                        IceCandidate(
                                            data["sdpMid"].toString(),
                                            Math.toIntExact(data["sdpMLineIndex"] as Long),
                                            data["sdpCandidate"].toString()
                                        )
                                    )
                                }
                                currentSDPType == ANSWER && data["type"] == "answerCandidate" -> {
                                    signalingListener.onIceCandidateReceived(
                                        IceCandidate(
                                            data["sdpMid"].toString(),
                                            Math.toIntExact(data["sdpMLineIndex"] as Long),
                                            data["sdpCandidate"].toString()
                                        )
                                    )
                                }
                            }
                            Log.w(TAG,"candidateQuery: $document")
                        }
                    }
                }
        } catch (ex:Exception){
            Log.e(TAG,"connectException: $ex")
        }
    }

    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        val type = when {
            isJoin -> "answerCandidate"
            else -> "offerCandidate"
        }
        val newCandidateConstant = candidate?.let {
            Candidate(it.serverUrl,it.sdpMid,it.sdpMLineIndex,it.sdp,type)
        }
        Log.e(TAG,newCandidateConstant.toString())
        if (newCandidateConstant != null) {
            RandomCallService.addCandidate(
                collection = Constants.RANDOM_CALLS_COLLECTION,
                meetingId = meetingId,
                type = type,
                candidate = newCandidateConstant
            )
        }
//        val candidateConstant = hashMapOf(
//        "serverUrl" to candidate?.serverUrl,
//        "sdpMid" to candidate?.sdpMid,
//        "sdpMLineIndex" to candidate?.sdpMLineIndex,
//        "sdpCandidate" to candidate?.sdp,
//        "type" to type
//        )
//        database.collection(Constants.RANDOM_CALLS_COLLECTION)
//            .document(meetingId).collection("candidates").document(type)
//            .set(candidateConstant as Map<*, *>)
//            .addOnSuccessListener {
//                Log.e(TAG, "sendIceCandidate: Success" )
//            }
//            .addOnFailureListener {
//                Log.e(TAG, "sendIceCandidate: Error $it" )
//            }
    }

    fun destroy(){
        job.complete()
    }
}