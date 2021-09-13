package com.example.anonycall.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.anonycall.R
import com.example.anonycall.databinding.FragmentSelectedCallBinding
import com.example.anonycall.databinding.FragmentWelcomeBinding
import com.example.anonycall.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SelectedCallFragment : Fragment() {
    private var _binding: FragmentSelectedCallBinding? = null
    private val binding get() = _binding!!

    //Gọi Firebase Database
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedCallBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Constants.isInitiatedNow = true
        Constants.isCallEnded = true
        binding.apply {
            startMeeting.setOnClickListener {
                if (meetingId.text.toString().trim().isEmpty())
                    meetingId.error = "Please enter meeting id"
                else {
                    db.collection("calls")
                        .document(meetingId.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                                meetingId.error = "Please enter new meeting ID"
                            } else {
                                Toast.makeText(requireContext(),"Cuộc gọi mới được tạo với ID ${meetingId.text}",Toast.LENGTH_LONG).show()
                                val action = SelectedCallFragmentDirections.actionSelectedCallFragmentToCallFragment(meetingId.text.toString(),false)
                                findNavController().navigate(action)
//                                val intent = Intent(this@MainActivity, RTCActivity::class.java)
//                                intent.putExtra("meetingID",meeting_id.text.toString())
//                                intent.putExtra("isJoin",false)
//                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            meetingId.error = "Please enter new meeting ID"
                        }
                }
            }
            joinMeeting.setOnClickListener {
                if (meetingId.text.toString().trim().isEmpty())
                    meetingId.error = "Please enter meeting id"
                else {
                    Toast.makeText(requireContext(),"Tham dự cuộc gọi với ID ${meetingId.text}",Toast.LENGTH_LONG).show()
                    val action = SelectedCallFragmentDirections.actionSelectedCallFragmentToCallFragment(meetingId.text.toString(),true)
                    findNavController().navigate(action)
//                    val intent = Intent(this@MainActivity, RTCActivity::class.java)
//                    intent.putExtra("meetingID",meeting_id.text.toString())
//                    intent.putExtra("isJoin",true)
//                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}