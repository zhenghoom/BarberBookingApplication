package com.example.barberbookingapplication.Model.EventBus;

import com.example.barberbookingapplication.Model.Barber;

import java.util.List;

public class BarberDoneEvent {
    private List<Barber> barberList;

    public BarberDoneEvent(List<Barber> barberList) {
        this.barberList = barberList;
    }

    public List<Barber> getBarberList() {
        return barberList;
    }

    public void setBarberList(List<Barber> barberList) {
        this.barberList = barberList;
    }
}
