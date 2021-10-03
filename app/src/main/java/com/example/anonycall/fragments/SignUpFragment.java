package com.example.anonycall.fragments;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.anonycall.R;
import com.example.anonycall.databinding.SignUpFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;

public class SignUpFragment extends Fragment {
    private final String TAG = "SignUpFragment";

    private UserViewModel mUserViewModel;
    private SignUpFragmentBinding binding;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SignUpFragmentBinding.inflate(inflater,container,false);

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginButton.setOnClickListener(v -> goToLoginFragment());

        binding.signUpButton.setOnClickListener(v -> signUpAndLogin());

        mUserViewModel.isNotFalse().observe(getViewLifecycleOwner(), newValue ->{
            int checkValue = newValue.intValue();
            switch (checkValue){
                case 1:{
                    mUserViewModel.resetCheckValue();
                    goToMainFragment();
                    break;
                }
                case 0:{
                    Toast.makeText(requireContext(),"Xự cố xảy ra khi đăng ký",Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.signUpButton.setEnabled(true);
                    break;
                }
            }
        });
    }

    private void signUpAndLogin() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.signUpButton.setEnabled(false);

        try {
            if(
                    binding.textinputlayoutNickname.getEditText() == null ||
                    binding.textinputlayoutEmail.getEditText() == null ||
                    binding.textinputlayoutPassword.getEditText() == null ||
                    binding.textinputlayoutRePassword.getEditText() == null
            ) {
                throw new NullPointerException();
            }
            String nicknameString = binding.textinputlayoutNickname.getEditText().getText().toString();
            String emailString = binding.textinputlayoutEmail.getEditText().getText().toString();
            String passwordString = binding.textinputlayoutPassword.getEditText().getText().toString();
            String repasswordString = binding.textinputlayoutRePassword.getEditText().getText().toString();
            if(nicknameString.isEmpty()){
                binding.textinputlayoutNickname.setError(getString(R.string.nickname_err));
                throw new Exception("Nickname của bạn đang để trống");
            } else {
                binding.textinputlayoutNickname.setError(null);
            }
            if(emailString.isEmpty()){
                binding.textinputlayoutEmail.setError(getString(R.string.email_err));
                throw new Exception("Email của bạn chưa đúng định dạng hoặc đang để trống");
            } else {
                binding.textinputlayoutEmail.setError(null);
            }
            if (passwordString.isEmpty() || passwordString.length() < 4){
                binding.textinputlayoutPassword.setError(getString(R.string.pass_err));
                throw new Exception("Mật khẩu của bạn quá ngắn");
            } else {
                binding.textinputlayoutPassword.setError(null);
            }
            if (repasswordString.isEmpty() || !repasswordString.equals(passwordString)){
                binding.textinputlayoutRePassword.setError(getString(R.string.repassword_err));
                throw new Exception("Mật khẩu nhập lại của bạn không trùng khớp");
            } else {
                binding.textinputlayoutRePassword.setError(null);
            }

            mUserViewModel.signupWithEmail(nicknameString,emailString,passwordString);

        }catch (NullPointerException ex){
            Log.e(TAG, "NullPointerException Raise: ", ex);
            binding.signUpButton.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        } catch (Exception ex) {
            Toast.makeText(requireContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception Raise: ", ex);
            binding.signUpButton.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void goToLoginFragment() {
        Navigation.findNavController(requireView()).navigate(SignUpFragmentDirections.actionSignUpFragmentToLoginFragment());
    }

    private void goToMainFragment() {
        Navigation.findNavController(requireView()).navigate(SignUpFragmentDirections.actionSignUpFragmentToWelcomeFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserViewModel.resetCheckValue();
        binding = null;
    }
}