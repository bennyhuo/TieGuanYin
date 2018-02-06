package com.bennyhuo.activitybuilder.runtime.result.fields;

import java.lang.reflect.Field;

/**
 * Created by benny on 2/6/18.
 */

public class ActivityField extends ListenerField {

    public ActivityField(Object receiver, Field field) {
        super(receiver, field);
    }
}
