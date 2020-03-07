package com.counterfiet.finalproject.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.counterfiet.finalproject.R;
import com.counterfiet.finalproject.helper.ShowMessage;
import com.counterfiet.finalproject.ui.advance.AdvanceCheckActivty;
import com.counterfiet.finalproject.ui.home.fragments.mainhome.HomeFragment;
import com.counterfiet.finalproject.ui.labelcheck.LabelCheckActivty;
import com.counterfiet.finalproject.ui.main.MainActivity;
import com.counterfiet.finalproject.ui.main.models.BarcodeItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_history, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == HomeFragment.homeFragment.GALLERY_REQUEST_CODE) {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode == HomeFragment.homeFragment.CAMERA_REQUEST_CODE) {
                CropImage.activity(HomeFragment.homeFragment.image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                Uri uri=result.getUri();
                if (HomeFragment.homeFragment.current_state==1)
                {
                    startActivity(new Intent(this, LabelCheckActivty.class).putExtra("image_uri",uri.toString()));
                }
                if (HomeFragment.homeFragment.current_state==2){
                    startActivity(new Intent(this, AdvanceCheckActivty.class).putExtra("image_uri",uri.toString()));
                }
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception exception=result.getError();
                new ShowMessage(this).showTost(exception.getMessage());
            }
        }
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                String code = result.getContents();
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher m = pattern.matcher(code);
                Log.d(HomeFragment.homeFragment.TAG, "onActivityResult: " + code + result.getBarcodeImagePath());

                if (m.find()) {
                    String i = m.group();
                    System.out.println("-->>" + i);
                    if (!i.isEmpty()) {
//                        boolean isInserted = helper.insertData(result.getFormatName(), result.getContents());
//                        checkDataBase(result.getContents(), result.getFormatName(),result.getBarcodeImagePath());
                        for (BarcodeItem s:HomeFragment.homeFragment.barcodelist){
                            if (TextUtils.equals(s.getBarcode(),code)){
                                if (!TextUtils.equals(s.getName().toLowerCase(),HomeFragment.homeFragment.homeBinding.txtLabelResult.getText().toString().toLowerCase())){
                                    new ShowMessage(this).showTost("Label and barcode not matched");
                                }
                                else {
                                    HomeFragment.homeFragment.homeBinding.txtBarcodeResult.setText("Barcode: "+code);
                                    Bitmap myBitmap = BitmapFactory.decodeFile(result.getBarcodeImagePath());
                                    HomeFragment.homeFragment.homeBinding.barcodeImage.setImageBitmap(myBitmap);
                                    HomeFragment.homeFragment.homeBinding.txtBarcodeResult.setVisibility(View.VISIBLE);
                                    HomeFragment.homeFragment.homeBinding.barcodeImage.setVisibility(View.VISIBLE);
                                    HomeFragment.homeFragment.collapseAndContinue(1);
                                }

                            }
                        }

//                    if (isInserted){
//                        barcodeItems.clear();
//                        barcodeItems=helper.getAllInfo();
//                        adapter=new MyRecyclerAdapter(this, barcodeItems, new MyRecyclerAdapter.OnItemClick() {
//                            @Override
//                            public void onClick(BarcodeItem item, int pos) {
//                                progressBar.setVisibility(View.VISIBLE);
//                                checkDataBase(item.getType());
//                            }
//                        });
//                        recycler_items_qr.setAdapter(adapter);
//                        adapter.notifyDataSetChanged();
//
//                    }
                    }
                } else {
                    new ShowMessage(HomeActivity.this).showTost("TRY WITH ANOTHER STREP");
                }
            }
        }
    }
}
