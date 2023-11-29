package com.delta.android.Core.Activity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.R;


public class AccountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_account);

        TextView tvUserId = findViewById(R.id.tvUserId);
        TextView tvUserName = findViewById(R.id.tvUserName);
        Switch swAllowNotification = findViewById(R.id.swAllowNotification);

        tvUserId.setText(getGlobal().getUserID());
        tvUserName.setText(getGlobal().getUserName());

        // region 由記錄設定允許通知的狀態
        Boolean isNotificationAllowd = getGlobal().getAllowedNotification();
        if(isNotificationAllowd)
            swAllowNotification.setChecked(true);
        else
            swAllowNotification.setChecked(false);
        // endregion

        // region 監聽 switch 狀態，並啟動或關閉服務
        swAllowNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    getGlobal().setAllowedNotification(true);

                } else {
                    getGlobal().setAllowedNotification(false);

                }

            }
        });
        // endregion
    }
}
