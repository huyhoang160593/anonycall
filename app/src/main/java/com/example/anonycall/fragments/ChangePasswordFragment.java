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
import com.example.anonycall.databinding.ChangePasswordFragmentBinding;
import com.example.anonycall.databinding.SignUpFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;

public class ChangePasswordFragment extends Fragment {
    private final String TAG ="ChangePasswordFragment";
    private ChangePasswordFragmentBinding binding;
    private UserViewModel mUserViewModel;

    public static ChangePasswordFragment newInstance() {
        return new ChangePasswordFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).hidingBottomNavigation(true);

        binding = ChangePasswordFragmentBinding.inflate(inflater,container,false);

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnSubmit.setOnClickListener(v -> changePassword());
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
                    Toast.makeText(requireContext(),"Xự cố xảy ra khi đăng ký",Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.btnSubmit.setEnabled(true);
                    break;
                }
            }
        });
    }

    private void changePassword() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);
        try {
            if(
                    binding.textinputlayoutOldPassword.getEditText() == null ||
                    binding.textinputlayoutNewPassword.getEditText() == null ||
                    binding.textinputlayoutRePassword.getEditText() == null
            ) {
                throw new NullPointerException();
            }
            String oldPasswordString = binding.textinputlayoutOldPassword.getEditText().getText().toString();
            String passwordString = binding.textinputlayoutNewPassword.getEditText().getText().toString();
            String repasswordString = binding.textinputlayoutRePassword.getEditText().getText().toString();
            if (oldPasswordString.isEmpty() || oldPasswordString.length() < 4){
                binding.textinputlayoutOldPassword.setError(getString(R.string.pass_err));
                throw new Exception("Mật khẩu của bạn quá ngắn");
            } else {
                binding.textinputlayoutOldPassword.setError(null);
            }
            if (passwordString.isEmpty() || passwordString.length() < 4){
                binding.textinputlayoutNewPassword.setError(getString(R.string.pass_err));
                throw new Exception("Mật khẩu của bạn quá ngắn");
            } else {
                binding.textinputlayoutNewPassword.setError(null);
            }
            if (repasswordString.isEmpty() || !repasswordString.equals(passwordString)){
                binding.textinputlayoutRePassword.setError(getString(R.string.repassword_err));
                throw new Exception("Mật khẩu nhập lại của bạn không trùng khớp");
            } else {
                binding.textinputlayoutRePassword.setError(null);
            }
            mUserViewModel.changePassword(oldPasswordString,passwordString);
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
        Navigation.findNavController(requireView()).navigate(ChangePasswordFragmentDirections.actionChangePasswordFragmentToWelcomeFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserViewModel.resetCheckValue();
        binding = null;
    }
}