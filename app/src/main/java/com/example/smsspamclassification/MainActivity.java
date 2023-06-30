package com.example.smsspamclassification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    TextView emptyListViewTextView;
    ListView listView;
    ArrayList<String> listItem;
    ArrayAdapter<String> adapter;
    DatabaseHelper helper;
    AlertDialog.Builder builder;
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSMSPermission();
        initializeVariables();

        new PostRequest(this, "", "", true).getResponse();

        updateData();
    }

    public void updateData()
    {
        listItem = new ArrayList<>();
        Cursor cursor = helper.getData();

        if (cursor.getCount() > 0)
        {
            emptyListViewTextView.setVisibility(View.INVISIBLE);

            while (cursor.moveToNext())
                listItem.add(cursor.getString(0) +": " +
                        cursor.getString(1) +
                        " -> Spam Confidence: " + cursor.getString(3));
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
        listView.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void initializeVariables()
    {
        listView = findViewById(R.id.listView);
        listView.setEmptyView(emptyListViewTextView);

        emptyListViewTextView = findViewById(R.id.emptyListViewTextView);

        helper = new DatabaseHelper(this);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() ->
        {
            updateData();
            swipeRefreshLayout.setRefreshing(false);
        });

        builder = new AlertDialog.Builder(this);
        builder.setTitle("SMS Collector - Delete Message");
        builder.setMessage("Are you sure you want to delete this message?");
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {});
        builder.setCancelable(false);

        listView.setOnItemLongClickListener((av, v, pos, id) ->
        {
            builder.setPositiveButton("Yes", (dialogInterface, i) ->
            {
                helper.deleteRow(listItem.get(pos).split(": ")[1].split(" ->")[0]);
                updateData();
            });

            builder.show();

            return false;
        });
    }

    private void checkSMSPermission()
    {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            else
            {
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();

                finish();
                System.exit(0);
            }
        }
    }
}