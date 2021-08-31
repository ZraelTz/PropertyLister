package com.ztech.propertylister;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ztech.lodgeme.R;
import com.ztech.propertylister.utils.ViewAnimation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AgentSignup extends AppCompatActivity {

    //class instance variables
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private EditText fullNameField;
    private EditText phoneField;
    private EditText gender;
    private Context mcontext;
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^" +
            "(?=.*[@#$%^&+=])" +     // at least 1 special character
            "(?=\\S+$)" +            // no white spaces
            ".{8,}" +                // at least 8 characters
            "$");
    private CoordinatorLayout parentView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference ref;


    //activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_signup);
        parentView = findViewById(R.id.parent_view);
        emailField = (EditText) findViewById(R.id.agent_email);
        passwordField = (EditText) findViewById(R.id.agent_password);
        confirmPasswordField = (EditText) findViewById(R.id.agent_confirmPassword);
        fullNameField = (EditText) findViewById(R.id.agent_name);
        phoneField = (EditText) findViewById(R.id.agent_phone);
        gender = (EditText) findViewById(R.id.agent_gender);
        mcontext = this.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        initToolbar();
        final LinearLayout agent_signup_progress = (LinearLayout) findViewById(R.id.agent_signup_progress);
        agent_signup_progress.setVisibility(View.GONE);
        initComponent();

    }
    //toolbar initialization method
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Signup as Agent");
        Tools.setSystemBarLight(this);
        Tools.setSystemBarColor(this, R.color.overlay_light_90);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.overlay_light_90));
    }
    //activity initialization methods
    private void initComponent() {

        emailField.addTextChangedListener(new TextWatcher() {
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

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validatePassword();
            }
        });

        confirmPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                confirmPasswordMatch();
            }
        });

        (findViewById(R.id.agent_gender)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderDialog(v);
            }
        });

        findViewById(R.id.signup_agent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingAndDisplayContent();
                if(!getSignUpEligibility()){
                    snackBarIconInfo("You need to correctly fill out all the fields",
                            R.color.blue_grey_700, R.drawable.ic_error_outline);
                } else {
                    try {
                        createAccount(emailField.getText().toString(),
                                passwordField.getText().toString(),
                                fullNameField.getText().toString(),
                                gender.getText().toString(),
                                phoneField.getText().toString());
                    } catch (Exception e) {
                        //handled
                    }
                }
            }

        });

        findViewById(R.id.agent_sign_in).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext, LoginActivity.class);
                startActivity(intent);
                finish();
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

    //form validation methods
    private boolean validateEmail(){
        String emailInput = emailField.getText().toString().trim();
        if(emailInput.isEmpty()){
            emailField.setError("Email is required", getResources().getDrawable(R.drawable.ic_error_outline));
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            emailField.setError("Please provide a valid Email Address", getResources().getDrawable(R.drawable.ic_error_outline));
            return false;
        } else {
            emailField.setBackgroundColor(getResources().getColor(R.color.mdtp_white));
            emailField.setHintTextColor(getResources().getColor(R.color.blue_grey_700));
            return true;
        }
    }

    private boolean validatePassword(){
        String passwordInput = passwordField.getText().toString().trim();
        if(passwordInput.isEmpty()){
            passwordField.setError("Password is required", getResources().getDrawable(R.drawable.ic_error_outline));
            return false;
        } else if(!PASSWORD_REGEX.matcher(passwordInput).matches()){
            passwordField.setError("Password must consist at least 8 characters including a symbol", getResources().getDrawable(R.drawable.ic_error_outline));
            return false;
        } else {
            passwordField.setBackgroundColor(getResources().getColor(R.color.mdtp_white));
            passwordField.setHintTextColor(getResources().getColor(R.color.blue_grey_700));
            return true;
        }
    }

    private boolean confirmPasswordMatch(){
        String confirmPasswordText = confirmPasswordField.getText().toString().trim();
        if(!confirmPasswordText.equals(passwordField.getText().toString())||confirmPasswordText.isEmpty()){ ;
            confirmPasswordField.setError("Passwords don't match", getResources().getDrawable(R.drawable.ic_error_outline));
            return false;
        } else {
            confirmPasswordField.setBackgroundColor(getResources().getColor(R.color.mdtp_white));
            confirmPasswordField.setHintTextColor(getResources().getColor(R.color.blue_grey_700));
            confirmPasswordField.setError("Passwords Matched!", getResources().getDrawable(R.drawable.ic_done));
            return true;
        }
    }

    private boolean getSignUpEligibility(){
        if(!validateEmail()||!validatePassword()||!confirmPasswordMatch()
                ||phoneField.getText().toString().isEmpty()
                ||fullNameField.getText().toString().isEmpty()
                ||gender.getText().toString().isEmpty()){

            return false;
        } else {
            return true;
        }
    }

    //UI and Animation methods
    private void loadingAndDisplayContent() {
        final LinearLayout agent_signup_progress = (LinearLayout) findViewById(R.id.agent_signup_progress);
        agent_signup_progress.setVisibility(View.VISIBLE);
        agent_signup_progress.setAlpha(1.0f);
        ViewAnimation.fadeOut(agent_signup_progress);
    }

    private void snackBarIconAction(String infoText, int colorId, int drawableIcon) {
         Snackbar snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_INDEFINITE)
                .setAction("RESEND LINK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar sb = Snackbar.make(parentView, "Verification link has been resent", Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(getResources().getColor(colorId));
//                sb.setActionTextColor(Color.WHITE);
                sb.show();
            }
        });;
        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);

        ((TextView) custom_view.findViewById(R.id.message)).setText(infoText);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(drawableIcon);
        (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(getResources().getColor(colorId));
        snackBarView.addView(custom_view, 0);
        snackbar.show();
    }

    private void snackBarIconInfo(String infoText, int colorId, int drawableIcon) {
        final Snackbar snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_LONG);
        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);

        ((TextView) custom_view.findViewById(R.id.message)).setText(infoText);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(drawableIcon);
        (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(getResources().getColor(colorId));
        snackBarView.addView(custom_view, 0);
        snackbar.show();

    }

    private void showGenderDialog(final View v) {
        final String[] array = new String[]{
                "Male", "Female"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gender");
        builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    //firebase and firestore methods
    private void createAccount(String email, String password, String name, String gender, String phone) throws FirebaseAuthUserCollisionException {
        loadingAndDisplayContent();
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingAndDisplayContent();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Msg", "createUserWithEmail:success");
                            storeUserDetails(email, password, name, gender, phone);

                        } else {
                            // If sign in fails, display a message to the user.
                            snackBarIconInfo("Registration Failed, " + task.getException().getLocalizedMessage(),
                                    R.color.blue_grey_700, R.drawable.ic_error_outline);
                            Log.w("Msg", "createUserWithEmail:failure", task.getException());
                        }
                    }


                });
        // [END create_user_with_email]
    }

    private void storeUserDetails(String email, String password, String name, String gender, String phone){
        loadingAndDisplayContent();
        ref = firebaseFirestore.collection("users").document(email);
        //and further firestore operations
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                loadingAndDisplayContent();
                Map<String, Object> signupEntry = new HashMap<>();
                signupEntry.put("Name", name);
                signupEntry.put("Email", email);
                signupEntry.put("Password", password);
                signupEntry.put("Gender", gender);
                signupEntry.put("Phone", phone);
                signupEntry.put("Role", "agent");

                //   String myId = ref.getId();
                firebaseFirestore.collection("users")
                        .add(signupEntry)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                loadingAndDisplayContent();
                                sendEmailVerificationLink();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingAndDisplayContent();
                                mAuth.getCurrentUser().delete();
                                snackBarIconInfo("Account not Created: " + e.getLocalizedMessage(),
                                        R.color.blue_grey_700, R.drawable.ic_error_outline);
                                Log.d("Error", e.getMessage());
                            }
                        });
            }
        });
    }

    private void sendEmailVerificationLink(){
        loadingAndDisplayContent();
        FirebaseUser user = mAuth.getCurrentUser();

        String url = "http://lodgeme.page.link/verify?uid=" + user.getUid();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                // The default for this is populated with the current android package name.
                .setAndroidPackageName("com.ztech.lodgeme", false, null)
                .build();

        user.sendEmailVerification(actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingAndDisplayContent();
                        if (task.isSuccessful()) {
                            loadingAndDisplayContent();
                            Log.d("Msg", "Email sent.");
                            snackBarIconInfo("Your Agent Account has been Created Successfully, " +
                                            "check your email for verification link",
                                    R.color.blue_grey_700, R.drawable.ic_done);

                        } else {
                            loadingAndDisplayContent();
                            snackBarIconAction(task.getException().getLocalizedMessage(),
                                    R.color.blue_grey_700, R.drawable.ic_error_outline);
                        }
                    }
                });
    }

    //optional UI method
    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(mcontext, LoginActivity.class);
        startActivity(intent);
    }
}
