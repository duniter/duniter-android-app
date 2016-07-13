package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duniter.app.R;
import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.BlockService;
import org.duniter.app.technical.callback.CallbackBlock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InfoDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private static boolean isWallet;
    private static Currency currency;
    private static int number;
    private AlertDialog alert;


    public static InfoDialogFragment newInstance(boolean _isWallet, Currency _currency, Bundle bundle, int _number) {
        InfoDialogFragment fragment = new InfoDialogFragment();
        isWallet = _isWallet;
        currency = _currency;
        number = _number;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_info, null);
        final ViewHolder holder = new ViewHolder(view);
        if (!isWallet) {
            holder.messages.setVisibility(View.GONE);
        } else {
            Bundle args = getArguments();
            boolean needSelf = args.getBoolean("needSelf");
            boolean needMembership = args.getBoolean("needMembership");
            boolean willNeedMembership = args.getBoolean("willNeedMembership");
            boolean needRenew = args.getBoolean("needRenew");
            int willNeedCertifications = args.getInt("willNeedCertifications");




            holder.messages.setVisibility(View.VISIBLE);

            holder.warning_wallet_self.setVisibility(needSelf ? View.VISIBLE : View.GONE);
            holder.warning_wallet_membership.setVisibility(
                    needMembership && !willNeedMembership ?
                            View.VISIBLE :
                            View.GONE
            );
            holder.warning_wallet_load_membership.setVisibility(
                    willNeedMembership ?
                            View.VISIBLE :
                            View.GONE
            );
            holder.warning_wallet_renew.setVisibility(
                    needRenew ?
                            View.VISIBLE :
                            View.GONE
            );
            holder.warning_wallet_certification.setVisibility(
                    willNeedCertifications>0 ?
                            View.VISIBLE :
                            View.GONE
            );

            if (willNeedCertifications==1){
                holder.txt_warning_wallet_certification.setText(getString(R.string.warning_wallet_certification));
            }else{
                holder.txt_warning_wallet_certification.setText(
                        String.format(getString(R.string.warning_wallet_certifications),String.valueOf(willNeedCertifications))
                );
            }
            boolean haveMessage = (needSelf ||
                    (needMembership && !willNeedMembership) ||
                    (willNeedMembership) ||
                    (needRenew) ||
                    (willNeedCertifications>0)
                    );

            holder.no_message.setVisibility(haveMessage ? View.GONE : View.VISIBLE);

        }
        builder.setView(view);
        builder.setTitle(getString(R.string.information));
        builder.setIcon(R.drawable.ic_info);

        BlockService.getBlock(getActivity(), currency, number, new CallbackBlock() {
            @Override
            public void methode(BlockUd blockUd) {
                holder.date.setText(new SimpleDateFormat("dd MMM yyyy").format(new Date(blockUd.getMedianTime() * 1000)));
                holder.progressBar.setVisibility(View.GONE);
                holder.date.setVisibility(View.VISIBLE);
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        view.clearFocus();
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = findAction(((TextView) view).getText().toString());
        if (text.length() != 0) {
            Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
        }
    }

    private String findAction(String text) {
        String result;
        Resources res = getActivity().getResources();
        if (text.equals(res.getString(R.string.warning_wallet_self))) {
            result = res.getString(R.string.warning_info_wallet_self);

        } else if (text.equals(res.getString(R.string.warning_wallet_membership))) {
            result = res.getString(R.string.warning_info_wallet_membership);

        } else if (text.equals(res.getString(R.string.warning_wallet_load_membership))) {
            result = res.getString(R.string.warning_info_wallet_load_membership);

        } else if (text.equals(res.getString(R.string.warning_wallet_renew))) {
            result = res.getString(R.string.warning_info_wallet_renew);

        } else if (text.equals(res.getString(R.string.not_important_message))) {
            result = "";
        } else {
            result = res.getString(R.string.warning_info_wallet_certification);
        }
        return result;
    }

    public static class ViewHolder {
        public View rootView;
        public LinearLayout warning_wallet_self;
        public LinearLayout warning_wallet_membership;
        public LinearLayout warning_wallet_load_membership;
        public LinearLayout warning_wallet_renew;
        public TextView txt_warning_wallet_certification;
        public LinearLayout warning_wallet_certification;
        public LinearLayout no_message;
        public LinearLayout messages;
        public TextView date;
        public ProgressBar progressBar;
        public RelativeLayout linearLayout;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.warning_wallet_self = (LinearLayout) rootView.findViewById(R.id.warning_wallet_self);
            this.warning_wallet_membership = (LinearLayout) rootView.findViewById(R.id.warning_wallet_membership);
            this.warning_wallet_load_membership = (LinearLayout) rootView.findViewById(R.id.warning_wallet_load_membership);
            this.warning_wallet_renew = (LinearLayout) rootView.findViewById(R.id.warning_wallet_renew);
            this.txt_warning_wallet_certification = (TextView) rootView.findViewById(R.id.txt_warning_wallet_certification);
            this.warning_wallet_certification = (LinearLayout) rootView.findViewById(R.id.warning_wallet_certification);
            this.no_message = (LinearLayout) rootView.findViewById(R.id.no_message);
            this.messages = (LinearLayout) rootView.findViewById(R.id.messages);
            this.date = (TextView) rootView.findViewById(R.id.date);
            this.progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            this.linearLayout = (RelativeLayout) rootView.findViewById(R.id.linearLayout);
        }

    }
}



