package com.example.donotcheat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ExamManagement extends AppCompatActivity {
    private Intent secondIntent = getIntent();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ManagementAdapter adapter;
    private HashMap<String,Object> examineeItems;
    private FirebaseFirestore examineeInfo = FirebaseFirestore.getInstance();
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
        getExamineeData(adapter,examineeItems,secondIntent.getStringExtra("방번호"));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new ManagementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ManagementAdapter.ManagementViewHolder holder, View view, int position) {
                /*Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
                //intent.putExtra("수험번호",adapter.getItem(position));
                //intent.putExtra("방번호",secondIntent.getStringExtra("방번호"));
                finish();
                startActivity(intent);*/
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ManagerSignIn.class);
        finish();
        startActivity(intent);
    }
    private void getExamineeData(ManagementAdapter adapter, HashMap<String,Object> userItems, String code){
        examineeInfo.collection("exam").document(code)
                .collection("userList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot user : task.getResult()) {
                                HashMap<String, Object> userObject = new HashMap<>();
                                userObject.put("examineeName", user.getData().get("examineeName"));

                                String userNum = (String) user.getId();
                                userItems.putAll(userObject);

                                adapter.addItem(userNum);
                                adapter.putItem(userItems);
                                adapter.addNum(userNum);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

    }
}