package com.ztech.propertylister;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.ztech.lodgeme.R;
import com.ztech.propertylister.utils.ViewAnimation;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
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
    private EditText dob;
    private EditText address;
    private EditText governmentId;
    private EditText gender;
    private TextView resendEmail;
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
        dob = (EditText) findViewById(R.id.dob);
        address = (EditText) findViewById(R.id.agent_address);
        governmentId = (EditText) findViewById(R.id.agent_id);
        gender = (EditText) findViewById(R.id.agent_gender);
        resendEmail = findViewById(R.id.resend_email_agent);
        mcontext = this.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        final LinearLayout agent_signup_progress = (LinearLayout) findViewById(R.id.agent_signup_progress);
        agent_signup_progress.setVisibility(View.GONE);

        initToolbar();
        initComponent();

    }
    //toolbar initialization method
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Signup as Agent");
        resendEmail.setVisibility(View.GONE);
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
                if(!getSignUpEligibility()){
                    Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(), "You need to correctly fill out all the fields",
                            R.color.blue_grey_700, R.drawable.ic_error_outline);
                } else {
                    try {
                        createAccount(emailField.getText().toString(),
                                passwordField.getText().toString(),
                                fullNameField.getText().toString(),
                                gender.getText().toString(),
                                phoneField.getText().toString(),
                                dob.getText().toString(),
                                address.getText().toString(),
                                governmentId.getText().toString());

                    } catch (Exception e) {
                        //handled
                    }
                }
            }

        });

        resendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerificationLink();
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
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight((EditText) v);
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
        return Tools.validateEmail(emailField, mcontext);
    }

    private boolean validatePassword(){
        return Tools.validatePassword(passwordField, mcontext);
    }

    private boolean confirmPasswordMatch(){
        return Tools.confirmPasswordMatch(passwordField, confirmPasswordField, mcontext);
    }

    private boolean getSignUpEligibility(){
        if(!validateEmail()||!validatePassword()||!confirmPasswordMatch()
                ||phoneField.getText().toString().isEmpty()
                ||fullNameField.getText().toString().isEmpty()
                ||gender.getText().toString().isEmpty()
                ||dob.getText().toString().isEmpty()
                ||address.getText().toString().isEmpty()
                ||governmentId.getText().toString().isEmpty()){

            return false;
        } else {
            return true;
        }
    }

    //UI and Animation methods
    private void dialogDatePickerLight(final EditText bt) {
        Calendar cur_calender = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long date_ship_millis = calendar.getTimeInMillis();
                        ((EditText) findViewById(R.id.dob)).setText(Tools.getFormattedDateSimple(date_ship_millis));
                    }
                },
                cur_calender.get(Calendar.YEAR),
                cur_calender.get(Calendar.MONTH),
                cur_calender.get(Calendar.DAY_OF_MONTH)
        );
        //set dark light
        datePicker.setThemeDark(false);
        datePicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
        //datePicker.setMinDate(cur_calender);
        datePicker.show(getFragmentManager(), "Datepickerdialog");
    }

    private void loadingAndDisplayContent() {
        final LinearLayout agent_signup_progress = (LinearLayout) findViewById(R.id.agent_signup_progress);
        agent_signup_progress.setVisibility(View.VISIBLE);
        agent_signup_progress.setAlpha(1.0f);
        ViewAnimation.fadeOut(agent_signup_progress);
    }

    private void stopLoadingAnimation(){
        final LinearLayout agent_signup_progress = (LinearLayout) findViewById(R.id.agent_signup_progress);
        agent_signup_progress.setVisibility(View.GONE);
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

    private void snackBarActionDark(String infoText) {
        final Snackbar snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_INDEFINITE);
        //inflate view
        View custom_view = getLayoutInflater().inflate(R.layout.snackbar_toast_floating_dark, null);

        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);
        (custom_view.findViewById(R.id.sb_dark_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerificationLink();
                snackbar.dismiss();
                Snackbar sb = Snackbar.make(parentView, "The email has been resent", Snackbar.LENGTH_LONG);
                sb.show();
            }
        });
        ((TextView) custom_view.findViewById(R.id.sb_dark_message)).setText(infoText);
        snackBarView.addView(custom_view, 0);
        snackbar.show();
    }

    //firebase and firestore methods
    private void createAccount(String email, String password, String name, String gender, String phone, String dob, String address, String governmentId) throws FirebaseAuthUserCollisionException {
        // [START create_user_with_email]
        loadingAndDisplayContent();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Msg", "createUserWithEmail:success");
                            storeUserDetails(email, name, gender, phone, dob, address, governmentId);

                        } else {
                            // If sign in fails, display a message to the user.
                            stopLoadingAnimation();
                            Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(), "Registration Failed, " + task.getException().getLocalizedMessage(),
                                    R.color.blue_grey_700, R.drawable.ic_error_outline);
                            Log.w("Msg", "createUserWithEmail:failure", task.getException());
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                stopLoadingAnimation();
                Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(), "Registration Failed, " + e.getLocalizedMessage(),
                        R.color.blue_grey_700, R.drawable.ic_error_outline);
                Log.w("Msg", "createUserWithEmail:failure", e);
            }
        });
        // [END create_user_with_email]
    }

    private void storeUserDetails(String email, String name, String gender, String phone, String dob, String address, String governmentId){
        ref = firebaseFirestore.collection("users").document(email);
        //and further firestore operations
        ref.get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> signupEntry = new HashMap<>();
                signupEntry.put("Name", name);
                signupEntry.put("Email", email);
                signupEntry.put("Gender", gender);
                signupEntry.put("Phone", phone);
                signupEntry.put("Dob", dob);
                signupEntry.put("Address", address);
                signupEntry.put("GovernmentId", governmentId);
                signupEntry.put("Role", "agent");

                //String myId = ref.getId();
                firebaseFirestore.collection("users")
                        .add(signupEntry)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                sendEmailVerificationLink();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Error", e.getMessage());
                                mAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        stopLoadingAnimation();
                                        Log.d("Msg", "User Deleted");
                                        Tools.snackBarIconInfo(mcontext, parentView,
                                                getLayoutInflater(), "Account not Created: " + e.getLocalizedMessage(),
                                                R.color.blue_grey_700, R.drawable.ic_error_outline);
                                    }
                                });
                            }
                        });
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                stopLoadingAnimation();
                Log.d("Error", e.getLocalizedMessage());
                mAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Msg", "User Deleted");
                        Tools.snackBarIconInfo(mcontext, parentView,
                                getLayoutInflater(), "Account not Created: " + e.getLocalizedMessage(),
                                R.color.blue_grey_700, R.drawable.ic_error_outline);
                    }
                });
            }
        });
    }

    private void sendEmailVerificationLink(){
        FirebaseUser user = mAuth.getCurrentUser();

        String url = "http://lodgeme.page.link/verify?uid=" + user.getUid();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                // The default for this is populated with the current android package name.
                .setAndroidPackageName("com.ztech.lodgeme", false, null)
                .build();

        user.sendEmailVerification(actionCodeSettings)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            stopLoadingAnimation();
                            Log.d("Msg", "Email sent.");
                            Tools.snackBarIconInfo(mcontext, parentView, getLayoutInflater(), "Your Agent Account has been Created Successfully, " +
                                            "check your email for verification link",
                                    R.color.blue_grey_700, R.drawable.ic_done);
                            resendEmail.setVisibility(View.VISIBLE);

                        } else {
                            stopLoadingAnimation();
                           snackBarActionDark(task.getException().getLocalizedMessage());
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                stopLoadingAnimation();
                snackBarActionDark(e.getLocalizedMessage());
            }
        });
    }

    //optional UI method
    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(mcontext, LoginActivity.class);
        startActivity(intent);
    }
}
