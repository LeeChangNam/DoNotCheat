package com.example.donotcheat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class CheatListAdapter extends RecyclerView.Adapter<CheatListAdapter.CheatListViewHolder> {
    private Context context;
    private OnItemClickListener listener;
    private ArrayList<String> items = new ArrayList<String>();
    private ArrayList<String> num = new ArrayList<String>();
    private HashMap<String,Object> userItems = new HashMap<>();
    //item 변경만 바꾸면됨
    public void putItem(HashMap<String,Object> userItem) { userItems.putAll(userItem);}
    public Object popItem(String key){ return userItems.get(key); }
    public void addNum(String roomNum){num.add(roomNum);}
    public String getNum(int position) { return num.get(position);}
    public void addItem(String roomName) {items.add(roomName);}
    public String getItem(int position) {return items.get(position);}


    public void setOnItemClickListener(OnItemClickListener listener) {this.listener = listener;}
    public static interface OnItemClickListener { public void onItemClick(CheatListViewHolder holder, View view, int position);}

    @Override
    public int getItemCount() {return items.size();}
    public CheatListAdapter(Context context) {this.context = context;}
    @Override
    public CheatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.user_list, parent, false);

        return new CheatListViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull CheatListViewHolder holder, int position) {
        String item = items.get(position);
        holder.setItem(item);
        holder.setOnItemClickListener(listener);
    }
    static class CheatListViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;
        TextView itemUserNum;
        public CheatListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemUserNum = (TextView) itemView.findViewById(R.id.userList);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(listener != null) {
                        listener.onItemClick(CheatListViewHolder.this, view, position);
                    }
                }
            });

        }
        public void setItem(String userNum) { itemUserNum.setText(userNum); }
        public void setOnItemClickListener(OnItemClickListener listener) {this.listener = listener;}
    }
}
