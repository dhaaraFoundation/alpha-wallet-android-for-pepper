package com.pepperwallet.app.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pepperwallet.app.entity.CurrencyItem;
import com.pepperwallet.app.entity.LocaleItem;
import com.pepperwallet.app.entity.Transaction;
import com.pepperwallet.app.entity.Wallet;
import com.pepperwallet.app.interact.GenericWalletInteract;
import com.pepperwallet.app.repository.CurrencyRepositoryType;
import com.pepperwallet.app.repository.LocaleRepositoryType;
import com.pepperwallet.app.repository.PreferenceRepositoryType;
import com.pepperwallet.app.router.ManageWalletsRouter;
import com.pepperwallet.app.router.MyAddressRouter;
import com.pepperwallet.app.service.TransactionsService;
import com.pepperwallet.app.util.LocaleUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Single;

@HiltViewModel
public class SettingGeneralViewmodel extends BaseViewModel {

    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Transaction[]> transactions = new MutableLiveData<>();
    private final MutableLiveData<String> backUpMessage = new MutableLiveData<>();
    private final GenericWalletInteract genericWalletInteract;
    private final MyAddressRouter myAddressRouter;
    private final ManageWalletsRouter manageWalletsRouter;
    private final PreferenceRepositoryType preferenceRepository;
    private final LocaleRepositoryType localeRepository;
    private final CurrencyRepositoryType currencyRepository;
    private final TransactionsService transactionsService;

    @Inject
    SettingGeneralViewmodel(
            GenericWalletInteract genericWalletInteract,
            MyAddressRouter myAddressRouter,
            ManageWalletsRouter manageWalletsRouter,
            PreferenceRepositoryType preferenceRepository,
            LocaleRepositoryType localeRepository,
            CurrencyRepositoryType currencyRepository,
            TransactionsService transactionsService) {
        this.genericWalletInteract = genericWalletInteract;
        this.myAddressRouter = myAddressRouter;
        this.manageWalletsRouter = manageWalletsRouter;
        this.preferenceRepository = preferenceRepository;
        this.localeRepository = localeRepository;
        this.currencyRepository = currencyRepository;
        this.transactionsService = transactionsService;
    }

    public ArrayList<LocaleItem> getLocaleList(Context context) {
        return localeRepository.getLocaleList(context);
    }

    public void setLocale(Context activity) {
        String currentLocale = localeRepository.getActiveLocale();
        LocaleUtils.setLocale(activity, currentLocale);
    }

    public void updateLocale(String newLocale, Context context) {
        localeRepository.setUserPreferenceLocale(newLocale);
        localeRepository.setLocale(context, newLocale);
    }

    public String getDefaultCurrency(){
        return currencyRepository.getDefaultCurrency();
    }

    public ArrayList<CurrencyItem> getCurrencyList() {
        return currencyRepository.getCurrencyList();
    }

    public Single<Boolean> updateCurrency(String currencyCode){
        currencyRepository.setDefaultCurrency(currencyCode);
        //delete tickers from realm
        return transactionsService.wipeTickerData();
    }

    public String getActiveLocale()
    {
        return localeRepository.getActiveLocale();
    }

    public void showManageWallets(Context context, boolean clearStack) {
        manageWalletsRouter.open(context, clearStack);
    }

    public boolean getNotificationState()
    {
        return preferenceRepository.getNotificationsState();
    }
    public void setNotificationState(boolean notificationState)
    {
        preferenceRepository.setNotificationState(notificationState);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }

    public LiveData<Transaction[]> transactions() {
        return transactions;
    }
    public LiveData<String> backUpMessage() { return backUpMessage; }

    public void prepare() {
        disposable = genericWalletInteract
                .find()
                .subscribe(this::onDefaultWallet, this::onError);
    }

    private void onDefaultWallet(Wallet wallet) {
        defaultWallet.setValue(wallet);

        TestWalletBackup();
    }

    public void TestWalletBackup()
    {
        if (defaultWallet.getValue() != null)
        {
            genericWalletInteract.getWalletNeedsBackup(defaultWallet.getValue().address)
                    .subscribe(backUpMessage::postValue).isDisposed();
        }
    }

    public void showMyAddress(Context context) {
        myAddressRouter.open(context, defaultWallet.getValue());
    }

    public void setIsDismissed(String walletAddr, boolean isDismissed)
    {
        genericWalletInteract.setIsDismissed(walletAddr, isDismissed);
    }

    public void setMarshMallowWarning(boolean shown) {
        preferenceRepository.setMarshMallowWarning(shown);
    }
}
