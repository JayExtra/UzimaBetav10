package com.example.uzimabetav10.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uzimabetav10.R;
import com.example.uzimabetav10.ui.SingleEmergencyPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyRecyclerAdapter extends RecyclerView.Adapter<EmergencyRecyclerAdapter.ViewHolder> {

    //setup the list with the emergency recycler adapter
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    public List<EmergencyPosts> emergency_list;
    public Context context;
    List<Address>adresses;
    Geocoder geocoder;


    public EmergencyRecyclerAdapter(List<EmergencyPosts>emergency_list){

        this.emergency_list = emergency_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_list_item, parent , false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        context = parent.getContext();

        return new ViewHolder(view);
    }


    //binds all the views
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {


        final String emergencyPostId = emergency_list.get(position).PostId;

        String dets_text = emergency_list.get(position).getDetails();

        holder.setDetText(dets_text);


        String image_url = emergency_list.get(position).getHouse_image_uri();
        holder.setImage(image_url);

        String title_text = emergency_list.get(position).getTitle();

        holder.setTitle(title_text);

        long milliseconds= emergency_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
        holder.setDate(dateString);

        GeoPoint locgeoPoint = emergency_list.get(position).getLocation();

        if (locgeoPoint == null){

            Toast.makeText(context,"Location error",Toast.LENGTH_SHORT).show();

        }else{


            double lat = locgeoPoint.getLatitude();
            double lng = locgeoPoint.getLongitude();
            //setup geocoder

            geocoder = new Geocoder(context, Locale.getDefault());

            try {

                adresses = geocoder.getFromLocation(lat,lng,1);
                String address = adresses.get(0).getAddressLine(0);

                String fulladdress=  address+"";

                holder.setLocation(fulladdress);



                //Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();



            } catch (IOException e) {
                e.printStackTrace();
            }
        }





        holder.postCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openPost = new Intent(context, SingleEmergencyPost.class);
                openPost.putExtra("DOCUMENT_ID",emergencyPostId);

                context.startActivity(openPost);
            }
        });


        //Get Likes Count
        firebaseFirestore.collection("Emergency_Feeds/" + emergencyPostId + "/comments").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateCommentsCount(count);

                } else {

                    holder.updateCommentsCount(0);

                }

            }
        });












    }

    @Override
    public int getItemCount() {
        return emergency_list.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        private View mView;
        private TextView descText,ttlText,postDate,postLocation;
        private ImageView emergencyImage;
        private CardView postCardView;
        private TextView commentCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            postCardView = mView.findViewById(R.id.post_card);

        }

        public void setDetText (String detText){

            descText = mView.findViewById(R.id.card_description);
            descText.setText(detText);

        }

        public void setImage(String downloadUrl){

            emergencyImage = mView.findViewById(R.id.post_image);

            if(downloadUrl != null){

                Glide.with(context).load(downloadUrl).into(emergencyImage);


            }else{

                emergencyImage.setImageResource(R.drawable.alaert_icon);
               // ViewGroup parent = (ViewGroup) emergencyImage.getParent();
                //parent.removeView(emergencyImage);


            }



        }

        public void setTitle (String titleText){

            ttlText = mView.findViewById(R.id.card_title);
            ttlText.setText(titleText);

        }

        public void setDate (String dateText){

            postDate = mView.findViewById(R.id.time_date);
            postDate.setText(dateText);

        }

        public void setLocation (String locationText){

            postLocation = mView.findViewById(R.id.loc_text);
            postLocation.setText(locationText);

        }

        public void updateCommentsCount(int count){

            commentCount = mView.findViewById(R.id.comment_count);
            commentCount.setText(count + " Comments");

        }


    }



}
