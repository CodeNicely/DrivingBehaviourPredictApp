package io.predict.example.journeys_app.helper;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by meghal on 22/5/17.
 */

public class PhoneUnlockedReceiver extends BroadcastReceiver {


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);

        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){

            if (keyguardManager.isKeyguardSecure()) {

                Toast.makeText(context, "Keyguard open", Toast.LENGTH_SHORT).show();
                //phone was unlocked, do stuff here
                System.out.println("User present and Keyguard Open");

                EventBus.getDefault().post(new LocationService.MessageEvent(true));
            }

        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            if( keyguardManager.inKeyguardRestrictedInputMode())
            {
                Toast.makeText(context, "Screen Off", Toast.LENGTH_SHORT).show();
                System.out.println("Screen off " + "LOCKED");
                EventBus.getDefault().post(new LocationService.MessageEvent(false));

            }
        }


    }
}