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
import java.util.*

private const val TAG = "UserViewModel"
class UserViewModel: ViewModel(){

    private val auth by lazy { Firebase.auth }
    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _isNotFalse = MutableLiveData<Number>(-1)
    val isNotFalse: LiveData<Number> = _isNotFalse

    private val _listTag = MutableLiveData<List<String>>(null)
    val listTag: LiveData<List<String>> = _listTag

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

    fun addTag(newTag: String) {
        if(_listTag.value != null) {
            val currentTagMutableList = _listTag.value!!.toMutableList()
            currentTagMutableList.add(newTag.lowercase(Locale.getDefault()))
            _listTag.value = currentTagMutableList.toSet().toList()
        } else {
            _listTag.value = listOf(newTag.lowercase(Locale.getDefault()))
        }
    }

    fun deleteTag(position: Int) {
        val currentTagMutableList = _listTag.value!!.toMutableList()
        val deletedElement = _listTag.value!![position]
        _listTag.value = currentTagMutableList.filter { tag ->
            tag != deletedElement
        }.toList()
    }

    fun getCurrentListTag():List<String>? {
        return _listTag.value
    }

    fun signupWithEmail(nickname:String,email:String,password:String) {
        val profileUpdate = userProfileChangeRequest {
            displayName = nickname
        }
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            val user = auth.currentUser
            user!!.updateProfile(profileUpdate).addOnCompleteListener { task ->
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
                Log.e(TAG,it.user.toString())
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