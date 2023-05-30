package com.example.donotcheat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button examSignInButton;
    private Button managerSignInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        examSignInButton = (Button) findViewById(R.id.examSignIn);
        managerSignInButton = (Button) findViewById(R.id.managerSignIn);

        examSignInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ExamSignIn.class);
                finish();
                startActivity(intent);
            }
        });

        managerSignInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ManagerSignIn.class);
                finish();
                startActivity(intent);
            }
        });
    }
}