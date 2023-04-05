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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ExamSignIn extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    EditText nameText;
    EditText codeText;
    Button joinButton;

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
                joinExam(codeText.getText().toString(),nameText.getText().toString());
            }
        });
    }
    public void nameInputDlg(String code, String name) {
        final LinearLayout linear = (LinearLayout) View.inflate(ExamSignIn.this, R.layout.profile_input_dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(ExamSignIn.this);
        dlg.setTitle("시험장 입장"); //제목

        if(linear.getParent() != null) ((ViewGroup) linear.getParent()).removeView(linear); // 다이얼로그 여러번 생성시 중복된 뷰그룹 들어가 발생하는 에러처리 부분
        dlg.setView(linear);

        dlg.setPositiveButton("입장",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText num = (EditText) linear.findViewById(R.id.usrCode);
                EditText room = (EditText) linear.findViewById(R.id.examRoomName);
                EditText name = (EditText) linear.findViewById(R.id.usrName);
                EditText phone = (EditText) linear.findViewById(R.id.usrPhone);
                //nameInputDlg를 SignIn 앞에 뷰 하나를 만들어서 거기서 처리하는게 나아보임
                //아니면 입력된 3개의 정보로 수험자와 일치하는지 판별을 여기서 해버리면 더 편할지도?
                Intent intent = new Intent(getApplicationContext(), Exam.class); // 시험 뷰로 넘어가야함
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
    void joinExam(String code, String name) {
        final int[] flag = {0};
        if (code == "") {
            Toast.makeText(ExamSignIn.this, "코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        if (code.length() < 4) {Toast.makeText(ExamSignIn.this, "4자리 코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        db.collection("examRoom")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String codes;
                        String names;
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                codes = (String) document.getData().get("code");
                                names = (String) document.getData().get("name");
                                if (code.equals(codes) == true && name.equals(names) == true){
                                    nameInputDlg(code,name);
                                    flag[0] = 1;
                                }
                            }
                        }
                    }
                });
        if (flag[0] == 0){Toast.makeText(ExamSignIn.this, "해당 코드로 입장 가능한 방이 없습니다.", Toast.LENGTH_LONG).show(); return;}
    }
}