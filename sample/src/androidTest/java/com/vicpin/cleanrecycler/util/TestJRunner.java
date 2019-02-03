package com.vicpin.cleanrecyclerview.util;

import android.os.Bundle;

import androidx.test.runner.AndroidJUnitRunner;

/**
 * Created by Oesia on 29/05/2017.
 */

public class TestJRunner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        /*
        The workaround for Mockito issue #922
        https://github.com/mockito/mockito/issues/922
        */
        arguments.putString("notPackage", "net.bytebuddy");
        super.onCreate(arguments);
    }

}
