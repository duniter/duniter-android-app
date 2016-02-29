package io.ucoin.app.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.task.GenerateQRCodeTask;

public class QrCodeDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mPublicKey;
    private ProgressBar mProgressBar;
    private ImageView mQrCode;

    public static QrCodeDialogFragment newInstance(Long walletId) {
        Bundle args = new Bundle();
        args.putLong(BaseColumns._ID, walletId);
        QrCodeDialogFragment fragment = new QrCodeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_qrcode, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPublicKey = (TextView) view.findViewById(R.id.public_key);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mQrCode = (ImageView) view.findViewById(R.id.qrcode);
        Button closeButton = (Button) view.findViewById(R.id.close_button);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Long walletId = args.getLong(BaseColumns._ID);

        String selection = SQLiteTable.Wallet._ID + "=?";
        String[] selectionArgs = new String[]{walletId.toString()};

        return new CursorLoader(
                getActivity(),
                UcoinUris.WALLET_URI,
                null, selection, selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        String pubKey = data.getString(data.getColumnIndex(SQLiteTable.Wallet.PUBLIC_KEY));
//        String currency = data.getString(data.getColumnIndex(SQLiteView.Wallet.CURRENCY_NAME));
//        String publicKey = pubKey.concat(":").concat(currency);
        mPublicKey.setText(pubKey);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = (size.x < size.y) ? (int) (size.x / 1.61) : (int) (size.y / 1.61);

        Bundle args = new Bundle();
        args.putInt("width", width);
        args.putString("public_key", pubKey);
        args.putInt("color", getActivity().getResources().getColor(R.color.primary));

        GenerateQRCodeTask task = new GenerateQRCodeTask(new GenerateQRCodeTask.OnCodeGeneratedListener() {
            @Override
            public void onCodeGenerated(Bitmap bitmap) {
                mProgressBar.setVisibility(View.GONE);
                mQrCode.setImageBitmap(bitmap);
                mQrCode.setVisibility(View.VISIBLE);
            }
        });

        task.execute(args);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}