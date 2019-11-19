package com.example.haemotracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.viewHolder>{
    public static final String TAG="com.example.haemotracker.recyclerViewAdapter";
    private ArrayList<String> mnames=new ArrayList<>();
    private Context mContext;

    public recyclerViewAdapter(Context context,ArrayList<String> names){
        mnames=names;
        mContext=context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        viewHolder holder =new viewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, final int position) {

        Log.d(TAG,"onBindViewHolder :Called");
        holder.name.setText(mnames.get(position));
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"clicked on :"+mnames.get(position));

            }
        });
    }

    @Override
    public int getItemCount() {
        return mnames.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{


        TextView name;
        LinearLayout parent_layout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.userName);
            parent_layout=itemView.findViewById(R.id.parent_layout);

        }
    }

}
