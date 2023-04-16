package com.example.donotcheat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ExamSignIn extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseFirestore input = FirebaseFirestore.getInstance();
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
                codeCheck(codeText.getText().toString(),nameText.getText().toString());
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
                EditText num = (EditText) linear.findViewById(R.id.userCode);
                EditText room = (EditText) linear.findViewById(R.id.examRoomName);
                EditText name = (EditText) linear.findViewById(R.id.userName);
                EditText phone = (EditText) linear.findViewById(R.id.userPhone);
                //nameInputDlg를 SignIn 앞에 뷰 하나를 만들어서 거기서 처리하는게 나아보임
                //아니면 입력된 3개의 정보로 수험자와 일치하는지 판별을 여기서 해버리면 더 편할지도?
                joinExam(code,num.getText().toString(),room.getText().toString(),name.getText().toString(),phone.getText().toString());
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
    void joinExam(String code, String userNum, String subject, String userName, String phoneNum){
        Map<String, Object> roomObject = new HashMap<>();
        Map<String, Object> errorObject = new HashMap<>();
        errorObject.put("errorCount","0");
        errorObject.put("errorImage","");
        errorObject.put("errorTime","");
        roomObject.put("name",userName);
        roomObject.put("phone",phoneNum);
        //사용자 정보를 db에 저장
        input.collection("exam").document(code)
                .collection("userList").document(userNum)
                .set(roomObject)
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
        //사용자 error정보를 db에 저장
        db.collection("exam").document(userNum)
                .collection("userList").document(userNum)
                .collection("cheat")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        long idx = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                idx = Long.parseLong(document.getId());
                                System.out.println(document.getId());
                            }
                        } else {
                        }
                        idx++;
                        final long id = idx;
                        input.collection("exam").document(userNum)
                                .collection("userList").document(userNum)
                                .collection("cheat").document(String.valueOf(id))
                                .set(errorObject)
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
                });
    }
    void codeCheck(String code, String name) {
        final int[] flag = {0};
        if (code == "") {
            Toast.makeText(ExamSignIn.this, "코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        if (code.length() < 8) {Toast.makeText(ExamSignIn.this, "8자리 코드를 입력해주세요.", Toast.LENGTH_LONG).show(); return;}
        db.collection("exam")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String codes;
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                codes = (String) document.getId();
                                if (code.equals(codes) == true){
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