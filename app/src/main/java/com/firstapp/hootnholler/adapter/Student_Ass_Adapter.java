package com.firstapp.hootnholler.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firstapp.hootnholler.R;
import com.firstapp.hootnholler.Y_AsgmDetails;
import com.firstapp.hootnholler.entity.Assignment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Student_Ass_Adapter extends RecyclerView.Adapter<Student_Ass_Adapter.MyViewHolder> {
    Context context;
    ArrayList<Assignment> asgmList;
    String currentClassCode;

    String uid;
    public Student_Ass_Adapter(Context context, ArrayList<Assignment> asgmList,String currentClassCode,String uid) {
        this.context = context;
        this.asgmList = asgmList;
        this.currentClassCode=currentClassCode;
        this.uid=uid;
    }

    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.asgm_item, parent, false);
        return new MyViewHolder(v);
    }

    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Assignment asgm = asgmList.get(position);
        holder.asgmTitle.setText(asgm.getTitle());

        if (asgm.getDueDate() != null) {
            try {
                long dueDateTimestamp = Long.parseLong(asgm.getDueDate());
                holder.asgmDueDate.setText("Due " + convertTimestampToDateTime(dueDateTimestamp));

                // Check the submission status from the database
                DatabaseReference submissionRef = FirebaseDatabase.getInstance().getReference("Classroom")
                        .child(currentClassCode).child("Assignment").child(asgm.getAssKey()).child("Submission").child(uid);

                submissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Assignment is submitted
                            holder.dueMsg.setText("Submitted");
                            holder.dueMsg.setTextColor(Color.GREEN);
                            holder.dueMsg.setVisibility(View.VISIBLE);
                        } else {
                            // Assignment is not submitted
                            long currentTimeMillis = System.currentTimeMillis();
                            if (dueDateTimestamp < currentTimeMillis) {
                                holder.dueMsg.setText("DUE");
                                holder.dueMsg.setVisibility(View.VISIBLE);
                            } else {
                                holder.dueMsg.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            holder.asgmDueDate.setText("No Due Date");
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Y_AsgmDetails.class);
                intent.putExtra("classCode", currentClassCode);
                intent.putExtra("assID", asgm.getAssKey());
                intent.putExtra("studentUID", uid);
                context.startActivity(intent);
            }
        });
    }
    public int getItemCount() {
        return asgmList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView asgmTitle,asgmDueDate,dueMsg;
        CardView card;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            asgmTitle = itemView.findViewById(R.id.asgmTitle);
            asgmDueDate = itemView.findViewById(R.id.dueMsg);
            dueMsg=itemView.findViewById(R.id.dateTime);
            card=itemView.findViewById(R.id.card);
        }
    }
    public static String convertTimestampToDateTime(long timestamp){
        try{
            Date currentDate = (new Date(timestamp));
            SimpleDateFormat sfd = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss", Locale.getDefault());
            return sfd.format(currentDate);
        } catch(Exception e){
            return "date";
        }

    }
}
