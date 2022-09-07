package com.pepperwallet.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.pepperwallet.app.steps.Steps.createNewWallet;
import static com.pepperwallet.app.steps.Steps.navigateToBrowser;
import static com.pepperwallet.app.steps.Steps.selectTestNet;
import static com.pepperwallet.app.steps.Steps.visit;
import static com.pepperwallet.app.util.Helper.waitUntil;

import com.pepperwallet.app.util.Helper;

import org.junit.Ignore;
import org.junit.Test;

public class DappBrowserTest extends BaseE2ETest
{

    @Test
    @Ignore
    public void should_switch_network()
    {
        String urlString = "https://opensea.io";

        createNewWallet();
        visit(urlString);
        onView(isRoot()).perform(waitUntil(withText("Ethereum"), 60));
        selectTestNet();
        navigateToBrowser();
        Helper.wait(3);
        pressBack();
        onView(isRoot()).perform(waitUntil(withText("Kovan"), 60));
    }
}
