package com.pepperwallet.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;
import com.pepperwallet.app.C;
import com.pepperwallet.app.R;
import com.pepperwallet.app.util.PreferenceManager;

public class Passcode extends AppCompatActivity {

    PasscodeView passcodeView;
    String from = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        passcodeView = findViewById(R.id.passcodeview);
        try
        {
            if(getIntent().getExtras() != null){
                from = getIntent().getExtras().getString("from");
            }
            PreferenceManager.init(this);
            if(from.equalsIgnoreCase("FirstTime")){
                firstTimePasscode();
            } else {
                nextpasscode();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void firstTimePasscode(){
        passcodeView.setListener(new PasscodeView.PasscodeViewListener()
                {
                    @Override
                    public void onFail()
                    {
                        Toast.makeText(Passcode.this, "Password is wrong!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String number)
                    {
                        PreferenceManager.setStringValue(C.PASSCODE_NUMBER,number);
                        PreferenceManager.setBoolValue(C.LOCK,true);
                        finish();

                    }
                });

    }

    public void nextpasscode(){
        passcodeView.setPasscodeLength(5).setLocalPasscode(PreferenceManager.getStringValue(C.PASSCODE))
//                .setPasscodeLength(5)
                // to set pincode or passcode
//                    .setLocalPasscode(passcodeView.getLocalPasscode())

                // to set listener to it to check whether
                // passwords has matched or failed
                .setListener(new PasscodeView.PasscodeViewListener()
                {
                    @Override
                    public void onFail()
                    {
                        // to show message when Password is incorrect
                        Toast.makeText(Passcode.this, "Password is wrong!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onSuccess(String number)
                    {
                        // here is used so that when password
                        // is correct user will be
                        // directly navigated to next activity
                        finish();

                    }
                });
    }
    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
    }
}