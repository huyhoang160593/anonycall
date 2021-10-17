package com.example.anonycall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anonycall.R;
import com.example.anonycall.models.ChatMsg;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ChatsmsAdapter extends RecyclerView.Adapter<ChatsmsAdapter.MyHolder>{
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Context mContext;
    private ArrayList<ChatMsg> arrayList = new ArrayList<>();

    public ChatsmsAdapter(Context context, ArrayList<ChatMsg> msgArrayList){
        this.mContext = context;
        this.arrayList = msgArrayList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.singleview_msg, parent, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        holder.txtTime1.setVisibility(View.GONE);
        holder.txtTime2.setVisibility(View.GONE);
        if (arrayList.get(position).getId().equals(user.getUid())){
            holder.txtSMS.setVisibility(View.GONE);
            holder.txtSMS2.setVisibility(View.VISIBLE);
            holder.txtSMS2.setText(arrayList.get(position).getMes());
        } else {
            holder.txtSMS.setVisibility(View.VISIBLE);
            holder.txtSMS2.setVisibility(View.GONE);
            holder.txtSMS.setText(arrayList.get(position).getMes());
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView txtSMS,txtSMS2,txtTime1,txtTime2;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            txtSMS = itemView.findViewById(R.id.textView);
            txtSMS2 = itemView.findViewById(R.id.textView2);
            txtTime1= itemView.findViewById(R.id.time_1);
            txtTime2= itemView.findViewById(R.id.time_2);
        }
    }


}
