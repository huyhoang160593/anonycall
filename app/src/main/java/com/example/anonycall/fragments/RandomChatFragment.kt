package com.example.anonycall.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_SEND
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anonycall.adapters.MessageAdapter
import com.example.anonycall.databinding.RandomChatFragmentBinding
import com.example.anonycall.models.Message
import com.example.anonycall.utils.Constants.RECEIVE_ID
import com.example.anonycall.utils.Constants.SEND_ID
import com.example.anonycall.utils.Time
import com.example.anonycall.viewModels.RandomChatViewModel
import com.example.anonycall.viewModels.RandomChatViewModelFactory
import kotlinx.coroutines.*

private const val TAG ="RandomChatFragment"
class RandomChatFragment : Fragment() {
    private var _binding: RandomChatFragmentBinding? = null
    private val binding get() = _binding!!
//    private val viewModel: RandomChatViewModel by viewModels()

    private lateinit var viewModel: RandomChatViewModel
    private lateinit var viewModelFactory: RandomChatViewModelFactory

    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RandomChatFragmentBinding.inflate(inflater,container,false)

        viewModelFactory = RandomChatViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)
            .get(RandomChatViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainScope().launch {
            delay(1000)
            withContext(Dispatchers.Main){
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
        // RecycleView Part
        adapter = MessageAdapter()
        binding.rvMessages.adapter = adapter
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())

        // Send Message Event
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        binding.etMessage.setOnEditorActionListener { _v, actionId, _event ->
            Log.e(TAG,"View:$_v, ActionId:$actionId, Event: $_event")
            when(actionId){
                IME_ACTION_SEND -> {
                    sendMessage()
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.etMessage.setOnClickListener {
            MainScope().launch {
                delay(1000)
                withContext(Dispatchers.Main){
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        // Create Offer
        viewModel.meetingId.observe(viewLifecycleOwner,{
            newId ->
            if(newId.isNotBlank()){
                Log.e(TAG, "your current ID: $newId")
                customMessage("ID phòng của bạn là $newId")
                viewModel.createRTCChatClient()
            }
        })
        //Receive Message
        viewModel.answerMessage.observe(viewLifecycleOwner,{
            newMessage ->
            if(newMessage != null && newMessage.isNotBlank()){
                messageResponse(newMessage)
            }
        })
        customMessage("Bắt đầu tìm kiếm người lạ cho bạn ...")
    }

    private fun sendMessage(){
        val message = binding.etMessage.text.toString()
        val timeStamp = Time.timeStamp()

        if(message.isNotEmpty()){
            binding.etMessage.setText("")
//            viewModel.sendMessage(message)
            adapter.insertMessage(Message(message,SEND_ID, timeStamp))
            binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun messageResponse(message: String) {
        val timeStamp = Time.timeStamp()
        MainScope().launch {
            withContext(Dispatchers.Main){
                adapter.insertMessage(Message(message, RECEIVE_ID,timeStamp))
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun customMessage(message:String){
        MainScope().launch {
            withContext(Dispatchers.Main){
                val timeStamp = Time.timeStamp()
                adapter.insertMessage(Message(message,RECEIVE_ID, timeStamp))
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}