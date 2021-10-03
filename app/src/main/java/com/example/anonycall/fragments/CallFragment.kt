package com.example.anonycall.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import com.example.anonycall.MainActivity
import com.example.anonycall.R
import com.example.anonycall.databinding.FragmentCallBinding
import com.example.anonycall.webRTC.AppSdpObserver
import com.example.anonycall.webRTC.PeerConnectionObserver
import com.example.anonycall.webRTC.SignalingClientListener
import com.example.anonycall.utils.Constants
import com.example.anonycall.webRTC.RTCAudioManager
import com.example.anonycall.webRTC.RTCClient
import com.example.anonycall.webRTC.SignalingClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.withContext
import org.webrtc.*

private const val TAG = "MainActivity"
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class CallFragment : Fragment() {
    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient

    private val audioManager by lazy { RTCAudioManager.create(requireContext()) }

    private lateinit var meetingID : String

    private var isJoin : Boolean = false

    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true

    private val sdpObserver = object : AppSdpObserver(){}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding bottom nav
        (activity as MainActivity).hidingBottomNavigation(status = true)

        arguments?.let {
            meetingID = it.getString("meetingId").toString()
            isJoin = it.getBoolean("isJoin")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraAndAudioPermission()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        binding.apply {
            switchCameraButton.setOnClickListener {
                rtcClient.switchCamera()
            }

            audioOutputButton.setOnClickListener {
                if (inSpeakerMode) {
                    inSpeakerMode = false
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
                } else {
                    inSpeakerMode = true
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
                }
            }
            videoButton.setOnClickListener {
                if (isVideoPaused) {
                    isVideoPaused = false
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                } else {
                    isVideoPaused = true
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
                }
                rtcClient.enableVideo(isVideoPaused)
            }
            micButton.setOnClickListener {
                if (isMute) {
                    isMute = false
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
                } else {
                    isMute = true
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                }
                rtcClient.enableAudio(isMute)
            }
            endCallButton.setOnClickListener {
                rtcClient.endCall(meetingID)
                binding.remoteView.isGone = false
                Constants.isCallEnded = true
                requireActivity().onBackPressed()
            }
        }
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(requireContext(), CAMERA_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(requireContext(),AUDIO_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted() {
        rtcClient = RTCClient(
            requireActivity().application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    signallingClient.sendIceCandidate(p0, isJoin)
                    rtcClient.addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.e(TAG, "onAddStream: $p0")
                    p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onIceConnectionChange: $p0")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.e(TAG, "onIceConnectionReceivingChange: $p0")
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    Log.e(TAG, "onConnectionChange: $newState")
                }

                override fun onDataChannel(p0: DataChannel?) {
                    Log.e(TAG, "onDataChannel: $p0")
                }

                override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onStandardizedIceConnectionChange: $newState")
                }

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                    Log.e(TAG, "onAddTrack: $p0 \n $p1")
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    Log.e(TAG, "onTrack: $transceiver" )
                }
            }
        ,false)

        rtcClient.initSurfaceView(binding.remoteView)
        rtcClient.initSurfaceView(binding.localView)
        rtcClient.startLocalVideoCapture(binding.localView)
        signallingClient =  SignalingClient(meetingID,createSignallingClientListener())
        if (!isJoin)
            rtcClient.call(sdpObserver,meetingID)
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            binding.endCallButton.isClickable = true
        }

        override fun onOfferReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
            rtcClient.answer(sdpObserver,meetingID)
            binding.remoteViewLoading.isGone = true
        }

        override fun onAnswerReceived(description: SessionDescription) {
            rtcClient.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
            binding.remoteViewLoading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            rtcClient.addIceCandidate(iceCandidate)
        }

        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(meetingID)
                requireActivity().onBackPressed()
            }
        }
    }
    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), CAMERA_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), AUDIO_PERMISSION) &&
            !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Camera And Audio Permission Required")
            .setMessage("This app need the camera and audio to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAndAudioPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(requireContext(), "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    private fun returnToWelcomeFragment() {
        findNavController().navigate(CallFragmentDirections.actionCallFragmentToWelcomeFragment())
    }
    override fun onDestroyView() {
        super.onDestroyView()
        signallingClient.destroy()
        _binding = null
    }
}