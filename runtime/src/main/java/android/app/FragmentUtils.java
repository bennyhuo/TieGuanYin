package android.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by benny on 2/13/18.
 */

public class FragmentUtils {

    static Field mWhoField;
    static Method findFragmentByWhoMethod;

    static {
        try {
            mWhoField = Fragment.class.getDeclaredField("mWho");
            mWhoField.setAccessible(true);

            findFragmentByWhoMethod = Class.forName("android.app.FragmentManagerImpl").getDeclaredMethod("findFragmentByWho", String.class);
            findFragmentByWhoMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getWhoFromFragment(Fragment fragment){
        try {
            return (String) mWhoField.get(fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Fragment findFragmentByWho(FragmentManager fragmentManager, String who){
        try {
            return (Fragment) findFragmentByWhoMethod.invoke(fragmentManager, who);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
