package com.example.anonycall.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.anonycall.MainActivity;
import com.example.anonycall.adapters.BlockedFrAdapter;
import com.example.anonycall.adapters.FriendsAdapter;
import com.example.anonycall.adapters.RequestAdapter;
import com.example.anonycall.databinding.FragmentContactBinding;
import com.example.anonycall.models.FBUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ContactFragment extends Fragment {
    private FragmentContactBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef,friendRef,requestRef,blockRef,mRef;
    private StorageReference storageRef;
    static ArrayList<FBUser> list= new ArrayList<>();
    private FriendsAdapter friendsAdapter;
    private RequestAdapter requestAdapter;
    private BlockedFrAdapter blockedFrAdapter;
    public static FBUser selectUser=new FBUser();
    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) requireActivity()).hidingBottomNavigation(false);

        LoadRequestList("");
        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.searchText.getText().toString().matches(""))
                    Toast.makeText(getContext(),"Hãy nhập tên của bạn bè",Toast.LENGTH_SHORT).show();
                else {

                    if (binding.searchText.getText().toString().contains("@gmail.com")){
                        System.out.println("Search email");
                        searchByEmail(binding.searchText.getText().toString());
                        binding.searchText.setText("");
                    }else {
                        searchByName(binding.searchText.getText().toString());
                        binding.searchText.setText("");
                    }

                }
            }
        });
        binding.totalFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadFriendsList("");
                binding.totalFriend.setBackgroundColor(Color.GRAY);
                binding.friendRequest.setBackgroundColor(Color.TRANSPARENT);
                binding.blockedPeople.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        binding.friendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadRequestList("");
                binding.totalFriend.setBackgroundColor(Color.TRANSPARENT);
                binding.friendRequest.setBackgroundColor(Color.GRAY);
                binding.blockedPeople.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        binding.blockedPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadBlockedList("");
                binding.totalFriend.setBackgroundColor(Color.TRANSPARENT);
                binding.friendRequest.setBackgroundColor(Color.TRANSPARENT);
                binding.blockedPeople.setBackgroundColor(Color.GRAY);
            }
        });

    }

    private void searchByEmail(String email) {
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = userRef.orderByChild("email").equalTo(email);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FBUser mUser = snapshot.getValue(FBUser.class);
                if (mUser!=null){
                    CheckBlocked(mUser);
                }else{
                    Toast.makeText(getContext(),"Địa chỉ email chưa đăng kí",Toast.LENGTH_SHORT).show();
                }            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchByName(String name){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = userRef.orderByChild("displayName").equalTo(name);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FBUser mUser = snapshot.getValue(FBUser.class);
                if (mUser!=null){
                    CheckBlocked(mUser);
                }else {
                    Toast.makeText(getContext(),"Không tìm thấy người dùng",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void CheckBlocked(FBUser fUser){

        blockRef = FirebaseDatabase.getInstance().getReference().child("Blocks").child(fUser.getUserID());
        Query query = blockRef.orderByChild("userID").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FBUser mUser = snapshot.getValue(FBUser.class);
                if (mUser==null){
                    sendRequestFriend(fUser);

                }else{
                    Toast.makeText(getContext(),"Bạn không thể gửi lời mời kết bạn đến người dùng này",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendRequestFriend(FBUser fbUser){

        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        HashMap hashMap = new HashMap();
        hashMap.put("displayName", user.getDisplayName());
        hashMap.put("email", user.getEmail());
        hashMap.put("avatarURL", user.getPhotoUrl().toString());
        hashMap.put("userID", user.getUid());
        requestRef.child(fbUser.getUserID()).child(user.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Toast.makeText(getContext(),"Đã gửi lời mời kết bạn",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Có lỗi xảy ra",Toast.LENGTH_SHORT).show();
            }
        });


    }


    //add vô đoạn signup
    private void addUser(){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("user");
        HashMap hashMap = new HashMap();
        hashMap.put("displayName",user.getDisplayName());
        hashMap.put("email",user.getEmail());
        hashMap.put("avatarURL","android.resource://com.example.anonycall/2131165332"); //đây là cái no photography nè
        hashMap.put("userID",user.getUid());
        userRef.child(user.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Toast.makeText(getContext(),"complete",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"failure",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadFriendsList(String s){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        Query query = friendRef;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                list.clear();
                for (DataSnapshot snapshot1: datasnapshot.getChildren()){
                    FBUser fbUser = snapshot1.getValue(FBUser.class);
                    list.add(fbUser);
                }
                System.out.println(list);
                friendsAdapter = new FriendsAdapter(getContext(),list);
                binding.recyclerViewContact.setAdapter(friendsAdapter);
                binding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LoadRequestList(String s){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(user.getUid());
        Query query = requestRef.orderByChild("displayName");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                list.clear();
                for (DataSnapshot snapshot1: datasnapshot.getChildren()){
                    FBUser fbUser = snapshot1.getValue(FBUser.class);
                    list.add(fbUser);

                }
                requestAdapter = new RequestAdapter(getContext(),list);
                binding.recyclerViewContact.setAdapter(requestAdapter);
                binding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void LoadBlockedList(String s){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        blockRef = FirebaseDatabase.getInstance().getReference().child("Blocks").child(user.getUid());
        Query query = blockRef.orderByChild("displayName");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                list.clear();
                for (DataSnapshot snapshot1: datasnapshot.getChildren()){
                    FBUser fbUser = snapshot1.getValue(FBUser.class);
                    list.add(fbUser);

                }
                System.out.println(list);
                blockedFrAdapter = new BlockedFrAdapter(getContext(),list);
                binding.recyclerViewContact.setAdapter(blockedFrAdapter);
                binding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}