package com.pepperwallet.app.di;

import android.content.Context;

import com.pepperwallet.app.repository.EthereumNetworkRepository;
import com.pepperwallet.app.repository.EthereumNetworkRepositoryType;
import com.pepperwallet.app.repository.OnRampRepository;
import com.pepperwallet.app.repository.OnRampRepositoryType;
import com.pepperwallet.app.repository.PreferenceRepositoryType;
import com.pepperwallet.app.repository.SharedPreferenceRepository;
import com.pepperwallet.app.repository.TokenLocalSource;
import com.pepperwallet.app.repository.TokenRepository;
import com.pepperwallet.app.repository.TokenRepositoryType;
import com.pepperwallet.app.repository.TokensRealmSource;
import com.pepperwallet.app.repository.TransactionLocalSource;
import com.pepperwallet.app.repository.TransactionRepository;
import com.pepperwallet.app.repository.TransactionRepositoryType;
import com.pepperwallet.app.repository.TransactionsRealmCache;
import com.pepperwallet.app.repository.WalletDataRealmSource;
import com.pepperwallet.app.repository.WalletRepository;
import com.pepperwallet.app.repository.WalletRepositoryType;
import com.pepperwallet.app.service.AccountKeystoreService;
import com.pepperwallet.app.service.AlphaWalletService;
import com.pepperwallet.app.service.AnalyticsService;
import com.pepperwallet.app.service.AnalyticsServiceType;
import com.pepperwallet.app.service.AssetDefinitionService;
import com.pepperwallet.app.service.GasService;
import com.pepperwallet.app.service.KeyService;
import com.pepperwallet.app.service.KeystoreAccountService;
import com.pepperwallet.app.service.NotificationService;
import com.pepperwallet.app.service.OpenSeaService;
import com.pepperwallet.app.service.RealmManager;
import com.pepperwallet.app.service.SwapService;
import com.pepperwallet.app.service.TickerService;
import com.pepperwallet.app.service.TokensService;
import com.pepperwallet.app.service.TransactionsNetworkClient;
import com.pepperwallet.app.service.TransactionsNetworkClientType;
import com.pepperwallet.app.service.TransactionsService;
import com.google.gson.Gson;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

import static com.pepperwallet.app.service.KeystoreAccountService.KEYSTORE_FOLDER;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoriesModule {
	@Singleton
	@Provides
	PreferenceRepositoryType providePreferenceRepository(@ApplicationContext Context context) {
		return new SharedPreferenceRepository(context);
	}

	@Singleton
	@Provides
    AccountKeystoreService provideAccountKeyStoreService(@ApplicationContext Context context, KeyService keyService) {
        File file = new File(context.getFilesDir(), KEYSTORE_FOLDER);
		return new KeystoreAccountService(file, context.getFilesDir(), keyService);
	}

	@Singleton
    @Provides
	TickerService provideTickerService(OkHttpClient httpClient, PreferenceRepositoryType sharedPrefs, TokenLocalSource localSource) {
		return new TickerService(httpClient, sharedPrefs, localSource);
    }

	@Singleton
	@Provides
	EthereumNetworkRepositoryType provideEthereumNetworkRepository(
            PreferenceRepositoryType preferenceRepository,
			@ApplicationContext Context context) {
		return new EthereumNetworkRepository(preferenceRepository, context);
	}

	@Singleton
	@Provides
    WalletRepositoryType provideWalletRepository(
			PreferenceRepositoryType preferenceRepositoryType,
			AccountKeystoreService accountKeystoreService,
			EthereumNetworkRepositoryType networkRepository,
			WalletDataRealmSource walletDataRealmSource,
			KeyService keyService) {
		return new WalletRepository(
		        preferenceRepositoryType, accountKeystoreService, networkRepository, walletDataRealmSource, keyService);
	}

	@Singleton
	@Provides
	TransactionRepositoryType provideTransactionRepository(
			EthereumNetworkRepositoryType networkRepository,
			AccountKeystoreService accountKeystoreService,
            TransactionLocalSource inDiskCache,
			TransactionsService transactionsService) {
		return new TransactionRepository(
				networkRepository,
				accountKeystoreService,
				inDiskCache,
				transactionsService);
	}

	@Singleton
	@Provides
	OnRampRepositoryType provideOnRampRepository(@ApplicationContext Context context, AnalyticsServiceType analyticsServiceType) {
		return new OnRampRepository(context, analyticsServiceType);
	}

	@Singleton
    @Provides
    TransactionLocalSource provideTransactionInDiskCache(RealmManager realmManager) {
        return new TransactionsRealmCache(realmManager);
    }

	@Singleton
	@Provides
    TransactionsNetworkClientType provideBlockExplorerClient(
			OkHttpClient httpClient,
			Gson gson,
			RealmManager realmManager) {
		return new TransactionsNetworkClient(httpClient, gson, realmManager);
	}

	@Singleton
    @Provides
    TokenRepositoryType provideTokenRepository(
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            TokenLocalSource tokenLocalSource,
			OkHttpClient httpClient,
			@ApplicationContext Context context,
			TickerService tickerService) {
	    return new TokenRepository(
	            ethereumNetworkRepository,
				tokenLocalSource,
				httpClient,
				context,
				tickerService);
    }

    @Singleton
    @Provides
    TokenLocalSource provideRealmTokenSource(RealmManager realmManager, EthereumNetworkRepositoryType ethereumNetworkRepository) {
	    return new TokensRealmSource(realmManager, ethereumNetworkRepository);
    }

	@Singleton
	@Provides
	WalletDataRealmSource provideRealmWalletDataSource(RealmManager realmManager) {
		return new WalletDataRealmSource(realmManager);
	}

	@Singleton
	@Provides
	TokensService provideTokensService(EthereumNetworkRepositoryType ethereumNetworkRepository,
									   TokenRepositoryType tokenRepository,
									   TickerService tickerService,
									   OpenSeaService openseaService,
									   AnalyticsServiceType analyticsService) {
		return new TokensService(ethereumNetworkRepository, tokenRepository, tickerService, openseaService, analyticsService);
	}

	@Singleton
	@Provides
	TransactionsService provideTransactionsService(TokensService tokensService,
												   EthereumNetworkRepositoryType ethereumNetworkRepositoryType,
												   TransactionsNetworkClientType transactionsNetworkClientType,
												   TransactionLocalSource transactionLocalSource) {
		return new TransactionsService(tokensService, ethereumNetworkRepositoryType, transactionsNetworkClientType, transactionLocalSource);
	}

	@Singleton
	@Provides
    GasService provideGasService(EthereumNetworkRepositoryType ethereumNetworkRepository, OkHttpClient client, RealmManager realmManager) {
		return new GasService(ethereumNetworkRepository, client, realmManager);
	}

	@Singleton
	@Provides
	OpenSeaService provideOpenseaService() {
		return new OpenSeaService();
	}

	@Singleton
	@Provides
	SwapService provideSwapService() {
		return new SwapService();
	}

	@Singleton
	@Provides
    AlphaWalletService provideFeemasterService(OkHttpClient okHttpClient,
                                               TransactionRepositoryType transactionRepository,
                                               Gson gson) {
		return new AlphaWalletService(okHttpClient, transactionRepository, gson);
	}

	@Singleton
	@Provides
    NotificationService provideNotificationService(@ApplicationContext Context ctx) {
		return new NotificationService(ctx);
	}

	@Singleton
	@Provides
    AssetDefinitionService provideAssetDefinitionService(OkHttpClient okHttpClient, @ApplicationContext Context ctx, NotificationService notificationService, RealmManager realmManager,
														 TokensService tokensService, TokenLocalSource tls, TransactionRepositoryType trt,
														 AlphaWalletService alphaService) {
		return new AssetDefinitionService(okHttpClient, ctx, notificationService, realmManager, tokensService, tls, trt, alphaService);
	}

	@Singleton
	@Provides
	KeyService provideKeyService(@ApplicationContext Context ctx, AnalyticsServiceType analyticsService) {
		return new KeyService(ctx, analyticsService);
	}

	@Singleton
	@Provides
	AnalyticsServiceType provideAnalyticsService(@ApplicationContext Context ctx) {
		return new AnalyticsService(ctx);
	}
}
