package com.example.anonycall.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.anonycall.MainActivity;
import com.example.anonycall.R;
import com.example.anonycall.databinding.UserManagementFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;

public class UserManagementFragment extends Fragment {
    private final String TAG = "UserManagementFragment";

    private UserViewModel mUserViewModel;
    private UserManagementFragmentBinding binding;

    public static UserManagementFragment newInstance() {
        return new UserManagementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding = UserManagementFragmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Use the ViewModel and biding
        //make bottom nav visible
        ((MainActivity) requireActivity()).hidingBottomNavigation(false);

        binding.deleteAccBtn.setVisibility(View.GONE);

        mUserViewModel.getUser().observe(getViewLifecycleOwner(), newUser -> {
            if(newUser != null) {
                binding.emailLl.setVisibility(View.VISIBLE);
                binding.avatarLl.setVisibility(View.VISIBLE);
                binding.emailTextview.setText(newUser.getEmail());
                if(newUser.getAvatarURL() != null){
                    binding.avatarBtn.setImageURI(Uri.parse(newUser.getAvatarURL()));
                }
                binding.avatarBtn.setOnClickListener(v -> goToSetAvatarFragment());

                binding.changeEmailBtn.setOnClickListener(v -> goToChangeDetailFragment());

                binding.logoutLoginBtn.setText(R.string.signout);
                binding.logoutLoginBtn.setOnClickListener(v -> signOutFirebase());
            } else {
                binding.emailLl.setVisibility(View.GONE);
                binding.avatarLl.setVisibility(View.GONE);
                binding.changePasswordBtn.setVisibility(View.GONE);
                binding.logoutLoginBtn.setText(R.string.login);
                binding.logoutLoginBtn.setOnClickListener(v -> loginFirebase());
            }
        });
    }

    private void goToChangeDetailFragment() {

    }

    private void goToSetAvatarFragment() {
        Navigation.findNavController(requireView())
                .navigate(UserManagementFragmentDirections.actionUserManagementFragmentToAvatarChangeFragment());
    }

    private void loginFirebase() {
        Navigation.findNavController(requireView())
                .navigate(UserManagementFragmentDirections.actionUserManagementFragmentToLoginFragment());
    }

    private void signOutFirebase() {
        mUserViewModel.signOut();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}