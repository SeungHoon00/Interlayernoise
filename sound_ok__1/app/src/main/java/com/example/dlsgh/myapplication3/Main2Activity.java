package com.example.dlsgh.myapplication3;

import java.io.File;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorer extends Activity {

    String mSdPath ="";


public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String result = new String("");
        ListView tv = (ListView) findViewById(R.id.Find_ListView);


        String ext = Environment.getExternalStorageState();

        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            mSdPath = Environment.MEDIA_UNMOUNTED;
        }

        try {
            File f = new File(mSdPath);

            String[] filenames = f.list(null);

            for (int i = 0; i < filenames.length; i++) {
                String filename = filenames[i];
                if (filename.toLowerCase().endsWith("jpg")) {
                    result = result + filename + "\n";
                }
            }

            tv.setText(result);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
}
