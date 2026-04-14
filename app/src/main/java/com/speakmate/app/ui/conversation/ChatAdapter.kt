package com.speakmate.app.ui.conversation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.speakmate.app.R
import com.speakmate.app.data.model.ChatMessage

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI   = 2
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_USER)
            R.layout.item_chat_user
        else
            R.layout.item_chat_ai
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvChatMessage)
        fun bind(message: ChatMessage) { tvMessage.text = message.content }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) =
            a.timestamp == b.timestamp && a.isUser == b.isUser
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }
}
