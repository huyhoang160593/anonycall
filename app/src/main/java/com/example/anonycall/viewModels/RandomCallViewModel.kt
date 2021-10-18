package com.example.anonycall.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anonycall.services.RandomCallService
import com.example.anonycall.utils.Constants
import kotlinx.coroutines.launch

private const val TAG ="RandomCallViewModel"
class RandomCallViewModel: ViewModel() {
    private val _meetingId = MutableLiveData<String?>(null)
    val meetingId: LiveData<String?> = _meetingId

    private val _isJoin = MutableLiveData<Boolean?>(null)
    val isJoin : LiveData<Boolean?> = _isJoin

    fun getMeetingId(listTag:List<String>?) = viewModelScope.launch {
        Log.e(TAG, "currentTagList: $listTag")
        if(listTag == null || listTag.isEmpty()){
            val result = RandomCallService.getFirstOfferCall(Constants.RANDOM_CALLS_COLLECTION)
            if(result == null) {
                _meetingId.value = RandomCallService.createCallId(Constants.RANDOM_CALLS_COLLECTION)
                _isJoin.value = false
            } else {
                _meetingId.value = result
                _isJoin.value = true
            }
        } else {
            val result = RandomCallService.getFirstOfferCall(Constants.RANDOM_CALLS_COLLECTION,listTag)
            if(result == null) {
                _meetingId.value = RandomCallService.createCallId(Constants.RANDOM_CALLS_COLLECTION,listTag)
                _isJoin.value = false
            } else {
                _meetingId.value = result
                _isJoin.value = true
            }
        }

    }

}