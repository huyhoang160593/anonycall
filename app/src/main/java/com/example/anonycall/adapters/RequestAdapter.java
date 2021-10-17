package com.example.anonycall.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anonycall.R;
import com.example.anonycall.models.FBUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyHolder> {
    private ArrayList<FBUser> listFriends;
    private Context mContext;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference requestRef,frRef,bRef;

    public RequestAdapter(Context mContext, ArrayList<FBUser> listFr) {
        this.mContext = mContext;
        this.listFriends = listFr;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_request_friend, parent, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.txtName.setText(listFriends.get(position).getDisplayName());
        holder.imgFr.setImageURI(Uri.parse(listFriends.get(position).getAvatarURL()));
        String id = listFriends.get(position).getUserID();
        FBUser mUser = listFriends.get(position);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveRequest(id);
                AddToFriendList(mUser);
            }
        });
        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveRequest(id);

            }
        });
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveRequest(id);
                AddToBlockedList(mUser);
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
        MaterialButton decline,accept,block;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.username_rqfr);
            imgFr = itemView.findViewById(R.id.image_rqfr);
            block = itemView.findViewById(R.id.block_h);
            decline = itemView.findViewById(R.id.decline_request);
            accept = itemView.findViewById(R.id.accept_request);
        }
    }

    private void RemoveRequest(String id){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(user.getUid());
        requestRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("Complete");
            }
        });
    }
    private void AddToFriendList(FBUser us){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        frRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(user.getUid());
        HashMap hashMap = new HashMap();
        hashMap.put("displayName",us.getDisplayName());
        hashMap.put("email",us.getEmail());
        hashMap.put("avatarURL",us.getAvatarURL());
        hashMap.put("userID",us.getUserID());
        frRef.child(us.getUserID()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
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
        frRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(us.getUserID());
        HashMap hashMap2 = new HashMap();
        hashMap.put("displayName",user.getDisplayName());
        hashMap.put("email",user.getEmail());
        hashMap.put("avatarURL",user.getPhotoUrl().toString());
        hashMap.put("userID",user.getUid());
        frRef.child(user.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
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

    private void AddToBlockedList(FBUser us){
        auth= FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bRef = FirebaseDatabase.getInstance().getReference().child("Blocks").child(user.getUid());
        HashMap hashMap = new HashMap();
        hashMap.put("displayName",us.getDisplayName());
        hashMap.put("email",us.getEmail());
        hashMap.put("avatarURL",us.getAvatarURL());
        hashMap.put("userID",us.getUserID());
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
}