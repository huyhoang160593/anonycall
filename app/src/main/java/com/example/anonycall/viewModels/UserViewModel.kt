package com.example.anonycall.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anonycall.models.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

private const val TAG = "UserViewModel"
class UserViewModel: ViewModel(){

    private val auth by lazy { Firebase.auth }
    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _isNotFalse = MutableLiveData<Number>(-1)
    val isNotFalse: LiveData<Number> = _isNotFalse

    init {
        getInitialUser()
    }

    fun getInitialUser() {
        val user = auth.currentUser
        if (user != null){
            _user.value = User(
                displayName = user.displayName,
                email = user.email,
                avatarURL = user.photoUrl.toString()
            )
        }
    }

    fun signupWithEmail(nickname:String,email:String,password:String) {
        val profileUpdate = userProfileChangeRequest {
            displayName = nickname
        }
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            it.user?.updateProfile(profileUpdate)?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    getInitialUser()
                    _isNotFalse.value = 1
                } else {
                    Log.e(TAG, "Exception raise: ", task.exception)
                    _isNotFalse.value = 0
                }
            }

        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        auth.signInWithEmailAndPassword(_user.value!!.email!!,oldPass)
            .addOnSuccessListener {
                it.user?.updatePassword(newPass)?.addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        getInitialUser()
                        _isNotFalse.value = 1
                    } else {
                        Log.e(TAG, "Exception raise: ", task.exception)
                        _isNotFalse.value = 0
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Exception raise: ", it)
                _isNotFalse.value = 0
            }
    }

    fun changeAvatar(avatarUri: String) {
        val profileUpdate = userProfileChangeRequest {
            photoUri = Uri.parse(avatarUri)
        }
        auth.currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                getInitialUser()
                _isNotFalse.value = 1
            } else {
                Log.e(TAG, "Exception raise: ", task.exception)
                _isNotFalse.value = 0
            }
        }
    }

    fun changeNickname(newNickname: String) {
        val profileUpdate = userProfileChangeRequest {
            displayName = newNickname
        }
        auth.currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                getInitialUser()
                _isNotFalse.value = 1
            } else {
                Log.e(TAG, "Exception raise: ", task.exception)
                _isNotFalse.value = 0
            }
        }
    }

    fun signOut() {
        _user.value = null
        auth.signOut()
    }

    fun resetCheckValue() {
        _isNotFalse.value = -1
    }
}