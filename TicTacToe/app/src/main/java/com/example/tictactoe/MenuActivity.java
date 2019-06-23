package com.example.tictactoe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void startGameSinglePlayer(View view) {
        Intent singlePlayer = new Intent(this, GameActivity.class);
        startActivity(singlePlayer);
    }

    public void exitGame(View view) {
        int myPid = android.os.Process.myPid();
        android.os.Process.killProcess(myPid);
    }

    public void startGameOnline(View view){
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
    }
}
