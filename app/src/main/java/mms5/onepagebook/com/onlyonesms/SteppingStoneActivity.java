package mms5.onepagebook.com.onlyonesms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;

import me.leolin.shortcutbadger.ShortcutBadger;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class SteppingStoneActivity extends AppCompatActivity implements Constants {
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        ShortcutBadger.removeCount(getApplicationContext());
        Utils.PutSharedPreference(getApplicationContext(), PREF_BADGE_CNT, 0);

        boolean isWorking = false;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList"));
        isWorking = tryActivityIntent(mContext, intent);
        if (!isWorking) {
            intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ConversationComposer"));
            isWorking = tryActivityIntent(mContext, intent);
        }
        if (!isWorking) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setType("vnd.android-dir/mms-sms");
            tryActivityIntent(mContext, intent);
        }

        finish();
    }

    private boolean tryActivityIntent(Context context, Intent activityIntent) {
        try {
            if (activityIntent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(activityIntent);
                return true;
            }
        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
