package com.example.donotcheat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

public class Exam extends AppCompatActivity {

    TextureView examStart;
    Button examFinishButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        examFinishButton = (Button) findViewById(R.id.finish);
        examStart = (TextureView) findViewById(R.id.cam);

        examFinishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(intent);

            }
        });
    }
}