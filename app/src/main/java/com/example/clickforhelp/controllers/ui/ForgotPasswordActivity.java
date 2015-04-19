package com.example.clickforhelp.controllers.ui;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.fragments.EmailFragment;
import com.example.clickforhelp.controllers.ui.fragments.EmailVerificationFragment;
import com.example.clickforhelp.controllers.ui.fragments.NewPasswordFragment;
import com.example.clickforhelp.controllers.ui.fragments.EmailFragment.EmailFragmentInterface;
import com.example.clickforhelp.controllers.ui.fragments.EmailVerificationFragment.EmailVerificationFragmentInterface;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ForgotPasswordActivity extends ActionBarActivity implements
        EmailFragmentInterface, EmailVerificationFragmentInterface,
        ServerResponse {
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private final static String EMAILTAG = "EmailFragment";
    private final static String VERFICATIONTAG = "EmailVerficationTAG";
    private final static String NEWPASSWORDTAG = "NEWPASSWORDTAG";

    // private final static String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        mFragmentManager = getFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = getFragmentManager()
                .findFragmentByTag(EMAILTAG);
        Fragment newPasswordFragment = getFragmentManager()
                .findFragmentByTag(NEWPASSWORDTAG);
        if (getIntent() != null) {
            if (getIntent().hasExtra(AppPreferences.IntentExtras.CHANGE)) {
                if (newPasswordFragment == null) {
                    mFragmentTransaction.replace(
                            R.id.forgotpassword_linear,
                            new NewPasswordFragment(), NEWPASSWORDTAG)
                            .commit();
                }
            } else {
                if (fragment == null) {
                    mFragmentTransaction.replace(
                            R.id.forgotpassword_linear,
                            new EmailFragment(), EMAILTAG).commit();
                }
            }
        } else {
            // Log.d(TAG, "email fragment is null in onCreate");
            mFragmentTransaction.replace(R.id.forgotpassword_linear,
                    new EmailFragment(), EMAILTAG).commit();
        }


    }



    @Override
    public void replaceWithVerificationCodeFragment() {
        mFragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(VERFICATIONTAG);
        if (fragment != null) {
            mFragmentTransaction.replace(R.id.forgotpassword_linear, fragment,
                    VERFICATIONTAG).commit();
        } else {
            fragment = new EmailVerificationFragment();
            mFragmentTransaction.replace(R.id.forgotpassword_linear, fragment,
                    VERFICATIONTAG).commit();
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(AppPreferences.IntentExtras.NEW_PASSWORD, true);
        fragment.setArguments(bundle);

    }

    @Override
    public void replaceWithNewPasswordFragment() {
        mFragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(NEWPASSWORDTAG);
        if (fragment != null) {
            mFragmentTransaction.replace(R.id.forgotpassword_linear, fragment,
                    NEWPASSWORDTAG).commit();
        } else {
            fragment = new NewPasswordFragment();
            mFragmentTransaction.replace(R.id.forgotpassword_linear, fragment,
                    NEWPASSWORDTAG).commit();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mFragmentManager.findFragmentByTag(VERFICATIONTAG);
        if (fragment != null && fragment.isVisible()) {
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.forgotpassword_linear,
                    new EmailFragment(), EMAILTAG).commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void IntegerResponse(int response, int flag) {
        Fragment emailFragment = mFragmentManager.findFragmentByTag(EMAILTAG);
        Fragment verificationFragment = mFragmentManager
                .findFragmentByTag(VERFICATIONTAG);
        Fragment newPasswordFragment = mFragmentManager
                .findFragmentByTag(NEWPASSWORDTAG);
        if (emailFragment != null && emailFragment.isVisible()) {
            ((EmailFragment) emailFragment).responseFromServer(response);
        } else if (newPasswordFragment != null
                && newPasswordFragment.isVisible()) {
            ((NewPasswordFragment) newPasswordFragment)
                    .responseFromServer(response);
        } else if (verificationFragment != null
                && verificationFragment.isVisible()) {
            ((EmailVerificationFragment) verificationFragment)
                    .responseFromServer(response, flag);

        }

    }
}
