package com.example.anonycall.fragments;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
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
import com.example.anonycall.databinding.LoginFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginFragment extends Fragment {
    private final String TAG = "LoginFragment";
    private UserViewModel mUserViewModel;
    private LoginFragmentBinding binding;
//    private GoogleSignInOptions gso;
//    private GoogleSignInClient gsoClient;


    FirebaseAuth auth = FirebaseAuth.getInstance();

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hiding bottom nav
        ((MainActivity) requireActivity()).hidingBottomNavigation(true);

//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.anony_web_client_id))
//                .requestEmail()
//                .build();
//        gsoClient = GoogleSignIn.getClient(requireContext(),gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LoginFragmentBinding.inflate(inflater,container,false);

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.progressHorizontal.setVisibility(View.INVISIBLE);

        binding.loginBtn.setOnClickListener(v -> {
            binding.progressHorizontal.setIndeterminate(true);
            binding.progressHorizontal.setVisibility(View.VISIBLE);
            LoginWithAccount();
        });
        binding.signUpTextview.setOnClickListener(v -> goToSignupFragment());
//        binding.loginGoogle.setOnClickListener(v -> {
//            LoginWithGoogle();
//        });
    }

//    private void LoginWithGoogle() {
//        Intent signInIntent = gsoClient.getSignInIntent();
//        activityResultLauncher.launch(signInIntent);
//    }

//    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getResultCode() == Activity.RESULT_OK) {
//                    Intent data = result.getData();
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                    try {
//                        // Google Sign In was successful, authenticate with Firebase
//                        GoogleSignInAccount account = task.getResult(ApiException.class);
//                        Log.e("GOOGLE LOGIN:", "firebaseAuthWithGoogle:" + account.getId());
//                        firebaseAuthWithGoogle(account.getIdToken());
//                    } catch (ApiException e) {
//                        // Google Sign In failed, update UI appropriately
//                        binding.loginBtn.setClickable(true);
//                        binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                        Log.e("GOOGLE LOGIN", "Google sign failed", e);
//                        Toast.makeText(requireContext(), "Google login failed!", Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    binding.loginBtn.setClickable(true);
//                    binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                    Log.e("ACTIVITY_RESULT","ResultCode:" + result.getResultCode());
//                }
//            }
//    );

//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.e("GOOGLE LOGIN", "signInWithCredential:success");
//                        // TODO: Vô màn chính ở đây
//                        goToMainFragment();
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        binding.loginBtn.setClickable(true);
//                        binding.progressHorizontal.setVisibility(View.INVISIBLE);
//                        Log.e("GOOGLE LOGIN", "signInWithCredential:failure", task.getException());
//                        Toast.makeText(requireContext(), "SignIn with credential failed!", Toast.LENGTH_LONG).show();
//                    }
//                });
//    }


    private void LoginWithAccount() {
        try {
            binding.loginBtn.setEnabled(false);
            if (
                    binding.textinputlayoutEmail.getEditText() == null ||
                    binding.textinputlayoutPassword.getEditText() == null
            ) {
                throw new NullPointerException();
            }
            String emailString = binding.textinputlayoutEmail.getEditText().getText().toString();
            String passwordString = binding.textinputlayoutPassword.getEditText().getText().toString();

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
            auth.signInWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.e(TAG,"Đăng nhập thành công");
                    mUserViewModel.getInitialUser();
                    goToMainFragment();
                } else {
                    Log.e(TAG, "signInWithEmailAndPassword:failure", task.getException());
                    Toast.makeText(requireContext(), "Email hoặc mật khẩu không đúng, hoặc tài khoản không tồn tại",
                            Toast.LENGTH_SHORT).show();
                    binding.loginBtn.setEnabled(true);
                    binding.progressHorizontal.setVisibility(View.INVISIBLE);
                }
            });

        } catch (NullPointerException ex){
            Log.e(TAG, "NullPointerException Raise: ", ex);
            binding.loginBtn.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.INVISIBLE);
        } catch (Exception ex) {
            Toast.makeText(requireContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception Raise: ", ex);
            binding.loginBtn.setEnabled(true);
            binding.progressHorizontal.setVisibility(View.INVISIBLE);
        }
    }

    private void goToSignupFragment() {
        Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment());
    }

    // TODO: Di chuyển đến màn hình chính
    private void goToMainFragment() {
        Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionLoginFragmentToWelcomeFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}