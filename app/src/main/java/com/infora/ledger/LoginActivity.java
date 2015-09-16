package com.infora.ledger;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.infora.ledger.application.commands.CreateSystemAccountCommand;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.support.GooglePlayServicesUtilWrapper;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getName();

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_UPDATE_PLAY_SERVICES = 1001;

    @Inject GooglePlayServicesUtilWrapper googlePlayServicesUtil;
    @Inject EventBus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DiUtils.injector(this).inject(this);
    }

    public void signIn(View view) {
        int playServicesCheckResult = googlePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (playServicesCheckResult) {
            case 0:
                String[] accountTypes = new String[]{"com.google"};
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        accountTypes, false, null, null, null, null);
                startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
                break;
            default:
                Log.e(TAG, "Play services are not available. Code: " + playServicesCheckResult);
                Dialog errorDialog = googlePlayServicesUtil.getErrorDialog(playServicesCheckResult, this, REQUEST_CODE_UPDATE_PLAY_SERVICES);
                errorDialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                bus.post(new CreateSystemAccountCommand(email));
                startActivity(Intent.makeMainActivity(new ComponentName(this, ReportActivity.class)));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "The account wasn't picked.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_PLAY_SERVICES) {
            Log.d(TAG, "Services update result");
        }
    }
}
