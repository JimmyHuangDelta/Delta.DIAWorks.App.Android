package com.delta.android.Sample.Client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;

import java.util.Vector;

public class Test3Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test3);

        Button btnTestBIModule = findViewById(R.id.btnTestBIModule);
        btnTestBIModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmObj.setModuleID("BIFetchWmsRegister");
                bmObj.setRequestID("BIModule");

                bmObj.params = new Vector<ParameterInfo>();

                ParameterInfo param1 = new ParameterInfo();
                //param1.setParameterID(BIWMSFetchInfoParam.Filter);
                //param1.setParameterValue(" R.REGISTER_ID='LOT01' ");//error
                param1.setParameterValue(" AND R.REGISTER_ID='LOT01' ");
                bmObj.params.add(param1);

                CallBIModule(bmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {

                        if (CheckBModuleReturnInfo(bModuleReturn)) {
                            DataTable dt = bModuleReturn.getReturnJsonTables().get("BIModule").get("SWMS_INVENTORY");
                            //((DataColumnCollection) dt.getColumns()).get("REGISTER_KEY").setColumnName("REG_KEY");//調整欄位名稱
                            if (dt.Rows.get(0).getValue("REGISTER_ID").equals("LOT01")) {
                                ShowMessage("==");
                            }
                            ShowMessage(String.valueOf(dt.Rows.size()));
                        }
                    }
                });
            }
        });


        Button btnTestBModule = findViewById(R.id.btnTestBModule);
        btnTestBModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Sheet.BCountMaintain");
                bmObj.setRequestID("BModule");

                bmObj.params = new Vector<ParameterInfo>();


                CallBModule(bmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {

                        if (CheckBModuleReturnInfo(bModuleReturn)) {


                        }
                    }
                });

            }
        });


        Button btnShowMessage = findViewById(R.id.btnShowMessage);
        btnShowMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShowMessage(getResString("USER_ID"));

            }
        });

    }
}
