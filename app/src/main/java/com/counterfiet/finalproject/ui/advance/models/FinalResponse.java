
package com.counterfiet.finalproject.ui.advance.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FinalResponse {

    @SerializedName("1ST_Image")
    @Expose
    private String _1STImage;
    @SerializedName("2ND_Image")
    @Expose
    private String _2NDImage;
    @SerializedName("Good_matches")
    @Expose
    private Integer goodMatches;
    @SerializedName("Result")
    @Expose
    private Double result;

    public String get1STImage() {
        return _1STImage;
    }

    public void set1STImage(String _1STImage) {
        this._1STImage = _1STImage;
    }

    public String get2NDImage() {
        return _2NDImage;
    }

    public void set2NDImage(String _2NDImage) {
        this._2NDImage = _2NDImage;
    }

    public Integer getGoodMatches() {
        return goodMatches;
    }

    public void setGoodMatches(Integer goodMatches) {
        this.goodMatches = goodMatches;
    }

    public Double getResult() {
        return result;
    }

    public void setResult(Double result) {
        this.result = result;
    }

}
