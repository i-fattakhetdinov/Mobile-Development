package com.example.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    ListView listViewLoginUsers;
    ArrayList<String> listLoginUsers = new ArrayList<>();
    ArrayAdapter adpt;

    ListView listViewRequestedUsers;
    ArrayList<String> listRequestedUsers = new ArrayList<>();
    ArrayAdapter reqUsersAdpt;

    TextView textViewUserID, textViewSendRequest, textViewAcceptRequest;
    String LoginUserID, UserName, LoginUID;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Получаем информацию о БД (инстанс и по не нему ссылку на нее)
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userName", UserName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            UserName = savedInstanceState.getString("userName");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Инициируем текстовые поля начальными значениями
        textViewSendRequest = findViewById(R.id.textViewSendRequest);
        textViewAcceptRequest = findViewById(R.id.textViewAcceptRequest);

        textViewSendRequest.setText("Please wait...");
        textViewAcceptRequest.setText("Please wait...");

        mAuth = FirebaseAuth.getInstance();

        listViewLoginUsers = findViewById(R.id.listViewLoginUsers);
        adpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listLoginUsers);
        listViewLoginUsers.setAdapter(adpt);

        listViewRequestedUsers = findViewById(R.id.listViewRequestedUsers);
        reqUsersAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listRequestedUsers);
        listViewRequestedUsers.setAdapter(reqUsersAdpt);


        textViewUserID = findViewById(R.id.textViewLoginUser);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    LoginUID = user.getUid();
                    Log.d("Auth", "onAuthStateChanged:signed_in:" + LoginUID);
                    LoginUserID = user.getEmail();
                    assert LoginUserID != null;
                    UserName = convertEmailToString(LoginUserID);
                    textViewUserID.setText("My ID:" +  UserName);
                    //UserName = UserName.replace(".", "");
                    myRef.child("users").child(UserName).child("request").setValue(LoginUID);
                    reqUsersAdpt.clear();
                    AcceptIncommingRequests();
                } else {
                    // User is signed out
                    Log.d("Auth", "onAuthStateChanged:signed_out");
                    JoinOnlineGame();
                }
            }
        };

        myRef.getRoot().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateLoginUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        listViewLoginUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String requestToUser = ((TextView)view).getText().toString();
                confirmRequest(requestToUser, "To");
            }
        });

        listViewRequestedUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String requestFromUser = ((TextView)view).getText().toString();
                confirmRequest(requestFromUser, "From");
            }
        });
    }

    // Принимаем запрос или отправляем запрос на начало игры по сети
    void confirmRequest(final String OtherPlayer, final String reqType){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //LayoutInflater inflater = this.getLayoutInflater();
        //final View dialogView = inflater.inflate(R.layout.activity_player_connect_dialog, null);
        //b.setView(dialogView);

        builder.setTitle("Start Game?");
        builder.setMessage("Connect with " + OtherPlayer);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myRef.child("users")
                        .child(OtherPlayer).child("request").push().setValue(LoginUserID);
                if(reqType.equalsIgnoreCase("From")) {
                    StartGame(OtherPlayer + ":" + UserName, OtherPlayer, "From");
                }else{
                    StartGame(UserName + ":" + OtherPlayer, OtherPlayer, "To");
                }
            }
        });
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    void StartGame(String PlayerGameID, String OtherPlayer, String requestType){
        myRef.child("playing").child(PlayerGameID).removeValue();
        Intent intent = new Intent(getApplicationContext(), OnlineGameActivity.class);
        intent.putExtra("player_session", PlayerGameID);
        intent.putExtra("user_name", UserName);
        intent.putExtra("other_player", OtherPlayer);
        intent.putExtra("login_uid", LoginUID);
        intent.putExtra("request_type", requestType);
        startActivity(intent);
    }

    public void updateLoginUsers(DataSnapshot dataSnapshot){
        String key;
        Set<String> set = new HashSet<>();
        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()){
            key = ((DataSnapshot) i.next()).getKey();
            assert key != null;
            if(!key.equalsIgnoreCase(UserName)){
                set.add(key);
            }
        }

        adpt.clear();
        adpt.addAll(set);
        adpt.notifyDataSetChanged();
        textViewSendRequest.setText("Send request to");
        textViewAcceptRequest.setText("Accept request from");
    }


    private String convertEmailToString(String Email){
        String value = Email.substring(0, Email.indexOf('@'));
        value = value.replace(".", ""); // Не можем положить точку в БД
        return value;
    }

    void AcceptIncommingRequests(){
        myRef.child("users").child(UserName).child("request")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                            if(map != null){
                                String value;
                                for(String key:map.keySet()){
                                    value = (String) map.get(key);
                                    assert value != null;
                                    reqUsersAdpt.add(convertEmailToString(value));
                                    reqUsersAdpt.notifyDataSetChanged();
                                    myRef.child("users").child(UserName).child("request").setValue(LoginUID);
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

    public void JoinOnlineGame() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_login_dialog, null);
        b.setView(dialogView);

        final EditText etEmail = (EditText) dialogView.findViewById(R.id.loginEmail);
        final EditText etPassword = (EditText) dialogView.findViewById(R.id.loginPassword);

        b.setTitle("Please register");
        b.setMessage("Enter you email and password for registration");
        b.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RegisterUser(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
        b.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void RegisterUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Auth Complete", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Auth failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


};




