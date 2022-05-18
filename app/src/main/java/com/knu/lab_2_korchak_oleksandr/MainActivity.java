package com.knu.lab_2_korchak_oleksandr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    String gatewayUrl = "http://91.202.128.107:38888/api/values/";
    volatile StringBuilder response = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            loadList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        setContentView(R.layout.activity_main);
        try {
            loadList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop(){
        super.onStop();
    }

    public void loadList() throws JSONException {

        loadListHandler();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ImageButton addButton = findViewById(R.id.addButton);
        ImageButton refresh = findViewById(R.id.refresh);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONArray items = new JSONArray(response.toString());

        for(int i = 0; i < items.length(); i++){
            if (inflater == null) {
                continue;
            }
            JSONObject item = items.getJSONObject(i);
            View v = inflater.inflate(R.layout.container, null);

            if(v!=null){
                TextView nameView = v.findViewById(R.id.name);
                TextView authorView = v.findViewById(R.id.author);
                TextView yearView = v.findViewById(R.id.year);
                TextView idView = v.findViewById(R.id.id);
                ImageButton delButton = v.findViewById(R.id.delButton);

                nameView.setText(item.getString("Name"));
                authorView.setText(item.getString("Author"));
                yearView.setText(item.getString("Year"));
                idView.setText(item.getString("Id"));

                delButton.setOnClickListener(v1 -> {
                    try {
                        delItemHandler(item.getString("Id"));
                        onRestart();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            }

            LinearLayout items_container = findViewById(R.id.list_items);
            if(items_container != null){
                items_container.addView(v);
            }

        }

        addButton.setOnClickListener(v1 -> {
            final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.add_items_dialog);

            dialog.setCancelable(false);

            EditText newId = dialog.findViewById(R.id.EditTextId);
            EditText newName = dialog.findViewById(R.id.EditTextName);
            EditText newAuthor = dialog.findViewById(R.id.EditTextAuthor);
            EditText newYear = dialog.findViewById(R.id.EditTextYear);
            Button btnAdd = dialog.findViewById(R.id.btn_add);
            Button btnCancel = dialog.findViewById(R.id.btn_cancel);

            btnAdd.setOnClickListener(v2 -> {
                String id = newId.getText().toString();
                String name = newName.getText().toString();
                String author = newAuthor.getText().toString();
                String year = newYear.getText().toString();

                if(id.contains(",") & name.contains(",") & author.contains(",") & year.contains(",")){
                    String[] idArr = id.split(",");
                    String[] nameArr = name.split(",");
                    String[] authorArr = author.split(",");
                    String[] yearArr = year.split(",");
                    JSONArray jsonArray = new JSONArray();

                    for(int c = 0; c < idArr.length; c++){
                        if(idArr[c].contains(" ")) idArr[c].replaceAll(" ", "");
                        
                        JSONObject jsonObject = new JSONObject();

                        if(TextUtils.isDigitsOnly(idArr[c]) & TextUtils.isDigitsOnly(yearArr[c])){
                            try {
                                jsonObject.put("Id", Integer.parseInt(idArr[c]));
                                jsonObject.put("Name", nameArr[c]);
                                jsonObject.put("Author", authorArr[c]);
                                jsonObject.put("Year", Integer.parseInt(yearArr[c]));
                                jsonArray.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    addListOfBooksHandler(jsonArray);

                }
                else{
                    if(id.contains(" ")) id.replaceAll(" ", "");

                    JSONObject jsonObject = new JSONObject();

                    if(TextUtils.isDigitsOnly(id) & TextUtils.isDigitsOnly(year)){
                        try {
                            jsonObject.put("Id", Integer.parseInt(id));
                            jsonObject.put("Name", name);
                            jsonObject.put("Author", author);
                            jsonObject.put("Year", Integer.parseInt(year));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        addBookHandler(jsonObject);

                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(), "id or year is not a number", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.show();
                        dialog.dismiss();
                    }
                }
                dialog.dismiss();
                onRestart();
            });

            btnCancel.setOnClickListener(v2 -> {
                dialog.dismiss();
            });

            dialog.show();
        });

        refresh.setOnClickListener(v1 -> onRestart());


    }

    public void loadListHandler(){
        String urlString = /*"https://reqres.in/api/products";*/gatewayUrl.concat("GetBooks");

        Thread loadItemsList = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(4000);
                    conn.setConnectTimeout(4000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");

                    int responseCode = conn.getResponseCode();
                    String line;

                    InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                    response = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        loadItemsList.start();

    }

    public void delItemHandler(String id){
        String urlString = gatewayUrl.concat(id);

        Thread deleteItem = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(4000);
                    conn.setConnectTimeout(4000);
                    conn.setRequestMethod("DELETE");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");

                    int responseCode = conn.getResponseCode();
                    String line;

                    InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                    response = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    while ((line = br.readLine()) != null) {
                        //response.append(line);
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        deleteItem.start();

    }

    public void addBookHandler(JSONObject body){
        String urlString = gatewayUrl.concat("PostAddOneBook");

        Thread addItem = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(4000);
                    conn.setConnectTimeout(4000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(body.toString());

                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    String line;

                    InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                    response = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    while ((line = br.readLine()) != null) {
                        //response.append(line);
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        addItem.start();

    }

    public void addListOfBooksHandler(JSONArray body){
        String urlString = gatewayUrl.concat("PostAddListOfBooks");

        Thread addListOfItems = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(4000);
                    conn.setConnectTimeout(4000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(body.toString());

                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    String line;

                    InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                    response = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    while ((line = br.readLine()) != null) {
                        //response.append(line);
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        addListOfItems.start();
    }
}