package com.delta.android.Core.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Common.Global;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.LoginUserObject;
import com.delta.android.Core.WebApiClient.LoginUserReturn;
import com.delta.android.Core.WebApiClient.WebAPIClient;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.Core.WebApiClient.WebAPILoginEvent;
import com.delta.android.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ActionBar的title改到中間
//        String title = this.getSupportActionBar().getTitle().toString();
//        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        this.getSupportActionBar().setCustomView(R.layout.style_core_actionbar_title);
//        ((TextView) this.getSupportActionBar().getCustomView().findViewById(R.id.tvTitle)).setText(title);

        //增加ActionBar的back功能
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //調整ActionBar的back icon
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_common_chevron_left_black_32dp);
    }

    //region Menu

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.activity_core_login, menu);
//        return true;
//    }

    //endregion

    //region ActionBar

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //endregion

    //region Global

    private Global _global = null;

    public Global getGlobal() {
        if (_global == null)
            _global = (Global) getApplication();
        return _global;
    }

    //endregion

    //region Resource

    public String getResString(String str) {
        int i = getResources().getIdentifier(str, "string", getPackageName());
        if (i == 0) {
            return str;
        }
        return getResources().getString(i);
    }

    //endregion

    //region ProgressDialog

    private ProgressDialog dialog = null;

    public void CreateWaitProgressDialog() {
        try {
            dialog = new ProgressDialog(BaseActivity.this, R.style.CommonProgressDialog);//套用共用的style
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void CloseWaitProgressDialog() {
        if (dialog == null) {
            return;
        }

        try {
            dialog.dismiss();
            dialog = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //endregion

    //region Call BModule/BIModule/Login

    private boolean IsCallWebApi = false;//是否正在背景執行呼叫WebApi

    private boolean CheckCanCallWebApi() {
        if (IsCallWebApi)//表示同一時間有做兩次網路的存取
        {
            Toast.makeText(this, "不允許連續呼叫BIModuel/BModule,請調整寫法", Toast.LENGTH_LONG).show();
            return false;
        } else {
            IsCallWebApi = true;
            return true;
        }
    }

    public void CallBIModule(BModuleObject object, WebAPIClientEvent event) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        CallBIModule(bobjs, event);
    }

    public void CallBIModule(final List<BModuleObject> object, final WebAPIClientEvent event) {
        if (CheckCanCallWebApi() == false) return;
        CreateWaitProgressDialog();
        final WebAPIClient client = new WebAPIClient(BaseActivity.this);
        Thread t = new Thread(new Runnable() {
            private BModuleReturn bmReturn = null;

            public void run() {
                try {
                    bmReturn = client.CallBIModule(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IsCallWebApi = false;
                CloseWaitProgressDialog();
                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //20201020 archie 發生401錯誤時,退回至登入畫面
                            if (getGlobal().getStatusCode() ==  401)
                            {
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                //20201103 archie 新增顯示被登出原因
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("MSG", bmReturn.getAckError().entrySet().iterator().next().getValue().toString());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }

                            event.onPostBack(bmReturn);
                        } catch (Exception ex) {
                            ShowMessage(ConvertExceptionToString(ex));
                        }
                    }
                });
            }
        });
        t.start();
    }

    public void CallBModule(BModuleObject object, WebAPIClientEvent event) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        CallBModule(bobjs, event);
    }

    public void CallBModule(final List<BModuleObject> object, final WebAPIClientEvent event) {
        if (CheckCanCallWebApi() == false) return;
        CreateWaitProgressDialog();
        final WebAPIClient client = new WebAPIClient(BaseActivity.this);
        Thread t = new Thread(new Runnable() {
            private BModuleReturn bmReturn = null;

            public void run() {
                try {
                    bmReturn = client.CallBModule(object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IsCallWebApi = false;
                CloseWaitProgressDialog();
                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //20201020 archie 發生401錯誤時,退回至登入畫面
                            if (getGlobal().getStatusCode() ==  401)
                            {
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                //20201103 archie 新增顯示被登出原因
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("MSG", bmReturn.getAckError().entrySet().iterator().next().getValue().toString());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }

                            event.onPostBack(bmReturn);
                        } catch (Exception ex) {
                            ShowMessage(ConvertExceptionToString(ex));
                        }
                    }
                });
            }
        });
        t.start();
    }

    public void Login(final LoginUserObject user, final WebAPILoginEvent event) {
        CreateWaitProgressDialog();
        final WebAPIClient client = new WebAPIClient(BaseActivity.this);
        Thread t = new Thread(new Runnable() {
            private LoginUserReturn loginReturn = null;

            public void run() {
                try {
                    loginReturn = client.Login(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CloseWaitProgressDialog();
                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            event.onPostBack(loginReturn);
                        } catch (Exception ex) {
                            ShowMessage(ConvertExceptionToString(ex));
                        }
                    }
                });
            }
        });
        t.start();
    }

    //20201019 archie SSO機制修改,取得Token
    public void GetToken(final Object user, final WebAPILoginEvent event) {
        CreateWaitProgressDialog();
        final WebAPIClient client = new WebAPIClient(BaseActivity.this);
        Thread t = new Thread(new Runnable() {
            private Object loginReturn = null;

            public void run() {
                try {
                    loginReturn = client.LoginSSO(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CloseWaitProgressDialog();
                BaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            event.onPostBack(loginReturn);
                        } catch (Exception ex) {
                            ShowMessage(ConvertExceptionToString(ex));
                        }
                    }
                });
            }
        });
        t.start();
    }

    public boolean CheckBModuleReturnInfo(BModuleReturn bModuleReturn) {
        if (bModuleReturn.getSuccess() == false) {
            ShowMessage(bModuleReturn.getAckError().entrySet().iterator().next().getValue().toString());
        }
        return bModuleReturn.getSuccess();
    }

    //endregion

    //region ShowMessage

    public void ShowMessage(String message, final ShowMessageEvent event) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_message, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(message);
        //tvMessage.setTextSize(getResources().getDimension(R.dimen.DialogTextSize));

        final AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        //builder.setTitle("");
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (event != null)
                    event.onDismiss();
            }
        });

    }

    public void ShowMessage(String message) {
        ShowMessage(message, null);
    }

    public void ShowMessage(int resErrorCode) {
        ShowMessage(getResString(getResources().getString(resErrorCode)));
    }

    public void ShowMessage(int resErrorCode, final ShowMessageEvent event) {
        ShowMessage(getResString(getResources().getString(resErrorCode)), event);
    }

    public void ShowMessage(int resErrorCode, Object... args) {
        ShowMessage(String.format(getResString(getResources().getString(resErrorCode)), args));
    }

    public void ShowMessage(int resErrorCode, final ShowMessageEvent event, Object... args) {
        ShowMessage(String.format(getResString(getResources().getString(resErrorCode)), args), event);
    }

    //endregion

    //region Navigation

    public void gotoHomeActivity() {
        Intent intent = new Intent(BaseActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //endregion

    //region Exception

    public String ConvertExceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    //endregion
}
