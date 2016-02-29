package io.ucoin.app.task;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.ucoin.app.BuildConfig;

public class GenerateQRCodeTask extends AsyncTask<Bundle, Void, Bitmap> {
    private OnCodeGeneratedListener mListener;

    public GenerateQRCodeTask(OnCodeGeneratedListener listener) {
        mListener = listener;
    }

    @Override
    protected Bitmap doInBackground(Bundle... args) {
        Bitmap bitmap;
        try {
            int width = args[0].getInt(("width"));
            String publicKey = args[0].getString(("public_key"));
            int color = args[0].getInt("color");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(publicKey, BarcodeFormat.QR_CODE, width, width);
            bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y)
                            ? color
                            : Color.WHITE);
                }
            }

            return bitmap;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("GenerateQRCodeTask", e.getMessage());
        }
        return null;
    }

    @Override
    public void onPostExecute(Bitmap bitmap) {
        mListener.onCodeGenerated(bitmap);
    }

    public interface OnCodeGeneratedListener {
        void onCodeGenerated(Bitmap bitmap);
    }
}


