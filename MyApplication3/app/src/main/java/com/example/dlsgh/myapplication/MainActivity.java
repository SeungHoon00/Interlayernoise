package com.example.dlsgh.myapplication;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final ArrayList<String> EXTENSIONS = new ArrayList<>(Arrays.asList(".wav", ".pcm", ".mp4", ".kml"));

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int i=0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView)findViewById(R.id.listview);
        ArrayList<String> files = extensionFilter(Environment.getExternalStorageDirectory());
        ArrayAdapter<String> filelist = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,files);

        listView.setAdapter(filelist);

    }

    private ArrayList<String> extensionFilter(File folder) {
        ArrayList<String> result = new ArrayList<>();

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(extensionFilter(file));
                }
                else {
                    if (EXTENSIONS.contains(file.getName().substring(file.getName().lastIndexOf(".")))) {
                        result.add(file.toString());
                    }
                }
            }
        }

        return result;
    }
}
