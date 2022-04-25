package com.example.tictactoe;

import android.app.Dialog;
import androidx.annotation.NonNull;

import android.content.Context;
import java.util.Objects;

public class WinDialogOnline extends Dialog {

    private final String message;
    public WinDialogOnline(@NonNull Context context, String message) {
        super(context);
        this.message = message;
    }
}