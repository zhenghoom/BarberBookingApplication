package com.example.barberbookingapplication.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberbookingapplication.Adapter.HomeSliderAdapter;
import com.example.barberbookingapplication.Adapter.LookbookAdapter;
import com.example.barberbookingapplication.BookingActivity;
import com.example.barberbookingapplication.CartActivity;
import com.example.barberbookingapplication.Common.Common;
import com.example.barberbookingapplication.Database.CartDatabase;
import com.example.barberbookingapplication.Database.DatabaseUtils;
import com.example.barberbookingapplication.HistoryActivity;
import com.example.barberbookingapplication.Interface.IBannerLoadListener;
import com.example.barberbookingapplication.Interface.IBookingInfoLoadListener;
import com.example.barberbookingapplication.Interface.IBookingInformationChangeListener;
import com.example.barberbookingapplication.Interface.ICountItemInCartListener;
import com.example.barberbookingapplication.Interface.ILookbookLoadListener;
import com.example.barberbookingapplication.MainActivity;
import com.example.barberbookingapplication.Model.Banner;
import com.example.barberbookingapplication.Model.BookingInformation;
import com.example.barberbookingapplication.R;
import com.example.barberbookingapplication.Service.PicassoImageLoadingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;


public class HomeFragment extends Fragment implements ILookbookLoadListener, IBannerLoadListener, IBookingInfoLoadListener, IBookingInformationChangeListener, ICountItemInCartListener {

    private Unbinder unbinder;

    CartDatabase cartDatabase;

    AlertDialog dialog;

    @BindView(R.id.notification_badge)
    NotificationBadge notificationBadge;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;
    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;

    @OnClick(R.id.layout_user_information)
    void onLogOutDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Sign out")
                .setMessage("Do you wish to sign out?")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Common.currentBarber=null;
                    Common.currentBooking=null;
                    Common.currentSalon=null;
                    Common.currentTimeSlot=-1;
                    Common.currentBookingId="";
                    Common.currentUser=null;

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();

                });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking()
    {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking()
    {
        changeBookingFromUser();
    }

    private void changeBookingFromUser() {
        androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("WARNING")
                .setMessage("Do you really want to change booking information?\nThis will delete your old booking information\n")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteBookingFromBarber(true);
                    }
                });
        confirmDialog.show();
    }

    private void deleteBookingFromBarber(boolean isChange) {
        //To delete booking, need delete from barber collection first
        //After that delete from user booking collections
        //Then delete event

        //Load Common.currentBooking because we need some data from bookingInformation
        if(Common.currentBooking != null)
        {
            dialog.show();

            //Get booking information in barber object
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCityBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barber")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());

            //When have document, delete it
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //after delete in barber, start delete from User
                    deleteBookingFromUser(isChange);

                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Current booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(boolean isChange) {
        //First, get information from user object
        if(!TextUtils.isEmpty(Common.currentBookingId))
        {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);

            androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                    .setCancelable(false)
                    .setTitle("WARNING")
                    .setMessage("Do you really wish to delete this booking?\nThis action cannot be undone!\n")
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            dialog.dismiss();
                        }
                    }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Delete
                            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //After delete from user, delete from Calendar
                                    //First save Uri of event
                                    Paper.init(getActivity());
                                    if (Paper.book().read(Common.EVENT_URI_CACHE) != null)
                                    {
                                        String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                                        Uri eventUri = null;
                                        if(eventString != null && !TextUtils.isEmpty(eventString))
                                            eventUri = Uri.parse(eventString);
                                        if(eventUri != null)
                                            getActivity().getContentResolver().delete(eventUri,null,null);
                                    }
                                    Toast.makeText(getActivity(), "Delete Booking Successful!", Toast.LENGTH_SHORT).show();
                                    //Refresh
                                    loadUserBooking();
                                    //CHeck if isChange -> Call from change button, will fired interface
                                    if(isChange)
                                        iBookingInformationChangeListener.onBookingInformationChange();

                                    dialog.dismiss();
                                }
                            });
                        }
                    });
            confirmDialog.show();

        }
        else
        {
            dialog.dismiss();

            Toast.makeText(getContext(), "Booking Information ID must not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.card_view_booking)
    void booking(){
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    @OnClick(R.id.card_view_history)
    void openHistoryActivity(){
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }
    @OnClick(R.id.card_view_cart)
    void openCartActivity(){
        startActivity(new Intent(getActivity(), CartActivity.class));
    }

    //Firestore
    CollectionReference bannerRef, lookbookRef;

    //Interface
    IBannerLoadListener iBannerLoadListener;
    ILookbookLoadListener iLookbookLoadListener;
    IBookingInfoLoadListener iBookingInfoLoadListener;
    IBookingInformationChangeListener iBookingInformationChangeListener;

    ListenerRegistration userBookingListener = null;
    com.google.firebase.firestore.EventListener<QuerySnapshot> userBookingEvent = null;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");

    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        //Select booking information from firebase with done=false and timestamp greater than today
        userBooking
                .whereGreaterThanOrEqualTo("timestamp",toDayTimeStamp)
                .whereEqualTo("done",false)
                .limit(1) //only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful())
                        {
                            if(!task.getResult().isEmpty())
                            {
                                for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult())
                                {
                                    BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                    iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                                    break;
                                }
                            }
                            else
                                iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());
                    }
                });
        //After userBooking has been assigned, call realtime listener
        if(userBookingEvent != null) //If userBookingEvent already init
        {
            if(userBookingListener == null) //Only add if userBookingListener == null
            {
                userBookingListener = userBooking
                        .addSnapshotListener(userBookingEvent);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this,view);

        cartDatabase = CartDatabase.getInstance(getContext());

        //Init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLookbookLoadListener = this;
        iBookingInfoLoadListener = this;
        iBookingInformationChangeListener = this;

        //Check if user log into account
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            setUserInformation();
            loadBanner();
            loadLookBook();
            initRealtimeUserBooking();
            loadUserBooking();
            countCartItem();
        }

        return view;
    }

    private void initRealtimeUserBooking() {
        if(userBookingEvent == null)
        {
            userBookingEvent = new EventListener<QuerySnapshot>(){

                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    //When it start, call loadUserBooking again
                    //to reload all booking information
                    loadUserBooking();
                }
            };
        }
    }

    private void countCartItem() {
        DatabaseUtils.countItemInCart(cartDatabase, this);
    }

    private void loadLookBook() {
        lookbookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> lookbooks = new ArrayList<>();
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot bannerSnapshot: task.getResult())
                            {
                                Banner banner = bannerSnapshot.toObject(Banner.class);
                                lookbooks.add(banner);
                            }
                            iLookbookLoadListener.onLookbookLoadSuccess(lookbooks);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iLookbookLoadListener.onLookbookLoadFailed(e.getMessage());
                    }
                });
    }

    private void loadBanner() {
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot bannerSnapshot: task.getResult())
                            {
                                Banner banner = bannerSnapshot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            iBannerLoadListener.onBannerLoadSuccess(banners);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBannerLoadListener.onBannerLoadFailed(e.getMessage());
                    }
                });
    }

    private void setUserInformation(){
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
        txt_user_phone.setText(Common.currentUser.getPhoneNumber());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(),banners));
    }

    @Override
    public void onLookbookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(),message ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(),0).toString();

        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInformationChange() {
        //start activity booking
        startActivity(new Intent(getActivity(),BookingActivity.class));
    }

    @Override
    public void onCartItemCountSuccess(int count) {
        notificationBadge.setText(String.valueOf(count));
    }

    @Override
    public void onDestroy() {
        if(userBookingListener != null)
            userBookingListener.remove();
        super.onDestroy();
    }
}