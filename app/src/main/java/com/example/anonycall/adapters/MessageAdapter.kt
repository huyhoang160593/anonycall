package com.example.anonycall.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anonycall.databinding.MessageItemBinding
import com.example.anonycall.models.Message
import com.example.anonycall.utils.Constants.RECEIVE_ID
import com.example.anonycall.utils.Constants.SEND_ID

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    var messageList = mutableListOf<Message>()

    inner class MessageViewHolder(val binding: MessageItemBinding):RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
//                messageList.removeAt(adapterPosition)
//                notifyItemRemoved(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)

        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]

        when (currentMessage.id){
            SEND_ID -> {
                holder.binding.tvMessage.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.binding.timeStampMessage.apply {
                    visibility = View.VISIBLE
                    text = "Gửi vào lúc ${currentMessage.time}"
                }
                holder.binding.tvReceiveMessage.visibility = View.INVISIBLE
                holder.binding.timeStampReceiveMessage.visibility = View.INVISIBLE
            }
            RECEIVE_ID -> {
                holder.binding.tvReceiveMessage.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.binding.timeStampReceiveMessage.apply {
                    visibility = View.VISIBLE
                    text = "Nhận vào lúc ${currentMessage.time}"
                }
                holder.binding.tvMessage.visibility = View.INVISIBLE
                holder.binding.timeStampMessage.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount() = messageList.size

    fun insertMessage(message: Message){
        messageList.add(message)
        notifyItemInserted(messageList.size)
        notifyDataSetChanged()
    }
}