package com.delta.android.PMS.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.R;

public class DownloadReceiver extends BroadcastReceiver {

    Context context;
    int size;
    int count = 1;

    public DownloadReceiver(Context context, int size){
        this.context = context;
        this.size = size;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
//            String fileName = intent.getExtras().getString("");
//            Toast.makeText(context, fileName + " 下載完成", Toast.LENGTH_SHORT).show();
            if(size == count) ShowMessage(R.string.DATA_DOWNLOAD_SUCCESS);
            count++;
            return;
        } catch (Exception ex) {
            ex.getCause();
            return;
        }
    }

    public void ShowMessage(String message) {
        ShowMessage(message, null);
    }

    public void ShowMessage(int resErrorCode) {
        ShowMessage(getResString(context.getResources().getString(resErrorCode)));
    }

    public void ShowMessage(String message, final ShowMessageEvent event) {
        LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_message, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(message);
        //tvMessage.setTextSize(getResources().getDimension(R.dimen.DialogTextSize));

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    public String getResString(String str) {
        int i = context.getResources().getIdentifier(str, "string", context.getPackageName());
        if (i == 0) {
            return str;
        }
        return context.getResources().getString(i);
    }
}
