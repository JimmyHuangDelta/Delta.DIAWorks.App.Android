package com.delta.android.WMS.Client.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.DeliveryNotePickingExecutedNewActivity;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNotePickingGridAdapter;

public class DeliveryNoteShipInfoFragment extends Fragment {

    private ListView lvPackingInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_note_ship_info, container, false);

        lvPackingInfo = view.findViewById(R.id.lvPackingInfo);

        return view;
    }

    public void getPickingInfo(DataTable dtPickDet) {

        if (dtPickDet == null) {
            lvPackingInfo.setAdapter(null);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        DeliveryNotePickingGridAdapter adapter = new DeliveryNotePickingGridAdapter(dtPickDet, inflater);
        lvPackingInfo.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return;
    }
}
