package com.example.donotcheat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ManagerSignIn extends AppCompatActivity {
    EditText code;
    Button join;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_sign_in);
        code = (EditText) findViewById(R.id.managerCode);
        join = (Button) findViewById(R.id.examCreate);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createExam(code.getText().toString());
            }
        });
    }
    void createExam(String code) {
        final int[] flag = {0};
        if (code == "") {
            Toast.makeText(ManagerSignIn.this, "코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        if (code.length() < 4) {Toast.makeText(ManagerSignIn.this, "4자리 코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        db.collection("examRoom")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String codes;
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                codes = (String) document.getData().get("code");
                                if (code.equals(codes)){
                                    flag[0] = 1;
                                }
                            }
                        }
                    }
                });
        if (flag[0]==1){Toast.makeText(ManagerSignIn.this, "중복된 코드 입니다.", Toast.LENGTH_LONG).show(); return;}
        else if(flag[0]==0){
            examCreateDlg(code);
        }
    }
    public void examCreateDlg(String code) {
        final LinearLayout linear = (LinearLayout) View.inflate(ManagerSignIn.this, R.layout.create_exam_dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(ManagerSignIn.this);
        dlg.setTitle("시험장 생성"); //제목

        if(linear.getParent() != null) ((ViewGroup) linear.getParent()).removeView(linear); // 다이얼로그 여러번 생성시 중복된 뷰그룹 들어가 발생하는 에러처리 부분
        dlg.setView(linear);

        dlg.setPositiveButton("입장",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TextView examCode = (EditText) linear.findViewById(R.id.managerCode);
                EditText name = (EditText) linear.findViewById(R.id.examName);
                EditText type = (EditText) linear.findViewById(R.id.examType);

                Intent intent = new Intent(getApplicationContext(), Exam.class); // 관리자 뷰로 넘어가야함
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
}