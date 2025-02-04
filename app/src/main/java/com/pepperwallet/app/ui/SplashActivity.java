package com.pepperwallet.app.ui;

import static com.pepperwallet.app.C.IMPORT_REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.hanks.passcodeview.PasscodeView;
import com.pepperwallet.app.C;
import com.pepperwallet.app.R;
import com.pepperwallet.app.entity.CreateWalletCallbackInterface;
import com.pepperwallet.app.entity.CustomViewSettings;
import com.pepperwallet.app.entity.Operation;
import com.pepperwallet.app.entity.Wallet;
import com.pepperwallet.app.router.HomeRouter;
import com.pepperwallet.app.router.ImportWalletRouter;
import com.pepperwallet.app.service.KeyService;
import com.pepperwallet.app.util.PreferenceManager;
import com.pepperwallet.app.util.RootUtil;
import com.pepperwallet.app.viewmodel.SplashViewModel;
import com.pepperwallet.app.widget.AWalletAlertDialog;
import com.pepperwallet.app.widget.SignTransactionDialog;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends BaseActivity implements CreateWalletCallbackInterface, Runnable
{
    SplashViewModel splashViewModel;
//    PasscodeView passcodeView;

    private Handler handler = new Handler(Looper.getMainLooper());
    private String errorMessage;
    private String pass;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //detect previous launch
        splashViewModel = new ViewModelProvider(this)
                .get(SplashViewModel.class);

        splashViewModel.cleanAuxData(getApplicationContext());
        setContentView(R.layout.activity_splash);

//        if(!PreferenceManager.getBoolValue(C.LOCK)){
//            gotoPasscode("FirstTime");
//        }
        splashViewModel.wallets().observe(this, this::onWallets);
        splashViewModel.createWallet().observe(this, this::onWalletCreate);
        splashViewModel.fetchWallets();

        checkRoot();
//        PreferenceManager.setBoolValue(C.ONBOARDING,true);
//            new HomeRouter().open(this);

    }



    protected Activity getThisActivity()
    {
        return this;
    }

    //wallet created, now check if we need to import
    private void onWalletCreate(Wallet wallet)
    {
        Wallet[] wallets = new Wallet[1];
        wallets[0] = wallet;
        onWallets(wallets);
    }


//private void onPasscode(){
//        passcodeView.setFirstInputTip(passcodeView.getCorrectInputTip());
//        passcodeView.setPasscodeLength(5)
//                // to set pincode or passcode
//                .setLocalPasscode("12345")
//
//                // to set listener to it to check whether
//                // passwords has matched or failed
//                .setListener(new PasscodeView.PasscodeViewListener()
//                {
//                    @Override
//                    public void onFail()
//                    {
//                        // to show message when Password is incorrect
//                        Toast.makeText(SplashActivity.this, "Password is wrong!", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    @Override
//                    public void onSuccess(String number)
//                    {
//                        // here is used so that when password
//                        // is correct user will be
//                        // directly navigated to next activity
//                        findViewById(R.id.layout1).setVisibility(View.VISIBLE);
//                        findViewById(R.id.passcodeview).setVisibility(View.GONE);
//
//                    }
//                });
//
//}


    private void onWallets(Wallet[] wallets) {
        //event chain should look like this:
        //1. check if wallets are empty:
        //      - yes, get either create a new account or take user to wallet page if SHOW_NEW_ACCOUNT_PROMPT is set
        //              then come back to this check.
        //      - no. proceed to check if we are importing a link
        //2. repeat after step 1 is complete. Are we importing a ticket?
        //      - yes - proceed with import
        //      - no - proceed to home activity
        if (wallets.length == 0)
        {
            splashViewModel.setDefaultBrowser();
            findViewById(R.id.layout_new_wallet).setVisibility(View.VISIBLE);
            findViewById(R.id.button_create).setOnClickListener(v -> {
                splashViewModel.createNewWallet(this, this);
            });
            findViewById(R.id.button_watch).setOnClickListener(v -> {
                new ImportWalletRouter().openWatchCreate(this, IMPORT_REQUEST_CODE);
            });
            findViewById(R.id.button_import).setOnClickListener(v -> {
                new ImportWalletRouter().openForResult(this, IMPORT_REQUEST_CODE);
            });
        }
        else
        {
            handler.postDelayed(this, CustomViewSettings.startupDelay());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode >= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS && requestCode <= SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS + 10)
        {
            Operation taskCode = Operation.values()[requestCode - SignTransactionDialog.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS];
            if (resultCode == RESULT_OK)
            {
                splashViewModel.completeAuthentication(taskCode);
            }
            else
            {
                splashViewModel.failedAuthentication(taskCode);
            }
        }
        else if (requestCode == IMPORT_REQUEST_CODE)
        {
            splashViewModel.fetchWallets();
        }
    }

    @Override
    public void HDKeyCreated(String address, Context ctx, KeyService.AuthenticationLevel level)
    {
        splashViewModel.StoreHDKey(address, level);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        handler = null;
    }

    @Override
    public void keyFailure(String message)
    {
        errorMessage = message;
        if (handler != null) handler.post(displayError);
    }

    Runnable displayError = new Runnable()
    {
        @Override
        public void run()
        {
            AWalletAlertDialog aDialog = new AWalletAlertDialog(getThisActivity());
            aDialog.setTitle(R.string.key_error);
            aDialog.setIcon(AWalletAlertDialog.ERROR);
            aDialog.setMessage(errorMessage);
            aDialog.setButtonText(R.string.dialog_ok);
            aDialog.setButtonListener(v -> aDialog.dismiss());
            aDialog.show();
        }
    };

    @Override
    public void cancelAuthentication()
    {

    }

    @Override
    public void fetchMnemonic(String mnemonic)
    {

    }

    @Override
    public void run()
    {
        PreferenceManager.init(this);
        if(PreferenceManager.getBoolValue(C.ONBOARDING)){
            new HomeRouter().open(this,true);
        } else
        {
            PreferenceManager.setBoolValue(C.ONBOARDING,true);
            new HomeRouter().open(this, true, this);
            finish();
        }
    }

    private void checkRoot()
    {
        if (RootUtil.isDeviceRooted())
        {
            AWalletAlertDialog dialog = new AWalletAlertDialog(this);
            dialog.setTitle(R.string.root_title);
            dialog.setMessage(R.string.root_body);
            dialog.setButtonText(R.string.ok);
            dialog.setIcon(AWalletAlertDialog.ERROR);
            dialog.setButtonListener(v -> dialog.dismiss());
            dialog.show();
        }
    }

}
