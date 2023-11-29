package com.delta.android.Core.Activity;

import android.app.AlertDialog;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.delta.android.Core.Adapter.FunctionCategoryAdapter;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.FunctionCategoryObject;
import com.delta.android.Core.WebApiClient.MesFunction;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIGetUserMenuParam;
import com.delta.android.WMS.Param.BIPdaMenuPortalParam;
import com.delta.android.WMS.Param.BUpdateUserLoginFactoryParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuNewActivity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_menu_new);

        getFunctionList();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_account:
                Intent intentAccount = new Intent(MenuNewActivity.this, AccountActivity.class);
                startActivity(intentAccount);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_exit, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText("確定要登出嗎?");
        //tvMessage.setTextSize(getResources().getDimension(R.dimen.DialogTextSize));

        final AlertDialog.Builder builder = new AlertDialog.Builder(MenuNewActivity.this);
        //builder.setTitle("");
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MenuNewActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_core_menu, menu);

        // 切換廠別的下拉選單
        MenuItem spinnerItem = menu.findItem(R.id.cmbFactory);
        Spinner cmbFactory = (Spinner)spinnerItem.getActionView();
        List<? extends Map<String, Object>> factories = getGlobal().getFactories();

        if(factories == null || factories.size() == 0)
            return false;

        int factoryPosition = -1;
        String selFactory = getGlobal().getFactoryId();

        for (int pos = 0; pos < factories.size(); pos++) {
            if (factories.get(pos).get("FACTORY_ID").toString().equals(selFactory)) {
                factoryPosition = pos;
                break;
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(this, getGlobal().getFactories(), R.layout.menu_simple_spinner_item, new String[]{"FACTORY_NAME"}, new int[]{android.R.id.text1});
        adapter.setDropDownViewResource(R.layout.menu_simple_spinner_item);
        cmbFactory.setAdapter(adapter);
        cmbFactory.setSelection(factoryPosition,true);

        cmbFactory.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Map<String, String> infoMap = (Map<String, String>)parent.getItemAtPosition(position);

                getGlobal().setFactoryKey(infoMap.get("FACTORY_KEY"));
                getGlobal().setFactoryId(infoMap.get("FACTORY_ID"));
                getGlobal().setFactoryName(infoMap.get("FACTORY_NAME"));
                updateUserLoginFactory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }

    // 取得排序過且有階層的功能清單 (限兩層)
    private void getFunctionList() {

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIPdaMenuPortal");
        bmObj.setModuleID("BIFetchPdaMenu");
        bmObj.setRequestID("BIFetchPdaMenu");
        bmObj.params = new Vector<>();

        ParameterInfo userParam = new ParameterInfo();
        userParam.setParameterID(BIPdaMenuPortalParam.UserKey);
        userParam.setParameterValue(getGlobal().getUserKey());
        bmObj.params.add(userParam);

        ParameterInfo funcTypeParam = new ParameterInfo();
        funcTypeParam.setParameterID(BIPdaMenuPortalParam.FuncType);
        funcTypeParam.setNetParameterValue2(getGlobal().getFunctionTypes());
        bmObj.params.add(funcTypeParam);

        ParameterInfo funcFuncSubTypeParam = new ParameterInfo();
        funcFuncSubTypeParam.setParameterID(BIPdaMenuPortalParam.FuncSubType);
        funcFuncSubTypeParam.setNetParameterValue2(getGlobal().getFunctionSubTypes());
        bmObj.params.add(funcFuncSubTypeParam);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                DataTable dtFunctionType1 = new DataTable(); // 父階層 e.g. 入庫維護作業
                DataTable dtFunctionType2 = new DataTable(); // 子階層 e.g. 人工收料作業
                List<FunctionCategoryObject> lstFunCategory = new ArrayList<>(); // 父階層轉換成物件 e.g. 入庫維護作業
                List<FunctionCategoryObject> lstFunSubCategory = new ArrayList<>(); // 子階層轉換成物件 e.g. 人工收料作業

                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    //DataTable dtFunction = bModuleReturn.getReturnJsonTables().get("BIFetchPdaMenu").get("PdaMenuByOrder");
                    dtFunctionType1 = bModuleReturn.getReturnJsonTables().get("BIFetchPdaMenu").get("dtFunctionType1");
                    dtFunctionType2 = bModuleReturn.getReturnJsonTables().get("BIFetchPdaMenu").get("dtFunctionType2");

                    for (DataRow drType1 : dtFunctionType1.Rows) {

                        FunctionCategoryObject fco = new FunctionCategoryObject();
                        fco.setFunctionId(drType1.getValue("FUNCTION_ID").toString());
                        fco.setFunctionName(drType1.getValue("FUNCTION_NAME").toString());
                        fco.setFunctionKey(drType1.getValue("FUNCTION_KEY").toString());
                        lstFunCategory.add(fco);

                    }

                    for (DataRow drType2 : dtFunctionType2.Rows) {

                        FunctionCategoryObject fco = new FunctionCategoryObject();
                        fco.setFunctionId(drType2.getValue("FUNCTION_ID").toString());
                        fco.setFunctionName(drType2.getValue("FUNCTION_NAME").toString());
                        fco.setParentKey(drType2.getValue("FATHER_FUNCTION_KEY").toString());
                        fco.setObjName(drType2.getValue("OBJ_NAME").toString());
                        lstFunSubCategory.add(fco);

                    }

                    for (FunctionCategoryObject parent : lstFunCategory) {

                        ArrayList<FunctionCategoryObject> subCategories = new ArrayList<>();
                        for (Iterator<FunctionCategoryObject> iterator = lstFunSubCategory.iterator(); iterator.hasNext();) {
                            FunctionCategoryObject fco = iterator.next();

                            if (fco.getParentKey().equals(parent.getFunctionKey())) {
                                subCategories.add(fco);
                                iterator.remove();
                            }
                        }
                        if (subCategories != null && subCategories.size() > 0)
                            parent.setSubCategories(subCategories);
                    }

                    for (Iterator<FunctionCategoryObject> iterator = lstFunCategory.iterator(); iterator.hasNext();) {
                        FunctionCategoryObject fco = iterator.next();
                        if (fco.getSubCategories() == null || fco.getSubCategories().size() == 0) {
                            iterator.remove();
                        }
                    }
                }

                // 父階層 RecyclerViewAdapter => FunctionCategoryAdapter.java 設定子階層內的項目顯示幾個，目前設定為 4
                // 子階層 RecyclerViewAdapter => FunctionCategoryItemAdapter.java 設定子階層各項目的 icon 及點擊後前往的 Activity

                RecyclerView rvFunctionCategory = findViewById(R.id.rvFunctionCategory);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rvFunctionCategory.setLayoutManager(linearLayoutManager);
                rvFunctionCategory.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL)); // 分隔線

                FunctionCategoryAdapter rvAdapter = new FunctionCategoryAdapter(getApplicationContext());
                rvFunctionCategory.setAdapter(rvAdapter);
                rvAdapter.setFunctionCategoryObjects(lstFunCategory);
            }
        });
    }

    // 切換廠別時更新最後登入的廠別資訊，並重新載入功能清單
    private void updateUserLoginFactory() {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.Authorization.BUpdateUserLoginFactory");
        bmObj.setModuleID("");
        bmObj.setRequestID("BUpdateUserLoginFactory");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo userParam = new ParameterInfo();
        userParam.setParameterID(BUpdateUserLoginFactoryParam.UserKey);
        userParam.setParameterValue(getGlobal().getUserKey());
        bmObj.params.add(userParam);

        ParameterInfo factoryParam = new ParameterInfo();
        factoryParam.setParameterID(BUpdateUserLoginFactoryParam.FactoryKey);
        factoryParam.setParameterValue(getGlobal().getFactoryKey());
        bmObj.params.add(factoryParam);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    getFunctionList();
                }
            }
        });
    }
}
