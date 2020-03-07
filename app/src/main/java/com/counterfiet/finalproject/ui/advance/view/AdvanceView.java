package com.counterfiet.finalproject.ui.advance.view;

public interface AdvanceView {
    public void onLoadStatrt();
    public void onLoadError(String error);
    public void onServerError(String error);
    public void onProgressUpdate(int percent);
    public void onProgressFinish();
}
