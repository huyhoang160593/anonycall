package com.example.anonycall.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anonycall.services.RandomCallService
import kotlinx.coroutines.launch

private const val TAG ="RandomCallViewModel"
class RandomCallViewModel: ViewModel() {
    private val _meetingId = MutableLiveData<String?>(null)
    val meetingId: LiveData<String?> = _meetingId

    private val _isJoin = MutableLiveData<Boolean?>(null)
    val isJoin : LiveData<Boolean?> = _isJoin

    fun getMeetingId() = viewModelScope.launch {
        val result = RandomCallService.getFirstOfferCall("random_calls")
        Log.e(TAG,"result :$result")
        if(result == null) {
            _meetingId.value = RandomCallService.createCallId("random_calls")
            _isJoin.value = false
        } else {
            _meetingId.value = result
            _isJoin.value = true
        }
    }

}