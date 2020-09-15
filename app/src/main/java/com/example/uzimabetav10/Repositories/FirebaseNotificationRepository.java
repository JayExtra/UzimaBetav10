package com.example.uzimabetav10.Repositories;


import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.uzimabetav10.Messaging.NotificationsConstructor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirebaseNotificationRepository {

    private OnFirestoreTaskComplete mOnFirestoreTaskComplete;

    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private String current_user;



    public FirebaseNotificationRepository(OnFirestoreTaskComplete onFirestoreTaskComplete){

        this.mOnFirestoreTaskComplete = onFirestoreTaskComplete;

    }


    public void getNotificationData(){

        current_user = mFirebaseAuth.getCurrentUser().getUid();

        CollectionReference notsRef = mFirebaseFirestore.collection("users")
                .document(current_user).collection("Notifications");

        notsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()){

                    mOnFirestoreTaskComplete.notListDataAdded(task.getResult().toObjects(NotificationsConstructor.class));

                }else{

                    mOnFirestoreTaskComplete.onError(task.getException());

                }

            }
        });


    }


    public interface OnFirestoreTaskComplete{

        void notListDataAdded(List<NotificationsConstructor> notListModelList);
        void onError(Exception e);


    }
}
