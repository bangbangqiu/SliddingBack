package com.vcredit.sliddingback;

import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.KeyGenerator;

public class Activity2 extends AppCompatActivity {

    private static final String TAG = "Activity2";
    private SliddingBackLayout sBLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        sBLayout = (SliddingBackLayout) findViewById(R.id.activity_main);

        for(int i=0;i<sBLayout.getChildCount();i++){
            View childAt = sBLayout.getChildAt(i);
            Log.d(TAG, "onCreate: "+childAt.toString());
        }
        sBLayout.setBackActivityListenner(new SliddingBackLayout.BackActivityListenner() {
            @Override
            public void onSliddingOver(boolean canBack) {
                if (canBack) {
                    finish();
                    overridePendingTransition(0,0);
                }
            }
        });

    }



}
