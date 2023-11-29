package com.delta.android.WMS.Client;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.WarehouseStorageNonSheetDataFragment;
import com.delta.android.WMS.Client.Fragment.WarehouseStorageNonSheetListFragment;

import java.util.List;

public class WarehouseStorageNonSheetDetailNewActivity extends BaseFlowActivity implements WarehouseStorageNonSheetDataFragment.ISendNonSheetData,
                                                                                        WarehouseStorageNonSheetListFragment.IModifyNonSheetData{

    // region -- 控制項 / Widgets --
    private WarehouseStorageNonSheetDataFragment warehouseStorageNonSheetDataFragment = new WarehouseStorageNonSheetDataFragment(); // 入庫頁籤/ Tab WarehouseStorage
    private WarehouseStorageNonSheetListFragment warehouseStorageNonSheetListFragment = new WarehouseStorageNonSheetListFragment(); // 入庫明細頁籤 Tab WarehouseStorageDetail
    private int currentPage = 0; // 紀錄目前所在頁籤的位置/Record the position of the current tab
    private TabLayout tabLayout;
    //

    // region 全域變數/global variables
    public String sheetTypeId = ""; // 前一頁傳來/From first page
    public String organId = ""; // 前一頁傳來/From first page
    public String wvSource = ""; // 前一頁傳來/From first page
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_warehouse_storage_non_sheet_detail_new);

        // 取得前一頁(WarehouseStorageNonSheetNewActivity)傳來的資料
        sheetTypeId = getIntent().getStringExtra("sheetTypeId");
        organId = getIntent().getStringExtra("organId");
        wvSource = getIntent().getStringExtra("wvSource");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentWarehouseStorageNonSheet, warehouseStorageNonSheetDataFragment, "WarehouseStorageNonSheetData");
        fragmentTransaction.add(R.id.fragmentWarehouseStorageNonSheet, warehouseStorageNonSheetListFragment, "WarehouseStorageNonSheetList");

        fragmentTransaction.hide(warehouseStorageNonSheetListFragment);
        fragmentTransaction.commit();

        setInitWidget();

        setListener();
    }

    /**
     * 元件初始化
     * Component initialization
     */
    private void setInitWidget() {
        tabLayout = findViewById(R.id.tabLayout);
    }

    /**
     *  設置監聽事件
     *  Set up listening events
     */
    private void setListener() {
        tabLayout.addOnTabSelectedListener(onTabSelect);
    }

    /**
     * 點選 Tab 頁籤切換 Fragment
     * Click Tab tab to switch Fragment
     */
    TabLayout.OnTabSelectedListener onTabSelect = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            fragmentChange(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    /**
     * 透過 Tab 點選的位置切換欲顯示的 Fragment
     * Switch the Fragment to be displayed by clicking the position of Tab
     * @param position
     */
    private void fragmentChange(int position){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (currentPage){

            case 0:
                fragmentTransaction.hide(warehouseStorageNonSheetDataFragment);
                break;

            case 1:
                fragmentTransaction.hide(warehouseStorageNonSheetListFragment);
                break;
        }

        switch (position){

            case 0:
                fragmentTransaction.show(warehouseStorageNonSheetDataFragment);
                break;

            case 1:
                fragmentTransaction.show(warehouseStorageNonSheetListFragment);
                break;
        }

        fragmentTransaction.commit();

        currentPage = position;
    }

    @Override
    public void sendWvrDet(DataTable dtWvrDet, DataTable dtWvrDetGroup) {
        WarehouseStorageNonSheetListFragment fragment = (WarehouseStorageNonSheetListFragment) getSupportFragmentManager().findFragmentByTag("WarehouseStorageNonSheetList");
        fragment.getWvrDet(dtWvrDet, dtWvrDetGroup);
    }

    @Override
    public void modifyDrWvrDet(List<String> lstExistSkuNum) {
        WarehouseStorageNonSheetDataFragment fragment = (WarehouseStorageNonSheetDataFragment) getSupportFragmentManager().findFragmentByTag("WarehouseStorageNonSheetData");
        fragment.setExistSkuNum(lstExistSkuNum);
    }
}
