package com.example.donotcheat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private Intent secondIntent;
    private HashMap<String,Object> cheatInfo = new HashMap<>();
    private CheatListAdapter cheatListAdapter;
    private FirebaseFirestore cheatDb = FirebaseFirestore.getInstance();
    private FirebaseFirestore cheatDbb = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageDb = storage.getReference();
    private StorageReference pathReference;
    private StorageReference imageGet;
    private String root="/";
    String examineeNum;
    String examCode;
    String time;
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

        secondIntent = getIntent();
        examineeNum = secondIntent.getStringExtra("수험번호");
        examCode = secondIntent.getStringExtra("방번호");
        time = secondIntent.getStringExtra("부정행위 시간");

        getCheatData(examineeNum,examCode,time);
        root = root.concat(examCode);
        root = root.concat("/");
        root = root.concat(examineeNum);
        System.out.println(root);

        getCheatImage(time,root);
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ExamineeInfo.class);
        finish();
        startActivity(intent);
    }
    private void getCheatImage(String cheatTime, String root){
        pathReference = imageDb.child(root);
        System.out.println(root);
        if (pathReference == null){
            Toast.makeText(CheatInfo.this,"저장소에 사진이 없습니다.",Toast.LENGTH_SHORT).show();
        }else{
            root = root.concat("/").concat(cheatTime).concat(".png");
            System.out.println(root);
            imageGet = imageDb.child(root);
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
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        cheatTime.setText((String)documentSnapshot.getData().get("cheatTime"));
                        imageName.setText((String)documentSnapshot.getData().get("imageName"));
                        length.setText((String)documentSnapshot.getData().get("length"));
                        otherInfo.setText((String)documentSnapshot.getData().get("otherInfo"));
                        empty.setText((String)documentSnapshot.getData().get("empty"));

                    }
                });

    }
}