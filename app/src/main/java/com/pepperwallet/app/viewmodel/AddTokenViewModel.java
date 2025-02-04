package com.pepperwallet.app.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pepperwallet.app.C;
import com.pepperwallet.app.entity.ContractType;
import com.pepperwallet.app.entity.NetworkInfo;
import com.pepperwallet.app.entity.QRResult;
import com.pepperwallet.app.entity.Wallet;
import com.pepperwallet.app.entity.tokens.Token;
import com.pepperwallet.app.entity.tokens.TokenInfo;
import com.pepperwallet.app.interact.FetchTokensInteract;
import com.pepperwallet.app.interact.FetchTransactionsInteract;
import com.pepperwallet.app.interact.GenericWalletInteract;
import com.pepperwallet.app.repository.EthereumNetworkRepositoryType;
import com.pepperwallet.app.repository.PreferenceRepositoryType;
import com.pepperwallet.app.service.AssetDefinitionService;
import com.pepperwallet.app.service.TokensService;
import com.pepperwallet.app.ui.ImportTokenActivity;
import com.pepperwallet.app.ui.SendActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class AddTokenViewModel extends BaseViewModel {

    private final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    private final MutableLiveData<TokenInfo> tokenInfo = new MutableLiveData<>();
    private final MutableLiveData<Long> switchNetwork = new MutableLiveData<>();
    private final MutableLiveData<Token> finalisedToken = new MutableLiveData<>();
    private final MutableLiveData<Token> tokentype = new MutableLiveData<>();
    private final MutableLiveData<Boolean> noContract = new MutableLiveData<>();
    private final MutableLiveData<Integer> scanCount = new MutableLiveData<>();

    private final MutableLiveData<Token> onToken = new MutableLiveData<>();
    private final MutableLiveData<Token[]> allTokens = new MutableLiveData<>();

    private final EthereumNetworkRepositoryType ethereumNetworkRepository;
    private final GenericWalletInteract genericWalletInteract;
    private final FetchTokensInteract fetchTokensInteract;
    private final FetchTransactionsInteract fetchTransactionsInteract;
    private final AssetDefinitionService assetDefinitionService;
    private final TokensService tokensService;
    private final PreferenceRepositoryType sharedPreference;

    private boolean foundNetwork;
    private int networkCount;
    private long primaryChainId = 1;
    public final List<Token> discoveredTokenList = new ArrayList<>();


    public MutableLiveData<Wallet> wallet() {
        return wallet;
    }
    public MutableLiveData<Token> tokenType() { return tokentype; }
    public LiveData<Long> switchNetwork() { return switchNetwork; }
    public LiveData<Integer> chainScanCount() { return scanCount; }
    public LiveData<Token> onToken() { return onToken; }
    public LiveData<Token[]> allTokens() { return allTokens; }

    @Nullable
    Disposable scanNetworksDisposable;

    private final List<Disposable> scanThreads = new ArrayList<>();

    @Inject
    AddTokenViewModel(
            GenericWalletInteract genericWalletInteract,
            FetchTokensInteract fetchTokensInteract,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            FetchTransactionsInteract fetchTransactionsInteract,
            AssetDefinitionService assetDefinitionService,
            TokensService tokensService,
            PreferenceRepositoryType sharedPreference) {
        this.genericWalletInteract = genericWalletInteract;
        this.fetchTokensInteract = fetchTokensInteract;
        this.ethereumNetworkRepository = ethereumNetworkRepository;
        this.fetchTransactionsInteract = fetchTransactionsInteract;
        this.assetDefinitionService = assetDefinitionService;
        this.tokensService = tokensService;
        this.sharedPreference = sharedPreference;
    }


    public void saveTokens(List<Token> toSave)
    {
        Log.d("data",toSave.toString());
        tokensService.addTokens(toSave);
//        getTokenList();
    }

    @Override
    protected void onCleared()
    {
        super.onCleared();
        if (scanNetworksDisposable != null && !scanNetworksDisposable.isDisposed()) scanNetworksDisposable.dispose();
    }

    public void setPrimaryChain(long chainId)
    {
        primaryChainId = chainId;
    }

    public long getSelectedChain()
    {
        return primaryChainId;
    }

    private void checkType(Throwable throwable, long chainId, String address, ContractType type)
    {
        if (type == ContractType.ERC1155)
        {
            onTokensSetup(new TokenInfo(address, "Holding Contract", "", 0, true, chainId));
        }
        else
        {
            onError(throwable);
        }
    }

    public void fetchToken(long chainId, String addr)
    {
        tokensService.update(addr, chainId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::gotTokenUpdate, this::onError).isDisposed();
    }

    private void gotTokenUpdate(TokenInfo tokenInfo)
    {
        disposable = tokensService.addToken(tokenInfo, wallet.getValue().address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resumeSend, this::onError);
    }

    private void resumeSend(Token token)
    {
        finalisedToken.postValue(token);
    }

    public NetworkInfo getNetworkInfo(long chainId) { return ethereumNetworkRepository.getNetworkByChain(chainId); }

    private void findWallet()
    {
        disposable = genericWalletInteract.find()
                .subscribe(wallet::setValue, this::onError);
    }

    private void onTokensSetup(TokenInfo info) {
//        findWallet();
        Log.d("address", wallet.getValue().address);
        disposable = tokensService.addToken(info, wallet.getValue().address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::finaliseToken, error -> tokenTypeError(error, info));
    }

    private void finaliseToken(Token token)
    {
        checkNetworkCount();
        discoveredTokenList.add(token);
//        getTokenList();
        Log.d("token_list",discoveredTokenList.toString());
        onToken.postValue(token);
        saveTokens(discoveredTokenList);
    }



    public List<Token> getTokenList(){
        return discoveredTokenList;
    }

    private void tokenTypeError(Throwable throwable, TokenInfo data)
    {
        checkNetworkCount();
        Token badToken = new Token(data, BigDecimal.ZERO, 0, "", ContractType.NOT_SET);
        tokentype.postValue(badToken);
    }

    public void prepare()
    {
        findWallet();
    }

    public void showSend(Context ctx, QRResult result, Token token)
    {
        Intent intent = new Intent(ctx, SendActivity.class);
        boolean sendingTokens = (result.getFunction() != null && result.getFunction().length() > 0);
        String address = wallet.getValue().address;
        int decimals = 18;

        if (sendingTokens)
        {
            address = result.getAddress();
            decimals = token.tokenInfo.decimals;
        }

        intent.putExtra(C.EXTRA_SENDING_TOKENS, sendingTokens);
        intent.putExtra(C.EXTRA_CONTRACT_ADDRESS, address);
        intent.putExtra(C.EXTRA_NETWORKID, token.tokenInfo.chainId);
        intent.putExtra(C.EXTRA_SYMBOL, ethereumNetworkRepository.getNetworkByChain(result.chainId).symbol);
        intent.putExtra(C.EXTRA_DECIMALS, decimals);
        intent.putExtra(C.Key.WALLET, wallet.getValue());
        intent.putExtra(C.EXTRA_AMOUNT, result);
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        ctx.startActivity(intent);
    }

    private List<Long> getNetworkIds()
    {
        List<Long> networkIds = new ArrayList<>();
        networkIds.add(primaryChainId); //test selected chain first
        for (long chainId : tokensService.getNetworkFilters())
        {
            if (!networkIds.contains(chainId)) networkIds.add(chainId);
        }

        //Now scan unselected networks
        for (NetworkInfo networkInfo : ethereumNetworkRepository.getAvailableNetworkList())
        {
            if (!networkIds.contains(networkInfo.chainId)) networkIds.add(networkInfo.chainId);
        }
        return networkIds;
    }

    public void testNetworks(String address)
    {

        Log.d("address",address);
        foundNetwork = false;
        discoveredTokenList.clear();
        networkCount = ethereumNetworkRepository.getAvailableNetworkList().length;
        scanCount.postValue(networkCount);

        ethereumNetworkRepository.getAllActiveNetworks();
        scanCount.postValue(networkCount);

        for (long networkId : getNetworkIds())
        {
            TokenInfo tokenInfo = new TokenInfo(address, "", "", 0, true, networkId);
            Disposable d = fetchTransactionsInteract.queryInterfaceSpec(tokenInfo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(type -> testNetworkResult(tokenInfo, type), this::onTestError);

            scanThreads.add(d);
            Log.d("address",String.valueOf(scanThreads.size()));
            Log.d("address",scanThreads.get(0).toString());
        }
    }

    private void testNetworkResult(final TokenInfo info, final ContractType type)
    {
        if (type != ContractType.OTHER)
        {
            foundNetwork = true;
            disposable = tokensService
                    .update(info.address, info.chainId)
                    .subscribe(this::onTokensSetup, error -> checkType(error, info.chainId, info.address, type));
            Log.d("address",info.address.toString()+"/n"+info.chainId);
            Log.d("address",info.address.toString()+"/n"+info.chainId);
        }
        else
        {
            checkNetworkCount();
        }
    }

    public void stopScan()
    {
        for (Disposable d : scanThreads)
        {
            if (!d.isDisposed()) d.dispose();
        }
        scanThreads.clear();
    }

    private void onTestError(Throwable throwable)
    {
        checkNetworkCount();
        onError(throwable);
    }

    private void checkNetworkCount()
    {
        networkCount--;
        scanCount.postValue(networkCount);
        if (networkCount == 0 && !foundNetwork)
        {
            noContract.postValue(true);
        }
        if (networkCount == 0 && discoveredTokenList.size() > 0)
        {
            allTokens.postValue(discoveredTokenList.toArray(new Token[0]));
        }
    }

    public void showImportLink(Context context, String importTxt)
    {
        Intent intent = new Intent(context, ImportTokenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(C.IMPORT_STRING, importTxt);
        context.startActivity(intent);
    }

    public Token getToken(long chainId, String address)
    {
        return tokensService.getToken(chainId, address);
    }

    public TokensService getTokensService()
    {
        return tokensService;
    }

    public AssetDefinitionService getAssetDefinitionService()
    {
        return assetDefinitionService;
    }
}
