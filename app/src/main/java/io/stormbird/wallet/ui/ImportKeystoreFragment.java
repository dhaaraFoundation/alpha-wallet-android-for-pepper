package io.stormbird.wallet.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import io.stormbird.wallet.R;
import io.stormbird.wallet.ui.widget.OnImportKeystoreListener;
import io.stormbird.wallet.widget.InputView;
import io.stormbird.wallet.widget.PasswordInputView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImportKeystoreFragment extends Fragment implements View.OnClickListener, TextWatcher
{
    private static final OnImportKeystoreListener dummyOnImportKeystoreListener = (k, p) -> {};
    private static final String validator = "[^a-z^0-9^{^}^\"^:^,^-]";

    private PasswordInputView keystore;
    private PasswordInputView password;
    private Button importButton;
    private TextView passwordText;
    private Pattern pattern;
    @NonNull
    private OnImportKeystoreListener onImportKeystoreListener = dummyOnImportKeystoreListener;

    public static ImportKeystoreFragment create() {
        return new ImportKeystoreFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_import_keystore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        keystore = view.findViewById(R.id.input_keystore);
        password = view.findViewById(R.id.input_password);
        passwordText = view.findViewById(R.id.text_password_notice);
        passwordText.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        importButton = view.findViewById(R.id.import_action);//.setOnClickListener(this);
        importButton.setOnClickListener(this);
        updateButtonState(false);
        keystore.getEditText().addTextChangedListener(this);
        password.getEditText().addTextChangedListener(this);

        pattern = Pattern.compile(validator, Pattern.MULTILINE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed())
        {
            if (isVisibleToUser) reset();
        }
    }

    private void updateButtonState(boolean enabled)
    {
        importButton.setActivated(enabled);
        importButton.setClickable(enabled);
        int colorId = enabled ? R.color.nasty_green : R.color.inactive_green;
        if (getContext() != null) importButton.setBackgroundColor(getContext().getColor(colorId));
    }

    @Override
    public void onClick(View view) {
        if (password.getVisibility() == View.GONE)
        {
            keystore.setVisibility(View.GONE);
            password.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            updateButtonState(false);
        }
        else
        {
            String keystore = this.keystore.getText().toString();
            String password = this.password.getText().toString();
            onImportKeystoreListener.onKeystore(keystore, password);
        }
    }

    public void setOnImportKeystoreListener(@Nullable OnImportKeystoreListener onImportKeystoreListener) {
        this.onImportKeystoreListener = onImportKeystoreListener == null
            ? dummyOnImportKeystoreListener
            : onImportKeystoreListener;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        this.keystore.setError(null);
        if (password.getVisibility() == View.GONE)
        {
            String txt = keystore.getText().toString();
            if (txt.length() > 0)
            {
                boolean validKeystore = true;
                //first check
                final Matcher matcher = pattern.matcher(txt);
                if (matcher.find()) validKeystore = false;

                if (!validKeystore)
                {
                    keystore.setError("Keystore file contains invalid characters");
                }

                if (txt.length() < 10) validKeystore = false;
                updateButtonState(validKeystore);
            }
            else
            {
                updateButtonState(false);
            }
        }
        else
        {
            String txt = password.getText().toString();
            if (txt.length() >= 6)
            {
                updateButtonState(true);
            }
            else
            {
                updateButtonState(false);
            }
        }
    }

    public void reset()
    {
        password.setText("");
        password.setVisibility(View.GONE);
        updateButtonState(false);
        keystore.setVisibility(View.VISIBLE);
        keystore.setError(null);
        keystore.setText("");
        passwordText.setVisibility(View.GONE);
    }
}
