package com.alphawallet.app.ui;

import static com.alphawallet.app.C.Key.WALLET;
import static com.alphawallet.app.entity.BackupOperationType.BACKUP_HD_KEY;
import static com.alphawallet.app.entity.BackupOperationType.BACKUP_KEYSTORE_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.BackupOperationType;
import com.alphawallet.app.entity.CustomViewSettings;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.WalletType;
import com.alphawallet.app.interact.GenericWalletInteract;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.viewmodel.NewSettingsViewModel;
import com.alphawallet.app.viewmodel.SettingGeneralViewmodel;
import com.alphawallet.app.viewmodel.SettingWalletViewmodel;
import com.alphawallet.app.widget.SettingsItemView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingWallet
        extends BaseActivity implements LifecycleObserver
{

    ActivityResultLauncher<Intent> handleBackupClick = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                String keyBackup = "";
                boolean noLockScreen = false;
                Intent data = result.getData();
                if (data != null) keyBackup = data.getStringExtra("Key");
                if (data != null) noLockScreen = data.getBooleanExtra("nolock", false);

                Bundle b = new Bundle();
                b.putBoolean(C.HANDLE_BACKUP, result.getResultCode() == RESULT_OK);
                b.putString("Key", keyBackup);
                b.putBoolean("nolock", noLockScreen);
//                getParentFragmentManager().setFragmentResult(C.HANDLE_BACKUP, b);
            });

    private LinearLayout supportSettingsLayout;


    private NewSettingsViewModel viewModel;
    private SettingsItemView myAddressSetting;
    private SettingsItemView changeWalletSetting;
    private SettingsItemView backUpWalletSetting;
    private SettingsItemView walletConnectSetting;
    private SettingsItemView showSeedPhrase;
    private SettingsItemView nameThisWallet;
    private Wallet wallet;
    private String default_wallet_value;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_settings);
        if (viewModel == null) initViewModel();
        getLifecycle().addObserver(this);
        LocaleUtils.setActiveLocale(this);
//        viewModel.backUpMessage().observe(this, this::backupWarning);
        toolbar();
        setTitle(getString(R.string.wallet));

        try {

            Intent intent = getIntent();
            default_wallet_value = intent.getStringExtra("wallet_value");
             Log.d("value",default_wallet_value);
            Log.d("default_value",default_wallet_value);

        } catch(Exception e) {
            e.printStackTrace();
        }
//        setInitialSettingsData();

        initializeSettings();

        addSettingsToLayout();
    }

    private void initViewModel()
    {
        viewModel = new ViewModelProvider(this)
                .get(NewSettingsViewModel.class);
        viewModel.defaultWallet().observe(this, this::onDefaultWallet);
    }


//    private void backupWarning(String s)
//    {
//        if (s.equals(viewModel.defaultWallet().getValue().address))
//        {
//            addBackupNotice(GenericWalletInteract.BackupLevel.WALLET_HAS_HIGH_VALUE);
//        }
//        else
//        {
//            if (layoutBackup != null)
//            {
//                layoutBackup.setVisibility(View.GONE);
//            }
//            //remove the number prompt
//            if (this != null)
//                ((HomeActivity) getApplication()).removeSettingsBadgeKey(C.KEY_NEEDS_BACKUP);
//            onDefaultWallet(viewModel.defaultWallet().getValue());
//        }
//    }

    private void initializeSettings()
    {
//        notificationsSetting =
//                new SettingsItemView.Builder(this)
//                        .withType(SettingsItemView.Type.TOGGLE)
//                        .withIcon(R.drawable.ic_settings_notifications)
//                        .withTitle(R.string.title_notifications)
//                        .withListener(this::onNotificationsSettingClicked)
//                        .build();

        myAddressSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_settings_wallet_address)
                        .withTitle(R.string.title_show_wallet_address)
                        .withListener(this::onShowWalletAddressSettingClicked)
                        .build();

        changeWalletSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_settings_change_wallet)
                        .withTitle(R.string.title_change_add_wallet)
                        .withListener(this::onChangeWalletSettingClicked)
                        .build();

        backUpWalletSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_settings_backup)
                        .withTitle(R.string.title_back_up_wallet)
                        .withListener(this::onBackUpWalletSettingClicked)
                        .build();

        showSeedPhrase = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_show_seed)
                .withTitle(R.string.show_seed_phrase)
                .withListener(this::onShowSeedPhrase) //onShow
                .build();

        nameThisWallet = new SettingsItemView.Builder(this)
                .withIcon(R.drawable.ic_settings_name_this_wallet)
                .withTitle(R.string.name_this_wallet)
                .withListener(this::onNameThisWallet)
                .build();

        walletConnectSetting =
                new SettingsItemView.Builder(this)
                        .withIcon(R.drawable.ic_wallet_connect)
                        .withTitle(R.string.title_wallet_connect)
                        .withListener(this::onWalletConnectSettingClicked)
                        .build();


    }



    private void onShowWalletAddressSettingClicked()
    {
        viewModel.showMyAddress(this);
    }

    private void onChangeWalletSettingClicked()
    {
        viewModel.showManageWallets(this, false);
    }

    private void onWalletConnectSettingClicked()
    {
        Intent intent = new Intent(this, WalletConnectSessionActivity.class);
        intent.putExtra("wallet", wallet);
        startActivity(intent);
    }
    private void onBackUpWalletSettingClicked()
    {
        Wallet wallet = viewModel.defaultWallet().getValue();
        if (wallet != null)
        {
            openBackupActivity(wallet);
        }
    }
//
//    public LiveData<Wallet> defaultWallet() {
//        return defaultWallet;
//    }

    private void openShowSeedPhrase(Wallet wallet)
    {
        if (wallet.type != WalletType.HDKEY) return;

        Intent intent = new Intent(this, ScammerWarningActivity.class);
        intent.putExtra(WALLET, wallet);
        startActivity(intent);
    }

    private void onShowSeedPhrase()
    {
        try
        {
            Log.d("wallet_value", viewModel.defaultWallet().getValue().toString());
            Wallet wallet = viewModel.defaultWallet().getValue();
            if (wallet != null)
            {
                openShowSeedPhrase(wallet);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onNameThisWallet()
    {
        Intent intent = new Intent(this, NameThisWalletActivity.class);
        startActivity(intent);
    }
    private void addSettingsToLayout()
    {
        supportSettingsLayout = findViewById(R.id.layout);
        supportSettingsLayout.addView(myAddressSetting);
        if (CustomViewSettings.canChangeWallets())
            supportSettingsLayout.addView(changeWalletSetting);
        supportSettingsLayout.addView(showSeedPhrase);
        supportSettingsLayout.addView(backUpWalletSetting);
        supportSettingsLayout.addView(walletConnectSetting);
        supportSettingsLayout.addView(nameThisWallet);


    }

    private void openBackupActivity(Wallet wallet)
    {
        Intent intent = new Intent(this, BackupFlowActivity.class);
        intent.putExtra(WALLET, wallet);

        switch (wallet.type)
        {
            case HDKEY:
                intent.putExtra("TYPE", BACKUP_HD_KEY);
                break;
            case KEYSTORE_LEGACY:
            case KEYSTORE:
                intent.putExtra("TYPE", BACKUP_KEYSTORE_KEY);
                break;
        }

        //override if this is an upgrade
        switch (wallet.authLevel)
        {
            case NOT_SET:
            case STRONGBOX_NO_AUTHENTICATION:
            case TEE_NO_AUTHENTICATION:
                if (wallet.lastBackupTime > 0)
                {
                    intent.putExtra("TYPE", BackupOperationType.UPGRADE_KEY);
                }
                break;
            default:
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        handleBackupClick.launch(intent);
    }

    private void onDefaultWallet(Wallet wallet)
    {
        this.wallet = wallet;
        if (wallet.address != null)
        {
            if (!wallet.ENSname.isEmpty())
            {
                changeWalletSetting.setSubtitle(wallet.ENSname + " | " + wallet.address);
            }
            else
            {
                changeWalletSetting.setSubtitle(wallet.address);
            }
        }

        switch (wallet.authLevel)
        {
            case NOT_SET:
            case STRONGBOX_NO_AUTHENTICATION:
            case TEE_NO_AUTHENTICATION:
                if (wallet.lastBackupTime > 0)
                {
                    backUpWalletSetting.setTitle(getString(R.string.action_upgrade_key));
                    backUpWalletSetting.setSubtitle(getString(R.string.not_locked));
                }
                else
                {
                    backUpWalletSetting.setTitle(getString(R.string.back_up_this_wallet));
                    backUpWalletSetting.setSubtitle(getString(R.string.back_up_now));
                }
                break;
            case TEE_AUTHENTICATION:
            case STRONGBOX_AUTHENTICATION:
                backUpWalletSetting.setTitle(getString(R.string.back_up_this_wallet));
                backUpWalletSetting.setSubtitle(getString(R.string.key_secure));
                break;
        }

        switch (wallet.type)
        {
            case NOT_DEFINED:
                break;
            case KEYSTORE:
                break;
            case HDKEY:
                showSeedPhrase.setVisibility(View.VISIBLE);
                break;
            case WATCH:
                backUpWalletSetting.setVisibility(View.GONE);
                break;
            case TEXT_MARKER:
                break;
            case KEYSTORE_LEGACY:
                break;
        }

        viewModel.setLocale(this);

//        changeLanguage.setSubtitle(LocaleUtils.getDisplayLanguage(viewModel.getActiveLocale(), viewModel.getActiveLocale()));
//
//        changeCurrency.setSubtitle(viewModel.getDefaultCurrency());
    }

}

