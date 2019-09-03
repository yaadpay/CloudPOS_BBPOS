package example.cloudpos_bbpos.YaadPay;
import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by yoni on 5/8/17.
 */
public class FontsManager {

    public static Typeface getRegular (Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/FbKolya-Regular.otf");
    }

    public static Typeface getBold (Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/FbKolya-Bold.otf");
    }




}
