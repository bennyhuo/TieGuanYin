package com.bennyhuo.tieguanyin.sample;

import android.app.Activity;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;

@Builder
public class PrimitiveDefaultTestActivity extends Activity{

    @Optional(charValue = '1')
    char charValue;

    @Optional(byteValue = 2)
    byte byteValue;

    @Optional(shortValue = 3)
    short shortValue;

    @Optional(intValue = 4)
    int intValue;

    @Optional(longValue = 5)
    long longValue;

    @Optional(floatValue = 6f)
    float floatValue;

    @Optional(doubleValue = 7)
    double doubleValue;
}
