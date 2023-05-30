package com.example.donotcheat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ExamineeInfo extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    LinearLayoutManager linearLayoutManager;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    CheatAdapter adapter;

    HashMap<String,Object> userItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examinee_info);
        Intent secondIntent = getIntent();
        recyclerView = (RecyclerView) findViewById(R.id.showCheatListRecylerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.white_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new CheatAdapter(getApplicationContext());
        getCheatData(adapter,userItems,secondIntent.getStringExtra("수험번호"),secondIntent.getStringExtra("방번호"));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new CheatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CheatAdapter.CheatViewHolder holder, View view, int position) {
                Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
                intent.putExtra("시간",adapter.getItem(position));
                intent.putExtra("방번호",secondIntent.getStringExtra("방번호"));
                intent.putExtra("수험번호",secondIntent.getStringExtra("수험번호"));
                finish();
                startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ExamManagement.class);
        finish();
        startActivity(intent);
    }
    public void getCheatData(CheatAdapter adapter, HashMap<String, Object> userItems,String userCode,String roomCode){
        FirebaseUser user = auth.getCurrentUser();
        db.collection("exam1").document(roomCode)
                .collection("userList").document(userCode)
                .collection("cheatList")
                .get()// cheatList에서 document들을 가져와 시간 값을 가져오는 작업해야함
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                adapter.addItem((String) document.getId());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}