package com.example.barberbookingapplication.Interface;

import com.example.barberbookingapplication.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);
}
