package com.alphawallet.app.ui;

import static com.alphawallet.app.C.EXTRA_CURRENCY;
import static com.alphawallet.app.C.EXTRA_LOCALE;
import static com.alphawallet.app.C.EXTRA_STATE;
import static com.alphawallet.app.ui.HomeActivity.RESET_TOKEN_SERVICE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.CustomViewSettings;
import com.alphawallet.app.entity.MediaLinks;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.viewmodel.MyAddressViewModel;
import com.alphawallet.app.viewmodel.SettingGeneralViewmodel;
import com.alphawallet.app.widget.SettingsItemView;

import java.util.Locale;

import dagger.hilt.EntryPoint;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


@AndroidEntryPoint
public class SettingGeneral extends BaseActivity {

    ActivityResultLauncher<Intent> updateLocale = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                updateLocale(result.getData());
            });

    ActivityResultLauncher<Intent> updateCurrency = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                updateCurrency(result.getData());
            });

    ActivityResultLauncher<Intent> networkSettingsHandler = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                //send instruction to restart tokenService
//                getParentFragmentManager().setFragmentResult(RESET_TOKEN_SERVICE, new Bundle());
            });

    private LinearLayout supportSettingsLayout;


    private SettingGeneralViewmodel viewModel;
    private SettingsItemView notificationsSetting;
    private SettingsItemView changeLanguage;
    private SettingsItemView changeCurrency;
    private SettingsItemView selectNetworksSetting;
    private SettingsItemView advancedSetting;
    private SettingsItemView darkModeSetting;
    private SettingsItemView supportSetting;
    private Wallet wallet;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (viewModel == null) initViewModel();
        LocaleUtils.setActiveLocale(this);
        setContentView(R.layout.activity_generic_settings);
        toolbar();
        setTitle(getString(R.string.general));
//        setInitialSettingsData();

        initializeSettings();

        addSettingsToLayout();
                    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(SettingGeneralViewmodel.class);
    }
    private void initializeSettings()
    {
//        notificationsSetting =
//                new SettingsItemView.Builder(this)
//                        .withType(SettingsItemView.Type.TOGGLE)
//                        .withIcon(R.drawable.ic_settings_notifications)
//                        .withTitle(R.string.title_notifications)
//                        .withListener(this::onNotificationsSettingClicked)
//                        .build();

        changeLanguage = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_language)
                .withTitle(R.string.title_change_language)
                .withListener(this::onChangeLanguageClicked)
                .build();

        changeCurrency = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_currency)
                .withTitle(R.string.settings_locale_currency)
                .withListener(this::onChangeCurrencyClicked)
                .build();

        darkModeSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_settings_darkmode)
                        .withTitle(R.string.title_dark_mode)
                        .withListener(this::onDarkModeSettingClicked)
                        .build();

        selectNetworksSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_settings_networks)
                        .withTitle(R.string.select_active_networks)
                        .withListener(this::onSelectNetworksSettingClicked)
                        .build();
        notificationsSetting =
                new SettingsItemView.Builder(this)
                        .withType(SettingsItemView.Type.TOGGLE)
                        .withIcon(R.drawable.ic_settings_notifications)
                        .withTitle(R.string.title_notifications)
                        .withListener(this::onNotificationsSettingClicked)
                        .build();



    }

    private void addSettingsToLayout() {
        supportSettingsLayout = findViewById(R.id.layout);
        supportSettingsLayout.addView(changeCurrency);
        supportSettingsLayout.addView(changeLanguage);
        supportSettingsLayout.addView(darkModeSetting);
        if (CustomViewSettings.getLockedChains().size() == 0)
            supportSettingsLayout.addView(selectNetworksSetting);
        supportSettingsLayout.addView(notificationsSetting);


    }

    private void onDefaultWallet(Wallet wallet)
    {
        this.wallet = wallet;

        viewModel.setLocale(this);

        changeLanguage.setSubtitle(LocaleUtils.getDisplayLanguage(viewModel.getActiveLocale(), viewModel.getActiveLocale()));

        changeCurrency.setSubtitle(viewModel.getDefaultCurrency());
    }



    private void onChangeLanguageClicked()
    {
        Intent intent = new Intent(this, SelectLocaleActivity.class);
        String selectedLocale = viewModel.getActiveLocale();
        intent.putExtra(EXTRA_LOCALE, selectedLocale);
        intent.putParcelableArrayListExtra(EXTRA_STATE, viewModel.getLocaleList(this));
        updateLocale.launch(intent);
    }

    private void onChangeCurrencyClicked()
    {
        Intent intent = new Intent(this, SelectCurrencyActivity.class);
        String currentLocale = viewModel.getDefaultCurrency();
        intent.putExtra(EXTRA_CURRENCY, currentLocale);
        intent.putParcelableArrayListExtra(EXTRA_STATE, viewModel.getCurrencyList());
        updateCurrency.launch(intent);
    }

    public void updateLocale(Intent data)
    {
        if (data != null)
        {
            String newLocale = data.getStringExtra(C.EXTRA_LOCALE);
            String oldLocale = viewModel.getActiveLocale();
            if (!TextUtils.isEmpty(newLocale) && !newLocale.equals(oldLocale))
            {
                viewModel.updateLocale(newLocale, this);
                this.recreate();
            }
        }
    }

    public void updateCurrency(Intent data)
    {
        if (data != null)
        {
            String currencyCode = data.getStringExtra(C.EXTRA_CURRENCY);
            if (!viewModel.getDefaultCurrency().equals(currencyCode))
            {
                viewModel.updateCurrency(currencyCode)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> this.recreate())
                        .isDisposed();
            }
        }
    }




    private boolean isAppAvailable(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void onDarkModeSettingClicked()
    {
        Intent intent = new Intent(this, SelectThemeActivity.class);
        startActivity(intent);
    }

    private void onSelectNetworksSettingClicked()
    {
        Intent intent = new Intent(this, SelectNetworkFilterActivity.class);
        intent.putExtra(C.EXTRA_SINGLE_ITEM, false);
        networkSettingsHandler.launch(intent);
    }

    private void onNotificationsSettingClicked()
    {
        viewModel.setNotificationState(notificationsSetting.getToggleState());
    }
    private void setInitialSettingsData()
    {
//        TextView appVersionText = findViewById(R.id.text_version);
//        appVersionText.setText(String.format(Locale.getDefault(), "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        //TextView tokenScriptVersionText = view.findViewById(R.id.text_tokenscript_compatibility);
        //tokenScriptVersionText.setText(TOKENSCRIPT_CURRENT_SCHEMA);

//        notificationsSetting.setToggleState(viewModel.getNotificationState());
    }

}


