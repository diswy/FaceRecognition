package com.baidu.idl.sample.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by litonghui on 2018/11/29.
 */

public class CameraPagerAdapter extends PagerAdapter {
    private List<View> mViewList;

    // 也可以重写构造器，传入Context，然后在PagerAdapter
    // 中初始话Layout布局，感觉这种通用一些
    public CameraPagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public int getCount() {
        // 返回有效的View的个数
        return mViewList.size();
    }

    // 判断是否page view与 instantiateItem(ViewGroup, int)返回的object的key 是否相同，以提供给其他的函数使用
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // instantiateItem该方法的功能是创建指定位置的页面视图。finishUpdate(ViewGroup)返回前，页面应该保证被构造好
    // 返回值：返回一个对应该页面的object，这个不一定必须是View，但是应该是对应页面的一些其他容器
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    // 该方法的功能是移除一个给定位置的页面。
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
