package com.example.donotcheat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ManagementAdapter extends RecyclerView.Adapter<ManagementAdapter.ManagementViewHolder> {
    private ArrayList<String> item;

    public ManagementAdapter( ArrayList<String> item){this.item = item;}

    public int getItemCount() {
        return item.size();
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
        holder.item_userNum.setText(item.get(position).getModelName());
    }

    public class ManagementViewHolder extends RecyclerView.ViewHolder {
        TextView item_userNum;
        View itemView;

        public ManagementViewHolder (View itemView){
            super(itemView);
            this.itemView=itemView;

            item_userNum=itemView.findViewById(R.id.userNum);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
