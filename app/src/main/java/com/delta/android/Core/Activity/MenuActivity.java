package com.delta.android.Core.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.MesFunction;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIGetUserMenuParam;
import com.delta.android.WMS.Param.BUpdateUserLoginFactoryParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MenuActivity extends BaseActivity {

    private int _perWidth;//每個項目的寬度
    private int _itemSize = 4;//config,顯示幾個

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_menu);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        _perWidth = displayWidth / _itemSize;

        final LinearLayout llModule = findViewById(R.id.llModule);
        llModule.removeAllViews();
        TableLayout tlFunction = findViewById(R.id.tlFunction);
        tlFunction.removeAllViews();//先移除全部控制項,再加入新的

        getFunctionList();
        //getPreUserLoginFactory();

    }

    private void LoadFunction(String moduleName) {
        TableLayout tlFunction = findViewById(R.id.tlFunction);
        tlFunction.removeAllViews();//先移除全部控制項,再加入新的

        if (getGlobal().getFunctions() == null) return;
        List<MesFunction> lstFunction = getGlobal().getFunctions().get(moduleName);
        if (lstFunction != null && lstFunction.size() > 0) {

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int funcCount = lstFunction.size();

            int rowCount = (int) Math.ceil((double) funcCount / (double) _itemSize);
            int funcIdx = 0;
            int perWidth = width / _itemSize;

            for (int i = 0; i < rowCount; i++) {
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                tlFunction.addView(tr);
                for (int j = 0; j < _itemSize; j++) {
                    if (funcIdx < funcCount) {
                        final MesFunction fun = lstFunction.get(funcIdx);

                        Button btn = new Button(this);
                        btn.setText(getResString(fun.FUNCTION_ID));
                        btn.setLayoutParams(new TableRow.LayoutParams(perWidth, TableRow.LayoutParams.WRAP_CONTENT));
                        btn.setBackgroundColor(getResources().getColor(R.color.common_activity_background));
                        btn.setTextColor(getResources().getColor(R.color.common_theme_dark3));
                        String iconName = "ic_log";//預設icon名稱
                        int strId = getResources().getIdentifier(fun.FUNCTION_ID + "_ICON", "string", getPackageName());
                        if (strId != 0) {
                            iconName = getResources().getString(strId);
                        }
                        int iconId = getResources().getIdentifier(iconName, "mipmap", getPackageName());
                        if (iconId == 0)//找不到圖片
                            btn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_log), null, null);
                        else
                            btn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(iconId), null, null);
                        //不要有邊框
                        TypedValue value = new TypedValue();
                        getApplicationContext().getTheme().resolveAttribute(R.attr.borderlessButtonStyle, value, true);
                        btn.setBackgroundResource(value.resourceId);
                        tr.addView(btn);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName(getApplicationContext(), fun.OBJ_NAME);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    //如果設定錯誤會exception
                                    ShowMessage(e.getMessage());
                                }
                            }
                        });
                        funcIdx++;
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_exit, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText("確定要登出嗎?");
        //tvMessage.setTextSize(getResources().getDimension(R.dimen.DialogTextSize));

        final AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
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
                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
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

        MenuItem spinnerItem = menu.findItem(R.id.cmbFactory);
        Spinner cmbFactory = (Spinner)spinnerItem.getActionView();
        List<? extends Map<String, Object>> factories = getGlobal().getFactories();

        if(factories == null || factories.size() == 0)
            return false;

        int factoryPosition = 0;
        String selFactory = getGlobal().getFactoryId();
        for (Map<String, Object> factory : factories)
        {
            if(factory.get("FACTORY_ID").toString().equals(selFactory))
                break;

            factoryPosition++;
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
                //getFunctionList();
                updateUserLoginFactory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }

    private void getFunctionList()
    {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.Authorization.BIGetUserMenu");
        bmObj.setModuleID("GetUserMenuByPda");
        bmObj.setRequestID("GetUserMenuByPda");
        bmObj.params = new Vector<>();

        ParameterInfo userParam = new ParameterInfo();
        userParam.setParameterID(BIGetUserMenuParam.UserKey);
        userParam.setParameterValue(getGlobal().getUserKey());
        bmObj.params.add(userParam);

        ParameterInfo funcTypeParam = new ParameterInfo();
        funcTypeParam.setParameterID(BIGetUserMenuParam.FuncType);
        funcTypeParam.setNetParameterValue2(getGlobal().getFunctionTypes());
        bmObj.params.add(funcTypeParam);

        ParameterInfo funcFuncSubTypeParam = new ParameterInfo();
        funcFuncSubTypeParam.setParameterID(BIGetUserMenuParam.FuncSubType);
        funcFuncSubTypeParam.setNetParameterValue2(getGlobal().getFunctionSubTypes());
        bmObj.params.add(funcFuncSubTypeParam);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                DataTable dtFunction = bModuleReturn.getReturnJsonTables().get("GetUserMenuByPda").get("*");
                List<MesFunction> lstMesFun = new ArrayList<MesFunction>();
                List<? extends Map<String, Object>> lstFunction = (List<? extends Map<String, Object>>) dtFunction.toListHashMap();
                for (Map<String, Object> fun : lstFunction) {
                    MesFunction mf = new MesFunction();
                    mf.FUNCTION_ID = fun.get("FUNCTION_ID").toString();
                    mf.FUNCTION_DESC = fun.get("FUNCTION_DESC").toString();
                    mf.FUNCTION_NAME = fun.get("FUNCTION_NAME").toString();
                    mf.FUNCTION_TYPE = fun.get("FUNCTION_TYPE").toString();
                    mf.OBJ_NAME = fun.get("OBJ_NAME").toString();
                    lstMesFun.add(mf);
                }
                getGlobal().setFunctions(lstMesFun);
                addFunctions();
            }
        });
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_account:
                Intent intentAccount = new Intent(MenuActivity.this, AccountActivity.class);
                startActivity(intentAccount);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFunctions() {
        final LinearLayout llModule = findViewById(R.id.llModule);
        llModule.removeAllViews();
        if (getGlobal().getFunctionTypes().length > 0) {
            String[] functionType = getGlobal().getFunctionTypes();
            for (int i = 0; i < functionType.length; i++) {
                Button btn = new Button(this);
                btn.setText(getResString(functionType[i]));
                btn.setTag(functionType[i]);
                btn.setLayoutParams(new LinearLayout.LayoutParams(_perWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
                btn.setTextColor(getResources().getColor(R.color.common_theme_light1));
                btn.setBackgroundColor(getResources().getColor(R.color.common_tool_bar_background));
                String iconName = "ic_data_managment";//預設icon名稱
                int strId = getResources().getIdentifier(functionType[i] + "_ICON", "string", getPackageName());
                if (strId != 0) {
                    iconName = getResources().getString(strId);
                }
                int iconId = getResources().getIdentifier(iconName, "mipmap", getPackageName());
                if (iconId == 0)//找不到圖片
                    btn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_data_managment), null, null);
                else
                    btn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(iconId), null, null);
                //不要有邊框
                TypedValue value = new TypedValue();
                getApplicationContext().getTheme().resolveAttribute(R.attr.borderlessButtonStyle, value, true);
                btn.setBackgroundResource(value.resourceId);
                llModule.addView(btn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //全部改為未選取的顏色
                        for (int j = 0; j < llModule.getChildCount(); j++) {
                            ((Button) llModule.getChildAt(j)).setTextColor(getResources().getColor(R.color.common_theme_light1));
                        }
                        //改為選取的顏色
                        ((Button) v).setTextColor(getResources().getColor(R.color.common_theme_dark2));
                        LoadFunction(v.getTag().toString());
                    }
                });
            }
            ((Button) llModule.getChildAt(0)).setTextColor(getResources().getColor(R.color.common_theme_dark2));
            LoadFunction(functionType[0]);
            if (getGlobal().getFunctionTypes().length == 1)//如果只有一個模組,隱藏最下面的模組工具列
            {
                llModule.setVisibility(LinearLayout.GONE);
            }
        }
    }
}