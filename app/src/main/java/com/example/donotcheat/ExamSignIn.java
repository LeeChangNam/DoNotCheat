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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ExamSignIn extends AppCompatActivity {
    private EditText nameText;
    private EditText codeText;
    private Button joinButton;
    private FirebaseFirestore codeCheck = FirebaseFirestore.getInstance();
    private FirebaseFirestore subjectCheck = FirebaseFirestore.getInstance();
    private FirebaseFirestore examineeInput = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_sign_in);

        nameText = (EditText) findViewById(R.id.examName);
        codeText = (EditText) findViewById(R.id.examCode);
        joinButton = (Button) findViewById(R.id.join);
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                codeCheck(codeText.getText().toString(),nameText.getText().toString());
            }
        });
    }
    private void codeCheck(String code,String subject){
        if (code.equals("")) {
            Toast.makeText(ExamSignIn.this, "코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        if (code.length() < 8) {Toast.makeText(ExamSignIn.this, "8자리 코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        codeCheck.collection("exam")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String codes;
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                codes = (String) document.getId();
                                if (code.equals(codes) == true){
                                    subjectCheck.collection("exam").document(code)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(subject.equals(task.getResult().getData().get("subject"))){
                                                        examineeInfoDlg(code,subject);
                                                    }
                                                    else{
                                                        Toast.makeText(ExamSignIn.this, "시험과목이 일치하지 않습니다.", Toast.LENGTH_LONG).show(); return;
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
    private void examineeInfoDlg(String code, String subject){
        final LinearLayout linear = (LinearLayout) View.inflate(ExamSignIn.this, R.layout.profile_input_dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(ExamSignIn.this);
        dlg.setTitle("시험장 입장"); //제목

        if(linear.getParent() != null) ((ViewGroup) linear.getParent()).removeView(linear); // 다이얼로그 여러번 생성시 중복된 뷰그룹 들어가 발생하는 에러처리 부분
        dlg.setView(linear);

        dlg.setPositiveButton("입장",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText num = (EditText) linear.findViewById(R.id.userCode);
                EditText name = (EditText) linear.findViewById(R.id.userName);
                //nameInputDlg를 SignIn 앞에 뷰 하나를 만들어서 거기서 처리하는게 나아보임
                //아니면 입력된 3개의 정보로 수험자와 일치하는지 판별을 여기서 해버리면 더 편할지도?
                joinExam(code,num.getText().toString(),name.getText().toString());
                Intent intent = new Intent(getApplicationContext(), Exam.class); // 시험 뷰로 넘어가야함
                intent.putExtra("수험번호",num.getText().toString());
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
    private void joinExam(String code,String examineeNum,String examineeName){
        HashMap<String,String> examineeInfo = new HashMap<>();
        examineeInfo.put("examineeName",examineeName);
        examineeInput.collection("exam").document(code)
                .collection("userList").document(examineeNum)
                .set(examineeInfo)
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