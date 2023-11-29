package com.delta.android.Core.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.delta.android.Core.WebApiClient.FunctionCategoryObject;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.List;

public class FunctionCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 父階層 Adapter

    private List<FunctionCategoryObject> functionCategoryObjects;
    private Context context;

    public FunctionCategoryAdapter(Context context) {
        functionCategoryObjects = new ArrayList<>();
        this.context = context;
    }

    public void setFunctionCategoryObjects(List<FunctionCategoryObject> categoryObjects) {
        if (categoryObjects != null) {
            this.functionCategoryObjects.clear();
            this.functionCategoryObjects.addAll(categoryObjects);
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv;
        private TextView tv;
        private RecyclerView rv;

        public ViewHolder(View view) {
            super(view);

            if (getItemCount() != 1) {
                iv = view.findViewById(R.id.item_category_icon);
                tv = view.findViewById(R.id.item_category_title);
            }

            rv = view.findViewById(R.id.item_category_item_rv);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {

        View view;

        // 沒有任何功能群組，只有一個階層時不顯示群組名稱列
        if (getItemCount() != 1) {

            view = LayoutInflater.from(context).inflate(R.layout.activity_core_menu_new_item, parent, false);

        } else {

            view = LayoutInflater.from(context).inflate(R.layout.activity_core_menu_new_item_no_title, parent, false);

        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        FunctionCategoryObject functionCategoryObject = functionCategoryObjects.get(position);

        if (functionCategoryObject != null) {

            ViewHolder viewHolder = (ViewHolder) holder;

            if (getItemCount() != 1) {
                viewHolder.tv.setText(functionCategoryObject.getFunctionName());
            }

            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4); // 設定子階層一列最多四個子項目
            viewHolder.rv.setLayoutManager(gridLayoutManager);
            FunctionCategoryItemAdapter rvItemAdapter = new FunctionCategoryItemAdapter(context);
            viewHolder.rv.setAdapter(rvItemAdapter);
            rvItemAdapter.setFunctionCategoryObjects(functionCategoryObject.getSubCategories());

        }
    }

    @Override
    public int getItemCount() {
        return functionCategoryObjects.size();
    }

}

