/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.babble.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.webkit.WebView;

import androidx.annotation.StringRes;

import java.util.Objects;

import io.mosaicnetworks.babble.R;

public class DialogUtils {

    public static void displayOkAlertDialog(Context context, @StringRes int titleId, @StringRes int messageId) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(messageId)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }



    public static void displayOkAlertDialogText(Context context, @StringRes int titleId, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(message)
                .setNeutralButton(R.string.ok_button, null)
                .create();
        alertDialog.show();
    }

    public static void displayOkAlertDialogHTML(Context context, @StringRes int titleId, String html) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setNeutralButton(R.string.ok_button, null)
                .create();

        String fullhtml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>\n" +
                html + "\n</body>\n</html>";

        WebView view = new WebView(context);
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        view.setPadding(10, 10, 10, 10);
        dialog.setView(view);
        dialog.show();
    }

    public static ProgressDialog displayLoadingDialog(Context context) {
        ProgressDialog loadingDialog = new ProgressDialog(context);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setTitle("Loading chat");
        loadingDialog.setMessage("Loading. Please wait...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCanceledOnTouchOutside(false);

        return loadingDialog;

    }


}
