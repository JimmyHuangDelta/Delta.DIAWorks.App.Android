package com.delta.android.PMS.Client.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class WorkPmInfoFragment extends DialogFragment {

    private FragmentTabHost mTabHost;
    private ViewPager viewPager;
    private PmInfoPagerAdapter adapter;
    private String strWoId;

    public static WorkPmInfoFragment newInstance() {
        WorkPmInfoFragment fragment = new WorkPmInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        strWoId = getArguments().getString("WO_ID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setLayoutParams(params);
        frameLayout.setEnabled(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.style_pms_pm_infomation, container);

        mTabHost = (FragmentTabHost) view.findViewById(R.id.tabs);
        mTabHost.setup(getActivity(), getChildFragmentManager());
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getString(R.string.PM_METHOD)), Fragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getString(R.string.PM_FIXT_TOOL)), Fragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getString(R.string.PM_CONSUMABLE)), Fragment.class, null);

        adapter = new PmInfoPagerAdapter(getChildFragmentManager(), getArguments());
        adapter.setTitles(new String[]{"PM_METHOD", "PM_FIXT_TOOL", "PM_CONSUMABLE"});

        viewPager = (ViewPager)view.findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                int i = mTabHost.getCurrentTab();
                viewPager.setCurrentItem(i);
            }
        });

        frameLayout.addView(view);
        return frameLayout;
    }

    public class PmInfoPagerAdapter extends FragmentPagerAdapter {

        Bundle bundle;
        String [] titles = {"PM_METHOD", "PM_FIXT_TOOL", "PM_CONSUMABLE"};

        public PmInfoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public PmInfoPagerAdapter(FragmentManager fm, Bundle bundle) {
            super(fm);
            this.bundle = bundle;
        }

        @Override
        public Fragment getItem(int num) {
            Fragment fragment = new PmInfoFragment();
            Bundle args = new Bundle();
            args.putString("WO_ID", strWoId);
            args.putString("TITLE", titles[num]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        public void setTitles(String[] titles) {
            this.titles = titles;
        }
    }

    public static class PmInfoFragment extends Fragment {

        private String woId;
        private String title;
        Data offData;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            woId = getArguments().getString("WO_ID");
            title = getArguments().getString("TITLE");
            offData = new Data(getContext());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(params);
            frameLayout.setEnabled(false);

            View viewlist = getLayoutInflater().inflate(R.layout.activity_pms_pm_check_item, null);
            ListView lsView = (ListView) viewlist.findViewById(R.id.lvWorkCheckItem);
            SQLiteDatabase db = offData.getReadableDatabase();
            LayoutInflater layoutInflater = getLayoutInflater();
            String TableName = "";

            switch (title) {
                case "PM_METHOD":
                    TableName = "SBRM_EMS_CHECK_METHOD";
                    Cursor CursorMETHOD = db.query(TableName, null, "MRO_WO_ID = '" + woId + "'", null, null, null, null);
                    METHODAdapter methodAdapter = new METHODAdapter(layoutInflater, CursorMETHOD);
                    lsView.setAdapter(methodAdapter);
                    frameLayout.addView(viewlist);
                    break;
                case "PM_FIXT_TOOL":
                    TableName = "SBRM_EMS_CHECK_FIX_TOOL";
                    Cursor CursorFIXTOOL = db.query(TableName, null, "MRO_WO_ID = '" + woId + "'", null, null, null, null);
                    FIXTOOLAdapter fixtoolAdapter = new FIXTOOLAdapter(layoutInflater, CursorFIXTOOL);
                    lsView.setAdapter(fixtoolAdapter);
                    frameLayout.addView(viewlist);
                    break;
                case "PM_CONSUMABLE":
                    TableName = "SBRM_EMS_CHECK_CONSUMABLE";
                    Cursor CursorCONSUMABLE = db.query(TableName, null, "MRO_WO_ID = '" + woId + "'", null, null, null, null);
                    CONSUMABLEAdapter consumableAdapter = new CONSUMABLEAdapter(layoutInflater, CursorCONSUMABLE);
                    lsView.setAdapter(consumableAdapter);
                    frameLayout.addView(viewlist);
                    break;
            }

            return frameLayout;
        }
    }

    public static class METHODAdapter extends BaseAdapter {

        private Cursor dtData;//定义数据。
        private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
        ArrayList<HashMap<String, String>> arData = new ArrayList<>();

        public METHODAdapter(LayoutInflater inflater, Cursor data) {
            mInflater = inflater;
            if (data.getCount() != 0)
            {
                dtData = data;
                dtData.moveToFirst();
                arData = new ArrayList<>();
                do{// 逐筆讀出資料
                    HashMap<String, String> checkItem = new HashMap<>();
                    checkItem.put("PM_METHOD_ID", dtData.getString(1));//PM_METHOD_ID
                    checkItem.put("PM_METHOD_NAME", dtData.getString(2));//PM_METHOD_NAME
                    checkItem.put("CHECK_ID", dtData.getString(3));//CHECK_ID
                    checkItem.put("CHECK_NAME", dtData.getString(4));//CHECK_NAME
                    checkItem.put("MRO_WO_ID", dtData.getString(5));//MRO_WO_ID
                    arData.add(checkItem);
                } while(dtData.moveToNext());    // 有一下筆就繼續迴圈
            }
        }

        @Override
        public int getCount() {
            return arData.size();
        }

        @Override
        public Object getItem(int position) {
            return dtData.moveToPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vCheckItem = mInflater.inflate(R.layout.activity_pms_pm_check_info_listview, null);
            TextView tvTitleID = (TextView) vCheckItem.findViewById(R.id.tvTitleID);
            TextView tvtitleContent = (TextView) vCheckItem.findViewById(R.id.tvtitleContent);
            tvTitleID.setText(R.string.PM_METHOD_ID);
            tvtitleContent.setText(R.string.PM_METHOD);

            TextView tvmehtodID = (TextView) vCheckItem.findViewById(R.id.tvpmID);
            TextView tvmethodName = (TextView) vCheckItem.findViewById(R.id.tvpmName);

            tvmehtodID.setText(arData.get(position).get("PM_METHOD_ID"));
            tvmethodName.setText(arData.get(position).get("PM_METHOD_NAME"));

            return vCheckItem;
        }
    }

    public static class FIXTOOLAdapter extends BaseAdapter {

        private Cursor dtData;//定义数据。
        private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
        ArrayList<HashMap<String, String>> arData = new ArrayList<>();

        public FIXTOOLAdapter(LayoutInflater inflater, Cursor data) {
            mInflater = inflater;
            if (data.getCount() != 0)
            {
                dtData = data;
                dtData.moveToFirst();
                arData = new ArrayList<>();
                do{// 逐筆讀出資料
                    HashMap<String, String> checkItem = new HashMap<>();
                    checkItem.put("FIX_TOOL_ID", dtData.getString(1));//FIX_TOOL_ID
                    checkItem.put("FIX_TOOL_NAME", dtData.getString(2));//FIX_TOOL_NAME
                    checkItem.put("CHECK_ID", dtData.getString(3));//CHECK_ID
                    checkItem.put("CHECK_NAME", dtData.getString(4));//CHECK_NAME
                    checkItem.put("MRO_WO_ID", dtData.getString(5));//MRO_WO_ID
                    arData.add(checkItem);
                } while(dtData.moveToNext());    // 有一下筆就繼續迴圈
            }
        }

        @Override
        public int getCount() {
            return arData.size();
        }

        @Override
        public Object getItem(int position) {
            return dtData.moveToPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vCheckItem = mInflater.inflate(R.layout.activity_pms_pm_check_info_listview, null);
            TextView tvTitleID = (TextView) vCheckItem.findViewById(R.id.tvTitleID);
            TextView tvtitleContent = (TextView) vCheckItem.findViewById(R.id.tvtitleContent);
            tvTitleID.setText(R.string.PM_FIXT_TOOL_ID);
            tvtitleContent.setText(R.string.PM_FIXT_TOOL);

            TextView tvmehtodID = (TextView) vCheckItem.findViewById(R.id.tvpmID);
            TextView tvmethodName = (TextView) vCheckItem.findViewById(R.id.tvpmName);

            tvmehtodID.setText(arData.get(position).get("FIX_TOOL_ID"));
            tvmethodName.setText(arData.get(position).get("FIX_TOOL_NAME"));

            return vCheckItem;
        }
    }

    public static class CONSUMABLEAdapter extends BaseAdapter {

        private Cursor dtData;//定义数据。
        private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
        ArrayList<HashMap<String, String>> arData = new ArrayList<>();

        public CONSUMABLEAdapter(LayoutInflater inflater, Cursor data) {
            mInflater = inflater;
            if (data.getCount() != 0)
            {
                dtData = data;
                dtData.moveToFirst();
                arData = new ArrayList<>();
                do{// 逐筆讀出資料
                    HashMap<String, String> checkItem = new HashMap<>();
                    checkItem.put("CONSUMABLE_LIST_ID", dtData.getString(1));//CONSUMABLE_LIST_ID
                    checkItem.put("CONSUMABLE_LIST_NAME", dtData.getString(2));//CONSUMABLE_LIST_NAME
                    checkItem.put("CONSUMABLE_TYPE_ID", dtData.getString(3));//CONSUMABLE_TYPE_ID
                    checkItem.put("CONSUMABLE_TYPE_NAME", dtData.getString(4));//CONSUMABLE_TYPE_NAME
                    checkItem.put("CHECK_ID", dtData.getString(5));//CHECK_ID
                    checkItem.put("CHECK_NAME", dtData.getString(6));//CHECK_NAME
                    checkItem.put("MRO_WO_ID", dtData.getString(7));//MRO_WO_ID
                    arData.add(checkItem);
                } while(dtData.moveToNext());    // 有一下筆就繼續迴圈
            }
        }

        @Override
        public int getCount() {
            return arData.size();
        }

        @Override
        public Object getItem(int position) {
            return dtData.moveToPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vCheckItem = mInflater.inflate(R.layout.activity_pms_pm_check_info_listview2, null);
            TextView tvTitleID = (TextView) vCheckItem.findViewById(R.id.tvTitleID);
            TextView tvtitleContent = (TextView) vCheckItem.findViewById(R.id.tvtitleContent);
            TextView tvTitle2ID = (TextView) vCheckItem.findViewById(R.id.tvTitle2ID);
            TextView tvtitle2Content = (TextView) vCheckItem.findViewById(R.id.tvtitle2Content);
            tvTitleID.setText(R.string.PM_CONSUMABLE_LIST_ID);
            tvtitleContent.setText(R.string.PM_CONSUMABLE_LIST);
            tvTitle2ID.setText(R.string.PM_CONSUMABLE_TYPE_ID);
            tvtitle2Content.setText(R.string.PM_CONSUMABLE_TYPE_NAME);

            TextView tvmehtodID = (TextView) vCheckItem.findViewById(R.id.tvpmID);
            TextView tvmethodName = (TextView) vCheckItem.findViewById(R.id.tvpmName);
            TextView tvmehtod2ID = (TextView) vCheckItem.findViewById(R.id.tvpm2ID);
            TextView tvmethod2Name = (TextView) vCheckItem.findViewById(R.id.tvpm2Name);

            tvmehtodID.setText(arData.get(position).get("CONSUMABLE_LIST_ID"));
            tvmethodName.setText(arData.get(position).get("CONSUMABLE_LIST_NAME"));
            tvmehtod2ID.setText(arData.get(position).get("CONSUMABLE_TYPE_ID"));
            tvmethod2Name.setText(arData.get(position).get("CONSUMABLE_TYPE_NAME"));

            return vCheckItem;
        }
    }
}
