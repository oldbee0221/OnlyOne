package mms5.onepagebook.com.onlyonesms;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-07-14.
 */
public class CBMUpdateActivity extends AppCompatActivity implements Constants, View.OnClickListener {

    private final int REQUEST_IMAGE_ALBUM = 201;
    private final int REQUEST_IMAGE_CAPTURE = 202;
    private final int REQUEST_IMAGE_CROP = 203;

    private Context mContext;

    private Uri mContentUri;
    private String mCurrentPhotoPath;
    private String mRealPath;
    private Bitmap mBmPhoto;

    private int mPhotoGetMode;

    private CallMsg dMsg = new CallMsg();

    private ImageView iv_photo, iv_delete;
    private FrameLayout fl_photo;
    private EditText edt_msg, edt_category, edt_title;
    private Button btn_save;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_reg2);

        mContext = getApplicationContext();

        iv_photo = findViewById(R.id.iv_photo);
        iv_delete = findViewById(R.id.iv_delete);

        edt_msg = findViewById(R.id.edt_msg);
        edt_category = findViewById(R.id.edt_category);
        edt_title = findViewById(R.id.edt_title);


        btn_save = findViewById(R.id.btn_save);
        btn_save.setText(R.string.update);
        btn_save.setOnClickListener(this);


        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.iv_menu).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);

        fl_photo = findViewById(R.id.fl_photo);
        fl_photo.setOnClickListener(this);

        dMsg = (CallMsg)getIntent().getSerializableExtra("data");
        edt_msg.setText(dMsg.contents);
        edt_category.setText(dMsg.category);
        edt_title.setText(dMsg.title);

        mCurrentPhotoPath = dMsg.imgpath;

        if(!TextUtils.isEmpty(mCurrentPhotoPath)) {
            iv_delete.setVisibility(View.VISIBLE);
            mBmPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
            GlideApp.with(this)
                    .load(mBmPhoto)
                    .into(iv_photo);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (rotatePhoto()) {
                        CropImage.activity(mContentUri).setGuidelines(CropImageView.Guidelines.ON).start(CBMUpdateActivity.this);
                    } else {
                        Message msg = new Message();
                        msg.what = REQUEST_IMAGE_CAPTURE;
                        mHandler.sendMessage(msg);
                    }
                    break;

                case REQUEST_IMAGE_ALBUM:
                    mContentUri = data.getData();
                    mRealPath = getPath(mContentUri);
                    CropImage.activity(mContentUri).setGuidelines(CropImageView.Guidelines.ON).start(CBMUpdateActivity.this);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == AppCompatActivity.RESULT_OK) {
                        Uri resultUri = result.getUri();
                        mCurrentPhotoPath = resultUri.getPath();

                        iv_delete.setVisibility(View.VISIBLE);
                        mBmPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
                        GlideApp.with(this)
                                .load(mBmPhoto)
                                .into(iv_photo);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        Utils.Log("CropImage error => " + error.getMessage());
                    }
                    break;

                case REQUEST_CODE_MSGBOX:
                    if (resultCode == Activity.RESULT_OK) {
                        finish();
                    }
                    break;

                case REQUEST_CODE_IMGBOX:
                    if (resultCode == Activity.RESULT_OK) {
                        mCurrentPhotoPath = data.getStringExtra(EXTRA_IMG_PATH);
                        iv_photo.setVisibility(View.VISIBLE);
                        mBmPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath);
                        GlideApp.with(this)
                                .load(mBmPhoto)
                                .into(iv_photo);
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
            case R.id.btn_save:
                save();
                break;

            case R.id.btn_cancel:
                finish();
                break;

            case R.id.fl_photo:
                if(mCurrentPhotoPath == null) {
                    onClickImageLoad();
                } else {
                    qDeletePhoto();
                }
                break;

            case R.id.iv_menu:
                startActivity(new Intent(CBMUpdateActivity.this, LogActivity.class));
                break;

            case R.id.iv_home:
                finish();
                break;
        }
    }

    private void save() {
        dMsg.category = edt_category.getText().toString();
        dMsg.title = edt_title.getText().toString();
        dMsg.contents = edt_msg.getText().toString();

        if (Utils.IsEmpty(dMsg.category)) {
            Toast.makeText(getApplicationContext(), R.string.please_input_category, Toast.LENGTH_LONG).show();
            return;
        }

        if (Utils.IsEmpty(dMsg.title)) {
            Toast.makeText(getApplicationContext(), R.string.please_input_title, Toast.LENGTH_LONG).show();
            return;
        }

        if (Utils.IsEmpty(dMsg.contents)) {
            Toast.makeText(getApplicationContext(), R.string.please_input_msg, Toast.LENGTH_LONG).show();
            return;
        }

        dMsg.imgpath = mCurrentPhotoPath;

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(mContext).getCallMsgDao().update(dMsg);
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
                switch (i) {
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

    private void qDeletePhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CBMUpdateActivity.this);
        builder.setCancelable(false);

        builder.setTitle(getString(R.string.img_delete));
        builder.setMessage(getString(R.string.q_img_delete));

        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                iv_photo.setImageDrawable(null);
                iv_delete.setVisibility(View.GONE);
                fl_photo.setBackgroundResource(R.drawable.box_cancel);
                mCurrentPhotoPath = null;
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });

        if (CBMUpdateActivity.this.isFinishing() == false) {
            builder.show();
        }
    }


    private void getPhoto() {
        if (mPhotoGetMode == REQUEST_IMAGE_ALBUM) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_ALBUM);
        } else if (mPhotoGetMode == REQUEST_IMAGE_CAPTURE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchTakePictureIntentEx() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFileEx();
                } catch (IOException e) {
                    Utils.Log("dispatchTakePictureIntentEx() " + e.toString());
                }

                if (photoFile != null) {
                    mContentUri = FileProvider.getUriForFile(this, "mms5.onepagebook.com.onlyonesms.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mContentUri);
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
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/onepagebook/");
        if (!storageDir.exists()) {
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
                if (mContentUri == null) {
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
            switch (msg.what) {
                case REQUEST_IMAGE_CAPTURE:
                    Toast.makeText(getApplicationContext(), R.string.donot_take_photo, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
