package com.alphawallet.app.ui;


import static android.app.Activity.RESULT_OK;
import static com.alphawallet.app.C.CHANGED_LOCALE;
import static com.alphawallet.app.C.CHANGE_CURRENCY;
import static com.alphawallet.app.C.EXTRA_CURRENCY;
import static com.alphawallet.app.C.EXTRA_LOCALE;
import static com.alphawallet.app.C.EXTRA_STATE;
import static com.alphawallet.app.C.Key.WALLET;
import static com.alphawallet.app.C.RESET_WALLET;
import static com.alphawallet.app.C.SETTINGS_INSTANTIATED;
import static com.alphawallet.app.entity.BackupOperationType.BACKUP_HD_KEY;
import static com.alphawallet.app.entity.BackupOperationType.BACKUP_KEYSTORE_KEY;
import static com.alphawallet.app.ui.HomeActivity.RESET_TOKEN_SERVICE;
import static com.alphawallet.token.tools.TokenDefinition.TOKENSCRIPT_CURRENT_SCHEMA;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.BackupOperationType;
import com.alphawallet.app.entity.CustomViewSettings;
import com.alphawallet.app.entity.MediaLinks;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.WalletPage;
import com.alphawallet.app.entity.WalletType;
import com.alphawallet.app.interact.GenericWalletInteract;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.util.UpdateUtils;
import com.alphawallet.app.viewmodel.AdvancedSettingsViewModel;
import com.alphawallet.app.viewmodel.NewSettingsViewModel;
import com.alphawallet.app.widget.NotificationView;
import com.alphawallet.app.widget.SettingsItemView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@AndroidEntryPoint
public class NewSettingsFragment extends BaseFragment
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
                getParentFragmentManager().setFragmentResult(C.HANDLE_BACKUP, b);
            });

    ActivityResultLauncher<Intent> networkSettingsHandler = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                //send instruction to restart tokenService
                getParentFragmentManager().setFragmentResult(RESET_TOKEN_SERVICE, new Bundle());
            });

    ActivityResultLauncher<Intent> advancedSettingsHandler = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                Intent data = result.getData();
                if (data == null) return;
                if (data.getBooleanExtra(RESET_WALLET, false))
                {
                    getParentFragmentManager().setFragmentResult(RESET_WALLET, new Bundle());
                }
                else if (data.getBooleanExtra(CHANGE_CURRENCY, false))
                {
                    getParentFragmentManager().setFragmentResult(CHANGE_CURRENCY, new Bundle());
                }
                else if (data.getBooleanExtra(CHANGED_LOCALE, false))
                {
                    getParentFragmentManager().setFragmentResult(CHANGED_LOCALE, new Bundle());
                }
            });

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

    private NewSettingsViewModel viewModel;
    private AdvancedSettingsViewModel viewModelAdS;
    private LinearLayout walletSettingsLayout;
    private LinearLayout systemSettingsLayout;
    private LinearLayout supportSettingsLayout;
    private SettingsItemView myAddressSetting;
    private SettingsItemView changeWalletSetting;
    private SettingsItemView backUpWalletSetting;
    private SettingsItemView notificationsSetting;
    private SettingsItemView changeLanguage;
    private SettingsItemView changeCurrency;
    private SettingsItemView biometricsSetting;
    private SettingsItemView selectNetworksSetting;
    private SettingsItemView advancedSetting;
    private SettingsItemView darkModeSetting;
    private SettingsItemView supportSetting;
    private SettingsItemView walletConnectSetting;
    private SettingsItemView showSeedPhrase;
    private SettingsItemView nameThisWallet;
    private SettingsItemView telegram;
    private SettingsItemView discord;
    private SettingsItemView email;
    private SettingsItemView twitter;
    private SettingsItemView reddit;
    private SettingsItemView facebook;
    private SettingsItemView blog;
    private SettingsItemView faq;
    private SettingsItemView github;
    private SettingsItemView clearBrowserCache;
    private LinearLayout layoutBackup;
    private Button backupButton;
    private TextView backupTitle;
    private TextView backupDetail;
    private ImageView closeBtn;
    private NotificationView notificationView;
    private MaterialCardView updateLayout;
    private int pendingUpdate = 0;
    private Wallet wallet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        viewModel = new ViewModelProvider(this)
                .get(NewSettingsViewModel.class);
        viewModelAdS =  new ViewModelProvider(this)
                .get(AdvancedSettingsViewModel.class);
        viewModel.defaultWallet().observe(getViewLifecycleOwner(), this::onDefaultWallet);
        viewModel.backUpMessage().observe(getViewLifecycleOwner(), this::backupWarning);
        LocaleUtils.setActiveLocale(getContext());

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        toolbar(view);

        setToolbarTitle(R.string.toolbar_header_settings);

        initializeSettings(view);

        addSettingsToLayout();

        setInitialSettingsData(view);

        initBackupWarningViews(view);

        initNotificationView(view);

        checkPendingUpdate(view);

        getParentFragmentManager().setFragmentResult(SETTINGS_INSTANTIATED, new Bundle());

        return view;
    }

    private void initNotificationView(View view)
    {
        notificationView = view.findViewById(R.id.notification);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
        {
            notificationView.setTitle(getContext().getString(R.string.title_version_support_warning));
            notificationView.setMessage(getContext().getString(R.string.message_version_support_warning));
            notificationView.setPrimaryButtonText(getContext().getString(R.string.hide_notification));
            notificationView.setPrimaryButtonListener(() ->
            {
                notificationView.setVisibility(View.GONE);
                viewModel.setMarshMallowWarning(true);
            });
        }
        else
        {
            notificationView.setVisibility(View.GONE);
        }
    }

    public void signalUpdate(int updateVersion)
    {
        //add wallet update signal to adapter
        pendingUpdate = updateVersion;
        checkPendingUpdate(getView());
    }

    private void initBackupWarningViews(View view)
    {
        layoutBackup = view.findViewById(R.id.layout_item_warning);
        backupTitle = view.findViewById(R.id.text_title);
        backupDetail = view.findViewById(R.id.text_detail);
        backupButton = view.findViewById(R.id.button_backup);
        closeBtn = view.findViewById(R.id.btn_close);
        layoutBackup.setVisibility(View.GONE);
    }

    private void initializeSettings(View view)
    {
        walletSettingsLayout = view.findViewById(R.id.layout_settings_wallet);
        systemSettingsLayout = view.findViewById(R.id.layout_settings_system);
        supportSettingsLayout = view.findViewById(R.id.layout_settings_support);
        updateLayout = view.findViewById(R.id.layout_update);


        myAddressSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_wallet_address)
                        .withTitle(R.string.general)
                        .withListener(this::openGeneralSetiingClicked)
                        .build();

        myAddressSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_wallet_address)
                        .withTitle(R.string.title_show_wallet_address)
                        .withListener(this::onShowWalletAddressSettingClicked)
                        .build();

        changeWalletSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_change_wallet)
                        .withTitle(R.string.title_change_add_wallet)
                        .withListener(this::onChangeWalletSettingClicked)
                        .build();

        backUpWalletSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_backup)
                        .withTitle(R.string.title_back_up_wallet)
                        .withListener(this::onBackUpWalletSettingClicked)
                        .build();

        showSeedPhrase = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_settings_show_seed)
                .withTitle(R.string.show_seed_phrase)
                .withListener(this::onShowSeedPhrase) //onShow
                .build();

        nameThisWallet = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_settings_name_this_wallet)
                .withTitle(R.string.name_this_wallet)
                .withListener(this::onNameThisWallet)
                .build();

        walletConnectSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_wallet_connect)
                        .withTitle(R.string.title_wallet_connect)
                        .withListener(this::onWalletConnectSettingClicked)
                        .build();

        notificationsSetting =
                new SettingsItemView.Builder(getContext())
                        .withType(SettingsItemView.Type.TOGGLE)
                        .withIcon(R.drawable.ic_settings_notifications)
                        .withTitle(R.string.title_notifications)
                        .withListener(this::onNotificationsSettingClicked)
                        .build();

        changeLanguage = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_settings_language)
                .withTitle(R.string.title_change_language)
                .withListener(this::onChangeLanguageClicked)
                .build();

        changeCurrency = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_currency)
                .withTitle(R.string.settings_locale_currency)
                .withListener(this::onChangeCurrencyClicked)
                .build();

//        biometricsSetting =
//                new SettingsItemView.Builder(getContext())
//                        .withType(SettingsItemView.Type.TOGGLE)
//                        .withIcon(R.drawable.ic_settings_biometrics)
//                        .withTitle(R.string.title_biometrics)
//                        .withListener(this::onBiometricsSettingClicked)
//                        .build();

        selectNetworksSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_networks)
                        .withTitle(R.string.select_active_networks)
                        .withListener(this::onSelectNetworksSettingClicked)
                        .build();

        advancedSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_advanced)
                        .withTitle(R.string.title_advanced)
                        .withListener(this::onAdvancedSettingClicked)
                        .build();
        clearBrowserCache = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_settings_cache)
                .withTitle(R.string.title_clear_browser_cache)
                .withListener(this::onClearBrowserCacheClicked)
                .build();

        darkModeSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_darkmode)
                        .withTitle(R.string.title_dark_mode)
                        .withListener(this::onDarkModeSettingClicked)
                        .build();

        supportSetting =
                new SettingsItemView.Builder(getContext())
                        .withIcon(R.drawable.ic_settings_support)
                        .withTitle(R.string.title_support)
                        .withListener(this::onSupportSettingClicked)
                        .build();
        telegram = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_logo_telegram)
                .withTitle(R.string.telegram)
                .withListener(this::onTelegramClicked)
                .build();

        discord = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_logo_discord)
                .withTitle(R.string.discord)
                .withListener(this::onDiscordClicked)
                .build();

        email = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_email)
                .withTitle(R.string.email)
                .withListener(this::onEmailClicked)
                .build();

        twitter = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_logo_twitter)
                .withTitle(R.string.twitter)
                .withListener(this::onTwitterClicked)
                .build();
        github = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_logo_github)
                .withTitle(R.string.github)
                .withListener(this::onGitHubClicked)
                .build();

        faq = new SettingsItemView.Builder(getContext())
                .withIcon(R.drawable.ic_settings_faq)
                .withTitle(R.string.title_faq)
                .withListener(this::onFaqClicked)
                .build();
    }

    private void addSettingsToLayout()
    {
        int walletIndex = 0;
        int systemIndex = 0;
        int supportIndex = 0;

        walletSettingsLayout.addView(myAddressSetting, walletIndex++);

        if (CustomViewSettings.canChangeWallets())
            walletSettingsLayout.addView(changeWalletSetting, walletIndex++);

        walletSettingsLayout.addView(backUpWalletSetting, walletIndex++);

        walletSettingsLayout.addView(showSeedPhrase, walletIndex++);
        showSeedPhrase.setVisibility(View.GONE);

        walletSettingsLayout.addView(nameThisWallet, walletIndex++);

        walletSettingsLayout.addView(walletConnectSetting, walletIndex++);

        if (CustomViewSettings.getLockedChains().size() == 0)
            systemSettingsLayout.addView(selectNetworksSetting, systemIndex++);

        if (biometricsSetting != null)
            systemSettingsLayout.addView(biometricsSetting, systemIndex++);

        systemSettingsLayout.addView(notificationsSetting, systemIndex++);

        systemSettingsLayout.addView(changeLanguage, systemIndex++);

        systemSettingsLayout.addView(changeCurrency, systemIndex++);

        systemSettingsLayout.addView(darkModeSetting, systemIndex++);

        //systemSettingsLayout.addView(advancedSetting, systemIndex++);
        systemSettingsLayout.addView(clearBrowserCache, systemIndex++);

        //supportSettingsLayout.addView(supportSetting, supportIndex++);
        supportSettingsLayout.addView(telegram, supportIndex++);
//        supportSettingsLayout.addView(discord, supportIndex++);
//        supportSettingsLayout.addView(email, supportIndex++);
//        supportSettingsLayout.addView(twitter, supportIndex++);
//        supportSettingsLayout.addView(github, supportIndex++);
//        supportSettingsLayout.addView(faq, supportIndex++);


    }


    private void setInitialSettingsData(View view)
    {
        TextView appVersionText = view.findViewById(R.id.text_version);
        appVersionText.setText(String.format(Locale.getDefault(), "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        //TextView tokenScriptVersionText = view.findViewById(R.id.text_tokenscript_compatibility);
        //tokenScriptVersionText.setText(TOKENSCRIPT_CURRENT_SCHEMA);

        notificationsSetting.setToggleState(viewModel.getNotificationState());
    }

    private void openShowSeedPhrase(Wallet wallet)
    {
        if (wallet.type != WalletType.HDKEY) return;

        Intent intent = new Intent(getContext(), ScammerWarningActivity.class);
        intent.putExtra(WALLET, wallet);
        startActivity(intent);
    }

    private void openBackupActivity(Wallet wallet)
    {
        Intent intent = new Intent(getContext(), BackupFlowActivity.class);
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

        viewModel.setLocale(getContext());

        changeLanguage.setSubtitle(LocaleUtils.getDisplayLanguage(viewModel.getActiveLocale(), viewModel.getActiveLocale()));

        changeCurrency.setSubtitle(viewModel.getDefaultCurrency());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (viewModel == null)
        {
            ((HomeActivity) getActivity()).resetFragment(WalletPage.SETTINGS);
        }
        else
        {
            viewModel.prepare();
        }
    }

    public void backupSeedSuccess(boolean hasNoLock)
    {
        if (viewModel != null) viewModel.TestWalletBackup();
        if (layoutBackup != null) layoutBackup.setVisibility(View.GONE);
        if (hasNoLock)
        {
            backUpWalletSetting.setSubtitle(getString(R.string.not_locked));
        }
    }

    private void backupWarning(String s)
    {
        if (s.equals(viewModel.defaultWallet().getValue().address))
        {
            addBackupNotice(GenericWalletInteract.BackupLevel.WALLET_HAS_HIGH_VALUE);
        }
        else
        {
            if (layoutBackup != null)
            {
                layoutBackup.setVisibility(View.GONE);
            }
            //remove the number prompt
            if (getActivity() != null)
                ((HomeActivity) getActivity()).removeSettingsBadgeKey(C.KEY_NEEDS_BACKUP);
            onDefaultWallet(viewModel.defaultWallet().getValue());
        }
    }

    void addBackupNotice(GenericWalletInteract.BackupLevel walletValue)
    {
        layoutBackup.setVisibility(View.VISIBLE);
        if (wallet != null)
        {
            backupButton.setText(getString(R.string.back_up_now));
            backupButton.setOnClickListener(v -> openBackupActivity(wallet));
            backupTitle.setText(getString(R.string.title_back_up_your_wallet));
            backupDetail.setText(getString(R.string.backup_wallet_detail));
            closeBtn.setOnClickListener(v -> {
                backedUp(wallet.address);
                viewModel.setIsDismissed(wallet.address, true);
            });

            if (getActivity() != null)
            {
                ((HomeActivity) getActivity()).addSettingsBadgeKey(C.KEY_NEEDS_BACKUP);
            }
        }
    }

    private void backedUp(String walletAddress)
    {
        layoutBackup.setVisibility(View.GONE);
        if (getActivity() != null)
            ((HomeActivity) getActivity()).postponeWalletBackupWarning(walletAddress);
    }

    private void onShowWalletAddressSettingClicked()
    {
        viewModel.showMyAddress(getContext());
    }

    private void onChangeWalletSettingClicked()
    {
        viewModel.showManageWallets(getContext(), false);
    }

    private void onBackUpWalletSettingClicked()
    {
        Wallet wallet = viewModel.defaultWallet().getValue();
        if (wallet != null)
        {
            openBackupActivity(wallet);
        }
    }

    private void onShowSeedPhrase()
    {
        Wallet wallet = viewModel.defaultWallet().getValue();
        if (wallet != null)
        {
            openShowSeedPhrase(wallet);
        }
    }

    private void onNameThisWallet()
    {
        Intent intent = new Intent(getActivity(), NameThisWalletActivity.class);
        requireActivity().startActivity(intent);
    }

    private void onNotificationsSettingClicked()
    {
        viewModel.setNotificationState(notificationsSetting.getToggleState());
    }

    private void onBiometricsSettingClicked()
    {
        // TODO: Implementation
    }

    private void onSelectNetworksSettingClicked()
    {
        Intent intent = new Intent(getActivity(), SelectNetworkFilterActivity.class);
        intent.putExtra(C.EXTRA_SINGLE_ITEM, false);
        networkSettingsHandler.launch(intent);
    }

    private void onAdvancedSettingClicked()
    {
        Intent intent = new Intent(getActivity(), AdvancedSettingsActivity.class);
        advancedSettingsHandler.launch(intent);
    }

    private void onDarkModeSettingClicked()
    {
        Intent intent = new Intent(getActivity(), SelectThemeActivity.class);
        startActivity(intent);
    }

    private void onSupportSettingClicked()
    {
        Intent intent = new Intent(getActivity(), SupportSettingsActivity.class);
        startActivity(intent);
    }

    private void onWalletConnectSettingClicked()
    {
        Intent intent = new Intent(getActivity(), WalletConnectSessionActivity.class);
        intent.putExtra("wallet", wallet);
        startActivity(intent);
    }

    private void checkPendingUpdate(View view)
    {
        if (updateLayout == null || view == null) return;

        if (pendingUpdate > 0)
        {
            updateLayout.setVisibility(View.VISIBLE);
            TextView current = view.findViewById(R.id.text_detail_current);
            TextView available = view.findViewById(R.id.text_detail_available);
            current.setText(getString(R.string.installed_version, String.valueOf(BuildConfig.VERSION_CODE)));
            available.setText(getString(R.string.available_version, String.valueOf(pendingUpdate)));
            if (getActivity() != null)
            {
                ((HomeActivity) getActivity()).addSettingsBadgeKey(C.KEY_UPDATE_AVAILABLE);
            }

            updateLayout.setOnClickListener(v ->
            {
                UpdateUtils.pushUpdateDialog(getActivity());
                updateLayout.setVisibility(View.GONE);
                pendingUpdate = 0;
                if (getActivity() != null)
                {
                    ((HomeActivity) getActivity()).removeSettingsBadgeKey(C.KEY_UPDATE_AVAILABLE);
                }
            });
        }
        else
        {
            updateLayout.setVisibility(View.GONE);
        }
    }

    private void onChangeLanguageClicked()
    {
        Intent intent = new Intent(getActivity(), SelectLocaleActivity.class);
        String selectedLocale = viewModel.getActiveLocale();
        intent.putExtra(EXTRA_LOCALE, selectedLocale);
        intent.putParcelableArrayListExtra(EXTRA_STATE, viewModel.getLocaleList(getContext()));
        updateLocale.launch(intent);
    }

    private void onChangeCurrencyClicked()
    {
        Intent intent = new Intent(getActivity(), SelectCurrencyActivity.class);
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
                viewModel.updateLocale(newLocale, getContext());
                getActivity().recreate();
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
                        .subscribe(res -> getActivity().recreate())
                        .isDisposed();
            }
        }
    }

    private void onTelegramClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MediaLinks.AWALLET_TELEGRAM_URL));
        if (isAppAvailable(C.TELEGRAM_PACKAGE_NAME)) {
            intent.setPackage(C.TELEGRAM_PACKAGE_NAME);
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onGitHubClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(Uri.parse(MediaLinks.AWALLET_GITHUB));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onDiscordClicked(){
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_DISCORD_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_DISCORD_URL));
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onEmailClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        final String at = "@";
        String email =
                "mailto:" + MediaLinks.AWALLET_EMAIL1 + at + MediaLinks.AWALLET_EMAIL2 +
                        "?subject=" + Uri.encode(MediaLinks.AWALLET_SUBJECT) +
                        "&body=" + Uri.encode("");
        intent.setData(Uri.parse(email));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onLinkedInClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MediaLinks.AWALLET_LINKEDIN_URL));
        if (isAppAvailable(C.LINKEDIN_PACKAGE_NAME)) {
            intent.setPackage(C.LINKEDIN_PACKAGE_NAME);
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onTwitterClicked() {
        Intent intent;
        try {
            getActivity().getPackageManager().getPackageInfo(C.TWITTER_PACKAGE_NAME, 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_TWITTER_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_TWITTER_URL));
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onRedditClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (isAppAvailable(C.REDDIT_PACKAGE_NAME)) {
            intent.setPackage(C.REDDIT_PACKAGE_NAME);
        }

        intent.setData(Uri.parse(MediaLinks.AWALLET_REDDIT_URL));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onFacebookClicked() {
        Intent intent;
        try {
            getActivity().getPackageManager().getPackageInfo(C.FACEBOOK_PACKAGE_NAME, 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_FACEBOOK_URL));
            //intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_FACEBOOK_ID));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MediaLinks.AWALLET_FACEBOOK_URL));
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void onBlogClicked() {

    }

    private void onFaqClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MediaLinks.AWALLET_FAQ_URL));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private boolean isAppAvailable(String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void onClearBrowserCacheClicked()
    {
        WebView webView = new WebView(getContext());
        webView.clearCache(true);
        viewModelAdS.blankFilterSettings();

        Single.fromCallable(() ->
                {
                    Glide.get(getContext()).clearDiskCache();
                    return 1;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v ->
                {
                    Toast.makeText(getContext(), getString(R.string.toast_browser_cache_cleared), Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }).isDisposed();
    }

    private void openGeneralSetiingClicked(){
        Intent intent = new Intent(getActivity(), SettingGeneral.class);
        startActivity(intent);
    }
}
