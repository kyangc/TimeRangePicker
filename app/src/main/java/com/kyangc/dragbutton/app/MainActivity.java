package com.kyangc.dragbutton.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;

/**
 * @author chengkangyang on 12/4/14
 */
public class MainActivity extends Activity {

    TimeRangePickerFragment timeRangePickerFragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        timeRangePickerFragment = new TimeRangePickerFragment();
        timeRangePickerFragment
                .setIsRectangleDraggable(false)
                .setScrollSpeed(10);
        fragmentTransaction.add(R.id.fr_fragment_container, timeRangePickerFragment, "time_rang_fragment").commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
