package com.example.donotcheat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ManagementAdapter extends RecyclerView.Adapter<ManagementAdapter.ManagementViewHolder> {
    Context context;
    private ArrayList<String> item = new ArrayList<>();
    OnItemClickListener listener;
    public ManagementAdapter(Context context){this.context = context;}

    public int getItemCount() {
        return item.size();
    }
    public static interface OnItemClickListener {
        public void onItemClick(ManagementViewHolder holder, View view, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ManagementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.user_list,parent,false);
        ManagementAdapter.ManagementViewHolder vh= new ManagementAdapter.ManagementViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ManagementViewHolder holder, int position) {
        String items = item.get(position);
        holder.setItem(items);
    }
    public void addItem(String roomName) {
        item.add(roomName);
    }
    public String getItem(int position) {
        return item.get(position);
    }

    public class ManagementViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;
        TextView itemUserNum;
        View itemView;

        public ManagementViewHolder (View itemView){
            super(itemView);
            itemUserNum = (TextView) itemView.findViewById(R.id.userNum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null) {
                        listener.onItemClick(ManagementViewHolder.this, v, position);
                    }
                }
            });
        }
        public void setItem(String roomName) { itemUserNum.setText(roomName); }
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }
}
