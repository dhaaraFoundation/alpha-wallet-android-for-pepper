package com.pepperwallet.app.ui;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pepperwallet.app.R;
import com.pepperwallet.app.viewmodel.BaseViewModel;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.empty);
        }
        enableDisplayHomeAsUp();
        return toolbar;
    }

    protected void setTitle(String title)
    {
        ActionBar actionBar = getSupportActionBar();
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            if (actionBar != null) {
                actionBar.setTitle(R.string.empty);
            }
            toolbarTitle.setText(title);
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
//        gotoPasscode();
    }



    protected void setSubtitle(String subtitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }
    }



    protected void enableDisplayHomeAsUp() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void enableDisplayHomeAsUp(@DrawableRes int resourceId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(resourceId);
        }
    }

    protected void enableDisplayHomeAsHome(boolean active) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(active);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_browser_home);
        }
    }

    protected void dissableDisplayHomeAsUp() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void hideToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    protected void showToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void displayToast(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            BaseViewModel.onPushToast(null);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void  gotoPasscode(String from){
        startActivity(new Intent(this,Passcode.class).putExtra("from",from));
    }
}
