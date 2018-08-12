package example.cloudpos_bbpos;

import android.app.Activity;
import android.content.Intent;
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

import java.util.HashMap;

import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener,  BBPosCallback {
    /** Variable that communicates with our library thgough commands **/
    private BBCommunication bbCommunication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        attachCallbacks();
        connectBt();

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
                            Toast.makeText(MainActivity.this, "Load Failed - Prompt Dialog Yes - No #NOTE: do not open load params again so it wont stuck in a loop", Toast.LENGTH_SHORT).show();
                            break;
                        case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                            Toast.makeText(MainActivity.this, "Load Finish", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            },false);
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
        String tokenFromServer = "GOT_TOKEN_FROM_SERVER";

        //After we get the token we update the library
        bbCommunication.updateToken(tokenFromServer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK){
                String results = data.getStringExtra(TranManagerActivity.EXTRA_RESULT);
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
                    Toast.makeText(this, "Need to load params!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Do stuff", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Load Param Couldnt load try again! #NOTE Not automatically so it wont be stuck in a loop", Toast.LENGTH_SHORT).show();
                        break;
                    case LoadParamsCallback.LOAD_PARAM_SUCCESS:
                        Toast.makeText(MainActivity.this, "Start EMV", Toast.LENGTH_SHORT).show();
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
        attachCallbacks();
        //Optional - Search for connection if device disconnected
        connectBt();
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
            bbCommunication.connectBT(MainActivity.this);
        }
    }
}
