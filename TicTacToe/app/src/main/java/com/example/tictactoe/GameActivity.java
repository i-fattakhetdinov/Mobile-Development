package com.example.tictactoe;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

enum GameState {
    INIT,
    PLAY,
    GAMEOVER,
    DRAW
}

public class GameActivity extends AppCompatActivity {

    GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameState = GameState.PLAY;

        secondTurn();

    }

    private void secondTurn() {
        Random rand = new Random();
        int randomValue = rand.nextInt(2);

        if (randomValue == 1){
            isInverse = true;
        } else {
            isInverse = false;
        }

        if(isInverse){

            Random rand2 = new Random();
            int selectedBlock = rand2.nextInt(9);

            ImageView selectedImage = findViewById(R.id.table_r1_c1);
            switch (selectedBlock){
                case 1: selectedImage = findViewById(R.id.table_r1_c1);
                    break;
                case 2: selectedImage = findViewById(R.id.table_r1_c2);
                    break;
                case 3: selectedImage = findViewById(R.id.table_r1_c3);
                    break;
                case 4: selectedImage = findViewById(R.id.table_r2_c1);
                    break;
                case 5: selectedImage = findViewById(R.id.table_r2_c2);
                    break;
                case 6: selectedImage = findViewById(R.id.table_r2_c3);
                    break;
                case 7: selectedImage = findViewById(R.id.table_r3_c1);
                    break;
                case 8: selectedImage = findViewById(R.id.table_r3_c2);
                    break;
                case 9: selectedImage = findViewById(R.id.table_r3_c3);
                    break;
            }

            selectedImage.setImageResource(R.drawable.ttt_x);
            player2.add(selectedBlock);
            selectedImage.setEnabled(false);
        }
    }


    public void GameBoardClick(View view) {
        ImageView selectedImage = (ImageView) view;

        int selectedBlock = 0;
        switch (selectedImage.getId()){
            case R.id.table_r1_c1: selectedBlock = 1;
                break;
            case R.id.table_r1_c2: selectedBlock = 2;
                break;
            case R.id.table_r1_c3: selectedBlock = 3;
                break;
            case R.id.table_r2_c1: selectedBlock = 4;
                break;
            case R.id.table_r2_c2: selectedBlock = 5;
                break;
            case R.id.table_r2_c3: selectedBlock = 6;
                break;
            case R.id.table_r3_c1: selectedBlock = 7;
                break;
            case R.id.table_r3_c2: selectedBlock = 8;
                break;
            case R.id.table_r3_c3: selectedBlock = 9;
                break;
        }

        playGame(selectedBlock, selectedImage);
    }

    int activePlayer = 1;
    boolean isSecondTurn = false;
    boolean isInverse = false;
    ArrayList<Integer> player1 = new ArrayList<Integer>();
    ArrayList<Integer> player2 = new ArrayList<Integer>();

    private void playGame(int selectedBlock, ImageView selectedImage) {
        if(gameState == GameState.PLAY){
            if (activePlayer == 1){
                if(isInverse){
                    selectedImage.setImageResource(R.drawable.ttt_o);
                }else {
                    selectedImage.setImageResource(R.drawable.ttt_x);
                }

                player1.add(selectedBlock);
                activePlayer = 2;
                Autoplay();
            } else if (activePlayer == 2){
                if(isInverse){
                    selectedImage.setImageResource(R.drawable.ttt_x);
                } else {
                    selectedImage.setImageResource(R.drawable.ttt_o);
                }
                player2.add(selectedBlock);
                activePlayer = 1;
            }

            selectedImage.setEnabled(false);
            checkWinner();
        }
    }



    private void Autoplay() {
        ArrayList<Integer> emptyBlocks = new ArrayList<Integer>();

        //Ищем пустые ячейки на поле
        for (int i = 1; i <= 9; i++){
            if(!(player1.contains(i) || player2.contains(i))){
                emptyBlocks.add(i);
            }
        }

        //Проверим случай ничьи
        if (emptyBlocks.size() == 0){
            checkWinner();
            if (gameState == GameState.PLAY){
                AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                showAlert("Draw");
            }
            gameState = GameState.DRAW;
        } else {
            Random rand = new Random();
            int randomValue = rand.nextInt(emptyBlocks.size());
            int selectedBlock = emptyBlocks.get(randomValue);

            ImageView selectedImage = findViewById(R.id.table_r1_c1);
            switch (selectedBlock){
                case 1: selectedImage = findViewById(R.id.table_r1_c1);
                    break;
                case 2: selectedImage = findViewById(R.id.table_r1_c2);
                    break;
                case 3: selectedImage = findViewById(R.id.table_r1_c3);
                    break;
                case 4: selectedImage = findViewById(R.id.table_r2_c1);
                    break;
                case 5: selectedImage = findViewById(R.id.table_r2_c2);
                    break;
                case 6: selectedImage = findViewById(R.id.table_r2_c3);
                    break;
                case 7: selectedImage = findViewById(R.id.table_r3_c1);
                    break;
                case 8: selectedImage = findViewById(R.id.table_r3_c2);
                    break;
                case 9: selectedImage = findViewById(R.id.table_r3_c3);
                    break;
            }
            playGame(selectedBlock, selectedImage);
        }

    }

    private void showAlert(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialog);
        builder.setTitle(text)
                .setMessage("Start new game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent menu = new Intent(getApplicationContext(), MenuActivity.class);
                        startActivity(menu);
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

    }

    private void restartGame() {


        gameState = GameState.PLAY;
        activePlayer = 1;
        player1.clear();
        player2.clear();

        clearImage(R.id.table_r1_c1);
        clearImage(R.id.table_r1_c2);
        clearImage(R.id.table_r1_c3);
        clearImage(R.id.table_r2_c1);
        clearImage(R.id.table_r2_c2);
        clearImage(R.id.table_r2_c3);
        clearImage(R.id.table_r3_c1);
        clearImage(R.id.table_r3_c2);
        clearImage(R.id.table_r3_c3);

        secondTurn();
    }

    private void clearImage(int imageId) {
        ImageView image = (ImageView) findViewById(imageId);
        image.setImageResource(0);
        image.setEnabled(true);
    }

    private void checkWinner() {
        int winner = 0;
        if (checkWinner(player1)){
            winner = 1;
        }
        if (checkWinner(player2)){
            winner = 2;
        }

        if (winner != 0 && gameState == GameState.PLAY){
            if (winner == 1){
                showAlert("You won the game!");
            }
            else if (winner == 2){
                showAlert("Your opponent won the game");
            }
            gameState = GameState.GAMEOVER;
        } else {
            ArrayList<Integer> emptyBlocks = new ArrayList<Integer>();

            //Ищем пустые ячейки на поле
            for (int i = 1; i <= 9; i++){
                if(!(player1.contains(i) || player2.contains(i))){
                    emptyBlocks.add(i);
                }
            }
            if (emptyBlocks.size() == 0 && gameState == GameState.PLAY){
                AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                showAlert("Draw");
                gameState = GameState.DRAW;
            }
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


}
