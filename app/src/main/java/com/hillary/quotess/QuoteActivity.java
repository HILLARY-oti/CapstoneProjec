package com.hillary.quotess;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class QuoteActivity extends AppCompatActivity {

    String [] quotesList = {

            "The purpose of our lives is to be happy",
            "Desing what you love",
            "A computer without Photoshop is like a dog with no legs. Sure is fun, but you can&#8217;t really do anything with it. ",
            "Web design is art wrapped in technology."
    };
    String [] authorList = {

            "James Weaver",
            "Helen Keller",
            "Mahatma Gandhi",
            "Truman Capote"

    };


    String cquote;
    String cauthor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quote);


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final TextView quoteview = (TextView) findViewById(R.id.quotev);
        final TextView authorview = (TextView) findViewById(R.id.authorv);

        Random random = new Random();
        int randIndex = random.nextInt(quotesList.length);
        String randQuote = quotesList[randIndex];
        String randAuthor = "\u2020 " + authorList[randIndex];

        randQuote = Html.fromHtml((String) randQuote).toString();

        quoteview.setText(randQuote);
        authorview.setText(randAuthor);
        cquote = randQuote;
        cauthor = randAuthor;
        ImageButton next_btn = (ImageButton) findViewById(R.id.imageButton2);
        next_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                GetQuoteTask task = new GetQuoteTask();
                task.execute();
            }

        });

        ImageButton send_btn = (ImageButton) findViewById(R.id.sendButton);
        send_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                String share_str = "\""+cquote+"\" "+cauthor+"\n(sent from Quotess app)";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                share.putExtra(Intent.EXTRA_TEXT,share_str );
                startActivity(Intent.createChooser(share, "Share this via"));
            }

        });
    }

    public class GetQuoteTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = GetQuoteTask.class.getSimpleName();
        final Dialog dialog = new Dialog(QuoteActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String quoteJsonString = null;

            try {
                URL url = new URL("https://quotesondesign.com/wp-json/wp/v2/posts/?orderby=rand");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return "null";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.i(LOG_TAG, line);
                }

                if (buffer.length() == 0) {
                    return "null";
                }
                quoteJsonString = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return "null";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return quoteJsonString;
        }

        protected void onPostExecute(String quoteJsonString) {
            int i, resId;
            String resAuthor="",resQuote="";

            try {
                JSONArray jArray = new JSONArray(quoteJsonString);
                for(i=0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);

                    resAuthor = jObject.getString("title");
                    resQuote = jObject.getString("content");
                    resId = jObject.getInt("id");

                }
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            }

            final TextView quote_tv = (TextView) findViewById(R.id.quotev);
            final TextView author_tv = (TextView) findViewById(R.id.authorv);
            resQuote = Html.fromHtml((String) resQuote).toString();
            resQuote = resQuote.replace("\n", "").replace("\r", "");

            resAuthor = Html.fromHtml((String) resAuthor).toString();
            resAuthor = "\u2020 " + resAuthor;
            quote_tv.setText(resQuote);
            author_tv.setText(resAuthor);

            cquote = resQuote;
            cauthor = resAuthor;
            dialog.dismiss();
        }
    }

}