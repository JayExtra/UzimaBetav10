package com.example.uzimabetav10.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimabetav10.EmergencyFeeds;
import com.example.uzimabetav10.R;
import com.example.uzimabetav10.SingleEmergencyPost;

import java.util.List;

public class DeploymentRecyclerAdapter extends RecyclerView.Adapter<DeploymentRecyclerAdapter.ViewHolder> {


    public List<DeploymentConstructor> deploymentsList;
    public Context context;
    private static final int REQUEST_CALL =1;

    public DeploymentRecyclerAdapter(List<DeploymentConstructor>deploymentList){
        this.deploymentsList = deploymentList;
    }

    public DeploymentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_deployment_item, parent , false);

        context = parent.getContext();


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeploymentRecyclerAdapter.ViewHolder holder, int position) {

        String name_text = deploymentsList.get(position).getDeployed_driver();
        holder.settingName(name_text);

        String ambulance_plate = deploymentsList.get(position).getDeployed_ambulance();
        holder.settingAmbulance(ambulance_plate);

        String drivers_company = deploymentsList.get(position).getDeployed_company();
        holder.settingCompany(drivers_company);

       final String number = deploymentsList.get(position).getDriver_number();


        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dial= "tel:" + number;
                Intent callDriver = new Intent(Intent.ACTION_CALL, Uri.parse(dial));

                context.startActivity(callDriver);
            }
        });




    }

    @Override
    public int getItemCount() {

        if(deploymentsList != null) {

            return deploymentsList.size();

        } else {

            return 0;

        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView driverName , ambPlate , driverComp;
        private TextView deployText;
        private Button callButton;

        private View mView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            callButton = mView.findViewById(R.id.call_driver_btn);
        }

        public void settingName( String nameText){

            driverName = mView.findViewById(R.id.driver_name);
            driverName.setText(nameText);

        }

        public void settingAmbulance( String ambText){

            ambPlate = mView.findViewById(R.id.ambulance_plate);
            ambPlate.setText(ambText);

        }

        public void settingCompany( String compText){

            driverComp = mView.findViewById(R.id.drivers_company);
            driverComp.setText(compText);

        }
    }

}

