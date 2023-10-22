package com.example.barberbookingapplication.Model.EventBus;

import com.example.barberbookingapplication.Model.BookingInformation;

import java.util.List;

public class UserBookingLoadEvent {
    private Boolean success;
    private String message;
    private List<BookingInformation> bookingInformationList;

    public UserBookingLoadEvent(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public UserBookingLoadEvent(Boolean success, List<BookingInformation> bookingInformationList) {
        this.success = success;
        this.bookingInformationList = bookingInformationList;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BookingInformation> getBookingInformationList() {
        return bookingInformationList;
    }

    public void setBookingInformationList(List<BookingInformation> bookingInformationList) {
        this.bookingInformationList = bookingInformationList;
    }
}
