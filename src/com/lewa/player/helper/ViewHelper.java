package com.lewa.player.helper;

import android.app.ActionBar;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.lewa.Lewa;
import com.lewa.player.R;

/**
 * Created by wuzixiu on 1/3/14.
 */
public class ViewHelper {
    public static void addTranspantFooter(ListView lv, int height) {
        View view = Lewa.inflater().inflate(R.layout.v_transpant_footer, null);

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//        if (height > 0) {
//            view.getLayoutParams().height = height;
//        }
        view.setLayoutParams(layoutParams);
        lv.addFooterView(view);
    }

    public static void addTranspantFooter(ListView lv) {
        addTranspantFooter(lv, 0);
    }

    public static void hideStatusBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = activity.getActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
