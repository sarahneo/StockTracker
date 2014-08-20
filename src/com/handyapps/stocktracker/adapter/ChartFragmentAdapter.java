package com.handyapps.stocktracker.adapter;

import java.util.List;

import com.handyapps.stocktracker.fragments.SingleChartFragment;
import com.handyapps.stocktracker.model.ChartInfoObject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ChartFragmentAdapter extends FragmentPagerAdapter{

	private List<ChartInfoObject> mChartInfoList;
    private int mCount;

    public ChartFragmentAdapter(FragmentManager fm, List<ChartInfoObject> clist) {
        super(fm);
        this.mChartInfoList = clist;
        this.mCount = clist.size();
    }

    @Override
    public Fragment getItem(int position) {
        return SingleChartFragment.newInstance(mChartInfoList.get(position % mChartInfoList.size()));
    }

    @Override
    public int getCount() {
        return this.mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mChartInfoList.get(position % mChartInfoList.size()).getDatePeriodUiShow();
    }

    public void setCount(int count) {
            this.mCount = count;
            notifyDataSetChanged();
    }
    
    public void setContentList(List<ChartInfoObject> clist) {
    	this.mChartInfoList = clist;
    	notifyDataSetChanged();
    }

}
