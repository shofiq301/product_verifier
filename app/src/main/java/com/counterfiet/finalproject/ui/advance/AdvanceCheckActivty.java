package com.counterfiet.finalproject.ui.advance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.counterfiet.finalproject.R;
import com.counterfiet.finalproject.databinding.ActivityAdvanceCheckActivtyBinding;
import com.counterfiet.finalproject.helper.ShowMessage;
import com.counterfiet.finalproject.ui.advance.models.FinalResponse;
import com.counterfiet.finalproject.ui.advance.view.AdvanceView;
import com.counterfiet.finalproject.ui.home.HomeActivity;
import com.counterfiet.finalproject.ui.home.fragments.mainhome.HomeViewModel;
import com.counterfiet.finalproject.ui.labelcheck.LabelCheckActivty;
import com.counterfiet.finalproject.ui.main.MainActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class AdvanceCheckActivty extends AppCompatActivity implements AdvanceView, View.OnClickListener///CameraBridgeViewBase.CvCameraViewListener2 //
{

    private ActivityAdvanceCheckActivtyBinding activityAdvanceCheckActivtyBinding;
    private Mat mat1, mat2, mat3;
    private BaseLoaderCallback baseLoaderCallback;
    private Uri uri;
    private Bitmap grayBitmap, imageBitmap;
     private ProgressDialog progressDialog;
     private AdvanceViewHodel advanceViewHodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        advanceViewHodel =
                ViewModelProviders.of(this).get(AdvanceViewHodel.class);
        activityAdvanceCheckActivtyBinding = DataBindingUtil.setContentView(this, R.layout.activity_advance_check_activty);

        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Processing...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);


//        activityAdvanceCheckActivtyBinding.cameraview.setVisibility(SurfaceView.VISIBLE);
//        activityAdvanceCheckActivtyBinding.cameraview.setCvCameraViewListener(this);
//
//        baseLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                switch (status) {
//                    case BaseLoaderCallback.SUCCESS:
//                        activityAdvanceCheckActivtyBinding.cameraview.enableView();
//                        break;
//                    default:
//                        super.onManagerConnected(status);
//                        break;
//                }
//            }
//        };
        if (getIntent() != null) {
            uri = Uri.parse(getIntent().getStringExtra("image_uri"));
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        activityAdvanceCheckActivtyBinding.imageResult.setImageBitmap(imageBitmap);
        activityAdvanceCheckActivtyBinding.btnAnalisis.setOnClickListener(this);

        advanceViewHodel.getFinalResponseMutableLiveData().observe(this, new Observer<FinalResponse>() {
            @Override
            public void onChanged(FinalResponse finalResponse) {
                new ShowMessage(AdvanceCheckActivty.this).showTost("This image accuracy "+String.format("%.2f",Double.parseDouble(finalResponse.getResult().toString()))+" %");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(AdvanceCheckActivty.this, HomeActivity.class)
                                .putExtra("result",String.format("%.2f",Double.parseDouble(finalResponse.getResult().toString()))+" %"));
                        finish();
                    }
                },1000);
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_analisis) {
//            statrtWorking();
            advanceViewHodel.setFinalResponseMutableLiveData(AdvanceCheckActivty.this,uri,AdvanceCheckActivty.this);
        }
    }

    private void statrtWorking() {
        Mat rgba = new Mat();
        Mat grayMat = new Mat();
        Mat cannyImage = new Mat();

        BitmapFactory.Options bo = new BitmapFactory.Options();
        bo.inDither = false;
        bo.inSampleSize = 4;

        int height = imageBitmap.getHeight();
        int width = imageBitmap.getWidth();

        grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //Bitmap to MAT
        Utils.bitmapToMat(imageBitmap, rgba);
        Imgproc.cvtColor(rgba, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(grayMat, cannyImage, 100, 80);
        Utils.matToBitmap(cannyImage, grayBitmap);
        activityAdvanceCheckActivtyBinding.imageResult.setImageBitmap(grayBitmap);
    }

//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        mat1 = new Mat(width, height, CvType.CV_8UC4);
//        mat2 = new Mat(width, height, CvType.CV_8UC4);
//        mat3 = new Mat(width, height, CvType.CV_8UC4);
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        mat1.release();
//        mat2.release();
//        mat3.release();
//    }

    //    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mat1 = inputFrame.rgba();
//        //rotate frame
//        Core.transpose(mat1,mat2);
//        Imgproc.resize(mat2,mat3,mat3.size(),0,0,0);
//        Core.flip(mat3,mat1,1);
//        return mat1;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (activityAdvanceCheckActivtyBinding.cameraview != null) {
//            activityAdvanceCheckActivtyBinding.cameraview.disableView();
//        }
//    }
//
    @Override
    protected void onResume() {
        super.onResume();
//        if (OpenCVLoader.initDebug())
//            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        OpenCVLoader.initDebug();
    }

    @Override
    public void onLoadStatrt() {
        if (progressDialog!=null){
            progressDialog.show();
        }
    }

    @Override
    public void onLoadError(String error) {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        Log.d("USER_DATA_ERROR", "onLoadError: "+error);
    }

    @Override
    public void onServerError(String error) {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        Log.d("USER_DATA_ERROR", "onServerError: "+error);
    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onProgressFinish() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (activityAdvanceCheckActivtyBinding.cameraview != null) {
//            activityAdvanceCheckActivtyBinding.cameraview.disableView();
//        }
//    }
}
