package com.counterfiet.finalproject.ui.labelcheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;

import com.counterfiet.finalproject.ui.home.HomeActivity;
import com.counterfiet.finalproject.ui.home.fragments.mainhome.HomeFragment;
import com.counterfiet.finalproject.ui.main.MainActivity;
import com.counterfiet.finalproject.R;
import com.counterfiet.finalproject.databinding.ActivityLabelCheckActivtyBinding;
import com.counterfiet.finalproject.helper.ShowMessage;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import static com.counterfiet.finalproject.ui.home.fragments.mainhome.HomeFragment.labellist;

public class LabelCheckActivty extends AppCompatActivity {

    private ActivityLabelCheckActivtyBinding activityLabelCheckActivtyBinding;
    private Uri uri;
    private boolean res=false;
    private String restext="";
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLabelCheckActivtyBinding = DataBindingUtil.setContentView(this, R.layout.activity_label_check_activty);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing...");
        dialog.show();
        if (getIntent() != null) {
            uri = Uri.parse(getIntent().getStringExtra("image_uri"));
        }
        try {
            Bitmap imageBitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            activityLabelCheckActivtyBinding.imgPreview.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            activityLabelCheckActivtyBinding.imgPreview.setImageURI(uri);
        }

        BitmapDrawable bitmapDrawable=(BitmapDrawable)activityLabelCheckActivtyBinding.imgPreview.getDrawable();
        Bitmap bitmap=bitmapDrawable.getBitmap();
        TextRecognizer recognizer=new TextRecognizer.Builder(getApplicationContext()).build();

        if (!recognizer.isOperational()){
            new ShowMessage(this).showTost("We can't detect any image, please try again");
        }
        else {
            Frame frame=new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items=recognizer.detect(frame);
            for (int i=0;i<items.size();i++){
                TextBlock textBlock=items.valueAt(i);
                for (String s:labellist){
                    if (TextUtils.equals(textBlock.getValue().toLowerCase(),s)){
                        activityLabelCheckActivtyBinding.resultText.setText(textBlock.getValue());
                        res=true;
                        restext=textBlock.getValue();
                        break;
                    }
                }

            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    if (res){
                        startActivity(new Intent(LabelCheckActivty.this, HomeActivity.class)
                                .putExtra("productname",restext)
                                .putExtra("image_uri",uri.toString()));
                        finish();
                    }
                    else {
                        new ShowMessage(LabelCheckActivty.this).showTost("NOT FOUND, PLEASE TRY WITH ANOTHER METHOD");
                        startActivity(new Intent(LabelCheckActivty.this, HomeActivity.class));
                        finish();
                    }
                }
            },1000);




        }
    }
}
