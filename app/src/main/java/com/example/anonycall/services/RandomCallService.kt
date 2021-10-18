package com.example.anonycall.services

import android.util.Log
import com.example.anonycall.models.Answer
import com.example.anonycall.models.Candidate
import com.example.anonycall.models.Offer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import kotlin.random.nextInt

private const val TAG = "RandomCall Service"
object RandomCallService {

    private val database = FirebaseFirestore.getInstance()

    private val noTagList = listOf("noTag")

    suspend fun getFirstOfferCall(collection: String): String? {
        val querySnapshot = database
            .collection(collection)
            .whereArrayContainsAny("tags", noTagList)
            .whereEqualTo("type","OFFER")
            .get().await()
        if(querySnapshot.isEmpty){
            return null
        }
        val snapShotSize = querySnapshot.documents.size
        val magicRandom = if(snapShotSize > 1) {Random.nextInt(0,snapShotSize)} else 0
        return querySnapshot.documents[magicRandom]?.id
    }

    suspend fun getFirstOfferCall(collection: String, listTag: List<String>) : String? {
        val querySnapshot = database
            .collection(collection)
            .whereEqualTo("type","OFFER")
            .whereArrayContainsAny("tags",listTag)
            .get().await()
        if(querySnapshot.isEmpty){
            return null
        }
        val snapShotSize = querySnapshot.documents.size
        val magicRandom = if(snapShotSize > 1) {Random.nextInt(0,snapShotSize)} else 0
        return querySnapshot.documents[magicRandom]?.id
    }

    fun createCallId(collection: String): String {
        val meetingId = database
            .collection(collection)
            .document()
            .id
        val hashMapTag = hashMapOf("tags" to noTagList)
        database.collection(collection).document(meetingId).set(hashMapTag)
        return meetingId
    }

    fun createCallId(collection: String, listTag: List<String>): String {
        val meetingId = database
            .collection(collection)
            .document()
            .id
        val hashMapTag = hashMapOf("tags" to listTag)
        database.collection(collection).document(meetingId).set(hashMapTag)
        return meetingId
    }

    fun addOffer(collection: String, meetingId: String,offer: Offer) {
        database.collection(collection)
            .document(meetingId)
            .set(offer, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(TAG,"Offer info with meetingId: $meetingId updated")
            }.addOnFailureListener {
                Log.e(TAG,"Exception happen", it)
            }
    }
    fun addAnswer(collection: String, meetingId: String, answer: Answer) {
        database.collection(collection)
            .document(meetingId)
            .set(answer, SetOptions.merge())
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