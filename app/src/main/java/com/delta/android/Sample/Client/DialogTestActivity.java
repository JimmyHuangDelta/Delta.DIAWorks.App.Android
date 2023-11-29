package com.delta.android.Sample.Client;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delta.android.R;

import java.util.ArrayList;
import java.util.List;

//http://givemepass.blogspot.com/2011/11/alertdialog.html
public class DialogTestActivity extends AppCompatActivity {

    int singleChoiceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_dialog_test);

        Button btnDialog1 = findViewById(R.id.btnDialog1);

        btnDialog1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(DialogTestActivity.this)
                        .setTitle("title")
                        .setMessage("message")
                        .setPositiveButton("btn1", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "按下btn1", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("btn2", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "按下btn2", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNeutralButton("btn3", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "按下btn3", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });


        Button btnDialog2 = findViewById(R.id.btnDialog2);
        btnDialog2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<String> lunch = new ArrayList<String>();
                lunch.add("A");
                lunch.add("B");
                lunch.add("C");
                lunch.add("D");
                lunch.add("E");
                lunch.add("F");
                lunch.add("G");
                lunch.add("H");
                lunch.add("I");
                lunch.add("J");
                lunch.add("K");
                lunch.add("L");
                lunch.add("M");


                new AlertDialog.Builder(DialogTestActivity.this)
                        .setItems(lunch.toArray(new String[lunch.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = lunch.get(which);
                                Toast.makeText(getApplicationContext(), "按下" + name, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

            }
        });


        Button btnDialog3 = findViewById(R.id.btnDialog3);
        btnDialog3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<String> lunch = new ArrayList<String>();
                lunch.add("A");
                lunch.add("B");
                lunch.add("C");
                lunch.add("D");
                lunch.add("E");
                lunch.add("F");
                lunch.add("G");
                lunch.add("H");
                lunch.add("I");
                lunch.add("J");
                lunch.add("K");
                lunch.add("L");
                lunch.add("M");


                new AlertDialog.Builder(DialogTestActivity.this)
                        .setSingleChoiceItems(lunch.toArray(new String[lunch.size()]), singleChoiceIndex,//預設選取項目
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        singleChoiceIndex = which;

                                    }
                                })
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });


        Button btnDialog4 = findViewById(R.id.btnDialog4);
        btnDialog4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<String> lunch = new ArrayList<String>();
                lunch.add("A");
                lunch.add("B");
                lunch.add("C");
                lunch.add("D");
                lunch.add("E");
                lunch.add("F");
                lunch.add("G");
                lunch.add("H");
                lunch.add("I");
                lunch.add("J");
                lunch.add("K");
                lunch.add("L");
                lunch.add("M");

                new AlertDialog.Builder(DialogTestActivity.this)
                        .setMultiChoiceItems(lunch.toArray(new String[lunch.size()]), new boolean[lunch.size()],
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked) {
                                            Toast.makeText(DialogTestActivity.this, "你勾選了" + lunch.get(which), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(DialogTestActivity.this, "你取消勾選了" + lunch.get(which), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).show();

            }
        });


    }

}
