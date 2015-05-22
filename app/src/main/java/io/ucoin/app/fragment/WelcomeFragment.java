package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.ucoin.app.R;

public class WelcomeFragment extends Fragment {

    private final String TAG = "WelcomeFragment";

    private ProgressBar mProgressBar;
    private TextView mProgressText;

    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_welcome,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar)view.findViewById(R.id.progressbar);
        mProgressText = (TextView)view.findViewById(R.id.progress_text);

        // Progression welcome message (convert to HTML)
        TextView progressionTitle = (TextView)view.findViewById(R.id.progress_welcome);
        progressionTitle.setText(Html.fromHtml(getString(R.string.creating_account_welcome)));

    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public TextView getProgressTextView() {
        return mProgressText;
    }
}



