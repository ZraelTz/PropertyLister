package com.ztech.propertylister;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ztech.lodgeme.R;

public class GuestProfileMenu extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_profile_menu);
        Tools.setSystemBarColor(this, R.color.grey_90);
        getWindow().setNavigationBarColor(getColor(R.color.grey_90));
        mAuth = FirebaseAuth.getInstance();
        nameText = findViewById(R.id.guest_name);
        setUserDisplayName();
        findViewById(R.id.guest_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                updateUI();
            }
        });
    }
    private void setUserDisplayName(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            nameText.setText(mAuth.getCurrentUser().getEmail());
        }
    }

    private void updateUI(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
           finish();
        }
    }
}
