package com.example.anonycall.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.anonycall.R;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    public Integer[] mAvtIds = {
            R.drawable.avt1, R.drawable.avt2,
            R.drawable.avt3,R.drawable.avt4,
            R.drawable.avt5,R.drawable.avt6,
            R.drawable.avt7,R.drawable.avt8
    };

    public GridAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mAvtIds.length;
    }

    @Override
    public Object getItem(int position) {
        return mAvtIds[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mAvtIds[position]);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return imageView;
    }
}
