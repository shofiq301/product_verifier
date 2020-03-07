package com.counterfiet.finalproject.ui.advance;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.counterfiet.finalproject.helper.NetworkHelper;
import com.counterfiet.finalproject.network.provider.RetrofitClient;
import com.counterfiet.finalproject.ui.advance.models.FinalResponse;
import com.counterfiet.finalproject.ui.advance.view.AdvanceView;
import com.counterfiet.finalproject.ui.advance.view.ProgressRequestBody;
import com.counterfiet.finalproject.utils.FileUtils;
import com.counterfiet.finalproject.utils.FileUtils1;


import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;


public class AdvanceViewHodel extends ViewModel {
    private MutableLiveData<FinalResponse> finalResponseMutableLiveData;

    public AdvanceViewHodel() {
        this.finalResponseMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<FinalResponse> getFinalResponseMutableLiveData() {
        return finalResponseMutableLiveData;
    }

    public void setFinalResponseMutableLiveData(Context context, Uri fileuri, AdvanceView advanceView) {
        advanceView.onLoadStatrt();
        if (NetworkHelper.hasNetworAccess(context)){
            // create upload service client
            File file=new File(FileUtils1.getPath(context,fileuri));
            ProgressRequestBody progressRequestBody=new ProgressRequestBody(file,advanceView);
            MultipartBody.Part body=MultipartBody.Part.createFormData("image",file.getName(),progressRequestBody);
//
//            // create RequestBody instance from file
//            RequestBody requestFile =
//                    RequestBody.create(
//                            MediaType.parse(context.getContentResolver().getType(fileuri)),
//                            file
//                    );
//
//            // MultipartBody.Part is used to send also the actual file name
//            MultipartBody.Part body =
//                    MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    RetrofitClient.getInstance().getApi().getImageResponse(body).enqueue(new Callback<FinalResponse>() {
                        @Override
                        public void onResponse(Call<FinalResponse> call, Response<FinalResponse> response) {
                            advanceView.onProgressFinish();
                            if (response.isSuccessful()){
                                finalResponseMutableLiveData.setValue(response.body());
                            }
                            else {
                                advanceView.onServerError(response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<FinalResponse> call, Throwable t) {
                            advanceView.onLoadError(t.getMessage());
                        }
                    });
                }
            }).start();

        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    advanceView.onLoadError("No Internet connection");
                }
            },100);
        }
    }
}
