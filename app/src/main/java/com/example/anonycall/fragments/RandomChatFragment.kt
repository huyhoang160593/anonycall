package com.example.anonycall.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_SEND
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anonycall.MainActivity
import com.example.anonycall.adapters.MessageAdapter
import com.example.anonycall.databinding.RandomChatFragmentBinding
import com.example.anonycall.models.Message
import com.example.anonycall.models.MessageEvent
import com.example.anonycall.utils.Constants.RECEIVE_ID
import com.example.anonycall.utils.Constants.SEND_ID
import com.example.anonycall.utils.Time
import com.example.anonycall.viewModels.RandomChatViewModel
import com.example.anonycall.viewModels.RandomChatViewModelFactory
import com.example.anonycall.viewModels.UserViewModel
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val TAG ="RandomChatFragment"
class RandomChatFragment : Fragment() {
    private var _binding: RandomChatFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RandomChatViewModel
    private lateinit var viewModelFactory: RandomChatViewModelFactory
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var adapter: MessageAdapter

    private var currentListTag: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory = RandomChatViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(viewModelStore,viewModelFactory)
            .get(RandomChatViewModel::class.java)
        currentListTag = userViewModel.getCurrentListTag()
        Log.e(TAG,"current list tag: $currentListTag")
        viewModel.getMeetingId(currentListTag)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RandomChatFragmentBinding.inflate(inflater,container,false)
        EventBus.getDefault().register(this)
        return binding.root
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        if(event == null) return
        messageResponse(event.message)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).hidingBottomNavigation(status = true)

        MainScope().launch {
            delay(1000)
            withContext(Dispatchers.Main){
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
        binding.endCallButton.setOnClickListener {
            backToWelcomeFragment()
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
        customMessage("Bắt đầu tìm kiếm người lạ cho bạn...")

        if(currentListTag != null && currentListTag!!.isNotEmpty()){
            customMessage("Người lạ sẽ có 1 trong những điểm chung liên quan đến $currentListTag")
        }

        viewModel.closeFragment.observe(viewLifecycleOwner, {
            checkToExit ->
            if(checkToExit == true) {
                backToWelcomeFragment()
            }
        })
    }

    private fun backToWelcomeFragment() {
        viewModel.endCall()
        findNavController().navigate(RandomChatFragmentDirections.actionRandomChatFragmentToWelcomeFragment())
    }

    private fun sendMessage(){
        val message = binding.etMessage.text.toString()
        val timeStamp = Time.timeStamp()

        if(message.isNotEmpty()){
            binding.etMessage.setText("")
            viewModel.sendMessage(message)
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

        EventBus.getDefault().unregister(this);

        viewModelStore.clear()
    }
}