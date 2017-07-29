package com.mingbikes.eplate.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingbikes.eplate.R;
import com.mingbikes.eplate.entity.BrandEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cronus-tropix on 17/7/29.
 */
public class BrandAdapter extends BaseAdapter {

    List<BrandEntity> mBrandList = new ArrayList<>();
    private LayoutInflater mInflater;

    public BrandAdapter(Context context) {

        mInflater = LayoutInflater.from(context);
    }

    public void addList(List<BrandEntity> _brandList) {
        mBrandList.clear();
        mBrandList.addAll(_brandList);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBrandList.size();
    }

    @Override
    public BrandEntity getItem(int position) {
        return mBrandList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_brand, null, false);

            viewHolder = new ViewHolder();

            viewHolder.iv_logo = (ImageView) convertView.findViewById(R.id.iv_logo);
            viewHolder.tv_brand_name = (TextView) convertView.findViewById(R.id.tv_brand_name);
            viewHolder.tv_brand_bike_count_in_park_space = (TextView) convertView.findViewById(R.id.tv_brand_bike_count_in_park_space);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BrandEntity brandEntity = mBrandList.get(position);

        if(brandEntity.getName().equals("OFO")) {
            viewHolder.iv_logo.setBackgroundResource(R.drawable.ofo);
        } else if (brandEntity.getName().equals("摩拜")) {
            viewHolder.iv_logo.setBackgroundResource(R.drawable.mobike);
        } else {
            viewHolder.iv_logo.setBackgroundResource(R.drawable.xiaoming);
        }

        viewHolder.tv_brand_name.setText(brandEntity.getName());
        viewHolder.tv_brand_bike_count_in_park_space.setText(brandEntity.getParkCount() + "");

        return convertView;
    }

    class ViewHolder {
        ImageView iv_logo;
        TextView tv_brand_name;
        TextView tv_brand_bike_count_in_park_space;
    }

}
