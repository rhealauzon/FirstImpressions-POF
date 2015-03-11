package ca.calvinrempel.firstimpressions_pof;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Chris on 2015-03-10.
 */
public class FontManager {
    public static void setFont(Context c, TextView tv, String fontName)
    {
        Typeface font = Typeface.createFromAsset(c.getAssets(), fontName);
        tv.setTypeface(font);
    }

    public static void setFont(Context c, Button b, String fontName)
    {
        Typeface font = Typeface.createFromAsset(c.getAssets(), fontName);
        b.setTypeface(font);
    }
}
