package com.example.anonycall.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.anonycall.MainActivity
import com.example.anonycall.databinding.FragmentWelcomeBinding
import com.example.anonycall.viewModels.UserViewModel

private const val TAG ="WelcomeFragment"
class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //make bottom nav visible
        (requireActivity() as MainActivity).hidingBottomNavigation(status = false)
        binding.apply {
            selectedCallButton.setOnClickListener {
                goToSelectedCallFragment()
            }
            randomCallButton.setOnClickListener {
                goToRandomCallFragment()
            }
            randomChatButton.setOnClickListener {
                goToRandomChatFragment()
            }
            viewModel.user.observe(viewLifecycleOwner, {
                currentUser ->
                run {
                    Log.e(TAG,currentUser.toString())
                    when {
                        currentUser == null -> {
                            textviewUser.text = "Xin chào người lạ!!!"
                        }
                        currentUser.displayName != null && currentUser.displayName.isNotEmpty() -> {
                            textviewUser.text = "Xin chào ${currentUser.displayName}!"
                            randomChatButton.visibility = View.VISIBLE
                            selectedCallButton.visibility = View.VISIBLE
                        }
                        currentUser.email != null -> {
                            textviewUser.text = "Xin chào email ${currentUser.email}!"
                            randomChatButton.visibility = View.VISIBLE
                            selectedCallButton.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

    private fun goToRandomChatFragment() {
        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToRandomChatFragment())
    }

    private fun goToSelectedCallFragment() {
        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSelectedCallFragment())
    }

    private fun goToRandomCallFragment(){
        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToRandomCall())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}