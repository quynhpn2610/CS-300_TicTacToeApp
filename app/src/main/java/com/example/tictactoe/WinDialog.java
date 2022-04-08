package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class WinDialog extends Dialog {

    private final String message;
    private final MainActivity_Offline mainActivity_offline;

    public WinDialog(@NonNull Context context, String message, MainActivity_Offline mainActivity_offline){
        super(context);
        this.message = message;
        this.mainActivity_offline = mainActivity_offline;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.win_dialog_layout);

        final TextView messageTxt = findViewById(R.id.messageTxt);
        final Button startAgainBtn = findViewById(R.id.startAgainBtn);

        messageTxt.setText(message);

        startAgainBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mainActivity_offline.restartMatch();
                dismiss();
            }
        });
    }
}
