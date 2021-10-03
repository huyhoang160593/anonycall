package com.example.anonycall.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.anonycall.MainActivity;
import com.example.anonycall.R;
import com.example.anonycall.adapters.GridAdapter;
import com.example.anonycall.databinding.AvatarChangeFragmentBinding;
import com.example.anonycall.viewModels.UserViewModel;

public class AvatarChangeFragment extends Fragment {

    private UserViewModel mUserViewModel;
    private AvatarChangeFragmentBinding binding;

    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";
    public Integer[] mAvtIds = {
            R.drawable.avt1, R.drawable.avt2,
            R.drawable.avt3,R.drawable.avt4,
            R.drawable.avt5,R.drawable.avt6,
            R.drawable.avt7,R.drawable.avt8
    };

    public static AvatarChangeFragment newInstance() {
        return new AvatarChangeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding = AvatarChangeFragmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).hidingBottomNavigation(true);

        binding.imageAvt.setAdapter(new GridAdapter(requireContext()));
        binding.imageAvt.setOnItemClickListener((adapterView, view1, position, l) -> {
            String photoUri = ANDROID_RESOURCE + requireActivity().getPackageName() + FORESLASH + mAvtIds[position];
            mUserViewModel.changeAvatar(photoUri);
        });

        mUserViewModel.isNotFalse().observe(getViewLifecycleOwner(), newValue ->{
            int checkValue = newValue.intValue();
            switch (checkValue){
                case 1:{
                    mUserViewModel.resetCheckValue();
                    goBackToUserManagement();
                    break;
                }
                case 0:{
                    Toast.makeText(requireContext(),"Xự cố xảy ra khi thay đổi avatar",Toast.LENGTH_LONG).show();
                    break;
                }
            }
        });
    }

    private void goBackToUserManagement() {
        Navigation.findNavController(requireView()).navigate(AvatarChangeFragmentDirections.actionAvatarChangeFragmentToUserManagementFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserViewModel.resetCheckValue();
        binding = null;
    }
}