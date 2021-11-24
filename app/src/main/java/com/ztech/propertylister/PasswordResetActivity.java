package com.ztech.propertylister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.ztech.lodgeme.R;
import com.ztech.propertylister.Tools;
import com.ztech.propertylister.utils.ViewAnimation;

public class PasswordResetActivity extends AppCompatActivity {
    private EditText emailAddress;
    private TextView sendEmail;
    private TextView login;
    private View parentView;
    private Context mcontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_blue);

        emailAddress = findViewById(R.id.verification_email);
        sendEmail = findViewById(R.id.send_link);
        login = findViewById(R.id.reset_login);
        parentView = findViewById(R.id.verification_parent_view);
        mcontext = this.getApplicationContext();

        final LinearLayout resetProgress = (LinearLayout) findViewById(R.id.reset_progress);
        resetProgress.setVisibility(View.GONE);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.grey_90);


    }

    private void initComponent() {
        emailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateEmail();
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!validateEmail()){
                    Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(),
                            "You need to provide a valid email address",
                            R.color.blue_grey_700, R.drawable.ic_error_outline);
                } else {
                    sendPasswordResetEmail();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent
            }
        });
    }

    private boolean validateEmail() {
        return Tools.validateEmail(emailAddress, mcontext);
    }

    private void sendPasswordResetEmail(){
        loadingAndDisplayContent();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String url = "http://lodgeme.page.link/?email=" + emailAddress.getText().toString();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                // The default for this is populated with the current android package name.
                .setAndroidPackageName("com.ztech.lodgeme", false, null)
                .build();

        auth.sendPasswordResetEmail(emailAddress.getText().toString(), actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MSG", "Email sent.");
                            Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(),
                                    "Sent! Please check your email to reset your password",
                                    R.color.blue_grey_700, R.drawable.ic_done);
                            stopLoadingAnimation();
                        } else {
                            snackBarActionLight(task.getException().getLocalizedMessage());
                            stopLoadingAnimation();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadingAndDisplayContent() {
        final LinearLayout resetProgress = (LinearLayout) findViewById(R.id.reset_progress);
        resetProgress.setVisibility(View.VISIBLE);
        resetProgress.setAlpha(1.0f);
        ViewAnimation.fadeOut(resetProgress);
    }

    private void stopLoadingAnimation(){
        final LinearLayout resetProgress = (LinearLayout) findViewById(R.id.reset_progress);
        resetProgress.setVisibility(View.GONE);
    }

    private void snackBarActionLight(String infoText) {
        final Snackbar snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_INDEFINITE);
        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.snackbar_toast_floating, null);

        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);
        (custom_view.findViewById(R.id.sb_light_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
                snackbar.dismiss();
                Snackbar sb = Snackbar.make(parentView, "The email has been resent", Snackbar.LENGTH_LONG);
                sb.show();
            }
        });
        ((TextView) custom_view.findViewById(R.id.sb_light_message)).setText(infoText);
        snackBarView.addView(custom_view, 0);
        snackbar.show();
    }
}
