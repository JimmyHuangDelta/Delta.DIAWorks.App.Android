package com.delta.android.WMS.Client.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.DeliveryNotePickingExecutedNewActivity;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetGridAdapter;

public class DeliveryNoteUnpickFragment extends Fragment {

    ListView lvUnpick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_note_unpick, container, false);

        lvUnpick = view.findViewById(R.id.lvDeliveryNoteUnPick);
        lvUnpick.setOnItemClickListener(getPickedData);

        DataTable dtNeedToPick = (DataTable)getFragmentManager().findFragmentByTag("DeliveryUnpick").getArguments().getSerializable("dtNeedToPick");

        DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtNeedToPick, inflater, false, getContext());
        lvUnpick.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return view;
    }

    public void getUnpickTable(DataTable dtUnpickData) { LayoutInflater inflater = LayoutInflater.from(getContext());
        DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtUnpickData, inflater, false, getContext());
        lvUnpick.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return;
    }

    private AdapterView.OnItemClickListener getPickedData = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            DataRow selectedRow = (DataRow) parent.getItemAtPosition(position);
            String pickShtId = selectedRow.getValue("SHEET_ID").toString();
            Double seq = Double.parseDouble(selectedRow.getValue("SEQ").toString());

            Bundle chooseSheetDet = new Bundle();
            chooseSheetDet.putString("pickShtId", pickShtId);
            chooseSheetDet.putDouble("seq", seq);
            //如果希望使用回傳的資料，則需要用此方法
            Intent intent = new Intent(getContext(), DeliveryNotePickingExecutedNewActivity.class);
            intent.putExtras(chooseSheetDet);
            getActivity().startActivityForResult(intent, 100);
        }
    };
}
