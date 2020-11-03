package example.cloudpos_bbpos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.bblib.TransactionState;

import com.bbpos.bbdevice.bblib.activities.TranManagerActivity;
import com.bbpos.bbdevice.bblib.communication.BBCommunication;
import com.bbpos.bbdevice.bblib.interfaces.BBPosCallback;
import com.bbpos.bbdevice.bblib.interfaces.BBUtilCallBacks;
import com.bbpos.bbdevice.bblib.interfaces.Constant;
import com.bbpos.bbdevice.bblib.interfaces.LoadParamsCallback;
import com.bbpos.bbdevice.bblib.utilities.j;
import com.bbpos.bbdevice.bblib.utilities.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import example.cloudpos_bbpos.YaadPay.YaadPaymentActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.GENERAL_TIME_OUT;
import static example.cloudpos_bbpos.MainActivity.StatusCallback.STATUS_FAILED;
import static example.cloudpos_bbpos.MainActivity.StatusCallback.STATUS_SUCCESS;


public class SchemeActivity extends Activity  implements View.OnClickListener,  BBPosCallback {
    private static final String TOKEN_TAG = "PREV_T";
    /** Variable that communicates with our library thgough commands **/
    private BBCommunication bbCommunication;

    private HashMap<String , Boolean >  maps = new HashMap<>();
    private String hostLink = null;
    public static String sameTab = "True";
    private String SERIAL_NUMBER;
    private RelativeLayout alertContainer = null;
    private Runnable runnable = null;
    public static String CLOSED_APP = "closed";
    private boolean FORCE_CLEAR = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FORCE_CLEAR = false;
        overridePendingTransition(0,0);
        setContentView(R.layout.activity_main_scheme);

        if( getIntent().getBooleanExtra("Exit me", false)){
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION
            );
            Log.d(TAG, "onCreate: Blabla" );
            finish();
            overridePendingTransition(0,0);
            return; // add this to prevent from doing unnecessary stuffs
        }


        Button btnx =(Button) findViewById(R.id.btnX);
        alertContainer =(findViewById(R.id.alert_Container));
        alertContainer.setVisibility(View.GONE);
        //btnx.setOnClickListener(this);
        maps.put("pay",true);
        maps.put("connectBT",true);
        maps.put("disconnectBT",true);
        maps.put("setToken",true);
        maps.put("loadParam",true);
        maps.put("manualPay",true);

        SEND_BT_RESPONSE = true;

        final Uri data = getIntent().getData();

        runnable  = new Runnable() {
            @Override
            public void run() {
                if(data != null) {

                    Log.e(TAG, "onCreate: " + getIntent().getDataString());
                    try {

                        ((TextView) findViewById(R.id.text1)).setText(getIntent().getDataString());
                        ((TextView) findViewById(R.id.text1)).append("\n\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    lastAction = getIntent().getData().getAuthority(); // v2
                    //cloudpos://v2/connectBt/xP?successLink=example://success&failedLink=example://failed&force=true&
/*
            Log.e(APP_TAG, "onCreate: lastAction getAuthority =" + lastAction);//
            Log.e(APP_TAG, "onCreate: lastAction getPath =" + getIntent().getData().getPath());//  getPath =/connectBt/xP
            Log.e(APP_TAG, "onCreate: lastAction getEncodedPath =" + getIntent().getData().getEncodedPath());//onCreate: lastAction getEncodedPath =/connectBt/xP
            Log.e(APP_TAG, "onCreate: lastAction getHost =" + getIntent().getData().getHost());//  getHost =v2
            Log.e(APP_TAG, "onCreate: lastAction getQuery =" + getIntent().getData().getQuery());//  getQuery =successLink=example://success&failedLink=example://failed&force=true&
            Log.e(APP_TAG, "onCreate: lastAction getSchemeSpecificPart =" + getIntent().getData().getSchemeSpecificPart());//  getSchemeSpecificPart =//v2/connectBt/xP?successLink=example://success&failedLink=example://failed&force=true&
            Log.e(APP_TAG, "onCreate: lastAction getFragment =" + getIntent().getData().getFragment());//  getFragment =null
            Log.e(APP_TAG, "onCreate: lastAction getLastPathSegment =" + getIntent().getData().getLastPathSegment());//  getLastPathSegment =xP
            Log.e(APP_TAG, "onCreate: lastAction =========================================== getEncodedPath =" + getIntent().getData().getEncodedPath());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getEncodedPath =" + getIntent().getData().getEncodedPath());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getEncodedQuery =" + getIntent().getData().getEncodedQuery());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getEncodedSchemeSpecificPart =" + getIntent().getData().getEncodedSchemeSpecificPart());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getData =" + getIntent().getData());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getData1 =" + getIntent().getData().getEncodedUserInfo());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getData2 =" + getIntent().getData().getEncodedFragment());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getData2 =" + getIntent().getData().toString());//  encodedPath =xP
            Log.e(APP_TAG, "onCreate: lastAction getData2 =" + getIntent().toString());//  encodedPath =xP
*/


                    isSuccess = false;
                    attachCallbacks();

                    String path = getIntent().getData().getPath();
                    String pths[] = path.split("/");
                    String version = lastAction;
                    try{
                        if(pths[1] != null){
                            lastAction = pths[1];
                        }
                    }catch (Exception e){
                    }
                    Log.e(TAG, "onCreate: GotURL: " + lastAction );
                    Toast.makeText(SchemeActivity.this, "Command: " + lastAction, Toast.LENGTH_SHORT).show();
                    switch (version){
                        case "v1":
                            handleCommandV1(lastAction);
                            break;
                        case "v2":
                            handleCommandV2(lastAction);
                            break;
                        default:
                            handleCommand(lastAction);
                    }
            /*
            for(String sc : pths){
                Log.e(TAG, "onCreate: ["  + sc+ "]");
            }
*/

                }

            }
        };
        boolean flag = requestLocationPermission();
        Log.e(TAG, "onCreate: permission check:" + flag );
        if(flag){
            new Handler().post(runnable);
        }

        /** Buttons initialize **/
        TextView button = (TextView) findViewById(R.id.openPaymentManager);
        button.setOnClickListener(this);

        button = (TextView) findViewById(R.id.connect_Bt);
        button.setOnClickListener(this);

        button = (TextView) findViewById(R.id.disconnect_Bt);
        button.setOnClickListener(this);

        button = (TextView) findViewById(R.id.loadParamsButton);
        button.setOnClickListener(this);

        button = (TextView) findViewById(R.id.openMSRThread);

        button.setOnClickListener(this);

        button = (TextView) findViewById(R.id.clear_edittexts);
        button.setOnClickListener(this);
    }

    int PRIVATE_MODE = 0;

    private void handleCommandV1(String command) {
        if(maps.get(command) == null){
            exit("command_not_found");
            Log.e(APP_TAG, "handleCommand: Command not found: [" + command +"]" );
            return;
        }

        String[] splited = getIntent().getData().getQuery().split("&");
        switch (command) {
            case "disconnectBT":/** ======================= Disconnect BT  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }
                if (!bbCommunication.isBtConnected()) {
                    exit("bt_already_disconnected");//connectBt
                    return;
                }
                disconnectBt();
                exit("bt_disconnected");//connectBt
                break;
            case "connectBT":/** ======================= Connect BT  ====================== **/

                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;

                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }
                if (bbCommunication.isBtConnected()) {
                    isSuccess = true;//exit("bt_already_connected");//connectBt
                    exit(TransactionState.getInstance().SERIAL_BUILD);//connectBt
                    return;
                }
                connectBt();


                break;

            case "loadParam": /** ======================= Load Param  ====================== **/
                boolean isForced = false;
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n LOAD PARAM- KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;

                        case "force":
                            try {
                                isForced = Boolean.parseBoolean(x[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();

                    exit("param_missing_successURL");//loadParams
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();

                    exit("param_missing_failedUrl");//loadParams
                    return;
                }


                loadParametersFunction(new LoadParamsCallback() {

                    //Actually paramloaded button
                    @Override
                    public void onParamLoaded(byte statusResult) {
                        switch (statusResult) {
                            case LoadParamsCallback.LOAD_PARAM_FAILED:
                                FORCE_CLEAR = true;
                                exit("load_param_failed");//loadParams
                                Log.d(TAG, "onParamLoaded: 11");

                                //  //Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", //Toast.LENGTH_SHORT).show();
                                break;
                            case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                isSuccess = true;
                                exit("load_param_success");//loadParams
                                //  //Toast.makeText(MainActivity.this, "Load Finish", //Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, isForced);


                break;

            case "setToken": /** ======================= Set Token  ====================== **/
                String res = "param_missing_token";
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;

                        case "token":
                            bbCommunication.updateToken(x[1]);
                            setTokenToMemory(TOKEN_TAG,x[1]);
                            isSuccess = true;
                            res = "token_updated";
                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//setToken
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//setToken
                    return;
                }
                if(res.equals("token_updated")){
                    if(BBCommunication.getInstance().isParamLoaded(this)){
                        loadParametersFunction(new LoadParamsCallback() {
                            @Override
                            public void onParamLoaded(byte statusResult) {
                                switch (statusResult) {
                                    case LoadParamsCallback.LOAD_PARAM_FAILED:
                                        FORCE_CLEAR = true;
                                        exit("load_param_failed");//loadParams
                                        Log.d(TAG, "onParamLoaded: 12");

                                        //  //Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", //Toast.LENGTH_SHORT).show();
                                        break;
                                    case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                        isSuccess = true;
                                        exit("load_param_success");//loadParams
                                        //  //Toast.makeText(MainActivity.this, "Load Finish", //Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, true);
                        return;
                    }
                }
                exit(res);//setToken
                break;

            case "pay":/** ======================= PAY  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "host":
                            hostLink = x[1];
                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//pay
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//pay
                    return;
                }

                if (hostLink == null) {
                    //Toast.makeText(this, "param_missing_hostLink", //Toast.LENGTH_LONG).show();
                    exit("param_missing_hostLink");
                    return;
                }
//                if (!bbCommunication.isBtConnected()) {
//                    exit("bt_disconnected");//v1/pay
//                    return;
//                }
                convertHost(hostLink);

                break;

        }



        /*
        switch (command) {
            case "/pay":
                HashMap<String, String> hashIt = getHashFromURL(getIntent().getData().getSchemeSpecificPart());

                findViewById(R.id.Wellcome).setVisibility(GONE);
                attachCallbacks();
                connectBt();
                populateTransaction(hashIt, true);
                break;
            case "/connectBt":
                attachCallbacks();
                connectBt();
                break;
        }
        */
    }

    private void setTokenToMemory(String tag,String val) {
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences("[YAAD CLOUDPOS EMV]", this.PRIVATE_MODE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(tag, val);
        editor.commit();

    }

    private String getTokenToMemory(String tag, String defaultVal) {
        String token = "";
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences("[YAAD CLOUDPOS EMV]", this.PRIVATE_MODE);
        return sharedPreferences.getString(tag, defaultVal);
    }


    private void handleCommand2(String command) {
        switch (command) {
            case "/pay":
                HashMap<String, String> hashIt = getHashFromURL(getIntent().getData().getSchemeSpecificPart());

                findViewById(R.id.Wellcome).setVisibility(GONE);
                attachCallbacks();
                connectBt();
                populateTransaction(hashIt, true);
                break;
            case "/connectBt":
                attachCallbacks();
                connectBt();
                break;
        }
    }

    private void handleCommand(String command) {
        if(maps.get(command) == null){
            exit("command_not_found");
            Log.e(APP_TAG, "handleCommand: Command not found: [" + command +"]" );
            return;
        }
        String query = getIntent().getData().getEncodedQuery();

        Log.e(TAG, "handleCommand: encoded " + query);
        String[] splited = query.split("&");
        switch (command) {
            case "disconnectBT":/** ======================= Disconnect BT  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }

                if (!bbCommunication.isBtConnected()) {
                    exit("bt_already_disconnected");//connectBt

                    return;
                }
                disconnectBt();
                exit("bt_disconnected");//connectBt
                break;
            case "connectBT":/** ======================= Connect BT  Def ====================== **/

                for (String param : splited) {
                    String[] x = param.split("=");

                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;

                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }
                if (bbCommunication.isBtConnected()) {
                    isSuccess = true;// exit("bt_already_connected");//connectBt
                    exit(TransactionState.getInstance().SERIAL_BUILD);//connectBt

                    return;
                }
                connectBt();


                break;

            case "loadParam": /** ======================= Load Param  ====================== **/
                boolean isForced = false;
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n LOAD PARAM- KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;

                        case "force":
                            try {
                                isForced = Boolean.parseBoolean(x[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();

                    exit("param_missing_successURL");//loadParams
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();

                    exit("param_missing_failedUrl");//loadParams
                    return;
                }

                loadParametersFunction(new LoadParamsCallback() {
                    @Override
                    public void onParamLoaded(byte statusResult) {
                        switch (statusResult) {
                            case LoadParamsCallback.LOAD_PARAM_FAILED:
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        FORCE_CLEAR = true;
                                        exit("load_param_failed");//loadParams
//                                        onBackPressed();
                                    }
                                }, 100);


                                Log.d(TAG, "onParamLoaded: 13");
                                //  //Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", //Toast.LENGTH_SHORT).show();
                                break;
                            case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                isSuccess = true;
                                exit("load_param_success");//loadParams
                                //  //Toast.makeText(MainActivity.this, "Load Finish", //Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, isForced);


                break;

            case "setToken": /** ======================= Set Token  ====================== **/
                String res = "param_missing_token";
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;

                        case "token":
                            try {
                                bbCommunication.updateToken(x[1]);
                                setTokenToMemory(TOKEN_TAG,x[1]);
                                isSuccess = true;
                                res = "token_updated";
                            }catch (Exception e){
                                e.printStackTrace();
                                res = "param_invalid_token";
                            }
                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//setToken
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//setToken
                    return;
                }
                exit(res);//setToken
                break;

            case "pay":/** ======================= PAY  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "host":
                            hostLink = x[1];
                            try {
                                hostLink = URLDecoder.decode(x[1], "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }


                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//pay
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//pay
                    return;
                }

                if (hostLink == null) {
                    //Toast.makeText(this, "param_missing_hostLink", //Toast.LENGTH_LONG).show();
                    exit("param_missing_hostLink");
                    return;
                }
//                if (!bbCommunication.isBtConnected()) {
//                    exit("bt_disconnected");//pay
//                    return;
//                }
                convertHost(hostLink);
                break;
            case "manualPay":/** ======================= MANUAL PAY  ====================== **/
                String masof = null;
                String PassP = null;
                for (String param : splited) {
                    String[] x = param.split("=");
                    //Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");


                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "masof":
                            masof = x[1];
                            break;
                        case "PassP":
                            PassP = x[1];
                            break;
                        case "host":
                            hostLink = x[1];
                            try {
                                hostLink = URLDecoder.decode(x[1], "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "sameTab":
                            sameTab = x[1];
                            break;
                    }
                }

                if (successURL == null) {
                    exit("param_missing_successURL");//manualPay
                    return;
                }
                if (failedUrl == null) {
                    exit("param_missing_failedUrl");////manualPay
                    return;
                }

                if (hostLink == null) {
                    exit("param_missing_hostLink");//manualPay
                    return;
                }

                if (masof == null) {
                    exit("param_missing_massof");//manualPay
                    return;
                }
                if (PassP == null) {
                    exit("param_missing_PassP");//manualPay
                    return;
                }

                securePayManual(masof,PassP);
                break;

        }
    }

    private void securePayManual(String masof, String PassP) {
        Bundle extras = new Bundle();                                                               //create new Bundle
        Intent intent = new Intent(this, YaadPaymentActivity.class);                                //create new Intent - the class name is YaadPaymentActivity.class. it found within our libary.

        /**
         * TRANSACTION PARAMETERS - MUST BE CONFIGURED
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */

        convertHostWithoutLunch(hostLink);

        /** THOSE MUST HAVE. */
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_MASOF,masof);                              //Contact with us in case you gettin trouble with that.
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_APIKEY,PassP);                                   //Contact with us in case you gettin trouble with that.
        if(CURRENT_PAY.get("inputObj.amount") == null){
            exit("inputObj.amoun_not_found");
            return;
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_AMOUNT,CURRENT_PAY.get("inputObj.amount"));                                     //Accepting also '.', like '10.00'.
        if(CURRENT_PAY.get("inputObj.currency") == null){
            exit("inputObj.currency_not_found");
            return;
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_CURRENCY, CURRENT_PAY.get("inputObj.currency"));        //YaadPaymentRequest.CURRENCY_ILS   CURRENCY_USD   CURRENCY_EUR.

        if(CURRENT_PAY.get("yaadObj.yaadPay.Info") == null){
            CURRENT_PAY.put("yaadObj.yaadPay.Info","");
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_INFO,CURRENT_PAY.get("yaadObj.yaadPay.Info"));

        //the SDK doesn't use EXTRA_HOLDER_NAME (it takes it from editText input) but an error will be invoked if we dont send it here
        if(CURRENT_PAY.get("yaadObj.yaadPay.ClientName") == null){
            CURRENT_PAY.put("yaadObj.yaadPay.ClientName","");
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HOLDER_NAME, CURRENT_PAY.get("yaadObj.yaadPay.ClientName"));


        if(CURRENT_PAY.get("yaadObj.yaadPay.ClientLName") == null){
            CURRENT_PAY.put("yaadObj.yaadPay.ClientLName","");
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HOLDER_LNAME, CURRENT_PAY.get("yaadObj.yaadPay.ClientLName"));



        if(CURRENT_PAY.get("inputObj.id") == null){
            CURRENT_PAY.put("inputObj.id","");
        }
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_ID, CURRENT_PAY.get("inputObj.id"));




        if(CURRENT_PAY.get("inputObj.creditTerms") == null){
            exit("inputObj.creditTerms_not_found");
            return;
        }
        String cerditTerms = CURRENT_PAY.get("inputObj.creditTerms");
        extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_CREDIT_TERM, CURRENT_PAY.get("inputObj.creditTerms"));
        if(CURRENT_PAY.get("yaadObj.yaadPay.Tash") == null) {
            switch (cerditTerms) {
                case "8":
                    extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_CREDIT_TERM, CURRENT_PAY.get("inputObj.creditTerms"));
                    if (CURRENT_PAY.get("inputObj.noPayments") == null) {
                        exit("inputObj.noPayments_not_found");
                        return;
                    }

                    extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_TASH_NUMBER, CURRENT_PAY.get("inputObj.noPayments"));

                    if (CURRENT_PAY.get("inputObj.firstPayment") == null) {
                        CURRENT_PAY.put("inputObj.firstPayment", "");
                    }
                    extras.putString(YaadPaymentActivity.YaadPaymentRequest.EXTRA_TASH_FIRST_PAYMENT, CURRENT_PAY.get("inputObj.firstPayment"));
                    break;
            }
        }

        /** THOSE CAN BE ADD FOR YOUR NEEDS. */

        //hashMap.put("J5","True");

        HashMap<String, String> hashMap = new HashMap<>(CURRENT_PAY);
        hashMap.put("MoreData","True");
        //hashMap.put("UTF8","True");
        hashMap.put("UTF8out","True");
        extras.putSerializable("hashMap", hashMap);

        //The meaning of set it to true, is that when transaction succeed no successful page will be. you'll get the result as usual at onActivityResult and you can handle the answer by yourself.
        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_SELF_HANDLE_SUCCESS_SCENARIO, true);
        //The meaning of set it to true, is that when transaction failed no error dialog will be. you'll get the result as usual at onActivityResult and you can handle the answer by yourself.
        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_SELF_HANDLE_ERROR_SCENARIO, false);


        /**
         * VISIBILITY MANAGEMENT
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */

        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_AMOUNT_SQUARE, false);

        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_ID_INPUT, false);
        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_CVV_INPUT, false);
        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_EXPIRY_INPUT, false);

        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_ICONS, false);
        extras.putBoolean(YaadPaymentActivity.YaadPaymentRequest.EXTRA_HIDE_IOCARD, true);

        //extras.putString(YaadPaymentRequest.EXTRA_BUTTON_TEXT           ,"בצע תשלום");


        /**
         * HEAD MANAGEMENT
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */


        /** Trying download the link. In case of error -> set a text label instead. the default is error, and hence the default is text label .
         *You can also change the text label in case of error*/

        //extras.putString(YaadPaymentRequest.EXTRA_ICON_LINK,"https://yaadpay.yaad.net/wp-content/uploads/2015/06/Yaad_Sarig_new-_for-web_fin-01.png");
        //extras.putString(YaadPaymentRequest.EXTRA_TITLE_TEXT,"TITLE");


        /**
         * COLOR MANAGEMENT
         * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         */

        /**
         * DEFAULT COLORS ARE:
         *
         *  <color name="background_top">#FFFFFF</color>
         *  <color name="background_amount_square">#D43939</color>
         *  <color name="background_bottom">#bae9f5</color>
         *
         */

        //extras.putInt(YaadPaymentRequest.EXTRA_BACKGROUND_DRAWABLE_TOP,R.drawable.example);
        //extras.putInt(YaadPaymentRequest.EXTRA_BACKGROUND_DRAWABLE_SQUARE,R.drawable.example);
        //extras.putInt(YaadPaymentRequest.EXTRA_BACKGROUND_DRAWABLE_BOTTOM,R.drawable.example);
        //extras.putInt(YaadPaymentRequest.EXTRA_BACKGROUND_DRAWABLE_BUTTON,R.drawable.example);

        //extras.putString(YaadPaymentRequest.EXTRA_BACKGROUND_COLOR_TOP,"#FFFF0000");
        //extras.putString(YaadPaymentRequest.EXTRA_BACKGROUND_COLOR_SQUARE,"#FFFF0000");
        //extras.putString(YaadPaymentRequest.EXTRA_BACKGROUND_COLOR_BOTTOM,"#FFFF0000");
        //extras.putString(YaadPaymentRequest.EXTRA_BACKGROUND_COLOR_BUTTON,"#FFFF0000");


        intent.putExtras(extras);                                                                   //Insert the bundle to the intent.
        startActivityForResult(intent, PAYMENT_PAGE_ID);                                            //Start the activity by startActivityForResult. you'll get the results on onActivityResult function. example below.
    }
    final static int PAYMENT_PAGE_ID = 1;

    private void handleCommandV2(String command) {
        if(maps.get(command) == null){
            exit("command_not_found");
            Log.e(APP_TAG, "handleCommand: Command not found: [" + command +"]" );
            return;
        }

        String[] splited = getIntent().getData().getQuery().split("&");
        switch (command) {
            case "disconnectBT":/** ======================= Disconnect BT  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
//                    Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }
                if (!bbCommunication.isBtConnected()) {
                    exit("bt_already_disconnected");//connectBt
                    return;
                }
                disconnectBt();
                exit("bt_disconnected");//connectBt
                break;
            case "connectBT":/** ======================= Connect BT  ====================== **/

                for (String param : splited) {
                    String[] x = param.split("=");
//                    Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                    }
                }
                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//connectBt
                    return;

                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//connectBt
                    return;

                }
                if (bbCommunication.isBtConnected()) {
                    isSuccess = true;
                    exit(TransactionState.getInstance().SERIAL_BUILD);//connectBt
                    return;
                }
                connectBt();


                break;

            case "loadParam": /** ======================= Load Param  ====================== **/
                boolean isForced = false;
                for (String param : splited) {
                    String[] x = param.split("=");
//                    Log.e(APP_TAG, "\n LOAD PARAM- KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;

                        case "force":
                            try {
                                isForced = Boolean.parseBoolean(x[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();

                    exit("param_missing_successURL");//loadParams
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();

                    exit("param_missing_failedUrl");//loadParams
                    return;
                }

                loadParametersFunction(new LoadParamsCallback() {
                    @Override
                    public void onParamLoaded(byte statusResult) {
                        switch (statusResult) {
                            case LoadParamsCallback.LOAD_PARAM_FAILED:
                                exit("load_param_failed");//loadParams
                                Log.d(TAG, "onParamLoaded: 14");
                                //  //Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", //Toast.LENGTH_SHORT).show();
                                break;
                            case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                isSuccess = true;
                                exit("load_param_success");//loadParams
                                //  //Toast.makeText(MainActivity.this, "Load Finish", //Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, isForced);


                break;

            case "setToken": /** ======================= Set Token  ====================== **/
                String res = "param_missing_token";
                for (String param : splited) {
                    String[] x = param.split("=");
//                    Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;

                        case "token":
                            bbCommunication.updateToken(x[1]);
                            setTokenToMemory(TOKEN_TAG,x[1]);
                            isSuccess = true;
                            res = "token_updated";

                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//setToken
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//setToken
                    return;
                }
                exit(res);//setToken
                break;

            case "pay":/** ======================= PAY  ====================== **/
                for (String param : splited) {
                    String[] x = param.split("=");
//                    Log.e(APP_TAG, "\n KEY: " + x[0] + ", VAL: " + x[1] + "\n");
                    switch (x[0]) {
                        case "successLink":
                            successURL = x[1];
                            break;
                        case "failedLink":
                            failedUrl = x[1];
                            break;
                        case "host":
                            hostLink = x[1];
                            break;
                    }
                }

                if (successURL == null) {
                    //Toast.makeText(this, "param_missing_successURL", //Toast.LENGTH_LONG).show();
                    exit("param_missing_successURL");//pay
                    return;
                }
                if (failedUrl == null) {
                    //Toast.makeText(this, "param_missing_failedUrl", //Toast.LENGTH_LONG).show();
                    exit("param_missing_failedUrl");//pay
                    return;
                }

                if (hostLink == null) {
                    //Toast.makeText(this, "param_missing_hostLink", //Toast.LENGTH_LONG).show();
                    exit("param_missing_hostLink");
                    return;
                }
//                if (!bbCommunication.isBtConnected()) {
//                    exit("bt_disconnected");//v2/pay
//                    return;
//                }
                convertHost(hostLink);

                break;

        }
    }
    private static HashMap<String,String> CURRENT_PAY = new HashMap<>();

    private void launchTransction(HashMap<String,String> hashMap){
        j.b("launchTransction","Started Transction from launchTransction");
        Log.e(TAG, "launchTransction: [" + RE_CONNECT_TRIES + "/" +MAX_TRIES  +"]");
        IS_TRANSCTION_SENT = false;
        if(RE_CONNECT_TRIES<MAX_TRIES){
            int startEmvResult = bbCommunication.startAmount(SchemeActivity.this, hashMap, RESULT_CODE);
            if(startEmvResult != BBPosCallback.SUCCESS)
            {
                switch (startEmvResult)
                {
                    case BBPosCallback.FAILED:
                        ////Toast.makeText(this, "Need to load params!", //Toast.LENGTH_SHORT).show();
                        loadParametersUsage();
                        break;
                    case BBPosCallback.BT_DISCONNECTED:
                        //Toast.makeText(this, "Bt is not connected", //Toast.LENGTH_SHORT).show();
                        IS_TRANSCTION_SENT = true;
                        RE_CONNECT_TRIES++;
                        //bbCommunication.connectBT(SchemeActivity.this);
                        connectBt();
                        //exit("bt_disconnected");
                        break;
                }
            }
        }else{

            exit("bt_disconnected");
        }
      //  int startEmvResult = bbCommunication.startAmount(SchemeActivity.this, hashMap, RESULT_CODE);
//        bbCommunication.connectBT(SchemeActivity.this);
//        if(startEmvResult != BBPosCallback.SUCCESS)
//        {
//            switch (startEmvResult)
//            {
//                case BBPosCallback.FAILED:
//                    loadParametersUsage();
//                    //exit("load_params_required");//pay
//                    break;
//                case BBPosCallback.BT_DISCONNECTED:
//                    j.b("launchTransaction","BT_DISCONNECTEd SENT");
//                    exit("bt_disconnected");//pay
//                    break;
//            }
//        }

        /*


         */
    }
    private void convertHostWithoutLunch(String hostLink) {
        HashMap<String,String> hashMap = new HashMap<>();

        Log.d(TAG, "convertHost:  HOSTLINK: " + hostLink );
        String[] params = hostLink.split("\\|");
        for(String param : params){
            Log.d(TAG, "convertHost: param: <" + param +">");
            String[] shvaParam = param.split("@",2);
            try{
                hashMap.put(shvaParam[0],shvaParam[1]);
            }catch (Exception e){
                e.printStackTrace();
            }
            CURRENT_PAY.clear();
            CURRENT_PAY.putAll(hashMap);
        }
    }

    private void convertHost(String hostLink) {
        HashMap<String,String> hashMap = new HashMap<>();

        Log.d(TAG, "convertHost:  HOSTLINK: " + hostLink );
        String[] params = hostLink.split("\\|");
        for(String param : params){
            Log.d(TAG, "convertHost: param: <" + param +">");
            String[] shvaParam = param.split("@",2);
            try{
                hashMap.put(shvaParam[0],shvaParam[1]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        CURRENT_PAY.clear();
        CURRENT_PAY.putAll(hashMap);
        Log.e(TAG, "convertHost: " + successURL + ", " + failedUrl );
        launchTransction(CURRENT_PAY);
    }

    private HashMap<String, String> getHashFromURL(String schemeSpecificPart) {
        HashMap<String,String > hash = new HashMap<>();
        String [] sp  = schemeSpecificPart.split("&");

        for(String param : sp){
            String[] a = param.split(",");
            try{
                hash.put(a[0],a[1]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return hash;
    }

    /** Example usage **/
    @Override
    public void onClick(View v)
    {
        int i = v.getId();
        if (i == R.id.btnX)
        {
            SEND_BT_RESPONSE = false;
            disconnectBt();
            exit("user_back_pressed");
        } else if (i == R.id.openPaymentManager)
        {
            //EMV SECTION
            populateTransaction(new HashMap<String,String>(),true);
        }
        else if (i == R.id.loadParamsButton)
        {
            loadParametersFunction(new LoadParamsCallback()
            {
                @Override
                public void onParamLoaded(byte statusResult)
                {
                    switch(statusResult){
                        case LoadParamsCallback.LOAD_PARAM_FAILED:
                         //  //Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", //Toast.LENGTH_SHORT).show();
                            exit("load_param_failed");
                            Log.d(TAG, "onParamLoaded: 15");
                            break;
                        case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                            exit("load_param_success");
                         //  //Toast.makeText(MainActivity.this, "Load Finish", //Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            },true);
        }
        else if (i == R.id.connect_Bt)
        {
            connectBt();
        }
        else if (i == R.id.disconnect_Bt)
        {
            disconnectBt();

        }
        else if (i == R.id.clear_edittexts)
        {
            clearTexts();
        }

    }

    private void clearTexts() {
        ((TextView) findViewById(R.id.text_results)).setText("Cloudpos_BBPos example.");
        ((TextView) findViewById(R.id.expiry_field)).setText("תוקף");
        ((TextView) findViewById(R.id.card_number)).setText("מספר כרטיס");
        ((TextView) findViewById(R.id.full_name)).setText("שם בעל כרטיס");
        ((TextView) findViewById(R.id.mag_stripe)).setText("פס מגנטי");
        //Toast.makeText(this, "שדות אותחלו בהצלחה", //Toast.LENGTH_SHORT).show();
    }
    private boolean SEND_BT_RESPONSE = true;
    @Override
        public void onBTResponse(byte statusCode, String data) {
        Log.d("onBTResponse" , "statusCode = ["+statusCode + "], data = [" +data + "]" );
        Log.e(TAG, "onBTResponse: IS_TRANSCTION_SENT -> [" + IS_TRANSCTION_SENT  + "]");
        Log.e(TAG, "onBTResponse: Try -> [" + RE_CONNECT_TRIES + "/" +MAX_TRIES  +"]");
        bbCommunication.updateToken(getTokenToMemory(TOKEN_TAG,""));
            if(SEND_BT_RESPONSE) {
                switch (statusCode) {
                    case BBUtilCallBacks.BT_CONNECTED:
                        SERIAL_NUMBER = data;
                        ((TextView) findViewById(R.id.current_version)).setText(data);
                        if (lastAction.equals("connectBT")) {
                            isSuccess = true;
                            exit(data);//connectBt

                            return;
                        }else if (lastAction.equals("pay")) {
                            if(IS_TRANSCTION_SENT){


                                launchTransction(CURRENT_PAY);
                            }else{
                                exit("bt_disconnected");
                            }
                        }

                        //getTokenFromServer(SERIAL_NUMBER);
                        break;
                    case BBUtilCallBacks.BT_DISCONNECTED:
                        ((TextView) findViewById(R.id.current_version)).setText("מנותק");

                        break;
                    case BBUtilCallBacks.BT_FAILD_START:
                        ((TextView) findViewById(R.id.current_version)).setText("מנותק.");
                        exit("failed_to_connect_bt");

                        break;
                }
            }
    }




    private void getTokenFromServer(String bbposSerial){
        //TODO: Request token from Server API: (@link)[http://yaadpay.yaad.net]

        getTokenFromServer("036715894", "Eden12345", bbposSerial, new StatusCallback() {
            @Override
            public void onTokenUpdate( final String status, final String data) {
                //The return is from diffrent thread inorder to prompt / change ui we need to work on the main thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status.equals(STATUS_SUCCESS)) {
                            //Re-establish bbcommunication with the token.
                            Log.e(TAG, "run: " + data );
                            bbCommunication.updateToken(data);

                            loadParametersFunction(new LoadParamsCallback()
                            {
                                @Override
                                public void onParamLoaded(byte statusResult)
                                {
                                    switch (statusResult)
                                    {
                                        case LoadParamsCallback.LOAD_PARAM_FAILED:
                                            ////Toast.makeText(MainActivity.this, "Load Param Couldnt load try again! Dialog - Yes / No #NOTE Not automatically so it wont be stuck in a loop", //Toast.LENGTH_SHORT).show();
                                            exit("load_param_failed");
                                            Log.d(TAG, "onParamLoaded: 16");
                                            break;
                                        case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                            exit("load_param_success");
                                            ////Toast.makeText(MainActivity.this, "Start EMV Dialog - Yes / No #NOTE Not automatically so it wont be stuck in a loop", //Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            },false);

                        } else if (status.equals(STATUS_FAILED)) {
                            //Prompt message error
                            //Log.e(TAG, "run: " + +0 );
                            //Toast.makeText(SchemeActivity.this, data, //Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

     //   String tokenFromServer = "GOT_TOKEN_FROM_SERVER";

        //After we get the token we update the library
      //  bbCommunication.updateToken(tokenFromServer);
    }



    public interface StatusCallback{
        String STATUS_FAILED = "failed";
        String STATUS_SUCCESS = "success";
        void onTokenUpdate(String status, String data);
    }



    /**
     *
     * @param username - YaadPayment's username !!- Should never reveal to anyone
     * @param password - YaadPayment's passowrd !!- Should never reveal to anyone
     * @param serial_number - mPos Serial Number from the bluetooth data returned.
     * @param callbacks -
     */
    public void getTokenFromServer(final String username, final String password, final String serial_number, final StatusCallback callbacks)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String SERVER_URL = "https://web3.yaad.net/cgi-bin/yaadpay/shvaemv.pl";
                HttpPost httpPost = new HttpPost(SERVER_URL);
                HttpClient httpClient = new DefaultHttpClient();

                HttpParams httpParams = httpClient.getParams();
                httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, GENERAL_TIME_OUT);
                httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, GENERAL_TIME_OUT);

                HttpResponse response;

                List<NameValuePair> params = new ArrayList<>();

                /** Constant call **/
                params.add(new BasicNameValuePair("action","PPlogin"));

                /** The Serial of the device achieved from the device's BT  **/
                params.add(new BasicNameValuePair("SERIAL",serial_number));

                /** Yaad Info User=[yaad_username] Pass=[yaad_password] !!- Should never reveal to anyone. -!! **/
                params.add(new BasicNameValuePair("User",username));
                params.add(new BasicNameValuePair("Pass",password));
                try
                {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                }
                catch (UnsupportedEncodingException e1)
                {
                    callbacks.onTokenUpdate(STATUS_FAILED, "Network Error");
                }

                try
                {
                    response = httpClient.execute(httpPost);
                    JSONObject jsonResult = null;
                    if (response != null && response.getStatusLine().getStatusCode() == 200)
                    {
                        String _response = EntityUtils.toString(response.getEntity(),"UTF-8");
                        jsonResult = new JSONObject(_response);
                        Log.e("XXX", _response);

                        try {
                            Log.e(TAG, jsonResult.toString());
                        }catch (Exception e){
                            //Should callback that the json is null.
                            callbacks.onTokenUpdate(STATUS_FAILED, "Json is null");
                        }

                        /*****/
                        Log.e(TAG, "run: result: " +jsonResult.toString() );
                        try
                        {
                            if (jsonResult.has("Status"))
                            {
                                String status = jsonResult.getString("Status");
                                String msg = "";

                                if (jsonResult.has("ashStatusDes"))
                                {
                                    msg = jsonResult.getString("ashStatusDes");
                                }

                                Log.e("XXX", "run: status:" + status);
                                if (status.equals("0"))
                                {
                                    //Should callback the "jsonResult.toString()" with success message
                                    if (jsonResult.has("TOKEN")){
                                        callbacks.onTokenUpdate(STATUS_SUCCESS, jsonResult.getString("TOKEN"));
                                    }else {
                                        callbacks.onTokenUpdate(STATUS_FAILED, "Token not found \n");
                                    }
                                    return;

                                }else{
                                    //Should callback with failed message
                                    callbacks.onTokenUpdate(STATUS_FAILED, jsonResult.toString());
                                    return;
                                }
                            }
                            //Should callback the "jsonResult.toString()" with failed message
                            callbacks.onTokenUpdate(STATUS_FAILED, jsonResult.toString());
                            return;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            //Should callback the "jsonResult.toString()" with failed message
                            callbacks.onTokenUpdate(STATUS_FAILED, "Failed to parse : \n" + jsonResult.toString());
                        }

                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //Should callback the failed parse Json message
                    callbacks.onTokenUpdate(STATUS_FAILED, "Failed to parse json message");
                }
            }
        }).start();
    }
    private static String failedUrl = null;
    private static String successURL = null;
    @Override
    public void onBackPressed() {

        //super.onBackPressed();
        SEND_BT_RESPONSE = false;
        disconnectBt();
        exit("user_back_pressed");
    }


    private boolean isSuccess = false;
    private String lastAction = "";
    private void exit(String results){
        //Log.e(TAG, "convertHost3: " + successURL + ", " + failedUrl );
        Log.e(TAG, "exit:  CLOUD ["  + lastAction + "], [" + results + "]");
        // BroadCastReceiver
        final Intent intent=new Intent();
        intent.setAction("example.cloudpos_bbpos");
        intent.putExtra("action",lastAction);
        intent.putExtra("result",results);
        intent.putExtra("status",isSuccess);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        Log.e(TAG, "exit:  >>" + getPackageName() + "." + SchemeActivity.class.getSimpleName() );
        Log.e(TAG, "action:  >>" + lastAction);
        Log.e(TAG, "status:  >>" + isSuccess );


        sendBroadcast(intent);

        //URL SCHEME results => "?results" +
        if(successURL == null){
            successURL = "";
        }

        if(failedUrl == null){
            failedUrl = "";
        }
        String results_scheme = ((isSuccess)?successURL:failedUrl) +"?action=" + lastAction +"&results=" + Uri.encode(results);
        Log.e(TAG, "exit:  results_scheme ["  + results_scheme + "]");
        Log.e(TAG, "exit:  results_scheme - encode ["  + Uri.encode(results) + "]");
        Log.e(TAG, "exit:  results_scheme - parse ["  + Uri.parse(results) + "]");

        Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(results_scheme));

        //StartActivity for result
        result.putExtra(lastAction, results);
        result.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setResult(Activity.RESULT_OK, result);
        successURL = null;
        failedUrl = null;
        try{
            startActivity(result);
            overridePendingTransition(0,0);
        }catch (Exception e){
            Toast.makeText(this, "No Activity found to handle Intent {dat="+results_scheme+" (has extras) }", Toast.LENGTH_LONG).show();
            Log.e(APP_TAG, "exit: " + "No Activity found to handle Intent {dat="+results_scheme+" (has extras) }" );
            Log.e(APP_TAG, "exit: " + "No Activity found to handle Intent {failUrl=["+failedUrl+"], succUrl=[" + successURL +"]}");
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "convertHost2: " + successURL + ", " + failedUrl );
        if (requestCode == RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK){
                final String results = data.getStringExtra(TranManagerActivity.EXTRA_RESULT);
                if(!results.equals("")) {


                    //exit(results);
                    Log.d(APP_TAG, "onActivityResult: " + data.getStringExtra(TranManagerActivity.EXTRA_RESULT));
                    ((TextView) findViewById(R.id.text_results)).setText(StringUtil.formatJsonString(data.getStringExtra(TranManagerActivity.EXTRA_RESULT)));
                    isSuccess = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FORCE_CLEAR = true;
                            exit(results);
                        }
                    }, 100);




                }else{
                    FORCE_CLEAR = true;
                    exit("user_back_pressed");
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no results
            }
            /**
             *
             onActivityResult:
             {
             "uid":"17122610103608822862891",
             "LastAction":"True",
             "ashStatusDes":"×ª×§××",
             "Status":"0",
             "id":"38931",
             "tranRecord":"MTcxMjI2MTAxMDM2MDg4MjI4NjI4OTG0G4Ca+PgcaPJ78E2IWsOCVsvMVRm530qXPwLXbFPf23pRZmW8G4BIIj\/ivsKSgrAEOtctPtAw9OBwPIAlYzxS1bxo953O0XAVuPasZDcjHlkOjCmc38ldTqZyxPoJzCiPRmp1M0sF8QVaF95DlaqaGsYJz0+y\/0ESgdcxIhl0FDZhhUNkI7yusnz0LfMitv5Mmh8dPOqcMLzgKJgcc+pduYZrs+KP\/8gOE2QuzhubMpTbMPiJJ6USeDRBMS8fjfxGQ851hL9Js9CAfkNr1NiT6KDf6nQJRCNAg+U2KzAwUJ8T77swRuepODRL+Y\/fHh9DMROnkVQIrA5XgA==",
             "SOAPAction":"AshFull",
             "WSKEY":"3BA6B7465E611EEEE1DDBD43DC2563C7B05188874917EBFAB61AA77D2BEE0649"
             }
             */

        }else  if (requestCode == PAYMENT_PAGE_ID) {
            if (data != null && data.hasExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_ANSWER)) {
                String result1 = data.getStringExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_ANSWER);
                String result2 = data.getStringExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_PARAMETERS_STRING);
                switch (result1) {
                    case YaadPaymentActivity.YaadPaymentResult.SUCCESS:
                        Log.d("[PAYMENT_PAGE]", " ---SUCCESS--- " + result1);
                        //Toast.makeText(this, "Success " + result1, Toast.LENGTH_SHORT).show();
                        isSuccess = true;
                        FORCE_CLEAR = true;
                        exit(result2);
                        break;
                    case "user_back_pressed":
                        Log.d("[PAYMENT_PAGE]", " ---user_back_pressed--- " + result1);
                        FORCE_CLEAR = true;
                        exit(result1);
                        break;
                    default:
                        Log.d("[PAYMENT_PAGE]", " --- " +  result1 + " ---, -- " + result2 + " -- ");
                            break;

                }
            }
            /*
            //Example for how analyze the result from the server.
            if (data != null && data.hasExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_PARAMETERS)) {
                HashMap<String, String> finalResult = (HashMap<String, String>) data.getSerializableExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_PARAMETERS);
                String str = "";
                for (Map.Entry<String, String> entry : finalResult.entrySet())
                    Log.d("[PAYMENT_PAGE]", " ---EXTRA_RESULT_PARAMETERS--- [KEY][" + entry.getKey() + "] [VALUE][" + entry.getValue() + "] ");
            }
            */
        }

    }

    private int RESULT_CODE = 1111;
    private void populateTransaction(HashMap<String, String> tranHash , boolean flag)
    {
        HashMap<String, String> hash = new HashMap<>();
        if(flag){
            //InputObj accourding to the - SHVA File
            hash.put("inputObj.amount", "100");
            hash.put("inputObj.cashbackAmount", "0");
            hash.put("inputObj.tranType", "01");
            hash.put("inputObj.currency", "376");

            //New lines for backup
            hash.put("yaadObj.notify", "url");
            hash.put("yaadObj.notifyURL", "http://www.mysite.com/emv_trans.php?id=1234");
        }else{
            hash.putAll(tranHash);
        }
        sendTransaction(hash);
    }
    private static boolean IS_TRANSCTION_SENT;
    private void sendTransaction(HashMap<String,String> hash){
        j.b("sendTransaction","Started Transction from sendTransaction");
        IS_TRANSCTION_SENT = false;
        if(RE_CONNECT_TRIES<MAX_TRIES){
            int startEmvResult = bbCommunication.startAmount(SchemeActivity.this, hash, RESULT_CODE);
            if(startEmvResult != BBPosCallback.SUCCESS)
            {
                switch (startEmvResult)
                {
                    case BBPosCallback.FAILED:
                        ////Toast.makeText(this, "Need to load params!", //Toast.LENGTH_SHORT).show();
                        loadParametersUsage();
                        break;
                    case BBPosCallback.BT_DISCONNECTED:
                        //Toast.makeText(this, "Bt is not connected", //Toast.LENGTH_SHORT).show();
                        IS_TRANSCTION_SENT = true;
                        RE_CONNECT_TRIES++;
                        bbCommunication.connectBT(SchemeActivity.this);
                        //exit("bt_disconnected");
                        break;
                }
            }
        }else{
            exit("bt_disconnected");
        }

    }

    private static final int MAX_TRIES = 3;
    private static int RE_CONNECT_TRIES = 0;
    private void loadParametersFunction(LoadParamsCallback loadParamsCallback, boolean force)
    {
        //{1} Integer callback that state whether the Loading Activity has successfully opened or not.
        int res =  bbCommunication.loadParams(this, force, loadParamsCallback);
        switch(res){
            case BBPosCallback.DEVICE_BUSY:
                exit("device_busy");


                //Toast.makeText(this, "Device is busy.", //Toast.LENGTH_SHORT).show();
                break;
            case BBPosCallback.BT_DISCONNECTED:
                exit("bt_disconnected");//loadParametersFunction
                ////Toast.makeText(this, "Do stuff", //Toast.LENGTH_SHORT).show();
                break;
            case BBPosCallback.PARAM_LOADED:
                isSuccess = true;
                exit("param_loaded");
                break;
        }
    }

    private void loadParametersUsage(){
        // {2} Custom Callback [LoadParamsCallback] that indicates if the loading was success or not.
        loadParametersFunction(new LoadParamsCallback()
        {
            @Override
            public void onParamLoaded(byte statusResult)
            {
                switch(statusResult){
                    case LoadParamsCallback.LOAD_PARAM_FAILED:
                        /*
                        View.OnClickListener clickListenerV = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadParametersUsage();
                            }
                        };

                        View.OnClickListener clickListenerX = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                exit("user_back_pressed");
                            }
                        };
setAlertContainerUI(true,R.string.retry, clickListenerV,true,R.string.cancel,clickListenerX,R.string.searchBt);
                         */
                        Log.e(TAG, "onParamLoaded: 121212" );
                        FORCE_CLEAR = true;
                        exit("user_back_pressed");
                        break;
                    case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                        launchTransction(CURRENT_PAY);
                        ////Toast.makeText(MainActivity.this, "Start EMV", //Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        },false);
    }

    private void attachCallbacks(){
        /** Get the current instance of our BBCommunication - Singleton **/
        bbCommunication = BBCommunication.getInstance();

        /** Initiate callbacks to this activity **/
        bbCommunication.init(this, this);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        //attachCallbacks();
        //Optional - Search for connection if device disconnected
        //connectBt();
    }
    private void disconnectBt()
    {
        bbCommunication.disconnectBt(this);
    }
    private void connectBt()
    {
        /** Checks if the device is not connected before searching **/
        if (!bbCommunication.isBtConnected())
        {
            View.OnClickListener clickListenerX = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SEND_BT_RESPONSE = false;
                    disconnectBt();
                    exit("user_back_pressed");

                }
            };
            setAlertContainerUI(false,-1, null,true,R.string.cancel,clickListenerX,R.string.searchBt);
            alertContainer.setVisibility(View.VISIBLE);

            bbCommunication.connectBT(SchemeActivity.this);
        }
    }
    private void setAlertContainerUI(Boolean btnV, int btnVStr,View.OnClickListener btnVclick,Boolean btnX, int btnXStr, View.OnClickListener btnXclick, int title){
        if(btnV){
            (findViewById(R.id.btnV)).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.btnV)).setText(getResources().getString(btnVStr));
            if(btnVclick != null){
                ((TextView)findViewById(R.id.btnV)).setOnClickListener(btnVclick);
            }

        }else{
            (findViewById(R.id.btnV)).setVisibility(GONE);
        }

        if(btnX){
            (findViewById(R.id.btnX)).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.btnX)).setText(getResources().getString(btnXStr));
            if(btnXclick != null){
                ((TextView)findViewById(R.id.btnX)).setOnClickListener(btnXclick);
            }

        }else{
            (findViewById(R.id.btnX)).setVisibility(GONE);
        }
        ((TextView)findViewById(R.id.msg)).setText(getResources().getString(title));
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
        if(EasyPermissions.hasPermissions(SchemeActivity.this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            EasyPermissions.requestPermissions(SchemeActivity.this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
            return false;
        }
    }

}
