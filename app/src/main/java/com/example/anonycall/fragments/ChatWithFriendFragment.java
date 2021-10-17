package com.example.anonycall.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.anonycall.MainActivity;
import com.example.anonycall.R;
import com.example.anonycall.adapters.ChatsmsAdapter;
import com.example.anonycall.databinding.FragmentChatWithFriendBinding;
import com.example.anonycall.models.ChatMsg;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatWithFriendFragment extends Fragment {
    private FragmentChatWithFriendBinding binding;
    private String FrID,FrName,FrAvt;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference mesRef,mRef;
    private ChatsmsAdapter adapter;
    static ArrayList<ChatMsg> list = new ArrayList<>();
    public ChatWithFriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrID = getArguments().getString("UID");
        FrName = getArguments().getString("NAME");
        FrAvt = getArguments().getString("AVT");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatWithFriendBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) requireActivity()).hidingBottomNavigation(true);

        binding.frImg.setImageURI(Uri.parse(FrAvt));
        binding.frName.setText(FrName);
        LoadSMS();
        binding.back.setOnClickListener(view1 -> goBackToContact());
        binding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSMS();
            }
        });
    }

    private void goBackToContact() {
        Navigation.findNavController(requireView()).navigate(ChatWithFriendFragmentDirections.actionChatFragmentToContactFragment());
    }

    private void SendSMS(){
        String msg = binding.inputMsg.getText().toString().trim();
        //upd firebase
        if (!msg.isEmpty()) {
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            mesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
            HashMap hashMap = new HashMap();
            hashMap.put("mes",msg);
            hashMap.put("time", Calendar.getInstance().getTime().toString());
            hashMap.put("id",user.getUid());
            String mesID = FrID + "_"+user.getUid();
            if (FrID.compareTo(user.getUid())<0){ //id lớn hơn ở trc
                mesID = user.getUid() + "_"+FrID;
            }
            mesRef.child(mesID).push().updateChildren(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(getContext(), "Đã gửi", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Không gửi được tin nhắn,hãy thử lại", Toast.LENGTH_SHORT).show());

            //clear
            binding.inputMsg.setText("");
        }
    }

    private void LoadSMS(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        String mesID = FrID + "_"+user.getUid();
        if (FrID.compareTo(user.getUid())<0){ //id lớn hơn ở trc
            mesID = user.getUid() + "_"+FrID;
        }
        Query query2=mRef.child(mesID);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    ChatMsg msg = snapshot1.getValue(ChatMsg.class);
                    list.add(msg);
                }
                adapter =new ChatsmsAdapter(getContext(),list);
                binding.recyclerViewMsg.setAdapter(adapter);
                binding.recyclerViewMsg.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}