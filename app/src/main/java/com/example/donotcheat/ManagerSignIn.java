package com.example.donotcheat;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ManagerSignIn extends AppCompatActivity {
    private EditText code;
    private Button create;
    private Button entrance;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_sign_in);

        code = (EditText) findViewById(R.id.managerCode);
        create = (Button) findViewById(R.id.examCreate);
        entrance = (Button) findViewById(R.id.examEntrance);

        System.out.println("뷰시작");
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("생성");
                createExam(code.getText().toString(),0);
            }
        });
        entrance.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("입장");
                createExam(code.getText().toString(),1);
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }
    private void createExam(String code,int flagInt){
        if (code.equals("")) {
            Toast.makeText(ManagerSignIn.this, "코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        if (code.length() < 8) {Toast.makeText(ManagerSignIn.this, "8자리 코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        System.out.println("createExam");
        db.collection("exam")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String flag = "";
                        System.out.println("onComplete");// 여기까진 들어가짐
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                if (code.equals((String)document.getId())){
                                    System.out.println((String)document.getId());
                                    flag = "중복";
                                }
                            }
                        }
                        if (flag.equals("중복") && flagInt == 0){
                            Toast.makeText(ManagerSignIn.this, "중복된 코드 입니다.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else if(flag.equals("중복") && flagInt == 1){
                            Intent intent = new Intent(getApplicationContext(), ExamManagement.class);
                            intent.putExtra("방번호",code);
                            finish();
                            startActivity(intent);
                        }
                        else if(flagInt == 0){
                            examCreateDlg(code);
                        }
                        else if(flagInt == 1){
                            Toast.makeText(ManagerSignIn.this, "해당코드로 입장 가능한 시험장이 없습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void examCreateDlg(String code){
        final LinearLayout linear = (LinearLayout) View.inflate(ManagerSignIn.this, R.layout.create_exam_dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(ManagerSignIn.this);
        dlg.setTitle("시험장 생성"); //제목

        if(linear.getParent() != null) ((ViewGroup) linear.getParent()).removeView(linear); // 다이얼로그 여러번 생성시 중복된 뷰그룹 들어가 발생하는 에러처리 부분
        dlg.setView(linear);

        dlg.setPositiveButton("생성",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText subject = (EditText) linear.findViewById(R.id.examType);
                EditText managerName = (EditText) linear.findViewById(R.id.managerName);
                EditText managerNum = (EditText) linear.findViewById(R.id.managerNum);


                examCreate(code,subject.getText().toString(),managerName.getText().toString(),managerNum.getText().toString());
                Intent intent = new Intent(ManagerSignIn.this, ExamManagement.class);
                intent.putExtra("방번호",code);
                finish();
                startActivity(intent);
            }
        });

        dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dlg.show();
    }
    private void examCreate(String code, String subject, String managerName, String managerNum){
        Map<String,Object> examObject = new HashMap<>();
        Map<String,Object> userList = new HashMap<>();
        examObject.put("subject",subject);
        examObject.put("managerName",managerName);
        examObject.put("managerNum",managerNum);

        db.collection("exam").document(code)
                .set(examObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        db.collection("exam").document(code)
                .collection("userList")
                .document("00000000")
                .set(userList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}