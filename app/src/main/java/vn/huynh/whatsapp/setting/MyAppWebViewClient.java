package vn.huynh.whatsapp.setting;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by duong on 4/10/2019.
 */

public class MyAppWebViewClient extends WebViewClient {
    // variable for onReceivedError
    private boolean refreshed;

    // handling external links as intents
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if( Uri.parse(url).getHost().endsWith("facebook.com") ) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if(!refreshed) {
            view.loadUrl(failingUrl);
            // when network error is real do not reload url again
            refreshed = true;
        }
    }
}
