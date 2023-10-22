package com.example.barberbookingapplication.Interface;

import com.example.barberbookingapplication.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);
}
