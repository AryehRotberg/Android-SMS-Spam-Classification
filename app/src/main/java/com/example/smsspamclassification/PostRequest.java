package com.example.smsspamclassification;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PostRequest
{
    private final String URL = "https://sms-spam-classification-python.onrender.com/predict";
    private final RequestQueue requestQueue;
    private final Context context;
    private final String text_input, sender;
    private final boolean loading;
    private ProgressDialog progressDialog;
    private final Handler handler;
    private final Runnable runnable;

    public PostRequest(Context context, String text_input, String sender, boolean loading)
    {
        this.context = context;
        this.text_input = text_input;
        this.sender = sender;
        this.loading = loading;

        this.handler = new Handler();

        this.runnable = () ->
        {
            PackageManager packageManager = this.context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(this.context.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        };

        this.requestQueue = Volley.newRequestQueue(this.context);

        if (this.loading)
        {
            this.progressDialog = new ProgressDialog(this.context);
            this.progressDialog.setMessage("Loading model. This might take a few minutes.");
            this.progressDialog.setCancelable(false);
            this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close App", (dialogInterface, i) -> System.exit(0));
        }
    }

    public JSONObject prepareInput()
    {
        JSONObject postData = new JSONObject();

        try
        {
            postData.put("text_input", this.text_input);
        }

        catch (JSONException e) { e.printStackTrace(); }

        return postData;
    }

    public void getResponse()
    {
        if (this.loading)
        {
            this.progressDialog.show();
            this.handler.postDelayed(runnable, 30000);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"}) JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, prepareInput(), response ->
        {
            try
            {
                if (!this.loading)
                {
                    String classType = response.getString("class");
                    double confidence = response.getDouble("confidence");

                    if (confidence >= .5)
                    {
                        try (DatabaseHelper helper = new DatabaseHelper(this.context))
                        {
                            helper.addRecord(this.sender, this.text_input, classType, String.format("%.2f", confidence * 100) + "%");
                        }
                    }
                }

                else
                {
                    this.progressDialog.dismiss();
                    this.handler.removeCallbacks(this.runnable);
                }
            }

            catch (JSONException e) { throw new RuntimeException(e); }
        }, Throwable::printStackTrace);

        this.requestQueue.add(jsonObjectRequest);
    }
}
