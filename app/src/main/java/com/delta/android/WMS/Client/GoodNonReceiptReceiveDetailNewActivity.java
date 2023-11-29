package com.delta.android.WMS.Client;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.GoodNonReceiptReceiveDataFragment;
import com.delta.android.WMS.Client.Fragment.GoodNonReceiptReceiveListFragment;

import java.util.List;

public class GoodNonReceiptReceiveDetailNewActivity extends BaseFlowActivity implements GoodNonReceiptReceiveDataFragment.ISendNonReceiptReceiveDataTable,
                                                                                     GoodNonReceiptReceiveListFragment.IModifyNonReceiptReceiveDataTable {

    private GoodNonReceiptReceiveDataFragment receiveDataFragment = new GoodNonReceiptReceiveDataFragment();; // 第一個頁籤，Position = 0
    private GoodNonReceiptReceiveListFragment receiveListFragment = new GoodNonReceiptReceiveListFragment(); // 第二個頁籤，Position = 1
    private int currentPage = 0; // 記錄目前所在頁籤的位置
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_non_receipt_receive_detail_new);

        DataTable dtReceiptMst = (DataTable) getIntent().getSerializableExtra("dtReceiptMst");
        DataTable dtVendorQcInfo = (DataTable) getIntent().getSerializableExtra("dtVendorQcInfo");

        // 將 Mst 的資料傳到下一頁的第一個頁籤
        Bundle bundle = new Bundle();
        bundle.putSerializable("dtReceiptMst", dtReceiptMst);
        bundle.putSerializable("dtVendorQcInfo", dtVendorQcInfo);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentReceive, receiveDataFragment, "ReceiveData");
        fragmentTransaction.add(R.id.fragmentReceive, receiveListFragment, "ReceiveList");
        receiveDataFragment.setArguments(bundle);
        receiveListFragment.setArguments(bundle);
        fragmentTransaction.hide(receiveListFragment);
        fragmentTransaction.commit();

        tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });
    }

    private void fragmentChange(int position){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (currentPage){

            case 0:
                fragmentTransaction.hide(receiveDataFragment);
                break;

            case 1:
                fragmentTransaction.hide(receiveListFragment);
                break;
        }

        switch (position){

            case 0:
                fragmentTransaction.show(receiveDataFragment);
                break;

            case 1:
                fragmentTransaction.show(receiveListFragment);
                break;
        }

        fragmentTransaction.commit();

        currentPage = position;
    }


//    public void sendDrLotToList(DataRow drLot, DataTable dtSn, String mode, int pos) {
//        GoodNonReceiptReceiveListFragment receiveList = (GoodNonReceiptReceiveListFragment) getSupportFragmentManager().findFragmentByTag("ReceiveList");
//        receiveList.getLotTable(drLot, dtSn, mode, pos);
//    }

    @Override
    public void sendDrLotToList(DataTable dtLotWithSkuLevel, DataTable dtLot, DataTable dtSn, String mode, int pos) {
        GoodNonReceiptReceiveListFragment receiveList = (GoodNonReceiptReceiveListFragment) getSupportFragmentManager().findFragmentByTag("ReceiveList");
        receiveList.getLotTable(dtLotWithSkuLevel, dtLot, dtSn, mode, pos);
    }

//    @Override
//    public void modifyDrLotData(DataRow drLotWithSkuLevel, DataTable dtSn, String mode, int pos) {
//        GoodNonReceiptReceiveDataFragment receiveData = (GoodNonReceiptReceiveDataFragment) getSupportFragmentManager().findFragmentByTag("ReceiveData");
//        receiveData.setLotTable(drLotWithSkuLevel, dtSn, mode, pos);
//    }

    public void modifyDrLotData(DataRow drLotWithSkuLevel, DataTable dtLot, DataTable dtSn, String mode, int pos) {
        GoodNonReceiptReceiveDataFragment receiveData = (GoodNonReceiptReceiveDataFragment) getSupportFragmentManager().findFragmentByTag("ReceiveData");
        receiveData.setLotTable(drLotWithSkuLevel, dtLot, dtSn, mode, pos);
    }
}
