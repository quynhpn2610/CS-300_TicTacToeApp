package com.example.tictactoe;

import android.app.Dialog;
import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class WinDialogOnline extends Dialog {

    private final String message;
    private final MainActivity_Online mainActivity_online;
    public WinDialogOnline(@NonNull Context context, String message) {
        super(context);
        this.message = message;
        this.mainActivity_online = ((MainActivity_Online)context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_dialog_layout_online);

        final TextView messageTV = findViewById(R.id.messageTV);
        final Button startBtn = findViewById(R.id.startNewBtn);

        messageTV.setText(message);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getContext().startActivity(new Intent(getContext(), OnlinePlayerName.class));
                mainActivity_online.finish();
            }
        });
    }
}
