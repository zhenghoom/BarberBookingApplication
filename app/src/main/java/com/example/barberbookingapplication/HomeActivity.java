package com.example.barberbookingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.barberbookingapplication.Common.Common;
import com.example.barberbookingapplication.Fragments.HomeFragment;
import com.example.barberbookingapplication.Fragments.ShoppingFragment;
import com.example.barberbookingapplication.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();

        //Check rating dialog
        checkRatingDialog();
    }

    private void checkRatingDialog() {
        Paper.init(this);
        String dataSerialized = Paper.book().read(Common.RATING_INFORMATION_KEY,"");
        if(!TextUtils.isEmpty(dataSerialized)) //If not null
        {
            Map<String,String> dataReceived = new Gson()
                    .fromJson(dataSerialized,new TypeToken<Map<String,String>>(){}.getType());
            if(dataReceived != null)
            {
                Common.showRatingDialog(HomeActivity.this,
                        dataReceived.get(Common.RATING_STATE_KEY),
                        dataReceived.get(Common.RATING_SALON_ID),
                        dataReceived.get(Common.RATING_SALON_NAME),
                        dataReceived.get(Common.RATING_BARBER_ID));
            }
        }
    }

        @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        //Init
        userRef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //Check intent, if is login = true, enable full access
        //If is login = false, just let user around shopping to view
        if(getIntent() != null)
        {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if(isLogin)
            {
                dialog.show();
                //Check if user is exists
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                Paper.init(HomeActivity.this);
                Paper.book().write(Common.LOGGED_KEY, user.getPhoneNumber());

                DocumentReference currentUser = userRef.document(user.getPhoneNumber());
                currentUser.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    DocumentSnapshot userSnapshot = task.getResult();
                                    if(!userSnapshot.exists())
                                    {
                                        showUpdateDialog(user.getPhoneNumber());
                                    } else {
                                        Common.currentUser = userSnapshot.toObject(User.class);
                                        bottomNavigationView.setSelectedItemId(R.id.action_home);
                                    }
                                    if(dialog.isShowing())
                                        dialog.dismiss();


                                }
                            }
                        });

            }

        }
        //View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if(menuItem.getItemId() == R.id.action_shopping)
                    fragment = new ShoppingFragment();

                return loadFragment(fragment);

            }
        });

    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(String phoneNumber){

        //Init dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(view -> {

            if(!dialog.isShowing())
                dialog.show();

            final User user = new User(edt_name.getText().toString(),
                    edt_address.getText().toString(),
                    phoneNumber);
            userRef.document(phoneNumber)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            bottomSheetDialog.dismiss();
                            if(dialog.isShowing())
                                dialog.dismiss();
                            Common.currentUser = user;
                            bottomNavigationView.setSelectedItemId(R.id.action_home);

                            Toast.makeText(HomeActivity.this,"Thank you" ,Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            bottomSheetDialog.dismiss();
                            if(dialog.isShowing())
                                dialog.dismiss();
                            Toast.makeText(HomeActivity.this,""+e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}