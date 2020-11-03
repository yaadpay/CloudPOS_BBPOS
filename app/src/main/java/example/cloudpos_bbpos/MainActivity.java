package example.cloudpos_bbpos;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.bblib.activities.TranManagerActivity;
import com.bbpos.bbdevice.bblib.communication.BBCommunication;
import com.bbpos.bbdevice.bblib.interfaces.BBPosCallback;
import com.bbpos.bbdevice.bblib.interfaces.BBUtilCallBacks;
import com.bbpos.bbdevice.bblib.interfaces.LoadParamsCallback;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.GENERAL_TIME_OUT;
import static example.cloudpos_bbpos.MainActivity.StatusCallback.STATUS_FAILED;
import static example.cloudpos_bbpos.MainActivity.StatusCallback.STATUS_SUCCESS;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener,  BBPosCallback {
    /** Variable that communicates with our library thgough commands **/
    private BBCommunication bbCommunication;

    private HashMap<String , Boolean >  maps = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maps.put("/pay",true);
        maps.put("/connectBT",true);
        maps.put("/setToken",true);
        maps.put("/loadParams",true);



        
        Uri data = getIntent().getData();

        if(data != null){

            Toast.makeText(this, "STARTED" + getIntent().getDataString(), Toast.LENGTH_SHORT).show();
            try{

                ((TextView)findViewById(R.id.text1)).setText(getIntent().getDataString());
                ((TextView)findViewById(R.id.text1)).append("\n\n");
            }catch (Exception e){
                e.printStackTrace();
            }

            Log.e(TAG, "onCreate:getSchemeSpecificPart " +  getIntent().getData().getSchemeSpecificPart()); // GET SCHEME SPECIFIC WILL GET US //PATH&PARAMS
            Log.e(TAG, "\n\n");
            Log.e(TAG, "onCreate:getScheme " +  getIntent().getData().getScheme());// GET SCHEME WILL GET US [bbpos]
            Log.e(TAG, "\n\n");
            Log.e(TAG, "onCreate:getPath " +  getIntent().getData().getPath()); // GET PATH WILL GET US [bbpos://][/A/B/C/D/E]
            Log.e(TAG, "\n\n");
            Log.e(TAG, "onCreate:getQueryParameterNames " +  getIntent().getData().getQueryParameterNames()); // LIST OF PARAMS NAME
            Log.e(TAG, "\n\n");
            Log.e(TAG, "onCreate:getQuery " +  getIntent().getData().getQuery()); // LIST OF PARAMS NAME
            Log.e(TAG, "\n\n");
//            Log.e(TAG, "onCreate: " +  getIntent().getData().getPathSegments().get(0));
            for (String segment : getIntent().getData().getQueryParameterNames()) {
                Log.e(TAG, "onCreate: 1ist\n " + segment );
                //
            }

            ((TextView)findViewById(R.id.text1)).append("Path: " + getIntent().getData().getPath());
            String[] splited = getIntent().getData().getQuery().split("&");
            for(String param : splited){
                String[] x = param.split("=");
                ((TextView)findViewById(R.id.text1)).append("\n KEY: "  + x[0] + ", VAL: " + x[1]  + "\n" );
            }
            String path  = getIntent().getData().getPath();

            if(maps.get(path) != null){

                switch (path){
                    case "/pay":
                        HashMap<String,String> hashIt = getHashFromURL( getIntent().getData().getSchemeSpecificPart());

                        findViewById(R.id.Wellcome).setVisibility(GONE);
                        attachCallbacks();
                        connectBt();
                        populateTransaction(hashIt,true);
                        break;
                    case "/connectBt":
                        attachCallbacks();
                        connectBt();
                        break;

                }
            }
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
        if (i == R.id.openPaymentManager)
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
                            break;
                        case LoadParamsCallback.LOAD_PARAM_SUCCESS:
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
        Toast.makeText(this, "שדות אותחלו בהצלחה", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBTResponse(byte statusCode, String data) {
        switch (statusCode) {
            case BBUtilCallBacks.BT_CONNECTED:
                String SERIAL_NUMBER = data;
                ((TextView) findViewById(R.id.current_version)).setText(data);
                getTokenFromServer(SERIAL_NUMBER);
                break;
            case BBUtilCallBacks.BT_DISCONNECTED:
                ((TextView) findViewById(R.id.current_version)).setText("מנותק");
                break;
            case BBUtilCallBacks.BT_FAILD_START:
                ((TextView) findViewById(R.id.current_version)).setText("מנותק.");
                break;
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
                                            break;
                                        case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                                            ////Toast.makeText(MainActivity.this, "Start EMV Dialog - Yes / No #NOTE Not automatically so it wont be stuck in a loop", //Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            },false);

                        } else if (status.equals(STATUS_FAILED)) {
                            //Prompt message error
                            Log.e(TAG, "run: " + +0 );
                            Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {

        //super.finish();
        exit("DONE-");
    }



    private void exit(String results){

        Intent intent2 = getIntent();
        intent2.putExtra("RES", results);
        setResult(RESULT_OK, intent2);

        final Intent intent=new Intent();
        intent.setAction("example.cloudpos_bbpos");
        intent.putExtra("RES",results);
//       if(SchemeActivity.sameTab.equalsIgnoreCase("True")) {
//           intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//       }
        Log.e(TAG, "exit:  >>" + getPackageName() + "." + MainActivity.class.getSimpleName() );
        sendBroadcast(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
        intent.putExtra("Exit me", true);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String results = data.getStringExtra(TranManagerActivity.EXTRA_RESULT);
                //exit(results);
                Log.d(APP_TAG, "onActivityResult: " + data.getStringExtra(TranManagerActivity.EXTRA_RESULT) );
                ((TextView) findViewById(R.id.text_results)).setText(StringUtil.formatJsonString(data.getStringExtra(TranManagerActivity.EXTRA_RESULT)));

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
        int startEmvResult = bbCommunication.startAmount(MainActivity.this, hash, RESULT_CODE);
        if(startEmvResult != BBPosCallback.SUCCESS)
        {
            switch (startEmvResult)
            {
                case BBPosCallback.FAILED:
                    ////Toast.makeText(this, "Need to load params!", //Toast.LENGTH_SHORT).show();
                    loadParametersUsage();
                    break;
                case BBPosCallback.BT_DISCONNECTED:
                    Toast.makeText(this, "Bt is not connected", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    private void loadParametersFunction(LoadParamsCallback loadParamsCallback, boolean force)
    {
        //{1} Integer callback that state whether the Loading Activity has successfully opened or not.
        int res =  bbCommunication.loadParams(this, force, loadParamsCallback);
        switch(res){
            case BBPosCallback.DEVICE_BUSY:
                Toast.makeText(this, "Device is busy.", Toast.LENGTH_SHORT).show();
                break;
            case BBPosCallback.BT_DISCONNECTED:
                ////Toast.makeText(this, "Do stuff", //Toast.LENGTH_SHORT).show();
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
                        ////Toast.makeText(MainActivity.this, "Load Param Couldnt load try again! #NOTE Not automatically so it wont be stuck in a loop", //Toast.LENGTH_SHORT).show();
                        break;
                    case LoadParamsCallback.LOAD_PARAM_SUCCESS:
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
        try {
            bbCommunication.disconnectBt(this);
        }catch (Exception e){

        }
    }
    private void connectBt()
    {
        /** Checks if the device is not connected before searching **/
        if (!bbCommunication.isBtConnected())
        {
            bbCommunication.connectBT(MainActivity.this);
        }
    }
}
