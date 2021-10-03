package com.example.anonycall.services

import android.util.Log
import com.example.anonycall.models.Answer
import com.example.anonycall.models.Candidate
import com.example.anonycall.models.Offer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "RandomCall Service"
object RandomCallService {

    private val database = FirebaseFirestore.getInstance()

    suspend fun getFirstOfferCall(collection: String): String? {
        val querySnapshot = database
            .collection(collection)
            .whereEqualTo("type","OFFER")
            .get().await()
        if(querySnapshot.isEmpty){
            return null
        }
        return querySnapshot.documents[0]?.id
    }

    fun createCallId(collection: String): String {
        return database
            .collection(collection)
            .document()
            .id
    }

    fun addOffer(collection: String, meetingId: String,offer: Offer) {
        database.collection(collection)
            .document(meetingId)
            .set(offer)
            .addOnSuccessListener {
                Log.e(TAG,"Offer info with meetingId: $meetingId updated")
            }.addOnFailureListener {
                Log.e(TAG,"Exception happen", it)
            }
    }
    fun addAnswer(collection: String, meetingId: String, answer: Answer) {
        database.collection(collection)
            .document(meetingId)
            .set(answer)
            .addOnSuccessListener {
                Log.e(TAG,"Answer info with meetingId: $meetingId updated")
            }.addOnFailureListener {
                Log.e(TAG,"Exception happen", it)
            }
    }

    fun endCall(collection: String, meetingId: String) {
        val endCall = hashMapOf(
            "type" to "END_CALL"
        )
        database.collection(collection)
            .document(meetingId)
            .set(endCall)
            .addOnSuccessListener {
                Log.e(TAG,"MeetingId: $meetingId has ended")
            }.addOnFailureListener {
                Log.e(TAG,"Exception happen", it)
            }
    }

    fun addCandidate(collection: String, meetingId: String,type: String, candidate: Candidate) {
        database.collection(collection)
            .document(meetingId)
            .collection("candidates")
            .document(type)
            .set(candidate)
            .addOnSuccessListener {
                Log.e(TAG,"Candidate in meetingId: $meetingId updated")
            }.addOnFailureListener {
                Log.e(TAG,"Exception happen", it)
            }
    }
}