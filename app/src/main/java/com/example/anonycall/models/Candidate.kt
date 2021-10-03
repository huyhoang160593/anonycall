package com.example.anonycall.models

data class Candidate(
    val serverUrl: String,
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val sdpCandidate: String,
    val type: String
)
