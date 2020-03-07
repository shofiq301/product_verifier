package com.counterfiet.finalproject.network.repositories;

import com.counterfiet.finalproject.ui.advance.models.FinalResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {

    @Multipart
    @POST(Constant.UPLOAD)
    Call<FinalResponse> getImageResponse(
            @Part MultipartBody.Part file
    );
//    @FormUrlEncoded
//    @POST(Common.ACCOUNT)
//    Call<SignupResponse> signupUser(
//            @Field("type") String type,
//            @Field("name") String name,
//            @Field("email") String email,
//            @Field("phone") String phone,
//            @Field("pass") String pass,
//            @Field("profession") String profession
//    );
//
//
//    @FormUrlEncoded
//    @POST(Common.ACCOUNT)
//    Call<LoginResponse> loginUser(
//            @Field("type") String type,
//            @Field("email") String email,
//            @Field("password") String pass
//    );
//
//    @FormUrlEncoded
//    @POST(Common.ACCOUNT)
//    Call<ForgotPassResponse> forgorPass(
//            @Field("type") String type,
//            @Field("email") String email
//    );
//
//    @GET(Common.HOME)
//    Call<HomeResponse> configureHome(@Query("user_id") String userID);
//
//    @GET(Common.IC_MOKE_TEST)
//    Call<IcMokeTestResponse> getIcMokeTest(@Query("cat_id") String catID,
//                                           @Query("user_id") String userID);
//
//    @GET(Common.IC_CHAPTER_TEST)
//    Call<IcMokeTestResponse> getIcChapterTest(@Query("cat_id") String catID,
//                                              @Query("user_id") String userID);
//
//    @GET(Common.QUESTION_LIST)
//    Call<QuestionListResponse> getExamQuestion(@Query("exam_id") String examID);
//
//    @FormUrlEncoded
//    @POST(Common.SUBMIT_RESULT)
//    Call<SubmitResResponse> getSubmitResult(
//            @Field("user_id") String userID,
//            @Field("exam_id") String examID,
//            @Field("ans") String answerArray);
//
//
//    @GET(Common.ACCOUNT_DATA)
//    Call<AccountResponse> profileView(@Query("user_id") String userID);
//
//    @GET(Common.PACKAGE_DATA)
//    Call<List<PackageResponse>> packageItem(@Query("action") String action);
//
//    @GET(Common.PACKAGE_DATA)
//    Call<List<PackageListItemResponse>> packageListItem(@Query("cat_id") String cat_id);
//
//    @GET(Common.NOTES_DATA)
//    Call<NoteItemResponse> getNoteItem(@Query("lang_id") String lang_id);
}
