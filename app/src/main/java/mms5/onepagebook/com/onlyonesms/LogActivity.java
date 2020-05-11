package mms5.onepagebook.com.onlyonesms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.realm.Realm;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;

public class LogActivity extends AppCompatActivity {
    private TextView mTextLogs;
    private View mBtnDelete;
    private TextView mTextStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        setContentView(R.layout.activity_log);

        mTextLogs = findViewById(R.id.text_logs);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteTask().execute();
            }
        });

        mTextStatus = findViewById(R.id.log_status);
        mTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgree();
            }
        });

        if(MainActivity.HAS_TO_SHOW_LOGS) {
            mTextStatus.setText(getString(R.string.log_on));
        } else {
            mTextStatus.setText(getString(R.string.log_off));
        }

        new LoadTask().execute();

        findViewById(R.id.btn_send_to_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "[onlyonesms] Logs");
                String data = mTextLogs.getText().toString();
                intent.putExtra(Intent.EXTRA_TEXT, data);
                try {
                    startActivity(Intent.createChooser(intent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(LogActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.tv_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogActivity.this, HomeActivity.class));
                finishAffinity();
            }
        });

        showAgree();
    }

    private class LoadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBtnDelete.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ArrayList<String> temp = RealmManager.loadLogs(Realm.getDefaultInstance());
            String logs = "";
            if(temp.size() > 0) {
                logs = temp.toString();
                logs = logs.replace(",", "");
            }

            return logs;
        }

        @Override
        protected void onPostExecute(String logs) {
            super.onPostExecute(logs);
            if(TextUtils.isEmpty(logs)) {
                mTextLogs.setText(getString(R.string.log_warning));
            } else {
                mTextLogs.setText(logs);
            }

            mBtnDelete.setVisibility(View.VISIBLE);
        }
    }

    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBtnDelete.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            RealmManager.deleteLogs(Realm.getDefaultInstance());
            return null;
        }

        @Override
        protected void onPostExecute(Void logs) {
            super.onPostExecute(logs);
            mTextLogs.setText("");
            mBtnDelete.setEnabled(true);
        }
    }

    private void showAgree() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(getString(R.string.log_title));
        ab.setMessage(getString(R.string.log_guide));
        ab.setPositiveButton(getString(R.string.log_on), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.HAS_TO_SHOW_LOGS = true;
                mTextStatus.setText(getString(R.string.log_on));
            }
        });
        ab.setNegativeButton(getString(R.string.log_off), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.HAS_TO_SHOW_LOGS = false;
                mTextStatus.setText(getString(R.string.log_off));
            }
        });
        ab.show();
    }
}
