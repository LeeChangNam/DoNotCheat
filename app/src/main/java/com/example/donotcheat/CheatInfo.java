package com.example.donotcheat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.demo.ExamineeInfo;
import com.google.mlkit.vision.demo.R;

import java.io.IOException;
import java.util.HashMap;

public class CheatInfo extends AppCompatActivity {
    private ImageView cheatImage;
    private TextView imageName;
    private TextView cheatTime;
    private TextView defaults;
    private TextView errorValue;
    private Intent secondIntent;
    private String root="";
    private String time="";
    private String examNum="";
    private String examineeNum="";
    private HashMap<String,Object> cheatInfo = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_info);
        System.out.println("onCreate");
        secondIntent = getIntent();
        time = secondIntent.getStringExtra("부정행위 시간");
        examNum = secondIntent.getStringExtra("방번호");
        examineeNum = secondIntent.getStringExtra("수험번호");
        System.out.println(time);
        System.out.println(examNum);
        System.out.println(examineeNum);

        cheatImage = (ImageView) findViewById(R.id.cheatImage);
        imageName = (TextView)findViewById(R.id.imageName);
        cheatTime= (TextView)findViewById(R.id.cheatTime);
        defaults= (TextView)findViewById(R.id.defaults);
        errorValue= (TextView)findViewById(R.id.errorValue);
        cheatTime.setText(time);
        //getCheatData(examineeNum,examNum,time,cheatInfo);
        db.collection("exam").document(examNum)
                .collection("userList").document(examineeNum)
                .collection("cheatList").document(time)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    HashMap<String,Object> cheatInfos = new HashMap<>();
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            cheatInfos.put("imageName", task.getResult().getData().get("cheatTime")+".png");
                            System.out.println(cheatInfos.get("imageName"));
                            imageName.setText((String)cheatInfos.get("imageName"));
                            cheatInfos.put("errorValue", task.getResult().getData().get("error_value").toString());
                            System.out.println(cheatInfos.get("errorValue"));
                            errorValue.setText((String)cheatInfos.get("errorValue"));
                            cheatInfo.putAll(cheatInfos);
                        }
                    }
                });
        db.collection("exam").document(examNum)
                .collection("userList").document(examineeNum)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    HashMap<String,Object> cheatInfos = new HashMap<>();
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            cheatInfos.put("default",task.getResult().getData().get("default").toString());
                            defaults.setText((String)cheatInfos.get("default"));
                            cheatInfo.putAll(cheatInfos);
                        }
                    }
                });
        StorageReference storageRef = storage.getReference();
        root = examNum+"/"+examineeNum+"/"+time+".png";

        StorageReference imageRef = storageRef.child(root);

        final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024; // 최대 다운로드 크기 (5MB)

        imageRef.getBytes(MAX_DOWNLOAD_SIZE)
                .addOnSuccessListener(bytes -> {
                    // 이미지 다운로드가 성공한 경우
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    cheatImage.setImageBitmap(bitmap);
                })
                .addOnFailureListener(exception -> {
                    // 다운로드 중 오류가 발생한 경우
                    exception.printStackTrace();
                });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
        finish();
        startActivity(intent);
    }
    private void getCheatImage(String examCode,String examineeNum) {
        StorageReference storageRef = storage.getReference();
        root = examCode+"/"+examineeNum+"/"+cheatInfo.get("imageName");

        StorageReference imageRef = storageRef.child(root);

        final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024; // 최대 다운로드 크기 (5MB)

        imageRef.getBytes(MAX_DOWNLOAD_SIZE)
                .addOnSuccessListener(bytes -> {
                    // 이미지 다운로드가 성공한 경우
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    cheatImage.setImageBitmap(bitmap);
                })
                .addOnFailureListener(exception -> {
                    // 다운로드 중 오류가 발생한 경우
                    exception.printStackTrace();
                });
    }
}