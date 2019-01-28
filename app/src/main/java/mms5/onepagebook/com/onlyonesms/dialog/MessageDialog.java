package mms5.onepagebook.com.onlyonesms.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import mms5.onepagebook.com.onlyonesms.R;

public class MessageDialog extends Dialog {
  private View.OnClickListener mCompleteListener;

  public MessageDialog(Context context) {
    super(context);
  }

  public static void showMessage(Activity activity, View.OnClickListener listener) {
    MessageDialog dialog = new MessageDialog(activity);
    dialog.setCompleteListener(listener);
    dialog.show();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    View v = getWindow().getDecorView();
    v.setBackgroundResource(android.R.color.transparent);
    setContentView(R.layout.view_message_dialog);

    TextView mTextPositive = findViewById(R.id.btn_apply);
    mTextPositive.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();

        if (mCompleteListener != null) {
          mCompleteListener.onClick(view);
        }
      }
    });

    findViewById(R.id.btn_not_apply).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
  }

  public void setCompleteListener(View.OnClickListener listener) {
    mCompleteListener = listener;
  }
}
