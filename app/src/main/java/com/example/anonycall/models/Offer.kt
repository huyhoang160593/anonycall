package com.example.anonycall.models

import org.webrtc.SessionDescription

data class Offer(
    val sdp : String,
    val type: SessionDescription.Type
)

data class Answer(
    val sdp : String,
    val type: SessionDescription.Type,
)
