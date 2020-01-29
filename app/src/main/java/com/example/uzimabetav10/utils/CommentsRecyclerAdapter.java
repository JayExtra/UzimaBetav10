package com.example.uzimabetav10.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimabetav10.R;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;

    public CommentsRecyclerAdapter(List<Comments>commentsList){
        this.commentsList = commentsList;
    }


    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent , false);

        context = parent.getContext();

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String message_text = commentsList.get(position).getMessage();

        holder.setMessageText(message_text);

        String user_id = commentsList.get(position).getUser_id();

    }

    @Override
    public int getItemCount() {
        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView commentText;
        private View mView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessageText (String messText){

            commentText = mView.findViewById(R.id.comment);
            commentText.setText(messText);

        }
    }
}
