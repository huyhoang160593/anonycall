package com.example.anonycall.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.anonycall.MainActivity;
import com.example.anonycall.R;
import com.example.anonycall.databinding.ChangeDetailFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;

public class ChangeDetailFragment extends Fragment {
    private final String TAG = "ChangeDetailFragment";

    private UserViewModel mUserViewModel;
    private ChangeDetailFragmentBinding binding;

    public static ChangeDetailFragment newInstance() {
        return new ChangeDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = ChangeDetailFragmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hidingBottomNavigation(true);

        binding.btnSubmit.setOnClickListener(v -> changeNickname());
        binding.btnBack.setOnClickListener(v -> goBackToMain());
        mUserViewModel.isNotFalse().observe(getViewLifecycleOwner(), newValue ->{
            int checkValue = newValue.intValue();
            switch (checkValue){
                case 1:{
                    mUserViewModel.resetCheckValue();
                    goBackToMain();
                    break;
                }
                case 0:{
                    Toast.makeText(requireContext(),"Xự cố xảy ra khi thay đổi thông tin",Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.btnSubmit.setEnabled(true);
                    break;
                }
            }
        });

    }

    private void changeNickname() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);
        try {
            if(binding.textinputlayoutNickname.getEditText() == null) {
                throw new NullPointerException();
            }
            String newNickname = binding.textinputlayoutNickname.getEditText().getText().toString();
            if (newNickname.isEmpty()){
                binding.textinputlayoutNickname.setError(getString(R.string.pass_err));
                throw new Exception("Không được để trường rỗng");
            } else {
                binding.textinputlayoutNickname.setError(null);
            }
            mUserViewModel.changeNickname(newNickname);
        }catch (NullPointerException ex){
            Log.e(TAG, "NullPointerException Raise: ", ex);
            binding.btnSubmit.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        } catch (Exception ex) {
            Toast.makeText(requireContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception Raise: ", ex);
            binding.btnSubmit.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void goBackToMain() {
        Navigation.findNavController(requireView())
                .navigate(ChangeDetailFragmentDirections.actionChangeDetailFragmentToUserManagementFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserViewModel.resetCheckValue();
        binding = null;
    }
}