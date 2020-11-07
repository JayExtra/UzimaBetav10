package com.example.uzimabetav10.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimabetav10.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class PaymentRecyclerAdapter  extends RecyclerView.Adapter<PaymentRecyclerAdapter.ViewHolder> {

    public List<Payment> payment_list;
    public Context context;


    public PaymentRecyclerAdapter(List<Payment>payment_list){

        this.payment_list = payment_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_payment_card, parent , false);

        context = parent.getContext();

        return new PaymentRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        //bind elements

        holder.phnNumTv.setText("+"+payment_list.get(position).getPhone());
        holder.amntTv.setText("Ksh. "+payment_list.get(position).getAmount());
        holder.transId.setText(payment_list.get(position).getReceipt());

        long milliseconds= payment_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
        holder.dateTv.setText(dateString);


    }

    @Override
    public int getItemCount() {
        if(payment_list != null) {

            return payment_list.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {

        private TextView transId , dateTv , amntTv , phnNumTv;
        private View mView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            transId = mView.findViewById(R.id.trans_id_tv);
            dateTv = mView.findViewById(R.id.date_tv);
            amntTv = mView.findViewById(R.id.amnt_tv);
            phnNumTv = mView.findViewById(R.id.phn_num_tv);
        }
    }
}
