package com.example.uzimabetav10.Messaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.uzimabetav10.Repositories.FirebaseNotificationRepository;
import com.example.uzimabetav10.ui.Deployments;
import com.example.uzimabetav10.R;
import com.example.uzimabetav10.ui.SingleEmergencyPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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

    //private OnNotificationItemClicked mOnNotificationItemClicked;



    public void setNotificationsList(List<NotificationsConstructor> notificationsList) {
        this.notificationsList = notificationsList;
    }

   /* public NotificationsRecyclerAdapter(List<NotificationsConstructor> notificationsList){

        this.notificationsList = notificationsList;

    }*/

    @NonNull
    @Override
    public NotificationsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_notification_item, parent , false);

        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsRecyclerAdapter.ViewHolder holder, final int position) {

        //instantiate firebase elements
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();


        final String current_user = firebaseAuth.getCurrentUser().getUid();


       //fetch details

        //fetch message

        String from_id = notificationsList.get(position).getFrom();
        final String documentId = notificationsList.get(position).getNotificationId();
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
                String image = documentSnapshot.getString("image");

                holder.fromText.setText(name);
                //holder.roleText.setText(role);

                if (image != null) {
                    //******replacing the dummy image with real profile picture******
                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.grey_background);

                    Glide.with(context).load(image).into(holder.mImageView);
                } else {

                    holder.mImageView.setImageResource(R.drawable.alaert_icon);

                }


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


        holder.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //delete item at position

                DocumentReference notRef = mFirebaseFirestore.collection("users/"+current_user+"/Notifications")
                        .document(documentId);

                notRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                notifyItemRemoved(position);

                                Toast.makeText(context , "Marked as read" , Toast.LENGTH_LONG).show();


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(context , "Could not mark as read:"+e.getMessage() , Toast.LENGTH_SHORT).show();

                            }
                        });


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
        private ImageView mImageView;
        private Button readBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            messageText = mView.findViewById(R.id.message_txt);
            fromText = mView.findViewById(R.id.from_txt);
            mNotsCrd = mView.findViewById(R.id.notification_card);
            mImageView = mView.findViewById(R.id.profile_image);
            readBtn = mView.findViewById(R.id.btn_mark_read);

        }

    }


}
