package mms5.onepagebook.com.onlyonesms;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;

public class LogActivity extends AppCompatActivity {
  private TextView mTextLogs;
  private View mBtnDelete;

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
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mTextLogs = findViewById(R.id.text_logs);
    mBtnDelete = findViewById(R.id.btn_delete);
    mBtnDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new DeleteTask().execute();
      }
    });

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
  }

  private class LoadTask extends AsyncTask<Void, Void, String> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mBtnDelete.setVisibility(View.GONE);
    }

    @Override
    protected String doInBackground(Void... voids) {
      String logs = RealmManager.loadLogs(Realm.getDefaultInstance()).toString();
      logs = logs.replace(",", "");
      return logs;
    }

    @Override
    protected void onPostExecute(String logs) {
      super.onPostExecute(logs);
      mTextLogs.setText(logs);
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
}
