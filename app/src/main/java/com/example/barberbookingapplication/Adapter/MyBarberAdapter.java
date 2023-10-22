package com.example.barberbookingapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbookingapplication.Common.Common;
import com.example.barberbookingapplication.Interface.IRecyclerItemSelectedListener;
import com.example.barberbookingapplication.Model.Barber;
import com.example.barberbookingapplication.Model.EventBus.EnableNextButton;
import com.example.barberbookingapplication.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barberList;
    List<CardView> cardViewList;

    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_barber, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_barber_name.setText(barberList.get(position).getName());
        if(barberList.get(position).getRatingTimes() != null)
            holder.ratingBar.setRating(barberList.get(position).getRating().floatValue() / barberList.get(position).getRatingTimes());
        else
            holder.ratingBar.setRating(0);
        if(!cardViewList.contains(holder.card_barber))
            cardViewList.add(holder.card_barber);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                //set background for all item not chosen
                for(CardView cardView : cardViewList)
                {
                    cardView.setCardBackgroundColor(context.getResources()
                            .getColor(android.R.color.white));
                }
                //set background for chosen
                holder.card_barber.setCardBackgroundColor(
                        context.getResources()
                                .getColor(android.R.color.holo_orange_dark)
                );


                //Event bus declaration
                EventBus.getDefault().postSticky(new EnableNextButton(2,barberList.get(position)));
            }
        });

    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_barber_name;
        RatingBar ratingBar;
        CardView card_barber;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_barber = (CardView)itemView.findViewById(R.id.card_barber);
            txt_barber_name = (TextView)itemView.findViewById(R.id.txt_barber_name);
            ratingBar = (RatingBar)itemView.findViewById(R.id.rtb_barber);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}
