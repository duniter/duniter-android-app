package org.duniter.app.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.task.GenerateQRCodeTask;

public class QrCodeDialogFragment extends DialogFragment{

    private TextView mPublicKey;
    private ProgressBar mProgressBar;
    private ImageView mQrCode;

    public static QrCodeDialogFragment newInstance(String publicKey) {
        Bundle args = new Bundle();
        args.putString(Application.PUBLIC_KEY, publicKey);
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

        showQrcode();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void showQrcode() {
        String pubKey = getArguments().getString(Application.PUBLIC_KEY);
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
}