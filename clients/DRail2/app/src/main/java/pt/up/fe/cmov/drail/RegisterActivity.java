package pt.up.fe.cmov.drail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextView mNameRegisterTextView;
    private TextView mUsernameRegisterTextView;
    private TextView mPasswordRegisterTextView;
    private TextView mCcRegisterTextView;
    private Button mRegisterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        mNameRegisterTextView = (TextView) findViewById(R.id.register_name);
        mUsernameRegisterTextView = (TextView) findViewById(R.id.register_username);
        mPasswordRegisterTextView = (TextView) findViewById(R.id.register_password);
        mCcRegisterTextView = (TextView) findViewById(R.id.register_cc);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        Button signInButton = (Button) findViewById(R.id.go_to_sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {


        mNameRegisterTextView.setError(null);
        mUsernameRegisterTextView.setError(null);
        mPasswordRegisterTextView.setError(null);
        mCcRegisterTextView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameRegisterTextView.getText().toString();
        String username = mUsernameRegisterTextView.getText().toString();
        String password = mPasswordRegisterTextView.getText().toString();
        String cc = mCcRegisterTextView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordRegisterTextView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordRegisterTextView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameRegisterTextView.setError(getString(R.string.error_field_required));
            focusView = mUsernameRegisterTextView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            mNameRegisterTextView.setError(getString(R.string.error_field_required));
            focusView = mNameRegisterTextView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Call<ApiService.LoginUserResponse> registerRequest = ApiService.service.register(new ApiService.RegisterUserRequest(name, username, password, cc));
            registerRequest.enqueue(new Callback<ApiService.LoginUserResponse>() {
                @Override
                public void onResponse(Response<ApiService.LoginUserResponse> response, Retrofit retrofit) {
                    if (response.isSuccess()) { // successful request, build graph
                        MainActivity.mLoginUser = response.body();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        setIntent(intent);
                    } else {
                        Log.d("Error", response.raw().request().urlString());
                    }
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}