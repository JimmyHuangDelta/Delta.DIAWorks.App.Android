package com.delta.android.Core.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.delta.android.R;

public class BaseFlowActivity extends BaseActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        TextView tvFactoryName = new TextView(this);
        tvFactoryName.setText(getGlobal().getFactoryName() + "     ");
        tvFactoryName.setTextColor(Color.WHITE);
        tvFactoryName.setPadding(5, 0, 5, 0);
        tvFactoryName.setTypeface(null, Typeface.BOLD);
        tvFactoryName.setTextSize(17);
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "FactoryName").setActionView(tvFactoryName).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Bundle getBundle() {
        return getIntent().getExtras();
    }

    //region goto Next/Previous Activity

    public void gotoNextActivity(Class<?> nextActivityClass) {
        gotoNextActivity(nextActivityClass, null);
    }

    public void gotoNextActivity(Class<?> nextActivityClass, Bundle bundle) {
        Intent intent = new Intent(BaseFlowActivity.this, nextActivityClass);
        if (bundle != null) intent.putExtras(bundle);
        startActivity(intent);
    }

    public void gotoPreviousActivity(Class<?> previousActivityClass) {
        gotoPreviousActivity(previousActivityClass, null);
    }

    public void gotoPreviousActivity(Class<?> previousActivityClass, boolean refreshActivity) {
        gotoPreviousActivity(previousActivityClass, null, refreshActivity);
    }

    public void gotoPreviousActivity(Class<?> previousActivityClass, Bundle bundle) {
        gotoPreviousActivity(previousActivityClass, bundle, false);
    }

    public void gotoPreviousActivity(Class<?> previousActivityClass, Bundle bundle, boolean refreshActivity) {
        Intent intent = new Intent(BaseFlowActivity.this, previousActivityClass);
        if (bundle != null) intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//清除到目標所開啓的Activity
        if (!refreshActivity) intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//目標不重新開啓
        startActivity(intent);
    }


    //endregion

    //region ActivityDialog

    private OnActivityResult activityResult;

    public void gotoNextActivityForResult(Class<?> nextActivityClass, Bundle bundle, OnActivityResult result) {
        activityResult = result;
        Intent intent = new Intent(BaseFlowActivity.this, nextActivityClass);
        if (bundle != null) intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }

    public void gotoNextActivityForResult(Class<?> nextActivityClass, OnActivityResult result) {
        gotoNextActivityForResult(nextActivityClass, null, result);
    }

    public void setActivityResult(Bundle bundle) {
        Intent intent = getIntent();
        intent.putExtras(bundle);
        setResult(1, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            if (activityResult != null) {
                activityResult.onResult(data.getExtras());
            }
        }
    }

    public interface OnActivityResult {
        void onResult(Bundle bundle);
    }

    //endregion
}
