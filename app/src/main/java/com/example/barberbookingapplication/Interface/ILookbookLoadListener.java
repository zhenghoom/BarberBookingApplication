package com.example.barberbookingapplication.Interface;

import com.example.barberbookingapplication.Model.Banner;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);
    void onLookbookLoadFailed(String message);
}
