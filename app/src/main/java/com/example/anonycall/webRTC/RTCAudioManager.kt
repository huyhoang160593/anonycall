package com.example.anonycall.webRTC

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.anonycall.R
import org.webrtc.ThreadUtils
import java.util.*
import kotlin.collections.HashSet

private const val TAG = "AppRTCAudioManager"
class RTCAudioManager(private val appRTCContext: Context) {
    /**
     * AudioDevice là những thiết bị mà hiện giờ đang hỗ trợ
     */
    enum class AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, NONE
    }

    /**
     * Trạng thái của AudioManager
     */
    enum class AudioManagerState {
        UNINITIALIZED, PREINITIALIZED, RUNNING
    }


    /** Bắt sự kiện khi thiết bị âm thanh bị thay đổi  */
    interface AudioManagerEvents {
        // Callback được kích hoạt một khi thiết bị âm thay bị thay đổi hoặc danh sách thiết bị âm thanh bị thay đổi
        fun onAudioDeviceChanged(
            selectedAudioDevice: AudioDevice?, availableAudioDevices: Set<AudioDevice?>?
        )
    }

    private val audioManager: AudioManager

    private var audioManagerEvents: AudioManagerEvents? = null
    private var amState: AudioManagerState
    private var savedAudioMode = AudioManager.MODE_NORMAL
    private var savedIsSpeakerPhoneOn = false
    private var savedIsMicrophoneMute = false
    private var hasWiredHeadset = false

    /**
     * Thiết bị âm thanh mặc định: có thể là loa thoại cho video call hay tai nghe khi chỉ gọi
     */
    private var defaultAudioDevice: AudioDevice? = null

    /**
     * Chứa thiết bị âm thanh hiện hành
     * Thiết bị sẽ được thay đổi tự động tuỳ theo từng điều kiện hiện hành ví dụ như tai nghe dây
     * "ưu tiên" so vói loa thoại. Cũng có thể nếu như người dùng lựa chọn thiết bị rõ ràng(và viết
     * đè lên mọi điều kiện khác)
     */
    private var selectedAudioDevice: AudioDevice? = null

    /**
     * Chứa thiết bị âm thanh do người dùng lựa chọn và nó sẽ được viết đè lên mọi điều kiện khác
     */
    private var userSelectedAudioDevice: AudioDevice? = null

    /**
     * Sử dụng cho cài đặt loa thoại: auto, true, false
     */
    private val useSpeakerphone: String?

    /**
     * bao gồm list các thiết bị âm thanh đang có sẵn, Set collection được sử dụng để ngăn chặn các
     * item bị lặp lại
     */
    private var audioDevices: MutableSet<AudioDevice?> = HashSet()

    /**
     * bộ thu phát sóng dành cho các tai nghe có dây
     */
    private val wiredHeadsetReceiver: BroadcastReceiver

    /**
     * hàm callback khi có sự thay đổi trong audio focus
     */
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    /**
    * Bộ thu kiểm soát sự thanh đổi khi tai nghe có dây được kết nối
    */
    private inner class WiredHeadsetReceiver : BroadcastReceiver() {
        private val stateUNPLUGGED = 0
        private val statePLUGGED = 1
        private val hasNoMIC = 0
        private val hasMIC = 1

        override fun onReceive(context: Context?, intent: Intent) {
            val state = intent.getIntExtra("state", stateUNPLUGGED)
            val microphone = intent.getIntExtra("microphone", hasNoMIC)
            val name = intent.getStringExtra("name")
            Log.d(TAG, "WiredHeadsetReceiver.onReceive"
                    + ": " + "a=" + intent.action.toString() + ", s=" +
                    (if (state == stateUNPLUGGED) "unplugged" else "plugged").toString()
                    + ", m=" + (if (microphone == hasMIC) "mic" else "no mic").toString()
                    + ", n=" + name.toString() + ", sb=" + isInitialStickyBroadcast)
            hasWiredHeadset = (state == statePLUGGED)
            updateAudioDeviceState()
        }
    }

    fun start(audioManagerEvents: AudioManagerEvents?) {
        Log.d(TAG, "start")
        ThreadUtils.checkIsOnMainThread()
        if (amState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active")
            return
        }
        // TODO có lẽ nên tạo một phương thức mới gọi preInitAudio() nếu như UNINITIALIZED
        Log.d(TAG, "AudioManager starts...")
        this.audioManagerEvents = audioManagerEvents
        amState = AudioManagerState.RUNNING

        // Store current audio state so we can restore it when stop() is called.
        // Lưu giữ trạng thái âm thanh hiện tại để chúng ta có thể lấy lại dùng khi stop() được gọi
        savedAudioMode = audioManager.mode
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn
        savedIsMicrophoneMute = audioManager.isMicrophoneMute
        hasWiredHeadset = hasWiredHeadset()

        // Tạo một thể hiện cuả AudioManager.OnAudioFocusChangeListener
        audioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                /**
                 * Gọi một listener để thông báo nếu như phần âm thanh tập chung của listener này bị thay
                 * đổi. giá trị _forcusChange_ thể hiện xem phần tập chung này thành công hay bị mất,
                 * hay việc thất thoát đó là tạm thời, hay phần tập chung mới sẽ giữ nó trong một khoản thời gian
                 * */
                val typeOfChange: String = when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> "AUDIOFOCUS_GAIN"
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> "AUDIOFOCUS_GAIN_TRANSIENT"
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE"
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK"
                    AudioManager.AUDIOFOCUS_LOSS -> "AUDIOFOCUS_LOSS"
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "AUDIOFOCUS_LOSS_TRANSIENT"
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
                    else -> "AUDIOFOCUS_INVALID"
                }
                Log.d(TAG, "onAudioFocusChange: $typeOfChange")
            }
        // Request audio playout focus (without ducking) and install listener for changes in focus.
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams")
        } else {
            Log.e(TAG, "Audio focus request failed")
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false)

        // Set initial device states.
        userSelectedAudioDevice = AudioDevice.NONE
        selectedAudioDevice = AudioDevice.NONE
        audioDevices.clear()

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a BT or wired headset or by covering/uncovering
        // the proximity sensor.
        updateAudioDeviceState()

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset.
        registerReceiver(wiredHeadsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        Log.d(TAG, "AudioManager started")
    }

    fun stop() {
        Log.d(TAG, "stop")
        ThreadUtils.checkIsOnMainThread()
        if (amState != AudioManagerState.RUNNING) {
            Log.e(
                TAG,
                "Trying to stop AudioManager in incorrect state: $amState"
            )
            return
        }
        amState = AudioManagerState.UNINITIALIZED
        unregisterReceiver(wiredHeadsetReceiver)

        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn)
        setMicrophoneMute(savedIsMicrophoneMute)
        audioManager.mode = savedAudioMode

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        audioManager.abandonAudioFocus(audioFocusChangeListener)
        audioFocusChangeListener = null
        Log.d(TAG, "Abandoned audio focus for VOICE_CALL streams")

        audioManagerEvents = null
        Log.d(TAG, "AudioManager stopped")
    }

    /** Changes selection of the currently active audio device.  */
    private fun setAudioDeviceInternal(device: AudioDevice?) {
        Log.d(TAG, "setAudioDeviceInternal(device=$device)")
        if (audioDevices.contains(device)) {
            when (device) {
                AudioDevice.SPEAKER_PHONE -> setSpeakerphoneOn(true)
                AudioDevice.EARPIECE -> setSpeakerphoneOn(false)
                AudioDevice.WIRED_HEADSET -> setSpeakerphoneOn(false)
                else -> Log.e(TAG, "Invalid audio device selection")
            }
        }
        selectedAudioDevice = device
    }

    /**
     * Changes default audio device.
     */
    fun setDefaultAudioDevice(defaultDevice: AudioDevice?) {
        ThreadUtils.checkIsOnMainThread()
        when (defaultDevice) {
            AudioDevice.SPEAKER_PHONE -> defaultAudioDevice = defaultDevice
            AudioDevice.EARPIECE -> defaultAudioDevice = if (hasEarpiece()) {
                defaultDevice
            } else {
                AudioDevice.SPEAKER_PHONE
            }
            else -> Log.e(TAG, "Invalid default audio device selection")
        }
        Log.d(TAG, "setDefaultAudioDevice(device=$defaultAudioDevice)")
        updateAudioDeviceState()
    }

    /** Changes selection of the currently active audio device.  */
    fun selectAudioDevice(device: AudioDevice) {
        ThreadUtils.checkIsOnMainThread()
        if (!audioDevices.contains(device)) {
            Log.e(
                TAG,
                "Can not select $device from available $audioDevices"
            )
        }
        userSelectedAudioDevice = device
        updateAudioDeviceState()
    }

    /** Returns current set of available/selectable audio devices.  */
    fun getAudioDevices(): Set<AudioDevice> {
        ThreadUtils.checkIsOnMainThread()

        @Suppress("UNCHECKED_CAST")
        return Collections.unmodifiableSet(HashSet(audioDevices)) as Set<AudioDevice>
    }

    /** Returns the currently selected audio device.  */
    fun getSelectedAudioDevice(): AudioDevice? {
        ThreadUtils.checkIsOnMainThread()
        return selectedAudioDevice
    }

    /** Helper method for receiver registration.  */
    private fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        appRTCContext.registerReceiver(receiver, filter)
    }

    /** Helper method for unregistration of an existing receiver.  */
    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        appRTCContext.unregisterReceiver(receiver)
    }

    /** Sets the speaker phone mode.  */
    private fun setSpeakerphoneOn(on: Boolean) {
        val wasOn = audioManager.isSpeakerphoneOn
        if (wasOn == on) {
            return
        }
        audioManager.isSpeakerphoneOn = on
    }

    /** Sets the microphone mute state.  */
    private fun setMicrophoneMute(on: Boolean) {
        val wasMuted = audioManager.isMicrophoneMute
        if (wasMuted == on) {
            return
        }
        audioManager.isMicrophoneMute = on
    }

    /** Gets the current earpiece state.  */
    private fun hasEarpiece(): Boolean {
        return appRTCContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated("")
    private fun hasWiredHeadset(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        for (device: AudioDeviceInfo in devices) {
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                Log.d(TAG, "hasWiredHeadset: found wired headset")
                return true
            } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                Log.d(TAG, "hasWiredHeadset: found USB audio device")
                return true
            }
        }
        return false
    }

    /**
     * Updates list of possible audio devices and make new device selection.
     */
    fun updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread()
        Log.d(
            TAG, ("--- updateAudioDeviceState: "
                    + "wired headset=" + hasWiredHeadset)
        )
        Log.d(
            TAG, ("Device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + selectedAudioDevice + ", "
                    + "user selected=" + userSelectedAudioDevice)
        )


        // Update the set of available audio devices.
        val newAudioDevices: MutableSet<AudioDevice?> = HashSet()

        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            newAudioDevices.add(AudioDevice.WIRED_HEADSET)
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE)
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE)
            }
        }
        // Store state which is set to true if the device list has changed.
        var audioDeviceSetUpdated = audioDevices != newAudioDevices
        // Update the existing audio device set.
        audioDevices = newAudioDevices
        // Correct user selected audio devices if needed.
        if (hasWiredHeadset && userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            // If user selected speaker phone, but then plugged wired headset then make
            // wired headset as user selected device.
            userSelectedAudioDevice = AudioDevice.WIRED_HEADSET
        }
        if (!hasWiredHeadset && userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            // If user selected wired headset, but then unplugged wired headset then make
            // speaker phone as user selected device.
            userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE
        }


        // Update selected audio device.
        val newAudioDevice: AudioDevice? = if (hasWiredHeadset) {
            // If a wired headset is connected, but Bluetooth is not, then wired headset is used as
            // audio device.
            AudioDevice.WIRED_HEADSET
        } else {
            // No wired headset and no Bluetooth, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            // |defaultAudioDevice| contains either AudioDevice.SPEAKER_PHONE or AudioDevice.EARPIECE
            // depending on the user's selection.
            defaultAudioDevice
        }
        // Switch to new device but only if there has been any changes.
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            setAudioDeviceInternal(newAudioDevice)
            Log.d(
                TAG, ("New device status: "
                        + "available=" + audioDevices + ", "
                        + "selected=" + newAudioDevice)
            )
            if (audioManagerEvents != null) {
                // Notify a listening client that audio device has been changed.
                audioManagerEvents!!.onAudioDeviceChanged(selectedAudioDevice, audioDevices)
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState done")
    }



    companion object {
//        private const val SPEAKERPHONE_AUTO = "auto"
//        private const val SPEAKERPHONE_TRUE = "true"
        private const val SPEAKERPHONE_FALSE = "false"

        /** Construction.  */
        fun create(context: Context): RTCAudioManager {
            return RTCAudioManager(context)
        }
    }

    init {
        ThreadUtils.checkIsOnMainThread()
        audioManager = appRTCContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amState = AudioManagerState.UNINITIALIZED
        wiredHeadsetReceiver = WiredHeadsetReceiver()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appRTCContext)
        useSpeakerphone = sharedPreferences.getString(
            appRTCContext.getString(R.string.pref_speakerphone_key),
            appRTCContext.getString(R.string.pref_speakerphone_default)
        )
        defaultAudioDevice = if ((useSpeakerphone == SPEAKERPHONE_FALSE)) {
            AudioDevice.EARPIECE
        } else {
            AudioDevice.SPEAKER_PHONE
        }
        Log.d(TAG, "defaultAudioDevice: $defaultAudioDevice")
    }
}