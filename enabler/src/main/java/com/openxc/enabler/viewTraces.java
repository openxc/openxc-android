package com.openxc.enabler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.openxcplatform.enabler.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class viewTraces extends Activity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_saved_data);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String dir = prefs.getString(getApplicationContext().getString(R.string.recording_directory_key), null);
        final String dirPath = Environment.getExternalStorageDirectory().getPath() + "/" + dir;

        listView = (ListView)findViewById(R.id.list);
        ArrayList<String> FilesInFolder = GetFiles(dirPath);
        Collections.sort(FilesInFolder, Collections.<String>reverseOrder());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, FilesInFolder);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {

                LayoutInflater layoutInflater = LayoutInflater.from(viewTraces.this);
                View promptView = layoutInflater.inflate(R.layout.prompt, null);

                final AlertDialog alertD = new AlertDialog.Builder(viewTraces.this).create();
                final String filename = listView.getItemAtPosition(position).toString();
                TextView filenameDialog = (TextView) promptView.findViewById(R.id.fileName);
                filenameDialog.setText(listView.getItemAtPosition(position).toString());
                Button btnView = (Button) promptView.findViewById(R.id.btnFileView);
                Button btnDelete = (Button) promptView.findViewById(R.id.btnFileDelete);

                alertD.setView(promptView);
                alertD.show();

                btnView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(dirPath + "/" +  filename);
                        intent.setDataAndType(uri, "text/plain");
                        startActivity(intent);
                        alertD.dismiss();
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        File file = new File(dirPath + "/" +  filename);
                        file.delete();
                        alertD.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                });
            }
        });
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        if(f.canRead())
        {
            f.mkdirs();
            File[] files = f.listFiles();
            if (files.length == 0)
                return null;
            else {
                for (File file : files) MyFiles.add(file.getName());
            }
        }
        return MyFiles;
    }

}