package example.cloudpos_bbpos.YaadPay;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by yoni on 5/4/17.
 */
public class BpEditText  extends EditText {

    Context context;


    public BpEditText (Context context, AttributeSet attrs) {

        super(context, attrs);
        this.context = context;
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {


        if(keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus();

        }
        return super.onKeyPreIme(keyCode, event);
    }

}
