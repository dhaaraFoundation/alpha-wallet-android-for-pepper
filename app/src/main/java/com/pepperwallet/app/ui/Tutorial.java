package com.pepperwallet.app.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pepperwallet.app.R;
import com.pepperwallet.app.router.HomeRouter;
import com.pepperwallet.app.ui.widget.adapter.OnBoardingAdapter;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Tutorial extends AppCompatActivity {



    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.skip)
    TextView skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorial);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        skip.setText(R.string.skip);

        try {
            updateStatusBarColorMainWhite("#FFFFFF");
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.skip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.skip:
                try {
                    new HomeRouter().open(this, true);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void initViews() {
        OnBoardingAdapter onBoardingAdapter = new OnBoardingAdapter(this);

        viewPager.setAdapter(onBoardingAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(viewListener);
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            try {
                if (i == 0) {
                    skip.setText(R.string.skip);
                } else if (i == 1) {
                    skip.setText(R.string.skip);
                } else if (i == 2) {
                    skip.setText(R.string.continue_);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    private void updatePageViews_OnViewPager(int page) {
        try {
            if (page == 0) {
                skip.setText(R.string.skip);
            } else if (page == 1) {
                skip.setText(R.string.skip);
            } else if (page == 2) {
                skip.setText(R.string.continue_);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @SuppressLint("ObsoleteSdkInt")
    public void updateStatusBarColorMainWhite(String color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor(color));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                int flagLightStatusBar = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                systemUiVisibility |= flagLightStatusBar;
                getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
