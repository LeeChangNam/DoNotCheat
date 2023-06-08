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

public class ExamManagement extends AppCompatActivity {
    private Intent secondIntent;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ManagementAdapter adapter;
    private HashMap<String,Object> examineeItems = new HashMap<>();
    private FirebaseFirestore examineeInfo = FirebaseFirestore.getInstance();
    private String code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_management);

        secondIntent = getIntent();
        System.out.println("ExamManagement");
        code = secondIntent.getStringExtra("방번호");
        System.out.println("방번호 : "+ code);
        recyclerView = (RecyclerView) findViewById(R.id.showUserListRecylerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.white_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new ManagementAdapter(getApplicationContext());
        getExamineeData(adapter,code);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new ManagementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ManagementAdapter.ManagementViewHolder holder, View view, int position) {
                Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
                intent.putExtra("수험번호",adapter.getItem(position));
                intent.putExtra("방번호",code);
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
    private void getExamineeData(ManagementAdapter adapter, String code){
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
                                examineeItems.putAll(userObject);

                                adapter.addItem(userNum);
                                adapter.putItem(examineeItems);
                                adapter.addNum(userNum);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

    }
}