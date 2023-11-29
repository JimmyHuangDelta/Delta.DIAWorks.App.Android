package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetGridAdapter;

public class DeliveryNotePickFragment extends Fragment {

    ListView lvPick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_note_pick, container, false);

        lvPick = view.findViewById(R.id.lvDeliveryNotePick);

        DataTable dtPickedFinish = (DataTable)getFragmentManager().findFragmentByTag("DeliveryPick").getArguments().getSerializable("dtPickedFinish");

        DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtPickedFinish, inflater, true, getContext());
        lvPick.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;
    }

    public void getPickTable(DataTable dtPickData) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtPickData, inflater, dtPickData, true, getContext());
        lvPick.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return;

    }

}
