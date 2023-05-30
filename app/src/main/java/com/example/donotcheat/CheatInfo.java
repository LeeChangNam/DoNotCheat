package com.example.donotcheat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class CheatInfo extends AppCompatActivity {
    private ImageView cheatImage;
    private TextView imageName;
    private TextView cheatTime;
    private TextView length;
    private TextView otherInfo;
    private TextView empty;
    private Intent secondIntent = getIntent();
    private HashMap<String,String> cheatInfo = new HashMap<>();
    private FirebaseFirestore cheatDb = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageDb = storage.getReference();
    private StorageReference pathReference;
    private StorageReference imageGet;
    String root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_info);

        cheatImage = (ImageView) findViewById(R.id.cheatImage);
        imageName = (TextView)findViewById(R.id.imageName);
        cheatTime= (TextView)findViewById(R.id.cheatTime);
        length= (TextView)findViewById(R.id.length);
        otherInfo= (TextView)findViewById(R.id.otherInfo);
        empty= (TextView)findViewById(R.id.empty);

        String examineeNum = secondIntent.getStringExtra("수험번호");
        String examCode = secondIntent.getStringExtra("방번호");
        String time = secondIntent.getStringExtra("부정행위 시간");

        getCheatData(examineeNum,examCode,time);

        root.concat(examCode).concat("/").concat(examineeNum);
        getCheatImage(time,root);
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
        finish();
        startActivity(intent);
    }
    private void getCheatImage(String cheatTime, String root){
        pathReference = imageDb.child(root);
        if (pathReference == null){
            Toast.makeText(CheatInfo.this,"저장소에 사진이 없습니다.",Toast.LENGTH_SHORT).show();
        }else{
            imageGet = imageDb.child(root.concat("/").concat(cheatTime).concat(".png"));
            imageGet.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(CheatInfo.this).load(uri).into(cheatImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }
    private void getCheatData(String examineeNum, String examCode, String time){
        cheatDb.collection("exam").document(examCode)
                .collection("userList").document(examineeNum)
                .collection("cheatList").document(time)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        cheatInfo.put("cheatTime",(String)task.getResult().getData().get("cheatTime"));
                        cheatInfo.put("imageName",(String)task.getResult().getData().get("imageName"));
                        cheatInfo.put("length",(String)task.getResult().getData().get("length"));
                        cheatInfo.put("otherInfo",(String)task.getResult().getData().get("otherInfo"));
                        cheatInfo.put("empty",(String)task.getResult().getData().get("empty"));
                    }
                });
        cheatTime.setText(cheatInfo.get("cheatTime"));
        imageName.setText(cheatInfo.get("imageName"));
        length.setText(cheatInfo.get("length"));
        otherInfo.setText(cheatInfo.get("otherInfo"));
        empty.setText(cheatInfo.get("empty"));
    }
}