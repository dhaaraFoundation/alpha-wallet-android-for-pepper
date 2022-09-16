package com.pepperwallet.app.ui.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pepperwallet.app.R;

public class OnBoardingAdapter extends PagerAdapter {

    private Context context;

    private TextView title_tv, desc_tv;
    private ImageView image_view;

    private String[] slide_title, slide_desc;
    private int[] images;

    public OnBoardingAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.tutorial_adapter, container, false);

        slide_title = new String[] {
                context.getString(R.string.tutorial_title_1),
                context.getString(R.string.tutorial_title_2),
                context.getString(R.string.tutorial_title_3)
        };

        slide_desc = new String[] {
                context.getString(R.string.tutorial_desc_1),
                context.getString(R.string.tutorial_desc_2),
                context.getString(R.string.tutorial_desc_3),
        };

        images = new int[] {
                R.drawable.ic_tutorial_1,
                R.drawable.ic_tutorial_2,
                R.drawable.ic_tutorial_3
        };

        title_tv = view.findViewById(R.id.title);
        desc_tv = view.findViewById(R.id.desc);
        image_view = view.findViewById(R.id.images);

        title_tv.setText(slide_title[position]);
        desc_tv.setText(slide_desc[position]);
        image_view.setImageResource(images[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ViewGroup) object);
    }

}

