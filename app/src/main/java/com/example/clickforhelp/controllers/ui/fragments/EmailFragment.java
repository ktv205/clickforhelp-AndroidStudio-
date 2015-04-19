package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EmailFragment extends Fragment {
    private final static String TAG = EmailFragment.class.getSimpleName();
    private final static int EMAIL_EMPTY = 0;
    private final static int RESULT_OK = 1;
    private final static int INVALID_EMAIL = 6;
    private final static int NEW_USER = 0;
    private final static int EXISTING_USER = 1;
    private final static int DORMANT_USER = -1;
    private Button mEmailButton;
    private static final String MESSAGE = "Please wait while we verify your email";
    private static final String FORGOT_PASSWORD = "forgotpassword";
    private LinearLayout mNoNetworkLinearLayout;
    private static final int NO_CONNECTION=999;

    private String mEmail;
    View mView;
    private EmailFragmentInterface mEmailFragmentInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mEmailFragmentInterface = (EmailFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public interface EmailFragmentInterface {
        public void replaceWithVerificationCodeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_email, container, false);
        mEmailButton = (Button) mView.findViewById(R.id.email_button_submit);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.string_text_email);
        mNoNetworkLinearLayout=(LinearLayout)mView.findViewById(R.id.email_linear_no_network);
        mNoNetworkLinearLayout.setVisibility(View.INVISIBLE);
        mEmailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int flag = getTextFromFields();
                String message = null;
                if (flag == EMAIL_EMPTY) {
                    message = "please enter the nyu email";
                } else if (flag == INVALID_EMAIL) {
                    message = "enter valid nyu email id";
                } else {
                    String[] paths = {FORGOT_PASSWORD, mEmail};
                    RequestParams params = CommonFunctions.setParams(paths,getActivity());
                    new CommonResultAsyncTask(getActivity(), MESSAGE, 0).execute(params);
                    mEmailButton.setEnabled(false);

                }
                if (message != null) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });
        return mView;
    }

    public int getTextFromFields() {
        EditText emailEdittext = (EditText) mView
                .findViewById(R.id.email_edit_email);
        mEmail = emailEdittext.getText().toString();
        int flag;
        if (mEmail.isEmpty()) {
            flag = EMAIL_EMPTY;
        } else if (!CommonFunctions.validNyuEmail(mEmail)) {
            flag = INVALID_EMAIL;
        } else {
            flag = RESULT_OK;
        }
        return flag;

    }

    public void responseFromServer(int code) {
        Log.d(TAG, "code->" + code);
        if (code == NEW_USER) {
            Toast.makeText(
                    getActivity(),
                    "we couldnt find your email,please sign up if you are a new user",
                    Toast.LENGTH_SHORT).show();
            mEmailButton.setEnabled(true);
        } else if (code == EXISTING_USER) {
            HashMap<String, String> values = new HashMap<String, String>();
            values.put(AppPreferences.SharedPrefAuthentication.user_email,
                    mEmail);
            values.put(AppPreferences.SharedPrefAuthentication.flag,
                    AppPreferences.SharedPrefAuthentication.FLAG_INACTIVE);
            CommonFunctions.saveInPreferences(getActivity(),
                    AppPreferences.SharedPrefAuthentication.name, values);
            mEmailFragmentInterface.replaceWithVerificationCodeFragment();

        } else if (code == DORMANT_USER) {
            Toast.makeText(getActivity(),
                    "unverified account please check your mail for the code",
                    Toast.LENGTH_SHORT).show();
            mEmailButton.setEnabled(true);
        }else if(code==NO_CONNECTION){
            mNoNetworkLinearLayout.setVisibility(View.VISIBLE);
            Button button=(Button)mNoNetworkLinearLayout.findViewById(R.id.email_button_retry);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(CommonFunctions.isConnected(getActivity())){
                        mNoNetworkLinearLayout.setVisibility(View.INVISIBLE);
                        mEmailButton.setEnabled(true);
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(),
                    "something went wrong please try again", Toast.LENGTH_SHORT)
                    .show();
            mEmailButton.setEnabled(true);
        }
    }
}
