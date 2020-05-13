package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Transaction;

import org.apache.commons.text.StringEscapeUtils;

import java.io.FileNotFoundException;
import java.util.List;

import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.dialog.ProgressDialog;
import mms5.onepagebook.com.onlyonesms.util.Settings;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2020/04/07.
 */
public class CBMAutoSendActivity extends AppCompatActivity implements Constants {
    private final int HANDLER_SEND = 301;

    private Context mContext;

    private TextView mTvPhoneNumber;
    private TextView mTvName;
    private RelativeLayout mLayoutPhoneNumber;

    private List<CallMsg> mMsgs;
    private boolean mIsFromMsg;

    private Transaction mSendTransaction;
    private Settings mSettings;
    private CallMsg mMsgForSending;
    private String mSndNumber;
    private String mWhich;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbm_list);
        mContext = getApplicationContext();

        mTvPhoneNumber = findViewById(R.id.tv_phonenum);
        mTvName = findViewById(R.id.tv_name);
        mLayoutPhoneNumber = findViewById(R.id.ll_phonenum);

        Intent intent = getIntent();
        mWhich = intent.getStringExtra(EXTRA_WHICH);

        if(TextUtils.isEmpty(intent.getStringExtra(EXTRA_FROM_DOOR))) {
            mLayoutPhoneNumber.setVisibility(View.VISIBLE);
            mIsFromMsg = true;
            mSndNumber = intent.getStringExtra(EXTRA_SND_NUM);
            mTvPhoneNumber.setText(makePhonenum(mSndNumber));
            mTvName.setText(getDisplayName(mSndNumber));
            Utils.Log("CBMListActivity mSndNumber => " + mSndNumber);
        } else {
            mLayoutPhoneNumber.setVisibility(View.GONE);
            mIsFromMsg = false;
            mSndNumber = "";
        }

        mProgressDialog = new ProgressDialog(CBMAutoSendActivity.this);

        init();
    }

    private String makePhonenum(String p) {
        if(p.length() == 11) {
            return p.substring(0,3) + "-" + p.substring(3,7) + "-" + p.substring(7,11);
        } else if(p.length() == 10) {
            return p.substring(0,3) + "-" + p.substring(3,6) + "-" + p.substring(6,10);
        } else {
            return p;
        }
    }

    private String getDisplayName(String tel) {
        String name = "";

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(tel));
        String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };

        Cursor cursor = getBaseContext().getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }

        return name;
    }

    private void init() {
        if(!TextUtils.isEmpty(mSndNumber)) {
            if(mWhich.equals("call")) {
                final long regdate = Utils.GetLongSharedPreference(mContext, PREF_CB_AUTO_MSG);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgForSending = AppDatabase
                                .getInstance(mContext)
                                .getCallMsgDao()
                                .getRow(regdate);

                        prepareSending();
                    }
                }).start();
            } else if(mWhich.equals("text")) {
                final long regdate = Utils.GetLongSharedPreference(mContext, PREF_CB_AUTO_MSG2);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgForSending = AppDatabase
                                .getInstance(mContext)
                                .getCallMsgDao()
                                .getRow(regdate);

                        prepareSending();
                    }
                }).start();
            }
        }
    }

    private Bitmap resize(Context context, String path, int resize) {
        Bitmap resizeBitmap = null;

        Uri uri = Uri.parse("file://" + path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resizeBitmap;
    }

    private void prepareSending() {
        mSettings = Settings.get(getApplicationContext());
        com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
        sendSettings.setMmsc(mSettings.getMmsc());
        sendSettings.setProxy(mSettings.getMmsProxy());
        sendSettings.setPort(mSettings.getMmsPort());
        sendSettings.setUseSystemSending(true);
        mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

        ApnUtils.initDefaultApns(getApplicationContext(), new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                mSettings = Settings.get(getApplicationContext(), true);
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(mSettings.getMmsc());
                sendSettings.setProxy(mSettings.getMmsProxy());
                sendSettings.setPort(mSettings.getMmsPort());
                sendSettings.setUseSystemSending(true);

                mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

                new SendMsgTask().execute();
            }
        });
    }

    private class SendMsgTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //Utils.Log("SendMsgTask 1");
            //Utils.Log("SendMsgTask " + mMsgForSending.imgpath);
            //Utils.Log("SendMsgTask " + mMsgForSending.title);
            //Utils.Log("SendMsgTask " + mMsgForSending.contents);

            com.klinker.android.send_message.Message msg = new com.klinker.android.send_message.Message();
            if (!TextUtils.isEmpty(mMsgForSending.imgpath)) {
                msg.setImage(resize(mContext, mMsgForSending.imgpath, 640));
            }
            msg.setSubject(StringEscapeUtils.unescapeHtml4(mMsgForSending.title).replace("\\", ""));
            msg.setText(StringEscapeUtils.unescapeHtml4(mMsgForSending.contents).replace("\\", ""));
            msg.setAddress(mSndNumber);
            msg.setSave(false);

            mSendTransaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
            Utils.Log("SendMsgTask 2");

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Message msg = new Message();
            msg.what = HANDLER_SEND;
            handler.sendMessage(msg);
        }
    }

    private void showFinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CBMAutoSendActivity.this);
        builder.setCancelable(false);

        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.sended_msg));

        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                finish();
            }
        });

        if (CBMAutoSendActivity.this.isFinishing() == false) {
            builder.show();
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_SEND:
                    Utils.PutSharedPreference(mContext, PREF_CB_MSG_SENT, true);
                    showFinDialog();
                    break;
            }
        }
    };
}
