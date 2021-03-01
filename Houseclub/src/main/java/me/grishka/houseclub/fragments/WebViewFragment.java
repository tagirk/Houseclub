package me.grishka.houseclub.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIController;
import me.grishka.houseclub.api.methods.UpdateInstagram;

public class WebViewFragment extends Fragment {

    private ProgressBar progressBar;
    private WebView webView;
    private View errorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.webview, container, false);
        progressBar = v.findViewById(R.id.progress);
        webView = v.findViewById(R.id.web_view);
        errorView = v.findViewById(R.id.error);

        initWebView();

        return v;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Boolean redirect = checkRedirect(request.getUrl().toString());
                view.loadUrl(request.getUrl().toString());
                return redirect;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Boolean redirect = checkRedirect(url);
                view.loadUrl(url);
                return redirect;
            }

        });

        webView.loadUrl(
                "https://www.instagram.com/oauth/authorize?client_id=" +
                        ClubhouseAPIController.INSTAGRAM_ID +
                        "&redirect_uri=" + ClubhouseAPIController.INSTAGRAM_CALLBACK +
                        "&scope=user_profile" +
                        "&response_type=code");
    }

    private Boolean checkRedirect(String url){
        if (url.startsWith(ClubhouseAPIController.INSTAGRAM_CALLBACK)) {

            // last2 chars is #_ by docs https://developers.facebook.com/docs/instagram-basic-display-api/getting-started
            String code = url.substring((ClubhouseAPIController.INSTAGRAM_CALLBACK+ "?code=").length(), url.length()-2);

            new UpdateInstagram(code)
                    .wrapProgress(getActivity())
                    .setCallback(new Callback<BaseResponse>(){
                        @Override
                        public void onSuccess(BaseResponse result){
                            Nav.finish(WebViewFragment.this);
                        }

                        @Override
                        public void onError(ErrorResponse error){
                            error.showToast(getActivity());
                        }
                    })
                    .exec();
            return false;
        } else {
            return true;
        }
    }
}
