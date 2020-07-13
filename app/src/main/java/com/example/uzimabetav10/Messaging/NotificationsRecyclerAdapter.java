package com.example.uzimabetav10.Messaging;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimabetav10.Deployments;
import com.example.uzimabetav10.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    public List<NotificationsConstructor> notificationsList;
    public Context context;
    public FirebaseFirestore mFirebaseFirestore;
    public FirebaseAuth firebaseAuth;
    String message , fromID;


    public NotificationsRecyclerAdapter(List<NotificationsConstructor> notificationsList){

        this.notificationsList = notificationsList;

    }

    @NonNull
    @Override
    public NotificationsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_notification_item, parent , false);

        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsRecyclerAdapter.ViewHolder holder, int position) {

        //instantiate firebase elements
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();


        //fetch details

        //fetch message

        String from_id = notificationsList.get(position).getFrom();
        fromID = from_id;

        holder.messageText.setText(notificationsList.get(position).getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.messageText.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

        mFirebaseFirestore.collection("Employee_Details").document(from_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String name = documentSnapshot.getString("first_name");
                String role = documentSnapshot.getString("employee_role");

                holder.fromText.setText(name);
                //holder.roleText.setText(role);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context , "Error in loading notifications",Toast.LENGTH_SHORT).show();

            }
        });


        holder.mNotsCrd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(context , Deployments.class);
                context.startActivity(intent);
                ((Activity)context).finish();


            }
        });


    }

    @Override
    public int getItemCount() {

        if(notificationsList != null) {

            return notificationsList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText , fromText , roleText;
        private View mView;
        private CardView mNotsCrd;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            messageText = mView.findViewById(R.id.message_txt);
            fromText = mView.findViewById(R.id.from_txt);
            mNotsCrd = mView.findViewById(R.id.notification_card);







        }
    }
}
