package androidx.fragment.app;

import android.os.Build;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * Created by benny on 2/13/18.
 */

public class SupportFragmentUtils {

    public static String getWhoFromFragment(Fragment fragment){
        return fragment.mWho;
    }

    public static Fragment findFragmentByWho(FragmentManager fragmentManager, String who){
        return ((FragmentManagerImpl)fragmentManager).findFragmentByWho(who);
    }

    public static void addSharedElement(FragmentTransaction fragmentTransaction, String sourceName, String targetName) {
        if (fragmentTransaction instanceof BackStackRecord) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                BackStackRecord backStackRecord = (BackStackRecord) fragmentTransaction;
                if (sourceName == null) {
                    throw new IllegalArgumentException("Unique transitionNames are required for all" +
                            " sharedElements");
                }
                if (backStackRecord.mSharedElementSourceNames == null) {
                    backStackRecord.mSharedElementSourceNames = new ArrayList<>();
                    backStackRecord.mSharedElementTargetNames = new ArrayList<>();
                } else if (backStackRecord.mSharedElementTargetNames.contains(targetName)) {
                    throw new IllegalArgumentException("A shared element with the target name '"
                            + targetName + "' has already been added to the transaction.");
                } else if (backStackRecord.mSharedElementSourceNames.contains(sourceName)) {
                    throw new IllegalArgumentException("A shared element with the source name '"
                            + sourceName + " has already been added to the transaction.");
                }

                backStackRecord.mSharedElementSourceNames.add(sourceName);
                backStackRecord.mSharedElementTargetNames.add(targetName);
            }
        }
    }
}
