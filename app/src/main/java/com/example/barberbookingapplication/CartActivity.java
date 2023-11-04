package com.example.barberbookingapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.example.barberbookingapplication.Adapter.MyCartAdapter;
import com.example.barberbookingapplication.Database.CartDatabase;
import com.example.barberbookingapplication.Database.CartItem;
import com.example.barberbookingapplication.Database.DatabaseUtils;
import com.example.barberbookingapplication.Interface.ICartItemLoadListener;
import com.example.barberbookingapplication.Interface.ICartItemUpdateListener;
import com.example.barberbookingapplication.Interface.ISumCartListener;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CartActivity extends AppCompatActivity implements ICartItemLoadListener, ICartItemUpdateListener, ISumCartListener {

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.btn_clear_cart)
    Button btn_clear_cart;

    @OnClick(R.id.btn_clear_cart)
    void clearCart(){

        DatabaseUtils.clearCart(cartDatabase);

        //Update adapter
        DatabaseUtils.getAllCart(cartDatabase,this);
        txt_total_price.setText("$0");

    }

    CartDatabase cartDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(CartActivity.this);

        cartDatabase = CartDatabase.getInstance(this);

        DatabaseUtils.getAllCart(cartDatabase,this);

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(linearLayoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this,linearLayoutManager.getOrientation()));
    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
        //After get all cart item from DB, display through recycler view
        MyCartAdapter adapter = new MyCartAdapter(this,cartItemList,this);
        Long totalPrice = 0L;
        for (CartItem item:cartItemList){
            totalPrice+= item.getProductPrice() * item.getProductQuantity();
        }

        txt_total_price.setText("$" + totalPrice.toString());
        recycler_cart.setAdapter(adapter);
    }

    @Override
    public void onCartItemUpdateSuccess() {
        DatabaseUtils.sumCart(cartDatabase,this);
    }

    @Override
    public void onSumCartSuccess(Long value) {
        Log.d("COUNT_CART", ""+value);
        txt_total_price.setText(new StringBuilder("$").append(value));
    }
}