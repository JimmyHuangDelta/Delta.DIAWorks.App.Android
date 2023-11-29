package com.delta.android.Sample.Client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TabHost;

import com.delta.android.R;

public class TabHostTestActivity extends AppCompatActivity {

    private EditText et1;
    private EditText et2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_tab_host_test);

        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);

        TabHost mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setOnTabChangedListener(mTabHost_OnTabChange);
        mTabHost.setup();

        //Lets add the Tab1
        TabHost.TabSpec mSpec = mTabHost.newTabSpec("First Tab");
        mSpec.setContent(R.id.Tab1);
        mSpec.setIndicator("First Tab");
        mTabHost.addTab(mSpec);

        //Lets add the Tab2
        mSpec = mTabHost.newTabSpec("Second Tab");
        mSpec.setContent(R.id.Tab2);
        mSpec.setIndicator("Second Tab");
        mTabHost.addTab(mSpec);

        //mTabHost.setCurrentTab(1);
        mTabHost.setCurrentTabByTag("Second Tab");

    }

    private TabHost.OnTabChangeListener mTabHost_OnTabChange = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if(tabId=="First Tab")
            {
                et1.setText("abc");
            }
            else
            {
                et2.setText("123");
            }
        }
    };
}
