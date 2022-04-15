package com.example.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OnlinePlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_player_name);

        final EditText playerNameEt = findViewById(R.id.playerNameEt);
        final Button startOnlineGameBtn = findViewById(R.id.startOnlineGameBtn);

        startOnlineGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting player name from EditText to a String variable
                final String getPlayerName = playerNameEt.getText().toString();

                // checking whether player has entered his name
                if(getPlayerName.isEmpty()){
                    Toast.makeText(OnlinePlayerName.this, "Please enter player name", Toast.LENGTH_SHORT).show();
                }
                else{
                    // creating intent to open MainActivity
                    Intent intent = new Intent(OnlinePlayerName.this, MainActivity_Online.class);

                    // adding player name along with intent
                    intent.putExtra("playerName", getPlayerName);

                    // opening MainActivity
                    startActivity(intent);

                    // destroy this(PlayerName) activity
                    finish();
                }
            }
        });
    }

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
}
