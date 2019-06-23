package com.example.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class OnlineGameActivity extends AppCompatActivity {


    TextView textViewTurn;

    String playerSession = "";
    String userName = "";
    String otherPlayer = "";
    String loginUID = "";
    String requestType = "", myGameSign = "X";

    int gameState = 0;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        userName = getIntent().getExtras().get("user_name").toString();
        loginUID = getIntent().getExtras().get("login_uid").toString();
        otherPlayer = getIntent().getExtras().get("other_player").toString();
        requestType = getIntent().getExtras().get("request_type").toString();
        playerSession = getIntent().getExtras().get("player_session").toString();


        textViewTurn = findViewById(R.id.textViewTurn);

        gameState = 1;

        if(requestType.equals("From")){
            myGameSign = "0";
            textViewTurn.setText("Your turn");
            myRef.child("playing").child(playerSession).child("turn").setValue(userName);
            //setEnableClick(true);
        }else{
            myGameSign = "X";
            textViewTurn.setText(otherPlayer + "\'s turn");
            myRef.child("playing").child(playerSession).child("turn").setValue(otherPlayer);
            //setEnableClick(false);
        }


        myRef.child("playing").child(playerSession).child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    String value = (String) dataSnapshot.getValue();
                    if(value.equals(userName)) {
                        textViewTurn.setText("Your turn");
                        setEnableClick(true);
                        activePlayer = 1;
                    }else if(value.equals(otherPlayer)){
                        textViewTurn.setText(otherPlayer + "\'s turn");
                        setEnableClick(false);
                        activePlayer = 2;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        myRef.child("playing").child(playerSession).child("game")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            Player1.clear();
                            Player2.clear();
                            activePlayer = 2;
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                            if(map != null){
                                String value = "";
                                String firstPlayer = userName;
                                for(String key:map.keySet()){
                                    value = (String) map.get(key);
                                    if(value.equals(userName)){
                                        activePlayer = 2;
                                    }else{
                                        activePlayer = 1;
                                    }
                                    firstPlayer = value;
                                    String[] splitID = key.split(":");
                                    OtherPlayer(Integer.parseInt(splitID[1]));
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }



    public void GameBoardClick(View view){
        ImageView selectedImage = (ImageView) view;

        if(playerSession.length() <= 0){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            int selectedBlock = 0;
            switch ((selectedImage.getId())) {
                case R.id.table_r1_c1: selectedBlock = 1; break;
                case R.id.table_r1_c2: selectedBlock = 2; break;
                case R.id.table_r1_c3: selectedBlock = 3; break;

                case R.id.table_r2_c1: selectedBlock = 4; break;
                case R.id.table_r2_c2: selectedBlock = 5; break;
                case R.id.table_r2_c3: selectedBlock = 6; break;

                case R.id.table_r3_c1: selectedBlock = 7; break;
                case R.id.table_r3_c2: selectedBlock = 8; break;
                case R.id.table_r3_c3: selectedBlock = 9; break;
            }
            myRef.child("playing").child(playerSession).child("game").child("block:"+selectedBlock).setValue(userName);
            myRef.child("playing").child(playerSession).child("turn").setValue(otherPlayer);
            setEnableClick(false);
            activePlayer = 2;
            PlayGame(selectedBlock, selectedImage);
        }
    }


    int activePlayer = 1;
    ArrayList<Integer> Player1 = new ArrayList<Integer>();
    ArrayList<Integer> Player2 = new ArrayList<Integer>();

    void PlayGame(int selectedBlock, ImageView selectedImage){
        if(gameState == 1) {
            if (activePlayer == 1) {
                selectedImage.setImageResource(R.drawable.ttt_x);
                Player1.add(selectedBlock);
            }else if (activePlayer == 2) {
                selectedImage.setImageResource(R.drawable.ttt_o);
                Player2.add(selectedBlock);
            }

            selectedImage.setEnabled(false);
            CheckWinner();
        }
    }


    private boolean checkWinner(ArrayList<Integer> player) {
        boolean isWinner = false;
        if (player.contains(1) && player.contains(2) && player.contains(3)){
            isWinner = true;
        }
        if (player.contains(4) && player.contains(5) && player.contains(6)){
            isWinner = true;
        }
        if (player.contains(7) && player.contains(8) && player.contains(9)){
            isWinner = true;
        }
        if (player.contains(1) && player.contains(4) && player.contains(7)){
            isWinner = true;
        }
        if (player.contains(2) && player.contains(5) && player.contains(8)){
            isWinner = true;
        }
        if (player.contains(3) && player.contains(6) && player.contains(9)){
            isWinner = true;
        }
        if (player.contains(1) && player.contains(5) && player.contains(9)){
            isWinner = true;
        }
        if (player.contains(3) && player.contains(5) && player.contains(7)){
            isWinner = true;
        }
        return isWinner;
    }


    void CheckWinner(){
        int winner = 0;

        if (checkWinner(Player1)){
            winner = 1;
        }
        if (checkWinner(Player2)){
            winner = 2;
        }


        if(winner != 0 && gameState == 1){
            if(winner == 1){
                ShowAlert(otherPlayer +" is winner");
            }else if(winner == 2){
                ShowAlert("You won the game");
            }
            gameState = 2;
        }

        ArrayList<Integer> emptyBlocks = new ArrayList<Integer>();
        for(int i=1; i<=9; i++){
            if(!(Player1.contains(i) || Player2.contains(i))){
                emptyBlocks.add(i);
            }
        }
        if(emptyBlocks.size() == 0) {
            if(gameState == 1) {
                AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                ShowAlert("Draw");
            }
            gameState = 3;
        }
    }


    void OtherPlayer(int selectedBlock) {

        ImageView selectedImage = (ImageView) findViewById(R.id.table_r1_c1);
        switch (selectedBlock) {
            case 1:
                selectedImage = (ImageView) findViewById(R.id.table_r1_c1);
                break;
            case 2:
                selectedImage = (ImageView) findViewById(R.id.table_r1_c2);
                break;
            case 3:
                selectedImage = (ImageView) findViewById(R.id.table_r1_c3);
                break;

            case 4:
                selectedImage = (ImageView) findViewById(R.id.table_r2_c1);
                break;
            case 5:
                selectedImage = (ImageView) findViewById(R.id.table_r2_c2);
                break;
            case 6:
                selectedImage = (ImageView) findViewById(R.id.table_r2_c3);
                break;

            case 7:
                selectedImage = (ImageView) findViewById(R.id.table_r3_c1);
                break;
            case 8:
                selectedImage = (ImageView) findViewById(R.id.table_r3_c2);
                break;
            case 9:
                selectedImage = (ImageView) findViewById(R.id.table_r3_c3);
                break;
        }

        PlayGame(selectedBlock, selectedImage);
    }



    void restartGame(){
        gameState = 1;
        activePlayer = 1;
        Player1.clear();
        Player2.clear();

        myRef.child("playing").child(playerSession).removeValue();

        clearImage(R.id.table_r1_c1);
        clearImage(R.id.table_r1_c2);
        clearImage(R.id.table_r1_c3);
        clearImage(R.id.table_r2_c1);
        clearImage(R.id.table_r2_c2);
        clearImage(R.id.table_r2_c3);
        clearImage(R.id.table_r3_c1);
        clearImage(R.id.table_r3_c2);
        clearImage(R.id.table_r3_c3);
    }

    private void clearImage(int imageId) {
        ImageView image = findViewById(imageId);
        image.setImageResource(0);
        image.setEnabled(true);
    }



    void ShowAlert(String Title){
        AlertDialog.Builder b = new AlertDialog.Builder(this, R.style.TransparentDialog);
        b.setTitle(Title)
                .setMessage("Start a new game?")
                .setNegativeButton("Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }



    void setEnableClick(boolean trueORfalse){
        ImageView iv;
        iv = (ImageView) findViewById(R.id.table_r1_c1); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r1_c2); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r1_c3); iv.setClickable(trueORfalse);

        iv = (ImageView) findViewById(R.id.table_r2_c1); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r2_c2); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r2_c3); iv.setClickable(trueORfalse);

        iv = (ImageView) findViewById(R.id.table_r3_c1); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r3_c2); iv.setClickable(trueORfalse);
        iv = (ImageView) findViewById(R.id.table_r3_c3); iv.setClickable(trueORfalse);
    }
}