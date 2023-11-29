package com.delta.android.PMS.Client.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.text.DecimalFormat;

public class WorkFragment extends Fragment {

    private int position;
    private String title;

    public static WorkFragment newInstance(int position, String pageTitle) {
        WorkFragment fragment = new WorkFragment();
        Bundle args = new Bundle();
        args.putInt("ARG_PAGE", position);
        args.putString("TITLE", pageTitle);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("ARG_PAGE");
        title = getArguments().getString("TITLE");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setLayoutParams(params);
        frameLayout.setEnabled(false);

        switch (position){
            case 1:

                break;

            case 2:

                break;

            case 3:

                break;

            case 4:

                break;

            case 5:

                break;

            case 6:

                break;

            case 7:

                break;

            case 8:

                break;
        }

        return frameLayout;
    }
}
