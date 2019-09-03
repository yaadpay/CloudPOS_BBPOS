package example.cloudpos_bbpos;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;

public class SchemeMainActivity extends Activity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }

        ((TextView)findViewById(R.id.text1)).append(getAndroidVersion());

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SchemeMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this will clear all the stack
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
        //super.onBackPressed();
    }

    public String getAndroidVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return "\nCurrent Version:  [" +  version +"]";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
