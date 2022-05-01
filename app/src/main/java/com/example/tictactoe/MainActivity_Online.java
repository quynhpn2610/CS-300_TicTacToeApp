package com.example.tictactoe;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    // connection id
    private String connectionId = "";

    // Generating ValueEventListeners for firebase
    // turnsEventListener listen for the players player's turns and wonEventListener if the player has won the match
    ValueEventListener turnsEventListener, wonEventListener;

    // selected boxes by players empty fields will be replaced by player ids
    private final String[] boxesSelectedBy = {"", "", "", "", "", "", "", "", ""};

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
                if(!opponentFound){
                    // check if there are others in firebase
                    if(snapshot.hasChildren()){
                        // check each connection to see if there are other users waiting
                        for(DataSnapshot connections: snapshot.getChildren()){
                            // get connection unique id
                            String conId = connections.getKey();

                            // if getPlayerCount = 1 -> other player is available, 2 -> connection is made
                            int getPlayerCount = (int)connections.getChildrenCount();

                            // after creating a new connection, wait for other to join
                            if(status.equals("waiting")){
                                if (getPlayerCount == 2){
                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);

                                    // true when opponent found
                                    boolean playerFound = false;

                                    // getting players in connection
                                    for (DataSnapshot players: connections.getChildren()){
                                        String getPlayerUniqueId = players.getKey();

                                        if (getPlayerUniqueId.equals(playerUniqueId)){
                                            playerFound = true;
                                        }
                                        else if(playerFound){
                                            String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            // set opponent playername to the text view
                                            player2TV.setText(getOpponentPlayerName);

                                            // assigning connection id
                                            connectionId = conId;

                                            opponentFound = true;

                                            // adding turns listener and won listener to db reference
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                            // hide progress dialogue if showing
                                            if(progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            // once the connection had made remove connection listener from the Database Reference
                                            databaseReference.child("connections").removeEventListener(this);
                                        }
                                    }
                                }
                            }
                            // in case user has not created the connection/room because of other room are available to join
                            else{

                                // checking if the connection has 1 player and need 1 more player to play the match then join this connection
                                if(getPlayerCount == 1) {

                                    // add players to the connection
                                    connections.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                                    // getting both players
                                    for(DataSnapshot players : connections.getChildren()){

                                        String getOpponentName = players.child("player_name").getValue(String.class);
                                        opponentUniqueId = players.getKey();

                                        // first turn who created the connection / room
                                        playerTurn = opponentUniqueId;
                                        applyPlayerTurn(playerTurn);

                                        // setting playername to the TextView
                                        player2TV.setText(getOpponentName);

                                        // assigning connection id
                                        connectionId = conId;
                                        opponentFound = true;

                                        // adding turns listener and won listener to db reference
                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                        // hide progress dialogue if showing
                                        if(progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        // once the connection had made remove connection from the Database Reference
                                        databaseReference.child("connections").removeEventListener(this);

                                        break;
                                    }
                                }
                            }
                        }

                        // check if opponent is found and user is not waiting for the opponent anymore then create a new connection
                        if(!opponentFound && !status.equals("waiting")){

                            // generating unique id for the connection
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());

                            // add first player to the connection and wait for another to connect
                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                            status = "waiting";
                        }
                    }

                    // if there is no one waiting in firebase, create new connection and waiting for opponent
                    else{
                        // generating unique id for the connection
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

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // getting all turns of the connection
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getChildrenCount() == 2){

                        // getting box position selected by user
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));

                        // getting player id who selected the box
                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);

                        // checking if user has not selected the box before
                        if(!doneBoxes.contains(String.valueOf((getBoxPosition)))){

                            // select the box
                            doneBoxes.add(String.valueOf(getBoxPosition));

                            if(getBoxPosition == 1){
                                selectBox(image1, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 2){
                                selectBox(image2, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 3){
                                selectBox(image3, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 4){
                                selectBox(image4, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 5){
                                selectBox(image5, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 6){
                                selectBox(image6, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 7){
                                selectBox(image7, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 8){
                                selectBox(image8, getBoxPosition, getPlayerId);
                            }
                            else if(getBoxPosition == 9){
                                selectBox(image9, getBoxPosition, getPlayerId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // check if a user has won the match
                if(snapshot.hasChild("player_id")){

                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);

                    final WinDialogOnline winDialogOnline;

                    if(getWinPlayerId.equals(playerUniqueId)){

                        // show win dialogue
                        winDialogOnline = new WinDialogOnline(MainActivity_Online.this, "You won the game");
                    }
                    else {
                        // show win dialogue
                        winDialogOnline = new WinDialogOnline(MainActivity_Online.this, "You won the game");
                    }

                    winDialogOnline.setCancelable(false);
                    winDialogOnline.show();

                    // remove listeners from Database
                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };


        // onClickListeners for all the images ----------
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("1") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("2") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("3") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("4") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("5") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("6") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("7") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("8") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
            }
        });

        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if the box is not selected before, and current user's turn
                if(doneBoxes.contains("9") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)view).setImageResource(R.drawable.x_icon);

                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn = opponentUniqueId;
                }
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

    // -------- ADDED MENU BAR, April 22 ---------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exit_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Exit) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    // -------------------------------------------------

    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer){

        boxesSelectedBy[selectedBoxPosition -1] = selectedByPlayer;

        if(selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.x_icon);
            playerTurn = opponentUniqueId;
        }
        else{
            imageView.setImageResource(R.drawable.o_icon);
            playerTurn = playerUniqueId;
        }

        applyPlayerTurn(playerTurn);

        // checking wheather player has won the match
        if(checkPlayerWin(selectedByPlayer)){

            // sending won player unique id to firebase database wo opponent can be notified
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }

        // over the game if there is no box left to be selected
        if(doneBoxes.size() == 9){
            final WinDialogOnline winDialog = new WinDialogOnline(MainActivity_Online.this, "It is a draw!");
            winDialog.setCancelable(false);
            winDialog.show();
        }
    }

    private boolean checkPlayerWin(String playerId){

        boolean isPlayerWon = false;

        // compare player turns with every winning combination
        for(int i = 0; i < combinationsList.size(); i++){

            final int[] combination = combinationsList.get(i);

            // checking last three turn of user
            if(boxesSelectedBy[combination[0]].equals(playerId) &&
                    boxesSelectedBy[combination[1]].equals(playerId) &&
                    boxesSelectedBy[combination[2]].equals(playerId)){
                isPlayerWon = true;
            }
        }

        return isPlayerWon;
    }
}
