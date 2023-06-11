package com.example.donotcheat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ExamineeInfo extends AppCompatActivity {
    private Intent secondIntent;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private CheatListAdapter adapter;
    private HashMap<String,Object> cheatItems = new HashMap<>();
    private FirebaseFirestore cheatInfo = FirebaseFirestore.getInstance();
    private String code;
    private String examineeNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examinee_info);

        secondIntent = getIntent();
        code = secondIntent.getStringExtra("방번호");
        examineeNum= secondIntent.getStringExtra("수험번호");

        recyclerView = (RecyclerView) findViewById(R.id.showCheatListRecylerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.white_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new CheatListAdapter(getApplicationContext());
        getCheatData(adapter,examineeNum,code);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new CheatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CheatListAdapter.CheatListViewHolder holder, View view, int position) {
                Intent intent = new Intent(getApplicationContext(), CheatInfo.class);
                intent.putExtra("부정행위 시간",adapter.getItem(position));
                intent.putExtra("방번호",secondIntent.getStringExtra("방번호"));
                intent.putExtra("수험번호",secondIntent.getStringExtra("수험번호"));
                finish();
                startActivity(intent);
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), com.google.mlkit.vision.demo.ExamManagement.class);
        finish();
        startActivity(intent);
    }
    private void getCheatData(CheatListAdapter adapter,String examineeNum, String examCode){
        cheatInfo.collection("exam").document(examCode)
                .collection("userList").document(examineeNum)
                .collection("cheatList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot cheat : task.getResult()) {
                                HashMap<String, Object> cheatObject = new HashMap<>();
                                cheatObject.put("cheatTime", cheat.getData().get("cheatTime"));
                                cheatObject.put("imageName", cheat.getData().get("imageName"));
                                cheatObject.put("length", cheat.getData().get("length"));

                                String cheatTime = (String) cheat.getId();
                                cheatItems.putAll(cheatObject);

                                adapter.addItem(cheatTime);
                                adapter.putItem(cheatItems);
                                adapter.addNum(cheatTime);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}