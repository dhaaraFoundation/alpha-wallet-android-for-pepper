package com.pepperwallet.app.viewmodel;

import com.pepperwallet.app.entity.NetworkInfo;
import com.pepperwallet.app.repository.EthereumNetworkRepositoryType;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NodeStatusViewModel extends BaseViewModel {

    private final EthereumNetworkRepositoryType networkRepository;

    @Inject
    public NodeStatusViewModel(EthereumNetworkRepositoryType ethereumNetworkRepositoryType)
    {
        this.networkRepository = ethereumNetworkRepositoryType;
    }

    public NetworkInfo[] getNetworkList()
    {
        return networkRepository.getAvailableNetworkList();
    }
}
