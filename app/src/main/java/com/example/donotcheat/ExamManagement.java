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
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ExamManagement extends AppCompatActivity{
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore userList;
    Intent secondIntent = getIntent();
    FirebaseFirestore db;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ManagementAdapter adapter;
    HashMap<String,Object> examineeItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_management);
        recyclerView = (RecyclerView) findViewById(R.id.showUserListRecylerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.white_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new ManagementAdapter(getApplicationContext());
        getExamData(adapter,examineeItems,secondIntent.getStringExtra("방번호"));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new ManagementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ManagementAdapter.ManagementViewHolder holder, View view, int position) {
                Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
                intent.putExtra("수험번호",adapter.getItem(position));
                intent.putExtra("방번호",secondIntent.getStringExtra("방번호"));
                finish();
                startActivity(intent);
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ManagerSignIn.class);
        finish();
        startActivity(intent);
    }
    public void getExamData(ManagementAdapter adapter, HashMap<String,Object> examineeItems, String roomCode){
        FirebaseUser user = auth.getCurrentUser();
        db.collection("exam1").document(roomCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                adapter.addItem((String)document.getData().get("subject"));
                                Log.d(TAG, "DocumentSnapshot data: " + document.getDate("subject"));
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }
}