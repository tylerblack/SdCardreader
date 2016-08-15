package com.example.tyler.sdcardreader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    public Handler mHandler;

    public Button mSearch;
    public boolean isSearching = false;
    public volatile boolean stopThread = false;


    public volatile Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {

            Collection files = FileUtils.listFiles(Environment.getExternalStorageDirectory(), null, true);
            long size = 0;
            int num = 0;

            final ArrayList<File> fileList = new ArrayList<>();
            final ArrayList<Pair<String, Integer>> frequency = new ArrayList<>();
            HashMap<String, Integer> fileTypes = new HashMap<>();

            for (Object file : files) {
                if (stopThread) return;
                Thread thisThread = Thread.currentThread();
                if (thisThread != thread) return;
                final int averageSize;
                if (num != 0) {
                    averageSize = (int) (size / num);
                } else {
                    averageSize = 0;
                }


                final File f = (File) file;
                if (f.isFile()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView current = (TextView) MainActivity.this.findViewById(R.id.current_file);
                            current.setText(f.getName());
                            TextView average = (TextView) MainActivity.this.findViewById(R.id.average_size);
                            average.setText("" + averageSize);
                            TextView big = (TextView) MainActivity.this.findViewById(R.id.biggest_files);
                            if (fileList.size() > 0) {
                                big.setText(fileList.get(0).getName() + ": "+ fileList.get(0).length());
                            }
                            TextView frequentType = (TextView) MainActivity.this.findViewById(R.id.frequent_extensions);
                            if (frequency.size() > 0) {
                                frequentType.setText(frequency.get(0).first + ": " + frequency.get(0).second);
                            }


                        }
                    });

                    size += f.length();
                    num++;
                    String name = f.getName();
                    String fileType = name.substring(name.lastIndexOf(".") + 1, name.length());
                    int typeCount;

                    if (fileTypes.containsKey(fileType)){
                        typeCount = fileTypes.get(fileType);
                    }
                    else {
                        typeCount = 0;
                    }

                    fileTypes.put(fileType, (typeCount+1));
                    fileList.add(f);
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File lhs, File rhs) {
                            return (int) (rhs.length() - lhs.length());
                        }
                    });


                    Set<String> types =fileTypes.keySet();
                    for (String s : types){
                        frequency.add(new Pair<>(s, fileTypes.get(s)));
                    }
                    Collections.sort(frequency, new Comparator<Pair<String, Integer>>() {
                        @Override
                        public int compare(Pair<String, Integer> lhs, Pair<String, Integer> rhs) {
                            int comp = (rhs.second - lhs.second);
                            return (comp);
                        }
                    });

                  }

            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mSearch = (Button) findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearching) {
                    if (!thread.isAlive()) {
                        stopThread = false;
                        isSearching = true;
                        thread.start();
                    }
                }
                else{
                    isSearching = false;
                    stopThread = true;
                }

            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        stopThread = true;
        isSearching = false;
    }

}
