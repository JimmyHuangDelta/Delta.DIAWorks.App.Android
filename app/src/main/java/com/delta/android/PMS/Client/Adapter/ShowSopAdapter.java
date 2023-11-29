package com.delta.android.PMS.Client.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.PMS.Client.SopFileMaintainActivity;
import com.delta.android.R;

import java.io.File;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;

public class ShowSopAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    ArrayList<String> arData;
    Context mContext;
    String filePath;

    public ShowSopAdapter(LayoutInflater inflater, ArrayList data, Context applicationContext, String path) {
        mInflater = inflater;
        arData = data;
        mContext = applicationContext;
        filePath = path;
    }

    @Override
    public int getCount() {
        return arData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View vSop = mInflater.inflate(R.layout.activity_pms_sop_show_listview, null);
        final Button btnOpen = (Button) vSop.findViewById(R.id.btnSopShowOpen);
        final Button btnDelete = (Button) vSop.findViewById(R.id.btnSopShowDel);
        TextView txtSopFileName = (TextView) vSop.findViewById(R.id.tvSopShowIdName);

        txtSopFileName.setText(arData.get(position));

        //開啟檔案
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File f1 = new File(filePath + arData.get(position));
                    Uri uri;
                    if (Build.VERSION.SDK_INT < 24) {
                        uri = Uri.fromFile(f1);
                    } else {
                        uri = FileProvider.getUriForFile(mContext, "com.delta.android.PMS.Client", f1);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    intent.setDataAndType(uri, "*/*");
                    mContext.startActivity(intent);
                } catch (Exception ex) {
                    ex.getMessage();
                    return;
                }
            }
        });

        //刪除檔案，也要一併刪除sqlite記錄的資料
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext, 0)
                        .setCancelable(false)
                        .setMessage(mContext.getResources().getString(R.string.IS_DELETE_THIS_SOP))
                        .setPositiveButton(mContext.getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //刪除本機檔案
                                File file = new File(filePath + arData.get(position));
                                file.delete();

                                vSop.setBackgroundColor(Color.RED);
                                btnDelete.setEnabled(false);
                                btnOpen.setEnabled(false);
                            }
                        }).setNegativeButton(mContext.getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });

        return vSop;
    }
}
