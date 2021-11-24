package com.ztech.propertylister;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ztech.lodgeme.R;
import com.ztech.propertylister.utils.ViewAnimation;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private TextView forgotPassword;
    private View parent_view;
    private FirebaseAuth mAuth;
    private CoordinatorLayout parentView;
    Context mcontext;
    FirebaseFirestore firebaseFirestore;
    private String userRole;
    private DocumentReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email_login);
        password = (EditText) findViewById(R.id.password_login);
        forgotPassword = findViewById(R.id.forgot_password);

        parentView = findViewById(R.id.login_parent_view);
        mAuth = FirebaseAuth.getInstance();
        mcontext = this.getApplicationContext();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
        final LinearLayout login_progress = (LinearLayout) findViewById(R.id.login_progress);
        login_progress.setVisibility(View.GONE);

        ((View) findViewById(R.id.register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, ProfileSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ((View) findViewById(R.id.forgot_password)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, PasswordResetActivity.class);
                startActivity(intent);
            }
        });

        ((View) findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    signIn(email.getText().toString(), password.getText().toString());
                } catch(IllegalArgumentException e){
                    Tools.snackBarIconInfo(mcontext, parentView,
                            getLayoutInflater(), "You need to provide your Email and Password",
                            R.color.blue_grey_700, R.drawable.ic_error_outline);
                    e.getMessage();
                }
            }
        });

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();

                            Log.w("MSG", deepLink.toString());
                            Tools.snackBarIconInfo(mcontext, parentView,
                                    getLayoutInflater(), "Your password reset for: "+
                                            deepLink.getQueryParameter("email") + " was successful",
                                    R.color.blue_grey_700, R.drawable.ic_done);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MSG", "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void loadingAndDisplayContent() {
        final LinearLayout login_progress = (LinearLayout) findViewById(R.id.login_progress);
        login_progress.setVisibility(View.VISIBLE);
        login_progress.setAlpha(1.0f);
        ViewAnimation.fadeOut(login_progress);
    }

    private void stopLoadingAnimation(){
        final LinearLayout login_progress = (LinearLayout) findViewById(R.id.login_progress);
        login_progress.setVisibility(View.GONE);
    }

    private void signIn(String email, String password) {
        loadingAndDisplayContent();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signIn Msg", "signInWithEmail:success");
                            getUserRole();
                        } else {
                            // If sign in fails, display a message to the user.
                            stopLoadingAnimation();
                            Log.w("signIn Msg", "signInWithEmail:failure", task.getException());
                            Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(),
                                    "Login Failed, " + task.getException().getLocalizedMessage(),
                                    R.color.blue_grey_700, R.drawable.ic_error_outline);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private String getUserRole(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            String currentUserEmail = mAuth.getCurrentUser().getEmail();
            ref = firebaseFirestore.collection("users").document(currentUserEmail);
            ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    firebaseFirestore.collection("users")
                            .whereEqualTo("Email", currentUserEmail)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            updateUI(document.getString("Role"));
                                            userRole = document.getString("Role");
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

    private void updateUI(String role) {
        if(role.contains("guest")){
            Intent intent = new Intent(mcontext, GuestMapNavigation.class);
            startActivity(intent);
            finish();
        } else if(role.contains("agent")){
            Intent intent = new Intent(mcontext, AgentDashboard.class);
            startActivity(intent);
            finish();
        }
    }
}
