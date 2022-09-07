package com.pepperwallet.app.di;

import com.pepperwallet.app.interact.ChangeTokenEnableInteract;
import com.pepperwallet.app.interact.CreateTransactionInteract;
import com.pepperwallet.app.interact.DeleteWalletInteract;
import com.pepperwallet.app.interact.ExportWalletInteract;
import com.pepperwallet.app.interact.FetchTokensInteract;
import com.pepperwallet.app.interact.FetchTransactionsInteract;
import com.pepperwallet.app.interact.FetchWalletsInteract;
import com.pepperwallet.app.interact.FindDefaultNetworkInteract;
import com.pepperwallet.app.interact.GenericWalletInteract;
import com.pepperwallet.app.interact.ImportWalletInteract;
import com.pepperwallet.app.interact.MemPoolInteract;
import com.pepperwallet.app.interact.SetDefaultWalletInteract;
import com.pepperwallet.app.interact.SignatureGenerateInteract;
import com.pepperwallet.app.repository.CurrencyRepository;
import com.pepperwallet.app.repository.CurrencyRepositoryType;
import com.pepperwallet.app.repository.EthereumNetworkRepositoryType;
import com.pepperwallet.app.repository.LocaleRepository;
import com.pepperwallet.app.repository.LocaleRepositoryType;
import com.pepperwallet.app.repository.PreferenceRepositoryType;
import com.pepperwallet.app.repository.TokenRepositoryType;
import com.pepperwallet.app.repository.TransactionRepositoryType;
import com.pepperwallet.app.repository.WalletRepositoryType;
import com.pepperwallet.app.router.ExternalBrowserRouter;
import com.pepperwallet.app.router.HomeRouter;
import com.pepperwallet.app.router.ImportTokenRouter;
import com.pepperwallet.app.router.ImportWalletRouter;
import com.pepperwallet.app.router.ManageWalletsRouter;
import com.pepperwallet.app.router.MyAddressRouter;
import com.pepperwallet.app.router.RedeemSignatureDisplayRouter;
import com.pepperwallet.app.router.SellDetailRouter;
import com.pepperwallet.app.router.TokenDetailRouter;
import com.pepperwallet.app.router.TransferTicketDetailRouter;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;

@Module
@InstallIn(ViewModelComponent.class)
/** Module for providing dependencies to viewModels.
 * All bindings of modules from BuildersModule is shifted here as they were injected in activity for ViewModelFactory but not needed in Hilt
 * */
public class ViewModelModule {

    @Provides
    FetchWalletsInteract provideFetchWalletInteract(WalletRepositoryType walletRepository) {
        return new FetchWalletsInteract(walletRepository);
    }

    @Provides
    SetDefaultWalletInteract provideSetDefaultAccountInteract(WalletRepositoryType accountRepository) {
        return new SetDefaultWalletInteract(accountRepository);
    }

    @Provides
    ImportWalletRouter provideImportAccountRouter() {
        return new ImportWalletRouter();
    }

    @Provides
    HomeRouter provideHomeRouter() {
        return new HomeRouter();
    }

    @Provides
    FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
            EthereumNetworkRepositoryType networkRepository) {
        return new FindDefaultNetworkInteract(networkRepository);
    }

    @Provides
    ImportWalletInteract provideImportWalletInteract(
            WalletRepositoryType walletRepository) {
        return new ImportWalletInteract(walletRepository);
    }

    @Provides
    ExternalBrowserRouter externalBrowserRouter() {
        return new ExternalBrowserRouter();
    }

    @Provides
    FetchTransactionsInteract provideFetchTransactionsInteract(TransactionRepositoryType transactionRepository,
                                                               TokenRepositoryType tokenRepositoryType) {
        return new FetchTransactionsInteract(transactionRepository, tokenRepositoryType);
    }

    @Provides
    CreateTransactionInteract provideCreateTransactionInteract(TransactionRepositoryType transactionRepository) {
        return new CreateTransactionInteract(transactionRepository);
    }

    @Provides
    MyAddressRouter provideMyAddressRouter() {
        return new MyAddressRouter();
    }

    @Provides
    FetchTokensInteract provideFetchTokensInteract(TokenRepositoryType tokenRepository) {
        return new FetchTokensInteract(tokenRepository);
    }

    @Provides
    SignatureGenerateInteract provideSignatureGenerateInteract(WalletRepositoryType walletRepository) {
        return new SignatureGenerateInteract(walletRepository);
    }

    @Provides
    MemPoolInteract provideMemPoolInteract(TokenRepositoryType tokenRepository) {
        return new MemPoolInteract(tokenRepository);
    }

    @Provides
    TransferTicketDetailRouter provideTransferTicketRouter() {
        return new TransferTicketDetailRouter();
    }

    @Provides
    LocaleRepositoryType provideLocaleRepository(PreferenceRepositoryType preferenceRepository) {
        return new LocaleRepository(preferenceRepository);
    }

    @Provides
    CurrencyRepositoryType provideCurrencyRepository(PreferenceRepositoryType preferenceRepository) {
        return new CurrencyRepository(preferenceRepository);
    }

    @Provides
    TokenDetailRouter provideErc20DetailRouterRouter() {
        return new TokenDetailRouter();
    }

    @Provides
    GenericWalletInteract provideGenericWalletInteract(WalletRepositoryType walletRepository) {
        return new GenericWalletInteract(walletRepository);
    }

    @Provides
    ChangeTokenEnableInteract provideChangeTokenEnableInteract(TokenRepositoryType tokenRepository) {
        return new ChangeTokenEnableInteract(tokenRepository);
    }

    @Provides
    ManageWalletsRouter provideManageWalletsRouter() {
        return new ManageWalletsRouter();
    }

    @Provides
    SellDetailRouter provideSellDetailRouter() {
        return new SellDetailRouter();
    }

    @Provides
    DeleteWalletInteract provideDeleteAccountInteract(
            WalletRepositoryType accountRepository) {
        return new DeleteWalletInteract(accountRepository);
    }

    @Provides
    ExportWalletInteract provideExportWalletInteract(
            WalletRepositoryType walletRepository) {
        return new ExportWalletInteract(walletRepository);
    }

    @Provides
    ImportTokenRouter provideImportTokenRouter() {
        return new ImportTokenRouter();
    }

    @Provides
    RedeemSignatureDisplayRouter provideRedeemSignatureDisplayRouter() {
        return new RedeemSignatureDisplayRouter();
    }
}
