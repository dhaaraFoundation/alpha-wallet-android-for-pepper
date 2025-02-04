package com.pepperwallet.app.ui;

import static com.pepperwallet.app.C.Key.WALLET;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pepperwallet.app.C;
import com.pepperwallet.app.R;
import com.pepperwallet.app.entity.Wallet;
import com.pepperwallet.app.entity.walletconnect.WalletConnectSessionItem;
import com.pepperwallet.app.repository.EthereumNetworkRepository;
import com.pepperwallet.app.ui.QRScanning.QRScanner;
import com.pepperwallet.app.viewmodel.WalletConnectViewModel;
import com.pepperwallet.app.widget.AWalletAlertDialog;
import com.bumptech.glide.Glide;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;


/**
 * Created by JB on 9/09/2020.
 */
@AndroidEntryPoint
public class WalletConnectSessionActivity extends BaseActivity
{
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LocalBroadcastManager broadcastManager;
    WalletConnectViewModel viewModel;
    private RecyclerView recyclerView;
    private Button btnConnectWallet;
    private LinearLayout layoutNoActiveSessions;
    private CustomAdapter adapter;
    private Wallet wallet;
    private List<WalletConnectSessionItem> wcSessions;
    private int connectionCount = -1;
    private final BroadcastReceiver walletConnectChangeReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(C.WALLET_CONNECT_COUNT_CHANGE))
            {
                handler.post(() -> adapter.notifyDataSetChanged());
                connectionCount = intent.getIntExtra("count", 0);
            }
        }
    };

    public WalletConnectSessionActivity()
    {
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallet_connect_sessions);
        toolbar();
        setTitle(getString(R.string.title_wallet_connect));
        wallet = getIntent().getParcelableExtra(WALLET);
        initViewModel();

        layoutNoActiveSessions = findViewById(R.id.layout_no_sessions);
        btnConnectWallet = findViewById(R.id.btn_connect_wallet);
        btnConnectWallet.setOnClickListener(v -> openQrScanner());
    }

    private void initViewModel()
    {
        if (viewModel == null)
        {
            viewModel = new ViewModelProvider(this)
                    .get(WalletConnectViewModel.class);
            viewModel.serviceReady().observe(this, this::onServiceReady);
        }
    }

    private void onServiceReady(Boolean aBoolean)
    {
        //refresh adapter
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
        else
        {
            setupList();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        stopConnectionCheck();
    }

    private void setupList()
    {
        wcSessions = viewModel.getSessions();

        if (wcSessions != null)
        {
            if (wcSessions.isEmpty())
            {
                layoutNoActiveSessions.setVisibility(View.VISIBLE);
                // remove ghosting when all items deleted
                if (recyclerView != null)
                {
                    RecyclerView.Adapter adapter = recyclerView.getAdapter();
                    if (adapter != null)
                    {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            else
            {
                layoutNoActiveSessions.setVisibility(View.GONE);
                recyclerView = findViewById(R.id.rv_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                adapter = new CustomAdapter();
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        connectionCount = -1;
        initViewModel();
        setupList();
        startConnectionCheck();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_wc_sessions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        else if (item.getItemId() == R.id.action_scan)
        {
            openQrScanner();
        }
        else if (item.getItemId() == R.id.action_delete)
        {
            View v = findViewById(R.id.action_delete);
            openDeleteMenu(v);
        }

        return super.onOptionsItemSelected(item);
    }

    private void openQrScanner()
    {
        Intent intent = new Intent(this, QRScanner.class);
        intent.putExtra("wallet", wallet);
        intent.putExtra(C.EXTRA_UNIVERSAL_SCAN, true);
        startActivity(intent);
    }

    private void openDeleteMenu(View v)
    {
        Timber.d("openDeleteMenu: view: %s", v);
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_wc_sessions_delete, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_empty)
            {
                // delete empty
                viewModel.removeEmptySessions(this, this::setupList);
                return true;
            }
            else if (item.getItemId() == R.id.action_delete_all)
            {
                Timber.d("openDeleteMenu: deleteAll: ");
                viewModel.removeAllSessions(this, this::setupList);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void setupClient(final String sessionId, final CustomAdapter.CustomViewHolder holder)
    {
        viewModel.getClient(this, sessionId, client -> handler.post(() -> {
            if (client == null || !client.isConnected())
            {
                holder.statusIcon.setVisibility(View.GONE);
            }
            else
            {
                holder.statusIcon.setVisibility(View.VISIBLE);
                holder.statusIcon.setImageResource(R.drawable.ic_connected);
            }
        }));
    }

    private void dialogConfirmDelete(WalletConnectSessionItem session)
    {
        AWalletAlertDialog dialog = new AWalletAlertDialog(this);
        dialog.setTitle(R.string.title_delete_session);
        dialog.setMessage(getString(R.string.delete_session, session.name));
        dialog.setButton(R.string.delete, v -> {
            dialog.dismiss();
            viewModel.deleteSession(session.sessionId);
            setupList();
        });
        dialog.setSecondaryButton(R.string.action_cancel, v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void startConnectionCheck()
    {
        broadcastManager.registerReceiver(walletConnectChangeReceiver, new IntentFilter(C.WALLET_CONNECT_COUNT_CHANGE));
    }

    private void stopConnectionCheck()
    {
        broadcastManager.unregisterReceiver(walletConnectChangeReceiver);
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>
    {
        @Override
        public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_wc_session, parent, false);

            return new CustomAdapter.CustomViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.CustomViewHolder holder, int position)
        {
            final WalletConnectSessionItem session = wcSessions.get(position);

            Glide.with(getApplication())
                    .load(session.icon)
                    .circleCrop()
                    .into(holder.icon);
            holder.peerName.setText(session.name);
            holder.peerUrl.setText(session.url);
            holder.chainIcon.setImageResource(EthereumNetworkRepository.getChainLogo(session.chainId));
            holder.clickLayer.setOnClickListener(v -> {
                //go to wallet connect session page
                Intent intent = new Intent(getApplication(), WalletConnectActivity.class);
                intent.putExtra("session", session.sessionId);
                startActivity(intent);
            });

            setupClient(session.sessionId, holder);

            holder.clickLayer.setOnLongClickListener(v -> {
                //delete this entry?
                dialogConfirmDelete(session);
                return true;
            });
        }

        @Override
        public int getItemCount()
        {
            return wcSessions.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder
        {
            final ImageView icon;
            final ImageView statusIcon;
            final TextView peerName;
            final TextView peerUrl;
            final LinearLayout clickLayer;
            final ImageView chainIcon;

            CustomViewHolder(View view)
            {
                super(view);
                icon = view.findViewById(R.id.icon);
                statusIcon = view.findViewById(R.id.status_icon);
                peerName = view.findViewById(R.id.session_name);
                peerUrl = view.findViewById(R.id.session_url);
                clickLayer = view.findViewById(R.id.item_layout);
                chainIcon = view.findViewById(R.id.status_chain_icon);
                chainIcon.setVisibility(View.VISIBLE);
                view.findViewById(R.id.chain_icon_background).setVisibility(View.VISIBLE);
            }
        }
    }
}
