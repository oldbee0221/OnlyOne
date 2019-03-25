package mms5.onepagebook.com.onlyonesms;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import me.leolin.shortcutbadger.ShortcutBadger;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class SteppingStoneActivity extends AppCompatActivity implements Constants {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShortcutBadger.removeCount(getApplicationContext());
        Utils.PutSharedPreference(getApplicationContext(), PREF_BADGE_CNT, 0);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.ConversationList"));
        startActivity(intent);
        finish();
    }
}
