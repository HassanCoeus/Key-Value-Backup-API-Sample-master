package org.mightyfrog.android.minimal.backupapi;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Backup API sample code.
 *
 * @author Shigehiro Soejima
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "BackupRestoreActivity";

    public static final String PREFS = "prefs";

    public static final String KEY = "key";

    private EditText edit;
    private BackupManager backupMgr = null;

    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        edit = (EditText) findViewById(R.id.editText);
        edit.addTextChangedListener(new TextWatcher() {

            private int start = 0;
            private int end = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(MainActivity.KEY, s.toString());
                editor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                this.start = after;
                this.end = after;
            }

            @Override
            public void afterTextChanged(Editable s) {
                edit.setSelection(start, end);
            }
        });

        backupMgr = new BackupManager(getApplicationContext());

    }

    @Override
    protected void onPause() {
        new BackupManager(this).dataChanged(); // creates and enqueues a backup
        Log.d(TAG, "onPause");
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        String val = sharedPrefs.getString(KEY, null);
        if (val != null) {
            edit.setText(val);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create:
                createTestFiles();
                break;
            case R.id.restore:
                new BackupManager(this).requestRestore(new RestoreObserver() {
                    @Override
                    public void restoreFinished(int error) {
                        if (error == 0) {
                            Toast.makeText(MainActivity.this, R.string.files_restored, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.restore_failed, error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    //
    //

    /**
     * Creates three test files in the external storage directory.
     */
    private void createTestFiles() {
        final File one = new File(Environment.getExternalStorageDirectory(), "one.txt");
        final File two = new File(Environment.getExternalStorageDirectory(), "two.txt");
        final File three = new File(Environment.getExternalStorageDirectory(), "three.txt");
        try {
            one.createNewFile();
            two.createNewFile();
            three.createNewFile();
            Toast.makeText(this, getString(R.string.files_created, one.getParentFile()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            android.util.Log.e(MainActivity.class.getSimpleName(), "" + e);
        }
    }


    public void backup(View v) {
        Log.d(TAG, "backup");
        backupMgr.dataChanged();
    }

    public void restore(View v) {
        Log.d(TAG, "restore");
        backupMgr.requestRestore(new RestoreObserver() {
            @Override
            public void restoreStarting(int numPackages) {
                Log.d(TAG, "restoreStarting: " + numPackages);
                super.restoreStarting(numPackages);
            }

            @Override
            public void restoreFinished(int error) {
                Log.d(TAG, "restoreFinished: " + error);
                super.restoreFinished(error);
            }

            @Override
            public void onUpdate(int nowBeingRestored, String currentPackage) {
                Log.d(TAG, "onUpdate: " + currentPackage);
                super.onUpdate(nowBeingRestored, currentPackage);
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY)) {
            String newVal = sharedPreferences.getString(key, null);
            if (newVal != null) {
                edit.setText(newVal);
            }
        }
    }
}
