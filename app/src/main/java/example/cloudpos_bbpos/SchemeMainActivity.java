package example.cloudpos_bbpos;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.bblib.interfaces.Constant;

import example.cloudpos_bbpos.YaadPay.ExitActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;

public class SchemeMainActivity extends Activity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if( getIntent().getBooleanExtra("Exit me", false)){
            getIntent().addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION
            );
            finish();
            overridePendingTransition(0,0);
            return; // add this to prevent from doing unnecessary stuffs
        }

        boolean flag = requestLocationPermission();
        Log.e("SecurePay", "Permissions: " + (flag?"Granted":"Did not Granted") );
        ((TextView)findViewById(R.id.text1)).append(getAndroidVersion());
        ((TextView)findViewById(R.id.text1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   ExitActivity.exit(SchemeMainActivity.this);
                safeExit2();
            }
        });

    }


    public void safeExit() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }


    private void safeExit2(){
        if(Build.VERSION.SDK_INT>=17 && Build.VERSION.SDK_INT<21){
            finishAffinity();
        } else if(Build.VERSION.SDK_INT>=21){
            finishAndRemoveTask();
        }
        overridePendingTransition(0,0);
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        safeExit2();
    }

    public String getAndroidVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return "\nCurrent Version:  [" +  version +"],  \nLib :["+ Constant.VERSION_CODE +"]";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    private final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public boolean requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(SchemeMainActivity.this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            EasyPermissions.requestPermissions(SchemeMainActivity.this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
            return false;
        }
    }
}
