package example.cloudpos_bbpos.YaadPay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.bblib.activities.TranManagerActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import example.cloudpos_bbpos.R;

import static com.bbpos.bbdevice.bblib.interfaces.Constant.APP_TAG;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.defaultCurrencyArray;
import static com.bbpos.bbdevice.bblib.interfaces.Constant.defaultCurrencyCodeArray;

public class YaadPaymentActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {


    private static final int MY_SCAN_REQUEST_CODE = 111;
    private YaadPaymentActivity.TransactionTask mAuthTask;
    private static final String SERVER_LINK = "https://icom.yaad.net/p/";
    private String masof = "";
    private String apiKey = "";
    private String currency = "1";
    private String holderName = "";
    private String info = "";
    private Bundle bundle;
    private HashMap<String, String> finalResult;
    private String finalStringResult;

    private int monthsInitCounter = 0;
    private int yearsInitCounter = 0;




    private ScrollView creditCardSv;
    private RelativeLayout okCreditScreen;

    private Spinner monthsSpinnner;
    private boolean lastStateVisible;
    private boolean wasVisible = false;
    private Spinner yearsSpinner;



//    BpEditText mailEt;

    private RelativeLayout fullNameRl;
    private RelativeLayout dummyHider;
    private RelativeLayout creditCardRl;
    private RelativeLayout mailRl;
    private RelativeLayout idRl;
    private BpEditText     creditCardEt;
    private EditText expireEt;
    private EditText mailEt;
    private EditText fullNameEt;
    private EditText cvvEt;
    private EditText idEt;
    private TextView headerTv;
    private TextView okButtonTv;

    private String monthSelected;
    private String yearSelected;

    //error images
    private Drawable rightSideError;
    private Drawable leftsideError;
    private Drawable middleError;
    private ProgressDialog circleDialog;
    private String amount;

    private HashMap<String,String> hashMap = new HashMap<>();
    private String holderID;
    private String tashNo = null;
    private String tashFirstPayment = null;
    private String tashType = null;
    private String Name = "";
    private String LName = "";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pay);
        setViewsReferences();
        setListeners();
        initDrawables();
        getSupportActionBar().hide();
        setMonthsSpinner();
        setAutoNextFocus();
        setYearsSpinner();
        getData();
        overrideMailEnterKey();
        setFontsToTvs();
        setFocusChangeListener();

    }


    @Override
    public void onBackPressed() {
      //  super.onBackPressed();
        exit("user_back_pressed");
    }

    private void overrideMailEnterKey () {

        mailEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction (TextView textView, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mailEt.getWindowToken(), 0);

                }
                return true;
            }
        });
    }

    private void setViewsReferences() {

        okCreditScreen     = (RelativeLayout) findViewById(R.id.okCreditScreen);
        creditCardRl       = (RelativeLayout) findViewById(R.id.creditCardRl);
        dummyHider         = (RelativeLayout) findViewById(R.id.dummyHider);
        fullNameRl         = (RelativeLayout) findViewById(R.id.fullNameRl);
        mailRl             = (RelativeLayout) findViewById(R.id.mailRl);
        idRl               = (RelativeLayout) findViewById(R.id.idRl);
        creditCardEt       = (BpEditText)     findViewById(R.id.creditCardEt);
        mailEt             = (EditText)       findViewById(R.id.mailEt);
        fullNameEt         = (EditText)       findViewById(R.id.fullNameEt);
        cvvEt              = (EditText)       findViewById(R.id.cvvEt);
        idEt               = (EditText)       findViewById(R.id.idEt);
        headerTv           = (TextView)       findViewById(R.id.headerTv);

        okButtonTv         = (TextView)       findViewById(R.id.okButtonTv);
        monthsSpinnner     = (Spinner)        findViewById(R.id.monthsSpinner);
        yearsSpinner       = (Spinner)        findViewById(R.id.yearsSpinner);
    }

    private void setListeners () {

        okCreditScreen.setOnClickListener(this);
        fullNameRl.setOnClickListener(this);

        //inputs
        creditCardEt.setOnTouchListener(this);
        creditCardRl.setOnTouchListener(this);
        fullNameEt.setOnTouchListener(this);
        mailEt.setOnTouchListener(this);
        cvvEt.setOnTouchListener(this);
        idEt.setOnTouchListener(this);
    }

    private void setFontsToTvs() {

        Typeface regularTF = FontsManager.getRegular(this);
        Typeface boldTF    = FontsManager.getBold(this);

        //header
        headerTv.setTypeface(regularTF);

        //inputs
        fullNameEt.setTypeface(regularTF);
        idEt.setTypeface(regularTF);
        creditCardEt.setTypeface(regularTF);
        cvvEt.setTypeface(regularTF);
        mailEt.setTypeface(regularTF);

        //disclaimer


        //button
        okButtonTv.setTypeface(regularTF);

    }

    private void getData() {
        bundle = this.getIntent().getExtras();

        try {
            if(bundle != null) {
                if(this.bundle.getString(YaadPaymentRequest.EXTRA_MASOF) != null) {
                    masof = bundle.getString(YaadPaymentRequest.EXTRA_MASOF);
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_MASOF_MISSING");
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_APIKEY) != null) {
                    apiKey = bundle.getString(YaadPaymentRequest.EXTRA_APIKEY);
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_APIKEY_MISSING");
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_ID) != null) {
                    holderID = bundle.getString(YaadPaymentRequest.EXTRA_ID);
                    if(holderID != null && holderID.length() > 0 ){
                        ((EditText)findViewById(R.id.idEt)).setText(holderID);
                    }
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_HOLDER_NAME_MISSING");
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_HOLDER_NAME) != null) {
                    holderName = bundle.getString("com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_HOLDER_NAME");
                    if(holderName != null && holderName.length() > 0 ){
                        ((EditText)findViewById(R.id.fullNameEt)).setText(holderName);
                        this.Name = holderName;
                    }
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_HOLDER_NAME_MISSING");
                }


                if(bundle.getString(YaadPaymentRequest.EXTRA_HOLDER_LNAME) != null) {
                    String holderLName = bundle.getString("com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_HOLDER_LNAME");
                    if(holderLName != null && holderLName.length() > 0 ){
                        this.LName = holderLName;
                    }
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_HOLDER_LNAME_MISSING");
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_AMOUNT) != null) {
                    amount = bundle.getString(YaadPaymentRequest.EXTRA_AMOUNT);
                    String amountTEXT = amount;
                    // Get with agura 600
                    //
                    amount = convertAgura2Shekel(amount);// (Double.parseDouble(amount) / 100 + "").replace("\\.","");

                    try{
                         amountTEXT =  Double.parseDouble(amount)+ "";
                    }catch (Exception e){
                        Log.e("CALC_ERROR", "getData: Wrong amount"  );
                    }

                    ((TextView)findViewById(R.id.amountEt)).setText(amountTEXT);

                    //Toast.makeText(this, "AMOUBT: " + amount, Toast.LENGTH_SHORT).show();
                } else {
                    exit("com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_AMOUNT_MISSING");
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_CURRENCY) != null) {
                    currency = bundle.getString(YaadPaymentRequest.EXTRA_CURRENCY);

                    ((TextView)findViewById(R.id.currencyText)).setText(parseCode(currency));

                } else {
                    exit(YaadPaymentRequest.EXTRA_CURRENCY);
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_TASH_NUMBER) != null) {
                    tashNo = bundle.getString(YaadPaymentRequest.EXTRA_TASH_NUMBER);
                }

                if(bundle.getString(YaadPaymentRequest.EXTRA_TASH_FIRST_PAYMENT) != null) {
                    tashFirstPayment = bundle.getString(YaadPaymentRequest.EXTRA_TASH_FIRST_PAYMENT);
                    tashFirstPayment = convertAgura2Shekel(tashFirstPayment);
                }
                String creditTermTX = "אמצעי תשלום:" ;
                SpannableString ss1 =  new SpannableString(creditTermTX);
              //  Log.e(APP_TAG, "getData: 1"  + ss1);
                if(bundle.getString(YaadPaymentRequest.EXTRA_CREDIT_TERM) != null) {
                    HashMap hashMap = (HashMap)YaadPaymentActivity.this.bundle.getSerializable("hashMap");

                    if(hashMap.get("yaadObj.yaadPay.Tash") != null) {
                        creditTermTX += "\n";
                        creditTermTX += "" +generateTashString((String)hashMap.get("yaadObj.yaadPay.Tash")) + "";
                        String total = creditTermTX;
                        ss1 =  new SpannableString(total);
                      //  Log.e(APP_TAG, "getData: 2"  + ss1);
                    }else{
                        tashType = bundle.getString(YaadPaymentRequest.EXTRA_CREDIT_TERM);
                      //  Log.e(APP_TAG, "getData: 3"  + tashType);
                        if(tashType != null ){
                            if(tashType.equals("8")){
                                if(hashMap.get("inputObj.noPayments") != null && hashMap.get("inputObj.firstPayment") != null && (hashMap.get("inputObj.notFirstPayment") != null)){
                                    try{
                                        int calcedPayments = Integer.parseInt((String)hashMap.get("inputObj.noPayments"));
                                        int calcedPaymentsFull= Integer.parseInt((String) hashMap.get("inputObj.noPayments"));
                                        calcedPaymentsFull++;

                                        // transType += "" +generateTashString(hash.get("inputObj.noPayments")) + "";

                                        String firstPaymnet = convertAgura2Shekel((String) hashMap.get("inputObj.firstPayment"));
                                        String noPayments = (String)(hashMap.get("inputObj.noPayments"));
                                        String notFirstPayment = convertAgura2Shekel((String)hashMap.get("inputObj.notFirstPayment"));


                                        String partTwo = "(" +firstPaymnet +" ועוד "+calcedPayments+"  תש' של "+ notFirstPayment  +")";
                                        String partOne =  " " + calcedPaymentsFull +  " תשלומים ";
                                        String total = creditTermTX + "\n" + partOne + "\n" + partTwo;
                                        ss1=  new SpannableString(total);
                                        //ss1.setSpan(new RelativeSizeSpan(1f), 0,partOne.length(), 0); // set size
                                        ss1.setSpan(new RelativeSizeSpan(0.7f), creditTermTX.length()+ partOne.length()+1,total.length(), 0); // set size
                                     //   Log.e(APP_TAG, "getData: 4"  + ss1);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }

                            }else if(tashType.equals("6")){
                                try{
                                    int calcedPayments = Integer.parseInt((String)hashMap.get("inputObj.noPayments"));
                                    int calcedPaymentsFull= Integer.parseInt((String)hashMap.get("inputObj.noPayments"));
                                    String partOne =  " " + calcedPaymentsFull +  " תשלומים ";
                                    String partTwo =  " בקרדיט ";
                                    String total = creditTermTX + "\n" + partOne + "" + partTwo;
                                    ss1=  new SpannableString(total);
                                   // Log.e(APP_TAG, "getData: 5"  + ss1);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            } else if(tashType.equals("1")) {

                                    String partTwo =  " באשראי ";
                                    String total = creditTermTX + partTwo;
                                    ss1=  new SpannableString(total);
                               // Log.e(APP_TAG, "getData: 6"  + ss1);
                            }
                        }
                    }


                }
              //  Log.e(APP_TAG, "getData: 7"  + ss1);
                headerTv.setText(ss1);

            }
        } catch (Exception error) {
            Log.e("[YaadPaymentActivity]", error.toString());
        }

    }

    private String generateTashString(String tashNo) {
        String creditTermTX = "";
        if(tashNo.equals("1")){
            creditTermTX += " תשלום אחד ";
        }else{
            creditTermTX += "" + tashNo + "";
            creditTermTX += " תשלומים ";
        }
        return creditTermTX;
    }

    public static String parseCode(String currencyCode)
    {
        int i = 0;
        for (String curr : defaultCurrencyCodeArray)
        {
            if (currencyCode.equals(curr))
            {
                return defaultCurrencyArray[i];
            }
            i++;
        }
        return null;
    }
    private void exit(String paymentPageResult) {
        hideCircleDialog(0);
        Log.d("EXIT", "exit: "+ paymentPageResult);
        Intent intent = getIntent();
        intent.putExtra("com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_RESULT_ANSWER", paymentPageResult);
        if(finalResult != null) {
            intent.putExtra("com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_RESULT_PARAMETERS", finalResult);
        }
        if(finalStringResult != null) {
            intent.putExtra(YaadPaymentActivity.YaadPaymentResult.EXTRA_RESULT_PARAMETERS_STRING, finalStringResult);
        }

        setResult(-1, intent);
        finish();
    }


    private HashMap<String, String> parseRespond(String respone) {
        String[] responeArray = respone.split("&");
        HashMap jsonObject = new HashMap();
        String[] var5 = responeArray;
        int var6 = responeArray.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String key = var5[var7];
            String[] temp = key.split("=");

            try {
                jsonObject.put(temp[0], temp.length > 1?temp[1]:"");
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }

        return jsonObject;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public void onClick(View view) {

        if(view == okCreditScreen) {
            doTransaction();
        }
    }

    @Override
    public boolean onTouch (View view, MotionEvent motionEvent) {

        if (view == creditCardEt) {
            removeError(creditCardRl);

        } else if (view == fullNameEt) {
            removeError(fullNameRl);

        } else if (view == cvvEt ) {
            removeError(dummyHider);

        } else if (view == idEt) {
            removeError(idRl);
        }


        return false;
    }

     private void setFocusChangeListener() {

         View.OnFocusChangeListener removeErrorListener = new View.OnFocusChangeListener() {

             @Override
             public void onFocusChange (View view, boolean hasFocus) {
                 if (hasFocus) {
                     View view1 = (View) view.getParent();
                     removeError(view1);

                     if (view == cvvEt) {
                         removeError(dummyHider);
                     }
                 }
             }
         };

         creditCardEt.setOnFocusChangeListener(removeErrorListener);
         fullNameEt.setOnFocusChangeListener(removeErrorListener);
         cvvEt.setOnFocusChangeListener(removeErrorListener);
         idEt.setOnFocusChangeListener(removeErrorListener);

     }


    public void showCircleDialog (Context mContext) {

        circleDialog = new ProgressDialog(mContext);
        try {
            circleDialog.show();
        } catch (WindowManager.BadTokenException e) {
        }
        circleDialog.setCancelable(false);
        circleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        circleDialog.setContentView(R.layout.progressdialog);
        circleDialog.show();
    }


    //this one is without a callback
    public void hideCircleDialog (int dur) {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run () {

                if (circleDialog != null && circleDialog.isShowing()) {
                    circleDialog.cancel();
                }
            }
        }, dur);
    }




    private void doTransaction () {

        if(!this.isNetworkConnected()) {
            Toast.makeText(this, "אין חיבור לאינטרנט", Toast.LENGTH_LONG).show();
            return;
        }

        if(!checkInputAndApply()) {
            return;
        }

        this.mAuthTask = new YaadPaymentActivity.TransactionTask(
                creditCardEt.getText().toString(),
                idEt.getText().toString(),
                monthSelected,
                yearSelected,
                cvvEt.getText().toString(),
                mailEt.getText().toString(),
                this.Name,
                this.LName);
        this.mAuthTask.execute(new Void[0]);
    }


    private void setError (View v, Drawable side) {

        v.setBackground(side);
    }

    private void setError (View v) {
        v.setBackground(getResources().getDrawable(R.drawable.error_card));
    }


    private void removeError (View v) {
        v.setBackground(getResources().getDrawable(R.drawable.first_closed));
    }

    private void initDrawables () {

        rightSideError = getResources().getDrawable(R.drawable.right_side_error);
        leftsideError  = getResources().getDrawable(R.drawable.left_side_error);
        middleError    = getResources().getDrawable(R.drawable.middle_error);
    }


    /**
     * trying to validate the inputs
     * @return true if passed the validation successfuly
     *
     */
    private boolean checkInputAndApply() {

        boolean returnValue = true;

        String nameInput = fullNameEt.getText().toString().trim();
        if(nameInput.length() < 2) {
            setError(fullNameRl);
            returnValue = false;

        } else {
            removeError(fullNameRl);
        }


        String idInput = idEt.getText().toString().trim();
        if(idInput.length() != 8 && idInput.length() != 9) {
            setError(idRl);
            returnValue = false;
        } else {
            removeError(idRl);
        }


        String cardnumInput = creditCardEt.getText().toString().trim();
        if(cardnumInput.length() < 7) {
            setError(creditCardRl);
            returnValue = false;
        } else {
            removeError(creditCardRl);
        }


        if(monthSelected == null ||monthSelected.equals("חודש")) {
            monthsSpinnner.setBackground(null);
            setError(monthsSpinnner, leftsideError);
            returnValue = false;
        } else {
            removeError(monthsSpinnner);
        }

        if(yearSelected == null || yearSelected.equals("שנה")) {
            yearsSpinner.setBackground(null);
            setError(yearsSpinner, middleError);
            returnValue = false;
        } else {
            removeError(yearsSpinner);
        }

        String cvvInput = cvvEt.getText().toString().trim();
        //keep in mind that length can't be bigger than 4 since we have set a limit through xml.
        if(cvvInput.length() < 3) {

            setError(dummyHider, rightSideError);
            returnValue = false;
        } else {
            removeError(dummyHider);
        }

        //don't validate email if the user left it empty
        if (mailEt.length() > 0) {
            if (!isEmailvaild(mailEt)) {
                setError(mailRl);
                returnValue = false;
            } else {
                removeError(mailRl);
            }
        }

        return returnValue;
    }

//    private boolean checkInputAndApply() {
//        String value = fullNameEt.getText().toString().trim();
//        if(value.isEmpty()) {
//            fullNameEt.setError("נא למלא שם");
//            fullNameEt.requestFocus();
//            return false;
//        } else {
//            value = creditCardEt.getText().toString().trim();
//            if(!value.isEmpty() && value.length() >= 8) {
//                value = cvvEt.getText().toString().trim();
//
//
//
//                if(!value.isEmpty() && value.length() >= 3) {
//                    //cvv is not empty, and longer than 2 chars
//
//
////                    value = expireTv.getText().toString().trim();
//                    value = monthsSpinnner.getSelectedItem().toString() + "/" + yearsSpinner.getSelectedItem().toString();
//
//                    if (!value.isEmpty() && value.length() >= 5) {
//                        value = this.idEt.getText().toString().trim();
//
//
//
//                        if (!value.isEmpty() && value.length() >= 9) {
//                            if (!this.isEmailvaild(this.mailEt)) {
//                                this.mailEt.setError("דואר אלקטרוני לא תקין");
//                                this.mailEt.requestFocus();
//                                return false;
//                            } else {
//                                return true;
//                            }
//                        } else {
//                            this.idEt.setError("מס\' ת.ז לא תקין");
//                            this.idEt.requestFocus();
//                            return false;
//                        }
//                    } else {
//
////                        monthsSpinnner.setError("נא למלא תוקף");
////                        this.expireTv.requestFocus();
//                        return false;
//                    }
//
//
//
//                } else {
//                    this.cvvEt.setError("חסרות ספרות");
//                    this.cvvEt.requestFocus();
//                    return false;
//                }
//            } else {
//                this.creditCardEt.setError("מס\' כרטיס לא תקין");
//                this.creditCardEt.requestFocus();
//                return false;
//            }
//        }
//    }

    private boolean isEmailvaild(EditText et) {
        String email = et.getText().toString().trim();
        String emailPattern_com = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+(\\.[a-z]+)*";
        String[] parts = email.split("\\@");
        boolean emailPatternTest = email.matches(emailPattern_com);
        boolean emailAtTest = parts.length == 2;
        return email.equals("")?false:(!emailPatternTest?false:emailAtTest);
    }

    private void setAutoNextFocus () {

        creditCardEt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(creditCardEt.getWindowToken(), 0);
                    monthsSpinnner.performClick();



                    return true;
                }
                return false;
            }
        });



        cvvEt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


                    return true;
                }
                return false;
            }
        });


    }




    public interface YaadPaymentRequest {
        String EXTRA_MASOF = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_MASOF";
        String EXTRA_ID = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_ID";
        String EXTRA_APIKEY = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_APIKEY";
        String EXTRA_AMOUNT = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_AMOUNT";
        String EXTRA_CURRENCY = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_CURRENCY";
        String EXTRA_HOLDER_NAME = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_HOLDER_NAME";
        String EXTRA_HOLDER_LNAME = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_HOLDER_LNAME";
        String EXTRA_INFO = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_INFO";
        String EXTRA_HIDE_AMOUNT_SQUARE = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_AMOUNT_SQUARE";
        String EXTRA_HIDE_ICONS = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_ICONS";
        String EXTRA_HIDE_ID_INPUT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_ID_INPUT";
        String EXTRA_HIDE_EXPIRY_INPUT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_EXPIRY_INPUT";
        String EXTRA_HIDE_CVV_INPUT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_CVV_INPUT";
        String EXTRA_HIDE_IOCARD = "com.YaadPayments.YaadPay.PayActivity.EXTRA_HIDE_IOCARD";

        String EXTRA_TASH_FIRST_PAYMENT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_TASH_FIRST_PAYMENT";
        String EXTRA_TASH_NUMBER = "com.YaadPayments.YaadPay.PayActivity.EXTRA_TASH_NUMBER";
        String EXTRA_CREDIT_TERM = "com.YaadPayments.YaadPay.PayActivity.EXTRA_CREDIT_TERM";


        String EXTRA_ICON_LINK = "com.YaadPayments.YaadPay.PayActivity.EXTRA_ICON_LINK";
        String EXTRA_TITLE_TEXT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_TITLE_TEXT";
        String EXTRA_BUTTON_TEXT = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BUTTON_TEXT";
        String EXTRA_SELF_HANDLE_SUCCESS_SCENARIO = "com.YaadPayments.YaadPay.PayActivity.EXTRA_SELF_HANDLE_SUCCESS_SCENARIO";
        String EXTRA_SELF_HANDLE_ERROR_SCENARIO = "com.YaadPayments.YaadPay.PayActivity.EXTRA_SELF_HANDLE_ERROR_SCENARIO";
        String EXTRA_BACKGROUND_COLOR_TOP = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_COLOR_TOP";
        String EXTRA_BACKGROUND_COLOR_SQUARE = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_COLOR_SQUARE";
        String EXTRA_BACKGROUND_COLOR_BOTTOM = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_COLOR_BOTTOM";
        String EXTRA_BACKGROUND_COLOR_BUTTON = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_COLOR_BUTTON";
        String EXTRA_BACKGROUND_DRAWABLE_TOP = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_DRAWABLE_TOP";
        String EXTRA_BACKGROUND_DRAWABLE_SQUARE = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_DRAWABLE_SQUARE";
        String EXTRA_BACKGROUND_DRAWABLE_BOTTOM = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_DRAWABLE_BOTTOM";
        String EXTRA_BACKGROUND_DRAWABLE_BUTTON = "com.YaadPayments.YaadPay.PayActivity.EXTRA_BACKGROUND_DRAWABLE_BUTTON";
        String CURRENCY_ILS = "1";
        String CURRENCY_USD = "2";
        String CURRENCY_EUR = "3";
    }

    public interface YaadPaymentResult {
        String ERROR_EXTRA_MASOF_MISSING = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_MASOF_MISSING";
        String ERROR_EXTRA_APIKEY_MISSING = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_APIKEY_MISSING";
        String ERROR_EXTRA_AMOUNT_MISSING = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_AMOUNT_MISSING";
        String ERROR_EXTRA_HOLDER_NAME_MISSING = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_HOLDER_NAME_MISSING";
        String ERROR_EXTRA_INFO_MISSING = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_EXTRA_INFO_MISSING";
        String ERROR_USER_CANCELED = "com.YaadPayments.YaadPay.YaadPaymentActivity.ERROR_USER_CANCELED";
        String SUCCESS = "com.YaadPayments.YaadPay.YaadPaymentActivity.SUCCESS";
        String SELF_HANDLE_SUCCESS = "com.YaadPayments.YaadPay.YaadPaymentActivity.SELF_HANDLE_SUCCESS";
        String SELF_HANDLE_ERROR = "com.YaadPayments.YaadPay.YaadPaymentActivity.SELF_HANDLE_ERROR";
        String EXTRA_RESULT_ANSWER = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_RESULT_ANSWER";
        String EXTRA_RESULT_PARAMETERS = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_RESULT_PARAMETERS";
        String EXTRA_RESULT_PARAMETERS_STRING = "com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_RESULT_PARAMETERS_STRING";
    }
    private String convertAgura2Shekel(String shekel){
        String shekelTemp = (Double.parseDouble(shekel) / 100 + "").replace("\\.","");;
        return shekelTemp;
    }
    private class TransactionTask extends AsyncTask<Void, Void, Boolean> {
        String responseString = "";
        private String mCardNumber;
        private String mHolderID;
        private String monthSelecetd;
        private String yearSelected;
        private String mCardCode;
        private String mEmail;
        private String mHoldername;
        private String mHoldeLrname;

        public TransactionTask(String mCardNumber, String mHolderID, String monthSelected, String yearSelected, String mCardCode, String mEmail, String mHoldername, String mHolderLname) {
            this.mCardNumber = mCardNumber;
            this.mHolderID = mHolderID;
            this.monthSelecetd = monthSelected;
            this.yearSelected = yearSelected;
            this.mCardCode = mCardCode;
            this.mEmail = mEmail;
            this.mHoldername = mHoldername;
            this.mHoldeLrname = mHolderLname;

            showCircleDialog(YaadPaymentActivity.this);
        }

        protected Boolean doInBackground(Void... params) {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://icom.yaad.net/p/");
            ArrayList nameValuePair = new ArrayList(2);
            nameValuePair.add(new BasicNameValuePair("action", "soft"));
            nameValuePair.add(new BasicNameValuePair("Masof", YaadPaymentActivity.this.masof));
            nameValuePair.add(new BasicNameValuePair("PassP", YaadPaymentActivity.this.apiKey));
            //new HashMap();
            HashMap hashMap = (HashMap)YaadPaymentActivity.this.bundle.getSerializable("hashMap");
            if(hashMap != null && hashMap.size() > 0) {
                Iterator e = hashMap.entrySet().iterator();

                while(e.hasNext()) {
                    Map.Entry out = (Map.Entry)e.next();
                    String key = (String)out.getKey();
                    if(key.contains("yaadObj.yaadPay.")){
                        key = key.replace("yaadObj.yaadPay.","");
                        Log.e("XXX", "doInBackground: key: " + key);
                    }
                    nameValuePair.add(new BasicNameValuePair(key , (String)out.getValue()));
                }
            }


            nameValuePair.add(new BasicNameValuePair("email", this.mEmail));
            nameValuePair.add(new BasicNameValuePair("UserId", this.mHolderID));
            nameValuePair.add(new BasicNameValuePair("ClientName", this.mHoldername));
            nameValuePair.add(new BasicNameValuePair("ClientLName", this.mHoldeLrname));
            nameValuePair.add(new BasicNameValuePair("Info", YaadPaymentActivity.this.bundle.getString("com.YaadPayments.YaadPay.YaadPaymentActivity.EXTRA_INFO")));
            nameValuePair.add(new BasicNameValuePair("Amount", amount));
            nameValuePair.add(new BasicNameValuePair("CC", this.mCardNumber.replaceAll("  ", "")));
            nameValuePair.add(new BasicNameValuePair("cvv", this.mCardCode));


            if(tashNo != null){
                nameValuePair.add(new BasicNameValuePair("Tash", tashNo));
            }
            if(tashType != null){
                switch (tashType){
                    case "8":
                    case "6":
                        nameValuePair.add(new BasicNameValuePair("tashType", tashType));
                        break;
                }
            }
            if(tashFirstPayment != null){
                nameValuePair.add(new BasicNameValuePair("TashFirstPayment", tashFirstPayment));
            }
            Log.e(APP_TAG, "doInBackground: 1 "+tashFirstPayment );
            Log.e(APP_TAG, "doInBackground: 2 "+amount );
            nameValuePair.add(new BasicNameValuePair("Tmonth", monthSelecetd));
            nameValuePair.add(new BasicNameValuePair("Tyear", yearSelected));
            String currencyX = parseCurrencyCode();

            nameValuePair.add(new BasicNameValuePair("Coin", currencyX));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, "Windows-1255"));
            } catch (UnsupportedEncodingException var12) {
                var12.printStackTrace();
            }

            try {
                HttpResponse e1 = httpClient.execute(httpPost);
                if(e1.getStatusLine().getStatusCode() != 200) {
                    e1.getEntity().getContent().close();
                    throw new IOException(e1.getStatusLine().getReasonPhrase());
                }

                ByteArrayOutputStream out1 = new ByteArrayOutputStream();
                e1.getEntity().writeTo(out1);
                this.responseString = out1.toString("Windows-1255");
                Log.d("Answer", "===============    doInBackground: " + responseString);
                out1.close();
            } catch (ClientProtocolException var10) {
                var10.printStackTrace();
            } catch (IOException var11) {
                var11.printStackTrace();
            }

            return Boolean.valueOf(true);
        }

        protected void onPostExecute(Boolean aBoolean) {
            hideCircleDialog(300);
//            YaadPaymentActivity.this._progressDialog.dismiss();
            //Toast.makeText(YaadPaymentActivity.this, "responseString: " +  responseString , Toast.LENGTH_SHORT).show();
            if(this.responseString.contains("<html>")) {
                alertbox("עסקה לא הושלמה",responseString,YaadPaymentActivity.this);
            } else {

                YaadPaymentActivity.this.finalResult = YaadPaymentActivity.this.parseRespond(this.responseString);
                YaadPaymentActivity.this.finalStringResult = responseString;
                String result = (String)YaadPaymentActivity.this.finalResult.get("CCode");

                if(result != null && (result.equals("0") || result.equals("600") || result.equals("700") || result.equals("800"))) {
                    exit(YaadPaymentActivity.YaadPaymentResult.SUCCESS);
                }
                else{
                    String response = "";
                    if(finalResult.get("errMsg") == null){
                        response = responseString;
                    }else{
                        response = finalResult.get("errMsg");
                        try {
                            response = URLDecoder.decode(response, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    alertbox("עסקה לא הושלמה",response,YaadPaymentActivity.this);
                }

            }
        }
    }

    private String parseCurrencyCode() {

        //String CURRENCY_ILS = "1";
        //String CURRENCY_USD = "2";
        //String CURRENCY_EUR = "3";
        //String[] defaultCurrencyArray = new String[]{"₪", "$", "€", "£"};
        String[] defaultCurrencyCodeArray = new String[]{"376", "840", "978"};
        for(int i =0 ;i< defaultCurrencyArray.length-1 ; i++){
            if(defaultCurrencyCodeArray[i].equals(currency)){
                return i + "";
            }
        }
        return "-1";
    }

    public void alertbox(String title, String message,Activity activiy) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activiy).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            } });
        alertDialog.show();
    }

    public static float dpToPx (Context context, float valueInDp) {

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void setMonthsSpinner () {

        final ArrayList<String> monthsArray = new ArrayList<String>();

        monthsArray.add("חודש");
        for (int i = 1; i < 13; i++) {
            monthsArray.add(String.valueOf(i));
        }
        final CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), monthsArray);
        monthsSpinnner.setAdapter(customAdapter);

        monthsSpinnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

                if (monthsInitCounter == 0) {
                    monthsInitCounter++;
                    return;
                }

                TextView tv = (TextView) view.findViewById(R.id.textView);
                monthSelected = tv.getText().toString();
                monthsSpinnner.setBackground(getResources().getDrawable(R.drawable.first_closed));

                //open months spinner if its not set to "שנה"
                if (yearSelected == null) {
                    yearsSpinner.performClick();
                } else {
                    cvvEt.requestFocus();
                }
            }

            @Override
            public void onNothingSelected (AdapterView<?> parent) {

            }
        });

        monthsSpinnner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch (View v, MotionEvent event) {
                if (monthsArray.get(0).equals("חודש")) {
                    monthsArray.remove(0);
                }
                return false;
            }
        });

        int itemHeightInDp   = 35; //the height of a single item in custom custom adapter
        int numOfItemsToShow = 4;
        float dpToShow = itemHeightInDp * numOfItemsToShow;
        int  pixelsToShow = (int) dpToPx(YaadPaymentActivity.this, dpToShow);


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(monthsSpinnner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(pixelsToShow);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

    }


    private void setYearsSpinner () {


        final ArrayList<String> yearsArray = new ArrayList<String>();
        yearsArray.add("שנה");

        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = year; i < year+12; i++) {
            yearsArray.add(String.valueOf(i));
        }
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), yearsArray);
        yearsSpinner.setAdapter(customAdapter);

        yearsSpinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch (View v, MotionEvent event) {
                if (yearsArray.get(0).equals("שנה")) {
                    yearsArray.remove(0);
                }

                return false;
            }
        });

        yearsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

                if (yearsInitCounter == 0) {
                    yearsInitCounter++;
                    return;
                }


                TextView tv = (TextView) view.findViewById(R.id.textView);
                yearSelected = tv.getText().toString();
                yearsSpinner.setBackground(getResources().getDrawable(R.drawable.first_closed));



                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                cvvEt.requestFocus();

            }

            @Override
            public void onNothingSelected (AdapterView<?> parent) {

            }
        });




        int itemHeightInDp   = 35; //the height of a single item in custom custom adapter
        int numOfItemsToShow = 4;
        float dpToShow = itemHeightInDp * numOfItemsToShow;
        int  pixelsToShow = (int) dpToPx(YaadPaymentActivity.this, dpToShow);


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(yearsSpinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(pixelsToShow);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        int width = monthsSpinnner.getWidth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            monthsSpinnner.setDropDownWidth(width);
        }
    }

}

