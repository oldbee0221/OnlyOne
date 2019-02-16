package mms5.onepagebook.com.onlyonesms;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.base.GlideApp;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMRegActivity extends AppCompatActivity implements Constants, View.OnClickListener,
        TimePickerDialog.OnTimeSetListener, CompoundButton.OnCheckedChangeListener {

    private final int START_TIME = 1000;
    private final int END_TIME = 1001;

    private final int REQUEST_IMAGE_ALBUM = 201;
    private final int REQUEST_IMAGE_CAPTURE = 202;
    private final int REQUEST_IMAGE_CROP = 203;

    private Context mContext;

    private Uri mContentUri;
    private String mCurrentPhotoPath;
    private String mRealPath;
    private Bitmap mBmPhoto;

    private boolean[] days_week = new boolean[7];
    private boolean mAllDay = false;
    private int mWhichTime = START_TIME;
    private int mHourS = 0, mMinS = 0, mHourE = 0, mMinE = 0;
    private int mPhotoGetMode;

    private Msg dMsg = new Msg();

    private TextView[] tv_days_week = new TextView[7];
    private View[] v_days_week = new View[7];
    private TextView tv_start, tv_end;
    private ImageView iv_photo;
    private CheckBox cb_send_abs, cb_all_day, cb_ad;
    private Spinner spn_sendtype, spn_sendoption;
    private EditText edt_msg1, edt_msg2;
    private LinearLayout ll_img_del;
    private LinearLayout ll_img_load;
    private LinearLayout ll_img_box;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_reg);

        mContext = getApplicationContext();

        dMsg.msgType = getIntent().getStringExtra(Constants.EXTRA_CB_MSGTYPE);

        for(int i=0; i<7; i++) {
            days_week[i] = false;
        }

        findViewById(R.id.ll_monday).setOnClickListener(this);
        findViewById(R.id.ll_tuesday).setOnClickListener(this);
        findViewById(R.id.ll_wednesday).setOnClickListener(this);
        findViewById(R.id.ll_thursday).setOnClickListener(this);
        findViewById(R.id.ll_friday).setOnClickListener(this);
        findViewById(R.id.ll_sunday).setOnClickListener(this);
        findViewById(R.id.ll_saturday).setOnClickListener(this);

        findViewById(R.id.ll_msg_box).setOnClickListener(this);
        findViewById(R.id.ll_msg_save).setOnClickListener(this);

        iv_photo = findViewById(R.id.iv_photo);

        tv_days_week[0] = findViewById(R.id.tv_monday);
        tv_days_week[1] = findViewById(R.id.tv_tuesday);
        tv_days_week[2] = findViewById(R.id.tv_wednesday);
        tv_days_week[3] = findViewById(R.id.tv_thursday);
        tv_days_week[4] = findViewById(R.id.tv_friday);
        tv_days_week[5] = findViewById(R.id.tv_saturday);
        tv_days_week[6] = findViewById(R.id.tv_sunday);

        v_days_week[0] = findViewById(R.id.v_monday);
        v_days_week[1] = findViewById(R.id.v_tuesday);
        v_days_week[2] = findViewById(R.id.v_wednesday);
        v_days_week[3] = findViewById(R.id.v_thursday);
        v_days_week[4] = findViewById(R.id.v_friday);
        v_days_week[5] = findViewById(R.id.v_saturday);
        v_days_week[6] = findViewById(R.id.v_sunday);

        tv_start = findViewById(R.id.tv_start);
        tv_end = findViewById(R.id.tv_end);
        tv_start.setOnClickListener(this);
        tv_end.setOnClickListener(this);
        tv_start.setText((mHourS < 10 ? "0" : "") + mHourS + ":" + (mMinS < 10 ? "0" : "") + mMinS);
        tv_end.setText((mHourE < 10 ? "0" : "") + mHourE + ":" + (mMinE < 10 ? "0" : "") + mMinE);

        cb_send_abs = findViewById(R.id.cb_send_abs);
        cb_all_day = findViewById(R.id.cb_all_day);
        cb_ad = findViewById(R.id.cb_ad);
        cb_send_abs.setOnCheckedChangeListener(this);
        cb_all_day.setOnCheckedChangeListener(this);
        cb_ad.setOnCheckedChangeListener(this);

        ll_img_del = findViewById(R.id.ll_img_del);
        ll_img_del.setOnClickListener(this);

        ll_img_load = findViewById(R.id.ll_img_load);
        ll_img_load.setOnClickListener(this);

        ll_img_box = findViewById(R.id.ll_img_box);
        ll_img_box.setOnClickListener(this);

        edt_msg1 = findViewById(R.id.edt_msg1);
        edt_msg2 = findViewById(R.id.edt_msg2);

        dMsg.allDayYn = "N";

        setSendTypeSpinner();
        setSendOptionSpinner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch(requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if(rotatePhoto()) {
                        CropImage.activity(mContentUri).setGuidelines(CropImageView.Guidelines.ON).start(CBMRegActivity.this);
                    } else {
                        Message msg = new Message();
                        msg.what = REQUEST_IMAGE_CAPTURE;
                        mHandler.sendMessage(msg);
                    }
                    break;

                case REQUEST_IMAGE_ALBUM:
                    mContentUri = data.getData();
                    mRealPath = getPath(mContentUri);
                    CropImage.activity(mContentUri).setGuidelines(CropImageView.Guidelines.ON).start(CBMRegActivity.this);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == AppCompatActivity.RESULT_OK) {
                        Uri resultUri = result.getUri();
                        mCurrentPhotoPath = resultUri.getPath();

                        iv_photo.setVisibility(View.VISIBLE);
                        mBmPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
                        GlideApp.with(this)
                                .load(mBmPhoto)
                                .into(iv_photo);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        Utils.Log("CropImage error => " + error.getMessage());
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch(vid) {
            case R.id.ll_monday:
                setDaysWeek(0);
                break;

            case R.id.ll_tuesday:
                setDaysWeek(1);
                break;

            case R.id.ll_wednesday:
                setDaysWeek(2);
                break;

            case R.id.ll_thursday:
                setDaysWeek(3);
                break;

            case R.id.ll_friday:
                setDaysWeek(4);
                break;

            case R.id.ll_saturday:
                setDaysWeek(5);
                break;

            case R.id.ll_sunday:
                setDaysWeek(6);
                break;

            case R.id.tv_start:
                if(!mAllDay) {
                    mWhichTime = START_TIME;
                    timePickerDlg();
                }
                break;

            case R.id.tv_end:
                if(!mAllDay) {
                    mWhichTime = END_TIME;
                    timePickerDlg();
                }
                break;

            case R.id.ll_img_del:
                mCurrentPhotoPath = "";
                iv_photo.setVisibility(View.GONE);
                break;

            case R.id.ll_img_load:
                onClickImageLoad();
                break;

            case R.id.ll_img_box:
                break;

            case R.id.ll_msg_box:
                break;

            case R.id.ll_msg_save:
                save();
                break;

        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int h, int m) {
        String tm;
        if(mWhichTime == START_TIME) {
            mHourS = h;
            mMinS = m;
            tm = (mHourS < 10 ? "0" : "") + mHourS + ":" + (mMinS < 10 ? "0" : "") + mMinS;
            tv_start.setText(tm);
            dMsg.startTime = tm;
        } else if(mWhichTime == END_TIME) {
            mHourE = h;
            mMinE = m;
            tm = (mHourE < 10 ? "0" : "") + mHourE + ":" + (mMinE < 10 ? "0" : "") + mMinE;
            tv_end.setText(tm);
            dMsg.endTime = tm;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int vid = compoundButton.getId();

        switch(vid) {
            case R.id.cb_all_day:
                mAllDay = b;

                if(mAllDay) {
                    tv_start.setTextColor(Color.parseColor("#66000000"));
                    tv_end.setTextColor(Color.parseColor("#66000000"));
                    dMsg.allDayYn = "Y";
                } else {
                    tv_start.setTextColor(Color.parseColor("#616161"));
                    tv_end.setTextColor(Color.parseColor("#616161"));
                    dMsg.allDayYn = "N";
                }
                break;

            case R.id.cb_send_abs:
                if(b) {
                    dMsg.sendOnAbsYn = "Y";
                } else {
                    dMsg.sendOnAbsYn = "N";
                }
                break;

            case R.id.cb_ad:
                if(b) {
                    dMsg.adYn = "Y";
                } else {
                    dMsg.adYn = "N";
                }
                break;
        }
    }

    private void setDaysWeek(int idx) {
        if(days_week[idx]) {
            days_week[idx] = false;
            tv_days_week[idx].setTextColor(Color.parseColor("#818181"));
            v_days_week[idx].setBackgroundColor(Color.parseColor("#818181"));
        } else {
            days_week[idx] = true;
            tv_days_week[idx].setTextColor(Color.parseColor("#eb3333"));
            v_days_week[idx].setBackgroundColor(Color.parseColor("#eb3333"));
        }
    }

    private void timePickerDlg() {
        int hour = 0;
        int min = 0;

        if(mWhichTime == START_TIME) {
            hour = mHourS;
            min = mMinS;
        } else if(mWhichTime == END_TIME) {
            hour = mHourE;
            min = mMinE;
        }

        TimePickerDialog pktmDlg = new TimePickerDialog(CBMRegActivity.this, this, hour, min, false);
        pktmDlg.show();
    }

    private void setSendTypeSpinner() {
        spn_sendtype = findViewById(R.id.spn_sendtype);

        ArrayAdapter<String> aa = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item2,
                getResources().getStringArray(R.array.send_types));
        aa.setDropDownViewResource(R.layout.custom_spinner_dropdown_item2);
        spn_sendtype.setAdapter(aa);

        spn_sendtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dMsg.sendType = Integer.toString(position+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSendOptionSpinner() {
        spn_sendoption = findViewById(R.id.spn_sendoption);

        ArrayAdapter<String> aa = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item2,
                getResources().getStringArray(R.array.send_options));
        aa.setDropDownViewResource(R.layout.custom_spinner_dropdown_item2);
        spn_sendoption.setAdapter(aa);

        spn_sendoption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dMsg.sendOption = Integer.toString(position+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void save() {
        dMsg.message1 = edt_msg1.getText().toString();
        dMsg.message2 = edt_msg2.getText().toString();

        if(Utils.IsEmpty(dMsg.message1)) {
            Toast.makeText(getApplicationContext(), R.string.please_input_msg, Toast.LENGTH_LONG).show();
            return;
        }

        if(Utils.IsEmpty(dMsg.message2)) {
            Toast.makeText(getApplicationContext(), R.string.please_input_msg, Toast.LENGTH_LONG).show();
            return;
        }

        StringBuffer sb = new StringBuffer();
        if(days_week[0]) sb.append("월 ");
        if(days_week[1]) sb.append("화 ");
        if(days_week[2]) sb.append("수 ");
        if(days_week[3]) sb.append("목 ");
        if(days_week[4]) sb.append("금 ");
        if(days_week[5]) sb.append("토 ");
        if(days_week[6]) sb.append("일");
        dMsg.dayOfWeek = sb.toString();

        dMsg.imgPath = mCurrentPhotoPath;
        dMsg.lastUpdateTime = System.currentTimeMillis();
        dMsg.useYn = "Y";

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(mContext).getMsgDao().updateUseYnYtoN(dMsg.msgType);
                AppDatabase.getInstance(mContext).getMsgDao().insert(dMsg);
                finish();
            }
        }).start();

        Utils.Log("mCurrentPhotoPath => " + mCurrentPhotoPath);
    }

    private void onClickImageLoad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] options = new String[2];
        options[0] = getString(R.string.photo_take);
        options[1] = getString(R.string.photo_capture);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               switch(i) {
                   case 0:
                       mPhotoGetMode = REQUEST_IMAGE_ALBUM;
                       getPhoto();
                       break;

                   case 1:
                       mPhotoGetMode = REQUEST_IMAGE_CAPTURE;
                       getPhoto();
                       break;
               }
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getPhoto() {
        if(mPhotoGetMode == REQUEST_IMAGE_ALBUM) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_ALBUM);
        } else if(mPhotoGetMode == REQUEST_IMAGE_CAPTURE) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dispatchTakePictureIntentEx();
            } else {
                dispatchTakePictureIntent();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Utils.Log("dispatchTakePictureIntent()" + ex.toString());
            }

            if (photoFile != null) {
                mContentUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,	Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchTakePictureIntentEx() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFileEx();
                } catch(IOException e) {
                    Utils.Log("dispatchTakePictureIntentEx() " + e.toString());
                }

                if(photoFile != null) {
                    mContentUri = FileProvider.getUriForFile(this, "com.amorepacific.apbeautytailor.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,	mContentUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath(); //나중에 Rotate하기 위한 파일 경로.

        return image;
    }

    private File createImageFileEx() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/onepagebook/");
        if(!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath(); //나중에 Rotate하기 위한 파일 경로.

        return image;
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(columnIndex);
    }

    private boolean rotatePhoto() {
        Utils.Log("rotatePhoto()");
        ExifInterface exif;
        try {
            if (mCurrentPhotoPath == null) {
                if(mContentUri == null) {
                    Utils.Log("rotatePhoto() - 1");
                    return false;
                } else {
                    mCurrentPhotoPath = mContentUri.getPath();
                }
            }
            exif = new ExifInterface(mCurrentPhotoPath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            if (exifDegree != 0) {
                Bitmap bitmap = getBitmap();
                Bitmap rotatePhoto = rotate(bitmap, exifDegree);
                saveBitmap(rotatePhoto);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Utils.Log("rotatePhoto() - 2");
            return false;
        }

        return true;
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }

        return 0;
    }

    private static Bitmap rotate(Bitmap image, int degrees) {
        if (degrees != 0 && image != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) image.getWidth(), (float) image.getHeight());

            try {
                Bitmap b = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);

                if (image != b) {
                    image.recycle();
                    image = b;
                }

                image = b;
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }

        return image;
    }

    private void saveBitmap(Bitmap bitmap) {
        File file = new File(mCurrentPhotoPath);

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inInputShareable = true;
        options.inDither = false;
        options.inTempStorage = new byte[32 * 1024];
        options.inPurgeable = true;
        options.inJustDecodeBounds = false;

        File f = new File(mCurrentPhotoPath);

        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bm = null;

        try {
            if (fs != null) bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bm;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case REQUEST_IMAGE_CAPTURE:
                    Toast.makeText(getApplicationContext(), R.string.donot_take_photo, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
