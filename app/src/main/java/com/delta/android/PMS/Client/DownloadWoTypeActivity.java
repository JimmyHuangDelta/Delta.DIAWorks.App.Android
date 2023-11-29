package com.delta.android.PMS.Client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.R;

public class DownloadWoTypeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_download_wo_type);

        ImageButton btnPm = (ImageButton)findViewById(R.id.btnDownloadPm);
        ImageButton btnRepair = (ImageButton)findViewById(R.id.btnDownloadRepair);
        ImageButton btnInspection = (ImageButton)findViewById(R.id.btnDownloadInspection);

        btnPm.setImageResource(R.mipmap.pm_wo);
        btnRepair.setImageResource(R.mipmap.repair_wo);
        btnInspection.setImageResource(R.mipmap.ins_wo);

        btnPm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownloadWoTypeActivity.this, DownloadWoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("downloadType", "PM");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownloadWoTypeActivity.this, DownloadWoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("downloadType", "REPAIR");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DownloadWoTypeActivity.this, DownloadWoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("downloadType", "CHECK");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
