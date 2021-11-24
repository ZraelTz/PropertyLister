package com.ztech.propertylister;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ztech.lodgeme.R;
import com.ztech.propertylister.utils.ViewAnimation;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {
    private Context mcontext;
    private FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    private DocumentReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mcontext=this.getApplicationContext();
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
        initComponent();
    }

    private void initComponent(){
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if(currentUser != null){
                            getUserRole();
                        } else {
                            showSteppersForFreshers();
                            finish();
                        }
                    }
                },1500

        );
    }

    private void loadingAndDisplayContent() {
        final LinearLayout splash_progress = (LinearLayout) findViewById(R.id.splash_progress);
        splash_progress.setVisibility(View.VISIBLE);
        splash_progress.setAlpha(1.0f);
        ViewAnimation.fadeOut(splash_progress);
    }

    private String getUserRole(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        if(!mAuth.getCurrentUser().equals(null)){
            String userRole = " ";
            String currentUserEmail = mAuth.getCurrentUser().getEmail();
            ref = firebaseFirestore.collection("users").document(currentUserEmail);
            ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    loadingAndDisplayContent();
                    firebaseFirestore.collection("users")
                            .whereEqualTo("Email", currentUserEmail)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                             updateUI(document.getString("Role"));
                                        }
                                    } else {
                                        Log.d("Msg", "Error getting result: ", task.getException());
                                    }
                                }
                            });
                }
            });
            return userRole;
        } else return "nonUser";

    }

    private void showSteppersForFreshers(){
        Intent intent = new Intent(mcontext, StepperActivity.class);
        startActivity(intent);
    }

    private void updateUI(String role) {
        if(role.contains("guest")){
            Intent intent = new Intent(mcontext, GuestMapNavigation.class);
            startActivity(intent);
            finish();
        } else if(role.contains("agent")){
            Intent intent = new Intent(mcontext, AgentDashboard.class);
            startActivity(intent);
            finish();
        } else if (getUserRole().equals("nonUser")) {
            showSteppersForFreshers();
            finish();
        }
    }

}
