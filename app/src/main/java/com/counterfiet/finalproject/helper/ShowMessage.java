package com.counterfiet.finalproject.helper;

import android.app.Activity;
import android.content.Context;

import com.google.android.material.snackbar.Snackbar;

public class ShowMessage {

    Activity activity;

    public ShowMessage(Context context) {
        this.activity = (Activity)context;
    }

    public void showTost(String message){
        Snackbar snack = Snackbar.make(
                (((Activity) activity).findViewById(android.R.id.content)),
                message + "", Snackbar.LENGTH_LONG);
        snack.setActionTextColor(activity.getResources().getColor(android.R.color.holo_red_light ));
        snack.show();
    }
}
