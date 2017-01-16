package com.scorpion.sleep.Uploading;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.scorpion.sleep.util.NetworkManager;
import com.scorpion.sleep.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UploadActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FileAdapter _fileListAdapter;
    private ListView _filesListView;
    private List<File> _filesList;
    private TextView _noFilesTextView;
    private Context _context;

    private AlertDialog.Builder _progressDialog;
    private ProgressBar _progressBar;
    private TextView _progressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setTitle("Upload Image");
        _context = this;
        _filesListView = (ListView) findViewById(R.id.files_listview);
        _noFilesTextView = (TextView) findViewById(R.id.no_files_textview);
        _filesList = new ArrayList<File>();

        _fileListAdapter = new FileAdapter(_filesList);
        _filesListView.setAdapter(_fileListAdapter);
        _filesListView.setOnItemClickListener(this);

        loadFilesIntoAdapter();
//        createProgressDialog();
    }

    private void loadFilesIntoAdapter()
    {
        String baseFilepath = Environment.getExternalStorageDirectory().toString();  //得到SD卡根目录
        File baseDirectory = new File(baseFilepath, "H0");   //打开目录，如不存在则生成
        File[] files = baseDirectory.listFiles();
        Toast.makeText(_context, baseFilepath + "/H0", Toast.LENGTH_LONG).show();
        if (baseDirectory.exists()) {
            if (files == null) {
                Log.e("FILEPATH", "No files found!");
                showNoFilesError(true);
                return;
            }

            _filesList.clear();
            _filesList.addAll(Arrays.asList(files));
            sortByNewest();
            _fileListAdapter.notifyDataSetChanged();

        } else {
            Log.e("FILEPATH", "BaseDirectory does not exist! " + baseDirectory.getAbsolutePath());
            showNoFilesError(true);
        }
    }

    private void sortByNewest()
    {
        Collections.sort(_filesList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                long file1LastModified = file.lastModified();
                long file2LastModified = t1.lastModified();

                if (file1LastModified < file2LastModified) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void showNoFilesError(boolean visible)
    {
        if (visible) {
            _noFilesTextView.setVisibility(View.VISIBLE);
        } else {
            _noFilesTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File clicked = _filesList.get(i);
        uploadFile(clicked);
    }

    private void uploadFile(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUploader.sendFileToServer(file.getAbsolutePath(), NetworkManager.UPLOAD_URL, uploadListener);
            }
        }).start();
    }

    private FileUploader.Listener uploadListener = new FileUploader.Listener() {
        @Override
        public void onUploadStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createProgressDialog();
                }
            });
        }

        @Override
        public void onUploadProgressChange(final int completionPercentage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_progressDialog != null) {
                        modifyProgressDialog(completionPercentage);
                    }
                }
            });
        }

        @Override
        public void onUploadComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_context, "Upload complete!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onUploadFailed(Throwable e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_context, "Upload failed...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void createProgressDialog() {
        View view = LayoutInflater.from(_context).inflate(R.layout.upload_dialog, null);
        _progressBar = (ProgressBar) view.findViewById(R.id.upload_progressBar);
        _progressTextView = (TextView) view.findViewById(R.id.upload_progresstext);
        modifyProgressDialog(0);

        _progressDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        _progressDialog.show();
    }

    private void modifyProgressDialog(int percentage) {
        _progressBar.setProgress(percentage);
        _progressTextView.setText(percentage + " percent complete");
    }
}
