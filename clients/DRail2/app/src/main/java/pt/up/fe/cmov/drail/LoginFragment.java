package pt.up.fe.cmov.drail;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private OnFragmentInteractionListener mListener;
    private TextView mEmailLoginTextView;
    private TextView mPasswordLoginTextView;
    private Button mLoginButton;
    private TextView mNameRegisterTextView;
    private TextView mUsernameRegisterTextView;
    private TextView mPasswordRegisterTextView;
    private TextView mCcRegisterTextView;
    private Button mRegisterButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(int sectionNumber) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the tlayout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailLoginTextView = (TextView) v.findViewById(R.id.email);
        mPasswordLoginTextView = (TextView) v.findViewById(R.id.password);
        mLoginButton = (Button) v.findViewById(R.id.email_sign_in_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence username = mEmailLoginTextView.getText();
                CharSequence password = mPasswordLoginTextView.getText();

                if (username == null || username.length() == 0 || password == null || password.length() == 0) {
                    return;
                }

                Call<ApiService.LoginUserResponse> loginRequest = ApiService.service.login(
                        new ApiService.LoginUserRequest(username.toString(), password.toString())
                );
                loginRequest.enqueue(new Callback<ApiService.LoginUserResponse>() {
                    @Override
                    public void onResponse(Response<ApiService.LoginUserResponse> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            MainActivity.mLoginUser = response.body();
                            Toast.makeText(getContext(), "Logged in with user #" + MainActivity.mLoginUser.id, Toast.LENGTH_SHORT).show();
                            MainActivity.mViewPager.setCurrentItem(0);
                            //((TicketListFragment)MainActivity.mSectionsPagerAdapter.getItem(1)).loadTickets();
                        } else {
                            try {
                                Toast.makeText(getContext(), "Login error: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(getContext(), "Login error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mNameRegisterTextView = (TextView) v.findViewById(R.id.register_name);
        mUsernameRegisterTextView = (TextView) v.findViewById(R.id.register_username);
        mPasswordRegisterTextView = (TextView) v.findViewById(R.id.register_password);
        mCcRegisterTextView = (TextView) v.findViewById(R.id.register_cc);
        mRegisterButton = (Button) v.findViewById(R.id.register_button);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence name = mNameRegisterTextView.getText();
                CharSequence username = mUsernameRegisterTextView.getText();
                CharSequence password = mPasswordRegisterTextView.getText();
                CharSequence cc = mCcRegisterTextView.getText();

                if (name == null || username == null || password == null || cc == null)
                    return;

                if (name.length() == 0 || username.length() == 0 || password.length() == 0 || cc.length() == 0)
                    return;

                Call<ApiService.LoginUserResponse> registerRequest = ApiService.service.register(
                        new ApiService.RegisterUserRequest(
                                name.toString(),
                                username.toString(),
                                password.toString(),
                                cc.toString())
                );

                registerRequest.enqueue(new Callback<ApiService.LoginUserResponse>() {
                    @Override
                    public void onResponse(Response<ApiService.LoginUserResponse> response, Retrofit retrofit) {
                        if (response.isSuccess()) {
                            MainActivity.mLoginUser = response.body();
                            Toast.makeText(getContext(), "Registered user #" + MainActivity.mLoginUser.id, Toast.LENGTH_SHORT).show();
                            MainActivity.mViewPager.setCurrentItem(0);
                        } else {
                            try {
                                Toast.makeText(getContext(), "Register error: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(getContext(), "Register error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
