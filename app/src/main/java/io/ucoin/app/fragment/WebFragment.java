package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.technical.StringUtils;


public class WebFragment extends Fragment {

    private static final String ARGS_URL = "webViewURL";

    private WebView mWebView;
    private ProgressViewAdapter mProgressAdapter;

    public static WebFragment newInstance() {
        WebFragment fragment = new WebFragment();
        Bundle newInstanceArgs = new Bundle();

        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    public static WebFragment newInstance(String url) {
        WebFragment fragment = new WebFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putString(ARGS_URL, url);

        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_web,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Read input args
        Bundle newInstanceArgs = getArguments();
        final String url = newInstanceArgs.getString(ARGS_URL);

        // Get the activity
        final Activity activity = getActivity();

        // Web view
        mWebView = (WebView)view.findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 100) {
                    mProgressAdapter.showProgress(false);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        // progress
        mProgressAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                mWebView);

        // If an URL as been given in args, use it
        if (StringUtils.isNotBlank(url)) {
            mWebView.loadUrl(url);
        }
        else {
            // default URL
            mWebView.loadUrl(Configuration.instance().getForumUrl());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.forum);
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
    }
}
