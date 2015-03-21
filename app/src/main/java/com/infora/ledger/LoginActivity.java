package com.infora.ledger;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class LoginActivity extends ActionBarActivity {
    private static final String TAG = LoginActivity.class.getName();

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_UPDATE_PLAY_SERVICES = 1001;

    private GooglePlayServicesUtilWrapper googlePlayServicesUtilWrapper;

    public GooglePlayServicesUtilWrapper getGooglePlayServicesUtil() {
        return googlePlayServicesUtilWrapper == null ?
                (googlePlayServicesUtilWrapper = new GooglePlayServicesUtilWrapper()) :
                googlePlayServicesUtilWrapper;
    }

    public void setGooglePlayServicesUtilWrapper(GooglePlayServicesUtilWrapper googlePlayServicesUtilWrapper) {
        this.googlePlayServicesUtilWrapper = googlePlayServicesUtilWrapper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void signIn(View view) {
        int playServicesCheckResult = getGooglePlayServicesUtil().isGooglePlayServicesAvailable(this);
        switch (playServicesCheckResult) {
            case 0:
                String[] accountTypes = new String[]{"com.google"};
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        accountTypes, false, null, null, null, null);
                startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
                break;
            default:
                Log.e(TAG, "Play services are not available. Code: " + playServicesCheckResult);
                Dialog errorDialog = getGooglePlayServicesUtil().getErrorDialog(playServicesCheckResult, this, REQUEST_CODE_UPDATE_PLAY_SERVICES);
                errorDialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new RuntimeException("Not implemented");
//        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
//            if (resultCode == RESULT_OK) {
//                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//                LedgerApplication app = (LedgerApplication) getApplicationContext();
//                app.rememberUserEamil(email);
//                startActivity(Intent.makeMainActivity(new ComponentName(this, ReportActivity.class)));
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "The account wasn't picked.", Toast.LENGTH_SHORT).show();
//            }
//        } else if (requestCode == REQUEST_CODE_UPDATE_PLAY_SERVICES) {
//            Log.d(TAG, "Services update result");
//        }
    }

    public static class GooglePlayServicesUtilWrapper {
        public int isGooglePlayServicesAvailable(Context context) {
            return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        }

        public Dialog getErrorDialog(int errorCode, Activity activity, int requestCode) {
            return GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode);
        }
    }
}
