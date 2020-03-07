package com.counterfiet.finalproject.network.provider;


import com.counterfiet.finalproject.network.repositories.Api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL="http://192.168.43.74:5000/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;
    private RetrofitClient(){
        retrofit=new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpCLient)
                .build();
    }

    public static synchronized RetrofitClient getInstance(){
        if (mInstance==null)
            mInstance=new RetrofitClient();
        return mInstance;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }
    final static OkHttpClient okHttpCLient=new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60,TimeUnit.SECONDS)
            .build();
}
