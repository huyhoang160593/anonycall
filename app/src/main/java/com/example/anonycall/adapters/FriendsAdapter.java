package com.example.anonycall.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anonycall.R;
import com.example.anonycall.fragments.ChatWithFriendFragment;
import com.example.anonycall.models.FBUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyHolder> {
    private ArrayList<FBUser> listFriends;
    private Context mContext;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference requestRef, frRef, bRef;

    public FriendsAdapter(Context mContext, ArrayList<FBUser> listFr) {
        this.mContext = mContext;
        this.listFriends = listFr;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_friend, parent, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.txtName.setText(listFriends.get(position).getDisplayName());
        holder.imgFr.setImageURI(Uri.parse(listFriends.get(position).getAvatarURL()));
        FBUser mUser = listFriends.get(position);
        String id = listFriends.get(position).getUserID();
        String avt = listFriends.get(position).getAvatarURL();
        String name = listFriends.get(position).getDisplayName();
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveFriend(id);
                AddToBlockedList(mUser);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveFriend(id);
            }
        });
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("UID",id);
                args.putString("AVT",avt);
                args.putString("NAME",name);
                Navigation.findNavController(view).navigate(R.id.action_contactFragment_to_chatFragment,args);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFriends.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgFr;
        MaterialButton delete, block;
        AppCompatImageButton chat;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.username_fr);
            imgFr = itemView.findViewById(R.id.image_fr);
            delete = itemView.findViewById(R.id.delete);
            block = itemView.findViewById(R.id.block);
            chat = itemView.findViewById(R.id.chat);

        }
    }

    private void AddToBlockedList(FBUser us) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bRef = FirebaseDatabase.getInstance().getReference().child("Blocks").child(user.getUid());
        HashMap hashMap = new HashMap();
        hashMap.put("displayName", us.getDisplayName());
        hashMap.put("email", us.getEmail());
        hashMap.put("avatarURL", us.getAvatarURL());
        hashMap.put("userID", us.getUserID());
        bRef.child(us.getUserID()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                System.out.println("Complete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("failure");
            }
        });
    }

    private void RemoveFriend(String id) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        frRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        frRef.child(id).removeValue().addOnCompleteListener(task -> System.out.println("Complete"))
                .addOnFailureListener(e -> System.out.println("Failure"));
    }

}
