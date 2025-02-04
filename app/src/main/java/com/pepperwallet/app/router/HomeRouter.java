package com.pepperwallet.app.router;

import android.content.Context;
import android.content.Intent;

import com.pepperwallet.app.C;
import com.pepperwallet.app.ui.HomeActivity;
import com.pepperwallet.app.ui.Tutorial;

public class HomeRouter {
    public void open(Context context, boolean isClearStack) {

        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(C.FROM_HOME_ROUTER, C.FROM_HOME_ROUTER); //HomeRouter should restart the app at the wallet
//        if (isClearStack) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        }
        context.startActivity(intent);
    }

    public void open(Context context,boolean isClearStack,Context cc) {
        Intent intent = new Intent(context, Tutorial.class);
        intent.putExtra(C.FROM_HOME_ROUTER, C.FROM_HOME_ROUTER); //HomeRouter should restart the app at the wallet
//        if (isClearStack) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        }
        context.startActivity(intent);
    }
}


//    public void open(Context context, boolean isClearStack) {
//        Intent intent = new Intent(context, HomeActivity.class);
//        intent.putExtra(C.FROM_HOME_ROUTER, C.FROM_HOME_ROUTER); //HomeRouter should restart the app at the wallet
//        if (isClearStack) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        }
//        context.startActivity(intent);
//    }
//
//    public void open(Context context,boolean isClearStack,Context cc) {
//        Intent intent = new Intent(context, Tutorial.class);
//        intent.putExtra(C.FROM_HOME_ROUTER, C.FROM_HOME_ROUTER); //HomeRouter should restart the app at the wallet
//        if (isClearStack) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        }
//        context.startActivity(intent);
//    }
//}
