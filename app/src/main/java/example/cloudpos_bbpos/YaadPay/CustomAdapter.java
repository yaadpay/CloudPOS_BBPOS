package example.cloudpos_bbpos.YaadPay;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Dimension;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import example.cloudpos_bbpos.R;

/**
 * Created by yoni on 5/4/17.
 */
public class CustomAdapter extends BaseAdapter {
    Context context;

    ArrayList<String> countryNames;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, ArrayList<String> countryNames) {

        this.context = applicationContext;
        this.countryNames = countryNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return countryNames.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner_items, null);
        TextView names = (TextView) view.findViewById(R.id.textView);
        Typeface regular = FontsManager.getRegular(context);
        names.setTypeface(regular);
        names.setTextSize(Dimension.SP, 19);
        names.setText(countryNames.get(i));
        return view;
    }
}
