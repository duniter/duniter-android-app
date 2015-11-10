package io.ucoin.app.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.List;

/**
 * Created by naivalf27 on 10/11/15.
 */
public class SingleChoiceDialogFragment extends DialogFragment
{
    public static final String DATA = "items";

    public static final String SELECTED = "selected";

    private SelectionListener listener;

    public SingleChoiceDialogFragment(SelectionListener listener){
        super();
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Resources res = getActivity().getResources();
        Bundle bundle = getArguments();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        dialog.setTitle("Please Select");
        dialog.setPositiveButton("Cancel", new PositiveButtonClickListener());

        List<String> list = (List<String>)bundle.get(DATA);
        int position = bundle.getInt(SELECTED);

        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
        dialog.setSingleChoiceItems(cs, position, selectItemListener);

        return dialog.create();
    }

    class PositiveButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();
        }
    }

    DialogInterface.OnClickListener selectItemListener = new DialogInterface.OnClickListener(){

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            if ( listener != null )
            {
                listener.selectItem(which);
            }
            dialog.dismiss();
        }

    };

    public interface SelectionListener
    {
        public void selectItem ( int position );
    }

}
