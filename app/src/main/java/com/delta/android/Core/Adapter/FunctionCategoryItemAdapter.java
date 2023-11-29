package com.delta.android.Core.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.WebApiClient.FunctionCategoryObject;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.List;

public class FunctionCategoryItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 子階層 Adapter

    private List<FunctionCategoryObject> functionCategoryObjects;
    private Context context;

    public FunctionCategoryItemAdapter(Context context) {
        functionCategoryObjects = new ArrayList<>();
        this.context = context;
    }

    public void setFunctionCategoryObjects(List<FunctionCategoryObject> functionCategoryObjects) {
        if (functionCategoryObjects != null) {
            this.functionCategoryObjects.clear();
            this.functionCategoryObjects.addAll(functionCategoryObjects);
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private ImageButton ib;

        public ViewHolder(View view) {
            super(view);
            ib = view.findViewById(R.id.item_image_button);
            tv = view.findViewById(R.id.item_category_name);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_core_menu_new_item_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final FunctionCategoryObject functionCategoryObject = functionCategoryObjects.get(position);

        if (functionCategoryObject != null) {

            // 設置子階層各作業的 icon 及點擊前往的 Activity

            ((ViewHolder)holder).tv.setText(functionCategoryObject.getFunctionName());

            String iconName = "ic_log";//預設icon名稱

            int strId = context.getResources().getIdentifier(functionCategoryObject.getFunctionId() + "_ICON", "string", context.getPackageName());

            if (strId != 0) {
                iconName = context.getResources().getString(strId);
            }

            int iconId = context.getResources().getIdentifier(iconName, "mipmap", context.getPackageName());

            if (iconId == 0) // 找不到圖片
                ((ViewHolder)holder).ib.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_log));
            else
                ((ViewHolder)holder).ib.setImageDrawable(context.getResources().getDrawable(iconId));

            // 不要有邊框
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.borderlessButtonStyle, value, true);
            ((ViewHolder)holder).ib.setBackgroundResource(value.resourceId);
            ((ViewHolder)holder).ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName(context, functionCategoryObject.getObjName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        //如果設定錯誤會exception
                        ((BaseFlowActivity)v.getContext()).ShowMessage(e.getMessage());
                        Log.d("Error", e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return functionCategoryObjects.size();
    }
}
