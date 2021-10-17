package com.example.anonycall.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anonycall.R;
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

public class BlockedFrAdapter extends RecyclerView.Adapter<BlockedFrAdapter.MyHolder> {
    private ArrayList<FBUser> listFriends;
    private Context mContext;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference bRef;

    public BlockedFrAdapter(Context mContext, ArrayList<FBUser> listFr) {
        this.mContext = mContext;
        this.listFriends = listFr;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_blocked_friend, parent, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.txtName.setText(listFriends.get(position).getDisplayName());
        holder.imgFr.setImageURI(Uri.parse(listFriends.get(position).getAvatarURL()));
        String id = listFriends.get(position).getUserID();
        holder.unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveBlock(id);
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
        MaterialButton unblock;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.username_bl);
            imgFr = itemView.findViewById(R.id.image_bl);
            unblock = itemView.findViewById(R.id.unblock);
        }
    }

    private void RemoveBlock(String id){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bRef = FirebaseDatabase.getInstance().getReference().child("Blocks").child(user.getUid());
        bRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Complete");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Failure");
            }
        });
    }
}