package io.ucoin.app.fragment.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.ucoin.app.R;

public class NewWalletDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static final int INSCRIPTION = 0;
    private static final int CONNECTION = 1;
    private static final int RECORDING = 2;
    private Action mListener;

    public static NewWalletDialogFragment newInstance(Action listener) {
        return new NewWalletDialogFragment(listener);
    }

    public NewWalletDialogFragment(Action mListener) {
        this.mListener = mListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        getDialog().setTitle(R.string.new_wallet);

        String[] listAction = new String[]{getString(R.string.inscription),getString(R.string.connection),getString(R.string.anonymous_recording)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,listAction);

        ListView list = (ListView) view.findViewById(R.id.list_item);

        list.setAdapter(adapter);


        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        switch (position){
            case INSCRIPTION:
                mListener.actionNew();
                break;
            case CONNECTION:
                mListener.actionConnect();
                break;
            case RECORDING:
                mListener.actionRecording();
                break;
        }
    }

    public interface Action{
        void actionNew();
        void actionConnect();
        void actionRecording();
    }
}