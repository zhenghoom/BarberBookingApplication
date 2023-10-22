package com.example.barberbookingapplication.Interface;

import com.example.barberbookingapplication.Database.CartItem;

import java.util.List;

public interface ICartItemLoadListener {
    void onGetAllItemFromCartSuccess(List<CartItem> cartItemList);
}
