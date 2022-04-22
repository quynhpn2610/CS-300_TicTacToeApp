package com.example.tictactoe;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_Online extends AppCompatActivity {
    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;

    // winning combinations
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<>(); // done boxes positions by users so users won't select the box again

    // player unique Id
    private String playerUniqueId = "0";

    // getting firebase database reference
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://cs300tictactoe-default-rtdb.firebaseio.com/");

    // whether opponent has been found
    private boolean opponentFound = false;

    // unique id for opponent
    private String opponentUniqueId = "0";

    // connection status (matching/waiting)
    private String status = "matching";

    // player turn
    private String playerTurn = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);

        // getting PlayerName from OnlinePlayerName file
        final String getPlayerName = getIntent().getStringExtra("playerName");

        // generating winning combinations
        combinationsList.add(new int[]{0,1,2});
        combinationsList.add(new int[]{3,4,5});
        combinationsList.add(new int[]{6,7,8});
        combinationsList.add(new int[]{0,3,6});
        combinationsList.add(new int[]{1,4,7});
        combinationsList.add(new int[]{2,5,8});
        combinationsList.add(new int[]{2,4,6});
        combinationsList.add(new int[]{0,4,8});

        // showing progress dialog while waiting for opponent
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Looking for Opponent");
        progressDialog.show();

        // generate player unique id. Player will be identified by this id.
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        // setting player name to the TextView
        player1TV.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check if opponent found or not. If not then look for the opponent.
                if(opponentFound){
                    // check if there are others in firebase
                    if(snapshot.hasChildren()){
                        // check each connection to see if there are other users waiting
                        for(DataSnapshot connections: snapshot.getChildren()){
                            // get connection unique id
                            long connectionId = Long.parseLong(connections.getKey());

                            // if getPlayerCount = 1 -> other player is available, 2 -> connection is made
                            int getPlayerCount = (int)connections.getChildrenCount();

                            // after creating a new connection, wait for other to join
                            if(status.equals("waiting")){
                                if (getPlayerCount == 2){
                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);
                                }
                            }
                        }
                    }

                    // if there is no one waiting in firebase, create new connection and waiting for opponent
                    else{
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                        // add first player to the connection and wait for another to connect
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);
                        status = "waiting";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //
    private void applyPlayerTurn(String playerUniqueId2){
        if (playerUniqueId2.equals(playerUniqueId)){
            player1Layout.setBackgroundResource(R.drawable.round_back_blue_border);
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue);
        }
        else{
            player2Layout.setBackgroundResource(R.drawable.round_back_blue_border);
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue);
        }
    }
}
