package com.delta.android.Sample.Client;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Sample.Param.ParamObj.ClassAObj;
import com.delta.android.Sample.Param.ParamObj.TrackingEntityObj;
import com.delta.android.Sample.Param.ParamObj.ClassBObj;
import com.delta.android.Sample.Param.BAndroidTestParam;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;


public class Test1Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test1);

        Button btnTestBModule = findViewById(R.id.btnTestBModule);
        Button btnTestBIModule = findViewById(R.id.btnTestBIModule);
        Button btnTest = findViewById(R.id.btnTest);

        //region BModule
        btnTestBModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<TrackingEntityObj> lstEntity = new ArrayList<TrackingEntityObj>();

                TrackingEntityObj tp1 = new TrackingEntityObj();
                tp1.setPathId("a");
                tp1.setEntityId("b");
                tp1.setEntitySerialKey(123);

                TrackingEntityObj tp2 = new TrackingEntityObj();
                tp2.setPathId("aaa");
                tp2.setEntityId("bbbb");
                tp2.setEntitySerialKey(321);

                lstEntity.add(tp1);
                lstEntity.add(tp2);

                VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.AFC.Parameter.ParameterObj.TrackingEntityObj", "bmAFC.Param");
                MList mListEnum = new MList(vListEnum);
                String strLsRelatData = mListEnum.generateFinalCode(lstEntity);


                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.AFC.BAndroidTest");
                bmObj.setModuleID("");
                bmObj.setRequestID("AndroidTest");
                bmObj.params = new Vector<ParameterInfo>();

                ParameterInfo param1 = new ParameterInfo();
                param1.setParameterID(BAndroidTestParam.Param1);
                param1.setParameterValue("ABC");
                bmObj.params.add(param1);

                ParameterInfo param2 = new ParameterInfo();
                param2.setParameterID(BAndroidTestParam.Param2);
                param2.setParameterValue(123);
                bmObj.params.add(param2);

                ParameterInfo param3 = new ParameterInfo();
                param3.setParameterID(BAndroidTestParam.Param3);
                param3.setNetParameterValue(strLsRelatData);
                //param3.setNetParameterValue2(lstEntity);
                bmObj.params.add(param3);

                List<String> lsManuSN = new ArrayList<String>();
                lsManuSN.add("a");
                lsManuSN.add("b");
                VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
                MList mList = new MList(vList);
                String strLsManuSN = mList.generateFinalCode(lsManuSN);

                ParameterInfo param4 = new ParameterInfo();
                param4.setParameterID(BAndroidTestParam.Param4);
                param4.setNetParameterValue(strLsManuSN);
                //param4.setNetParameterValue2(lsManuSN);
                bmObj.params.add(param4);

                List<ClassAObj> lstEntityA = new ArrayList<ClassAObj>();
                List<ClassBObj> lstEntityB = new ArrayList<ClassBObj>();

                ClassBObj b1 = new ClassBObj();
                b1.setName("dsd");
                b1.setAge(213);
                lstEntityB.add(b1);

                ClassAObj a1 = new ClassAObj();
                a1.setSeq(10);
                a1.setId("asss");
                a1.setB(lstEntityB);
                lstEntityA.add(a1);

                VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.AFC.Parameter.ParameterObj.ClassAObj", "bmAFC.Param");
                MList mListEnum2 = new MList(vListEnum2);
                String strdata = mListEnum2.generateFinalCode(lstEntityA);


                ParameterInfo param5 = new ParameterInfo();
                param5.setParameterID(BAndroidTestParam.Param5);
                param5.setNetParameterValue(strdata);
                //param5.setNetParameterValue2(lstEntityA);
                bmObj.params.add(param5);

                HashMap<String, TrackingEntityObj> mm = new HashMap<String, TrackingEntityObj>();
                mm.put("tp1", tp1);
                mm.put("tp2", tp2);

                VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
                VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.AFC.Parameter.ParameterObj.TrackingEntityObj", "bmAFC.Param");
                MesSerializableDictionary msd = new MesSerializableDictionary(vkey, vVal);
                String aaa = msd.generateFinalCode(mm);

                ParameterInfo param6 = new ParameterInfo();
                param6.setParameterID(BAndroidTestParam.Param6);
                param6.setNetParameterValue(aaa);
                bmObj.params.add(param6);


                HashMap<String, List<?>> mm2 = new HashMap<String, List<?>>();
                mm2.put("tp1", lstEntity);
                mm2.put("tp2", lstEntity);
                MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
                String bbb = msdl.generateFinalCode(mm2);

                ParameterInfo param7 = new ParameterInfo();
                param7.setParameterID(BAndroidTestParam.Param7);
                param7.setNetParameterValue(bbb);
                bmObj.params.add(param7);


                List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
                lstBmObj.add(bmObj);

                CallBModule(lstBmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        if (CheckBModuleReturnInfo(bModuleReturn)) {
                            Toast.makeText(Test1Activity.this, "succuss", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        //endregion

        //region BIModule(多個)

        btnTestBIModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.AFC.Monitor.BIAFCMonitor");
                bmObj.setModuleID("GetMonitorData");
                bmObj.setRequestID("BIAFCMonitor");

                BModuleObject bmObj2 = new BModuleObject();
                bmObj2.setBModuleName("Unicom.Uniworks.BModule.AFC.Flow.BIDTModuleList");
                bmObj2.setModuleID("GetCheckBModuleList");
                bmObj2.setRequestID("BIAFCCheckList");

                List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
                lstBmObj.add(bmObj);
                lstBmObj.add(bmObj2);

                CallBIModule(lstBmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        if (CheckBModuleReturnInfo(bModuleReturn)) {
                            DataTable dtMonitor = bModuleReturn.getReturnJsonTables().get("BIAFCMonitor").get("SAFC_DT_CURRENT_STATE");
                            DataTable dtCheckList = bModuleReturn.getReturnJsonTables().get("BIAFCCheckList").get("CheckModuleListTableName");
                            ShowMessage("succuss");
                        }
                    }
                });

            }
        });

        //endregion

        //region BIModule(一個)

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.AFC.Monitor.BIAFCMonitor");
                bmObj.setModuleID("GetMonitorData");
                bmObj.setRequestID("BIAFCMonitor");

                CallBIModule(bmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        if (CheckBModuleReturnInfo(bModuleReturn)) {
                            DataTable dtMonitor = bModuleReturn.getReturnJsonTables().get("BIAFCMonitor").get("SAFC_DT_CURRENT_STATE");
                            ShowMessage("succuss");
                        }
                    }
                });
            }
        });

        //endregion
    }
}
