package net.huntersharpe.bfhslibrary;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class Notifications extends ListFragment {

    private List<String> dueBooks = new ArrayList<>();

    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    public Notifications() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = getView().findViewById(android.R.id.list);
        TextView emptyTextView = getView().findViewById(android.R.id.empty);
        listView.setEmptyView(emptyTextView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateDueBooks(){
        DatabaseManager dbManager = new DatabaseManager();
        //TODO: Load into list
        mAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, dueBooks);
        setListAdapter(mAdapter);
    }

}
