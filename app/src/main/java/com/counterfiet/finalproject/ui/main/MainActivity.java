package com.counterfiet.finalproject.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.counterfiet.finalproject.R;
import com.counterfiet.finalproject.databinding.ActivityMainBinding;
import com.counterfiet.finalproject.helper.ShowMessage;
import com.counterfiet.finalproject.helper.ViewAnimation;
import com.counterfiet.finalproject.ui.advance.AdvanceCheckActivty;
import com.counterfiet.finalproject.ui.barcode.CustomScanActivity;
import com.counterfiet.finalproject.ui.labelcheck.LabelCheckActivty;
import com.counterfiet.finalproject.ui.main.models.BarcodeItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding mainBinding;

    private static final int CAMERA_REQUEST_CODE=10001;
    private static final int GALLERY_REQUEST_CODE=20001;
    private static final int STORAGE_REQUEST_CODE=30001;


    private int current_state=-1;


    private IntentIntegrator integrator;
    private DatabaseReference databaseReference;
    DatabaseReference lebel_db;
    DatabaseReference barcode_db;
    private String TAG = "MAIN_ACTIVITY";
    public static List<String> labellist;
    public static List<BarcodeItem> barcodelist;
    private static final int BarCodeScannerViewControllerUserCanceledErrorCode = 99991;


    private List<View> view_list = new ArrayList<>();
    private List<RelativeLayout> step_view_list = new ArrayList<>();
    private int success_step = 0;
    private int current_step = 0;


    private Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding= DataBindingUtil.setContentView(this, R.layout.activity_main);
        labellist=new ArrayList<>();
        barcodelist=new ArrayList<>();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("ProductInfo");
        lebel_db=databaseReference.child("label");
        barcode_db=databaseReference.child("barcode");
        lebel_db.keepSynced(true);
        barcode_db.keepSynced(true);

        initComponent();

//        List<String> source=new ArrayList<>();
//        source.add("Step 1");
//        source.add("Step 2");
//        source.add("Step 3");
        if (getIntent().getStringExtra("productname")!=null){
            mainBinding.textView.setText(getIntent().getStringExtra("productname"));
            collapseAndContinue(0);
        }

//        mainBinding.stepView.setStepsViewIndicatorComplectingPosition(source.size()-1).
//                reverseDraw(false).
//                setStepViewTexts(source).setLinePaddingProportion(0.88f);


        integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomScanActivity.class);
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(true);
        integrator.setDesiredBarcodeFormats(integrator.ALL_CODE_TYPES);
        integrator.setOrientationLocked(false);


        mainBinding.button.setOnClickListener(this);
        mainBinding.button2.setOnClickListener(this);
        mainBinding.button3.setOnClickListener(this);
        getLabellist();
        getBarcodeList();

    }

    private void initComponent() {

        view_list.add(findViewById(R.id.lyt_title));
        view_list.add(findViewById(R.id.lyt_description));
        view_list.add(findViewById(R.id.lyt_time));

        // populate view step (circle in left)
        step_view_list.add(((RelativeLayout) findViewById(R.id.step_title)));
        step_view_list.add(((RelativeLayout) findViewById(R.id.step_description)));
        step_view_list.add(((RelativeLayout) findViewById(R.id.step_time)));

        for (View v : view_list) {
            v.setVisibility(View.GONE);
        }

        view_list.get(0).setVisibility(View.VISIBLE);
    }

    public void clickLabel(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tv_label_title:
                if (success_step >= 0 && current_step != 0) {
                    current_step = 0;
                    collapseAll();
                    ViewAnimation.expand(view_list.get(0));
                }
                break;
            case R.id.tv_label_description:
                if (success_step >= 1 && current_step != 1) {
                    current_step = 1;
                    collapseAll();
                    ViewAnimation.expand(view_list.get(1));
                }
                break;
            case R.id.tv_label_time:
                if (success_step >= 2 && current_step != 2) {
                    current_step = 2;
                    collapseAll();
                    ViewAnimation.expand(view_list.get(2));
                }
                break;
        }
    }

    private void collapseAndContinue(int index) {
        ViewAnimation.collapse(view_list.get(index));
        setCheckedStep(index);
        index++;
        current_step = index;
        success_step = index > success_step ? index : success_step;
        ViewAnimation.expand(view_list.get(index));
    }

    private void collapseAll() {
        for (View v : view_list) {
            ViewAnimation.collapse(v);
        }
    }

    private void setCheckedStep(int index) {
        RelativeLayout relative = step_view_list.get(index);
        relative.removeAllViews();
        ImageButton img = new ImageButton(this);
        img.setImageResource(R.drawable.ic_done);
        img.setBackgroundColor(Color.TRANSPARENT);
        img.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        relative.addView(img);
    }

    public void getLabellist(){
        lebel_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        String name=dataSnapshot1.child("name").getValue(String.class).toLowerCase();
                        labellist.add(name);
                    }

                }else {
                    Log.d("USER_DATA", "onDataChange: ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getBarcodeList(){
        barcode_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: "+dataSnapshot1.getValue().toString());
                        String barcode=String.valueOf(dataSnapshot1.child("barcode").getValue(long.class));
                        String name=dataSnapshot1.child("name").getValue(String.class);
                        BarcodeItem barcodeItem=new BarcodeItem();
                        barcodeItem.setBarcode(barcode);
                        barcodeItem.setName(name);
                        barcodelist.add(barcodeItem);
                        Log.d(TAG, "onActivityResult: "+dataSnapshot1.child("barcode").getValue(long.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        if (id==R.id.button){
            current_state=1;
            captureLabelImage();
        }else if (id==R.id.button2){
            if (((TextView) findViewById(R.id.textView)).getText().toString().trim().equals("")) {
                new ShowMessage(this).showTost("Complete first step");
                return;
            }
            scanBarcode();
        }
        else if (id==R.id.button3){
            if (((TextView) findViewById(R.id.textView2)).getText().toString().trim().equals("")) {
                new ShowMessage(this).showTost("Complete 2nd step");
                return;
            }
            current_state=2;
            takeRandomImage();
        }
    }

    private void takeRandomImage() {
        showImageImportDialog();
    }

    private void scanBarcode() {
        integrator.initiateScan();
    }

    private void captureLabelImage() {
        showImageImportDialog();
    }

    private void showImageImportDialog() {
        String[] items={" Camera", " Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    if (checkPermission()){
                        captureImage();
                    }
                    else {
                        requestPermission();
                    }
                }
                if (i==1){
                    if (checkPermission()){
                        pickImage();
                    }
                    else {
                        requestPermission();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void pickImage() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private void captureImage() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPicture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
    }

    private void requestPermission() {
        Dexter.withActivity(MainActivity.this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            captureImage();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int result1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result3==PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode == CAMERA_REQUEST_CODE) {
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                Uri uri=result.getUri();
                if (current_state==1)
                {
                    startActivity(new Intent(this, LabelCheckActivty.class).putExtra("image_uri",uri.toString()));
                }
                if (current_state==2){
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
                Log.d(TAG, "onActivityResult: " + code + result.getBarcodeImagePath());

                if (m.find()) {
                    String i = m.group();
                    System.out.println("-->>" + i);
                    if (!i.isEmpty()) {
//                        boolean isInserted = helper.insertData(result.getFormatName(), result.getContents());
//                        checkDataBase(result.getContents(), result.getFormatName(),result.getBarcodeImagePath());
                        for (BarcodeItem s:barcodelist){
                            if (TextUtils.equals(s.getBarcode(),code)){
                                if (!TextUtils.equals(s.getName().toLowerCase(),mainBinding.textView.getText().toString().toLowerCase())){
                                    new ShowMessage(MainActivity.this).showTost("Label and barcode not matched");
                                }
                                else {
                                    mainBinding.textView2.setText("Scanned barcode: "+code);
                                    new ShowMessage(MainActivity.this).showTost("This step done");
                                    collapseAndContinue(1);
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
                    new ShowMessage(MainActivity.this).showTost("TRY WITH ANOTHER STREP");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
