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
import com.example.anonycall.models.FBUser;
import com.example.anonycall.viewModels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AvatarChangeFragment extends Fragment {

    private UserViewModel mUserViewModel;
    private AvatarChangeFragmentBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef,frRef,blRef,rqRef,mRef,dRef;
    public String[] mAvtIds = {
            "https://i.imgur.com/l38Y2Nb.jpg", "https://i.imgur.com/EgeTMEY.jpg",
            "https://i.imgur.com/earGc0b.jpg","https://i.imgur.com/dI2GL0U.jpg",
            "https://i.imgur.com/cRPHDHT.jpg","https://i.imgur.com/lqNTe6O.jpg",
            "https://i.imgur.com/w7u4538.jpg","https://i.imgur.com/kN1WAW6.jpg"
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
            String photoUrl = mAvtIds[position];
            mUserViewModel.changeAvatar(photoUrl);
            updatePhotoUriFirebase(photoUrl);
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

    public void updatePhotoUriFirebase(String photoUri) {
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(user.getUid()).child("avatarURL").setValue(photoUri);
        frRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        Query query = frRef;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    FBUser fbUser = snapshot1.getValue(FBUser.class);
                    mRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(fbUser.getUserID()).child(user.getUid());
                    mRef.child("avatarURL").setValue(photoUri);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        blRef = FirebaseDatabase.getInstance().getReference().child("Blocks");
        Query query2 = blRef;
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    for (DataSnapshot snapshot2:snapshot1.getChildren()){
                        mRef=FirebaseDatabase.getInstance().getReference().child("Blocks").child(snapshot1.getKey()).child(snapshot2.getKey());
                        Query mQuery= mRef.orderByChild("userID").equalTo(user.getUid());
                        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mRef.child("avatarURL").setValue(photoUri);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rqRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        Query query3 = rqRef;
        query3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    for (DataSnapshot snapshot2:snapshot1.getChildren()){
                        System.out.println("@@@@@@@@@@"+snapshot1.getKey());
                        System.out.println("@@@@@@@@@@"+snapshot2.getKey());
                        dRef=FirebaseDatabase.getInstance().getReference().child("Requests").child(snapshot1.getKey()).child(snapshot2.getKey());
                        Query mQuery2= dRef.orderByChild("userID").equalTo(user.getUid());
                        mQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                dRef.child("avatarURL").setValue(photoUri);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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