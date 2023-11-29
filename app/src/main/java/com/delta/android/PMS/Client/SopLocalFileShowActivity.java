package com.delta.android.PMS.Client;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.PMS.Client.Adapter.ShowSopAdapter;
import com.delta.android.R;

import java.io.File;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class SopLocalFileShowActivity extends BaseActivity {

    ListView lvLocalSop;
    ShowSopAdapter sopAdapter;
    ArrayList arFile = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_sop_local_file_show);

        lvLocalSop = (ListView) findViewById(R.id.lvLocalSopFile);

        //取得本機SOP檔案
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR);
//        String path = Environment.getExternalStorageDirectory().getPath() +"/Android/data/com.delta.android.demo/files/storage/emulated/0/download/SOP_FILE";
        File directory = new File(path);

        File[] fList = directory.listFiles();

        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    arFile.add(file.getName());
                }
            }
        }

        sopAdapter = new ShowSopAdapter(getLayoutInflater(), arFile, SopLocalFileShowActivity.this, path);
        lvLocalSop.setAdapter(sopAdapter);
    }
}
