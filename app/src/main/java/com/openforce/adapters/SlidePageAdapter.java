package com.openforce.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.openforce.R;

public class SlidePageAdapter extends PagerAdapter {

    private Context context;
    private OnPageClickedListener onItemClickListener;

    public SlidePageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        ImageView itemView = (ImageView) LayoutInflater.from(context).inflate(R.layout.pager_image_slide, container, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onPageClicked(position);
                }
            }
        });

        switch (position) {
            case 0:
                itemView.setImageResource(R.drawable.experience_skills);
                break;
            case 1:
                itemView.setImageResource(R.drawable.identity_references);
                break;
            case 2:
                itemView.setImageResource(R.drawable.secure_account);
                break;
            case 3:
                itemView.setImageResource(R.drawable.payment_information);
                break;
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

    @Override
    public float getPageWidth(int position) {
        return 0.66f;
    }

    public void setOnItemClickListener(OnPageClickedListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnPageClickedListener {

        void onPageClicked(int position);
    }
}
