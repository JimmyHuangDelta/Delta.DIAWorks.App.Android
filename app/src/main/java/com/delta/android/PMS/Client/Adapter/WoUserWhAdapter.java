package com.delta.android.PMS.Client.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

public class WoUserWhAdapter extends SimpleCursorAdapter {
    private LayoutInflater mInflater;
    private int layoutId;
    private Cursor data;
    private boolean isWork;
    private Context context;
    private Data offData;
    private String woId;
    private String[] dataColumn;
    private int[] layoutUnit;
    private ListView lsUserWh;

    public WoUserWhAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, boolean isWork, Data offData, String woId, ListView lsUserWh)
    {
        super(context,layout,c,from,to,flags);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutId = layout;
        data = c;
        this.isWork = isWork;
        this.context = context;
        this.offData = offData;
        this.woId = woId;
        dataColumn = from;
        layoutUnit = to;
        this.lsUserWh = lsUserWh;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(layoutId, null);
        ImageButton ibDelUserWh = (ImageButton) convertView.findViewById(R.id.ibDelUserWH);
        ibDelUserWh.setOnClickListener(new ibDelUserWhListener(position));

        data.moveToPosition(position);
        TextView tvWhUserId = (TextView) convertView.findViewById(R.id.tvWhUserId);
        TextView tvWhStart = (TextView) convertView.findViewById(R.id.tvWhStart);
        TextView tvWhEnd = (TextView) convertView.findViewById(R.id.tvWhEnd);
        TextView tvWhCmt = (TextView) convertView.findViewById(R.id.tvWhCmt);

        tvWhUserId.setText(data.getString(data.getColumnIndexOrThrow("IDNAME")));
        tvWhStart.setText(data.getString(data.getColumnIndexOrThrow("START_DT")));
        tvWhEnd.setText(data.getString(data.getColumnIndexOrThrow("END_DT")));
        tvWhCmt.setText(data.getString(data.getColumnIndexOrThrow("CMT")));

        return convertView;


    }

    public void rebindData()
    {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        //重新綁定資料，因保養維修點檢三支作業都是使用相同Table去記錄人員工時，因此語法都相同，若需要客制更改，請在複製此Adapter重寫
        data= dbQuery.query("SEMS_MRO_WO_WH", new String[]{"USER_ID,USER_NAME,USER_ID || '_' || USER_NAME as IDNAME,_id,strftime('%Y/%m/%d %H:%M',replace(START_DT,'/','-')) as START_DT,strftime('%Y/%m/%d %H:%M',replace(END_DT,'/','-'))  as END_DT,CMT"},
                " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
        data.moveToFirst();
        WoUserWhAdapter userAdapter = new WoUserWhAdapter(
                context,
                layoutId,
                data,
                dataColumn,
                layoutUnit,
                0,
                isWork,
                offData,
                woId,
                lsUserWh);

        lsUserWh.setAdapter(userAdapter);
        dbQuery.close();
    }

    class ibDelUserWhListener implements View.OnClickListener{
        private int position;

        ibDelUserWhListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            if (!isWork) {
                final AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage(R.string.EAPE105020)
                        .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.CONFIRM, null)
                        .setCancelable(true)
                        .create();
                dialog.show();

                return;
            }

            data.moveToPosition(position);
            final String whUserId = data.getString(data.getColumnIndex("USER_ID"));

            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(context, 0)
                    .setTitle(context.getResources().getString(R.string.DELETE_USER))
                    .setCancelable(false)
                    .setMessage(context.getResources().getString(R.string.DELETE_THIS_FILE))
                    .setPositiveButton(context.getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            offData.getWritableDatabase().delete("SEMS_MRO_WO_WH"
                                    , "MRO_WO_ID = ? AND USER_ID = ?"
                                    , new String[]{woId, whUserId});

                            rebindData();
                        }
                    }).setNegativeButton(context.getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });

            android.support.v7.app.AlertDialog alertDialog = alert.create();
            alertDialog.show();

            return;
        }
    }
}
