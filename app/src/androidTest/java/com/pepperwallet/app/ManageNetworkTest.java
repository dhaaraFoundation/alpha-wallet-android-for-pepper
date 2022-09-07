package com.pepperwallet.app;

import static com.pepperwallet.app.assertions.Should.shouldSee;
import static com.pepperwallet.app.steps.Steps.addNewNetwork;
import static com.pepperwallet.app.steps.Steps.createNewWallet;
import static com.pepperwallet.app.steps.Steps.gotoSettingsPage;

import org.junit.Ignore;
import org.junit.Test;

public class ManageNetworkTest extends BaseE2ETest
{
    @Test
    @Ignore
    public void should_add_custom_network()
    {
        createNewWallet();
        gotoSettingsPage();
        addNewNetwork("MyTestNet");
        shouldSee("MyTestNet");
    }
}
