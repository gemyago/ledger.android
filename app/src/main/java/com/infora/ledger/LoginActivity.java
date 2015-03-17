package com.infora.ledger;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;


public class LoginActivity extends ActionBarActivity {
    private static final String TAG = LoginActivity.class.getName();

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_UPDATE_PLAY_SERVICES = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void signIn(View view) {
        int playServicesCheckResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (playServicesCheckResult) {
            case 0:
                String[] accountTypes = new String[]{"com.google"};
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        accountTypes, false, null, null, null, null);
                startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
                break;
            default:
                Log.e(TAG, "Play services are not available. Code: " + playServicesCheckResult);
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playServicesCheckResult, this, REQUEST_CODE_UPDATE_PLAY_SERVICES);
                errorDialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "The account wasn't picked.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_PLAY_SERVICES) {
            Log.d(TAG, "Services update result");
        }
    }
}
