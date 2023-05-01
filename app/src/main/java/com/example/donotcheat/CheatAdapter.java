package com.example.donotcheat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CheatAdapter extends RecyclerView.Adapter<CheatAdapter.CheatViewHolder>{
    Context context;
    private ArrayList<String> item = new ArrayList<>();

    OnItemClickListener listener;

    public CheatAdapter(Context context){this.context = context;}

    public static interface OnItemClickListener {
        public void onItemClick(CheatAdapter.CheatViewHolder holder, View view, int position);
    }

    public void setOnItemClickListener(CheatAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public CheatAdapter.CheatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.user_list,parent,false);
        CheatAdapter.CheatViewHolder vh= new CheatAdapter.CheatViewHolder(view);
        return vh;
    }
    @Override
    public void onBindViewHolder(CheatAdapter.CheatViewHolder holder, int position) {
        String items = item.get(position);
        holder.setItem(items);
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class CheatViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;
        TextView itemUserNum;
        View itemView;

        public CheatViewHolder (View itemView){
            super(itemView);
            itemUserNum = (TextView) itemView.findViewById(R.id.userList);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null) {
                        listener.onItemClick(CheatViewHolder.this, v, position);
                    }
                }
            });
        }
        public void setItem(String roomName) { itemUserNum.setText(roomName); }
        public void setOnItemClickListener(CheatAdapter.OnItemClickListener listener) {
            this.listener = listener;
        }
    }
}
