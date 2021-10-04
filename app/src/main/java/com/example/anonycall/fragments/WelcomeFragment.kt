package com.example.anonycall.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import co.lujun.androidtagview.TagView.OnTagClickListener
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
            viewModel.listTag.observe(viewLifecycleOwner, {
                newTagsList ->
                if(newTagsList != null){
                    binding.tagContainerView.tags = newTagsList
                }
            })
            tagContainerView.setOnTagClickListener(object : OnTagClickListener {
                override fun onTagClick(position: Int, text: String?) {}
                override fun onTagLongClick(position: Int, text: String?) {}
                override fun onSelectedTagDrag(position: Int, text: String?) {}

                override fun onTagCrossClick(position: Int) {
                    deleteTags(position)
                }
            })
            viewModel.user.observe(viewLifecycleOwner, {
                currentUser ->
                run {
                    when {
                        currentUser == null -> {
                            textviewUser.text = "Xin chào người lạ!!!"

                            textinputlayoutTags.visibility = View.GONE
                            btnAddTag.visibility = View.INVISIBLE
                            tagContainerView.visibility = View.GONE
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
                    btnAddTag.setOnClickListener { addTags() }
                }
            })
        }
    }

    private fun addTags() {
        if(binding.textinputlayoutTags.editText != null) {
            val tagString = binding.textinputlayoutTags.editText!!.text.toString()
            if(tagString.isNotBlank()){
                viewModel.addTag(tagString)
            }
            binding.textinputlayoutTags.editText!!.setText("")
        }
    }

    private fun deleteTags(position: Int) {
        viewModel.deleteTag(position)
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