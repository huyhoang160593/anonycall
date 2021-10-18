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

public class ChangeDetailFragment extends Fragment {
    private final String TAG = "ChangeDetailFragment";

    private UserViewModel mUserViewModel;
    private ChangeDetailFragmentBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef,frRef,blRef,rqRef,mRef,dRef;
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
            updateNameFirebase(newNickname);
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


    private void updateNameFirebase(String name) {
        System.out.println(R.drawable.ic_baseline_no_photography_121);
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(user.getUid()).child("displayName").setValue(name);
        frRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        Query query = frRef;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    FBUser fbUser = snapshot1.getValue(FBUser.class);
                    mRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(fbUser.getUserID()).child(user.getUid());
                    mRef.child("displayName").setValue(name);
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
                                mRef.child("displayName").setValue(name);
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
                        dRef=FirebaseDatabase.getInstance().getReference().child("Requests").child(snapshot1.getKey()).child(snapshot2.getKey());
                        Query mQuery2= dRef.orderByChild("userID").equalTo(user.getUid());
                        mQuery2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                dRef.child("displayName").setValue(name);
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserViewModel.resetCheckValue();
        binding = null;
    }
}