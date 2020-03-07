package com.counterfiet.finalproject.ui.home.fragments.mainhome;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.counterfiet.finalproject.R;
import com.counterfiet.finalproject.databinding.FragmentHomeBinding;
import com.counterfiet.finalproject.helper.ShowMessage;
import com.counterfiet.finalproject.helper.ViewAnimation;
import com.counterfiet.finalproject.ui.advance.AdvanceCheckActivty;
import com.counterfiet.finalproject.ui.barcode.CustomScanActivity;
import com.counterfiet.finalproject.ui.labelcheck.LabelCheckActivty;
import com.counterfiet.finalproject.ui.main.MainActivity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment implements View.OnClickListener {

    public static final int CAMERA_REQUEST_CODE=10001;
    public static final int GALLERY_REQUEST_CODE=20001;
    public static final int STORAGE_REQUEST_CODE=30001;


    public int current_state=-1;


    public IntentIntegrator integrator;
    public DatabaseReference databaseReference;
    DatabaseReference lebel_db;
    DatabaseReference barcode_db;
    public String TAG = "MAIN_ACTIVITY";
    public static List<String> labellist;
    public static List<BarcodeItem> barcodelist;
    public static final int BarCodeScannerViewControllerUserCanceledErrorCode = 99991;


    public List<View> view_list = new ArrayList<>();
    public List<RelativeLayout> step_view_list = new ArrayList<>();
    public int success_step = 0;
    public int current_step = 0;


    public Uri image_uri;
    private HomeViewModel homeViewModel;
    public FragmentHomeBinding homeBinding;
    public static HomeFragment homeFragment;

    public static HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        homeBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        View root = homeBinding.getRoot();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        homeFragment=this;
        labellist=new ArrayList<>();
        barcodelist=new ArrayList<>();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("ProductInfo");
        lebel_db=databaseReference.child("label");
        barcode_db=databaseReference.child("barcode");
        lebel_db.keepSynced(true);
        barcode_db.keepSynced(true);

        initComponent();

        if (getActivity().getIntent().getStringExtra("productname")!=null){
            homeBinding.txtLabelResult.setText(getActivity().getIntent().getStringExtra("productname"));
            homeBinding.txtLabelResult.setVisibility(View.VISIBLE);
            collapseAndContinue(0);
        }
        if (getActivity().getIntent().getStringExtra("image_uri")!=null){
            homeBinding.labelImage.setVisibility(View.VISIBLE);
            try {
                Bitmap imageBitmap=MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),Uri.parse(getActivity().getIntent().getStringExtra("image_uri")));
                homeBinding.labelImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                homeBinding.labelImage.setImageURI(Uri.parse(getActivity().getIntent().getStringExtra("image_uri")));
            }

        }
        if (getActivity().getIntent().getStringExtra("result")!=null){

        }

        integrator = new IntentIntegrator(getActivity());
        integrator.setCaptureActivity(CustomScanActivity.class);
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(true);
        integrator.setDesiredBarcodeFormats(integrator.ALL_CODE_TYPES);
        integrator.setOrientationLocked(false);


        homeBinding.btnCheckLabel.setOnClickListener(this);
        homeBinding.btnCheckBarcode.setOnClickListener(this);
        homeBinding.btnCheckRandomImage.setOnClickListener(this);

        homeBinding.tvLabelTitle.setOnClickListener(this::clickLabel);
        homeBinding.tvLabelDescription.setOnClickListener(this::clickLabel);
        homeBinding.tvLabelTime.setOnClickListener(this::clickLabel);
        getLabellist();
        getBarcodeList();
    }

    private void initComponent() {

        view_list.add(homeBinding.lytTitle);
        view_list.add(homeBinding.lytDescription);
        view_list.add(homeBinding.lytTime);

        // populate view step (circle in left)
        step_view_list.add(homeBinding.stepTitle);
        step_view_list.add(homeBinding.stepDescription);
        step_view_list.add(homeBinding.stepTime);

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

    public void collapseAndContinue(int index) {
        ViewAnimation.collapse(view_list.get(index));
        setCheckedStep(index);
        index++;
        current_step = index;
        success_step = index > success_step ? index : success_step;
        ViewAnimation.expand(view_list.get(index));
    }

    public void collapseAll() {
        for (View v : view_list) {
            ViewAnimation.collapse(v);
        }
    }

    private void setCheckedStep(int index) {
        RelativeLayout relative = step_view_list.get(index);
        relative.removeAllViews();
        ImageButton img = new ImageButton(getContext());
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
        AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
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
        getActivity().startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private void captureImage() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPicture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        getActivity().startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
    }

    private void requestPermission() {
        Dexter.withActivity(getActivity())
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
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        int result1 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result3==PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult: image "+requestCode+" "+CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
//        new ShowMessage(getActivity()).showTost("Hello");
////        if (resultCode == RESULT_OK) {
////
////        }
//        if (requestCode == GALLERY_REQUEST_CODE) {
//            CropImage.activity(data.getData())
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(getActivity());
//        }
//        if (requestCode == CAMERA_REQUEST_CODE) {
//            CropImage.activity(image_uri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(getActivity());
//        }
//        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            CropImage.ActivityResult result=CropImage.getActivityResult(data);
//            if (resultCode==Activity.RESULT_OK){
//                Uri uri=result.getUri();
//                if (current_state==1)
//                {
//                    startActivity(new Intent(getContext(), LabelCheckActivty.class).putExtra("image_uri",uri.toString()));
//                }
//                if (current_state==2){
//                    startActivity(new Intent(getContext(), AdvanceCheckActivty.class).putExtra("image_uri",uri.toString()));
//                }
//            }
//            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
//                Exception exception=result.getError();
//                new ShowMessage(getContext()).showTost(exception.getMessage());
//            }
//        }
//        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            if (result.getContents() == null) {
//            } else {
//                String code = result.getContents();
//                Pattern pattern = Pattern.compile("(\\d+)");
//                Matcher m = pattern.matcher(code);
//                Log.d(TAG, "onActivityResult: " + code + result.getBarcodeImagePath());
//
//                if (m.find()) {
//                    String i = m.group();
//                    System.out.println("-->>" + i);
//                    if (!i.isEmpty()) {
////                        boolean isInserted = helper.insertData(result.getFormatName(), result.getContents());
////                        checkDataBase(result.getContents(), result.getFormatName(),result.getBarcodeImagePath());
//                        for (BarcodeItem s:barcodelist){
//                            if (TextUtils.equals(s.getBarcode(),code)){
//                                if (!TextUtils.equals(s.getName().toLowerCase(),homeBinding.txtBarcodeResult.getText().toString().toLowerCase())){
//                                    new ShowMessage(getContext()).showTost("Label and barcode not matched");
//                                }
//                                else {
//                                    homeBinding.txtBarcodeResult.setText("Scanned barcode: "+code);
//                                    new ShowMessage(getContext()).showTost("This step done");
//                                    collapseAndContinue(1);
//                                }
//
//                            }
//                        }
//
////                    if (isInserted){
////                        barcodeItems.clear();
////                        barcodeItems=helper.getAllInfo();
////                        adapter=new MyRecyclerAdapter(this, barcodeItems, new MyRecyclerAdapter.OnItemClick() {
////                            @Override
////                            public void onClick(BarcodeItem item, int pos) {
////                                progressBar.setVisibility(View.VISIBLE);
////                                checkDataBase(item.getType());
////                            }
////                        });
////                        recycler_items_qr.setAdapter(adapter);
////                        adapter.notifyDataSetChanged();
////
////                    }
//                    }
//                } else {
//                    new ShowMessage(getContext()).showTost("TRY WITH ANOTHER STREP");
//                }
//            }
//        }
//
//    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        if (id==R.id.btn_check_label){
            current_state=1;
            captureLabelImage();
        }else if (id==R.id.btn_check_barcode){
            if (homeBinding.txtLabelResult.getText().toString().trim().equals("")) {
                new ShowMessage(getContext()).showTost("Complete first step");
                return;
            }
            scanBarcode();
        }
        else if (id==R.id.btn_check_random_image){
            if (homeBinding.txtBarcodeResult.getText().toString().trim().equals("")) {
                new ShowMessage(getContext()).showTost("Complete 2nd step");
                return;
            }
            current_state=2;
            takeRandomImage();
        }
    }
}