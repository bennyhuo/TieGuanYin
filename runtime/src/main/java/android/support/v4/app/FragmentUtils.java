package android.support.v4.app;

import java.util.ArrayList;

/**
 * Created by benny on 2/13/18.
 */

public class FragmentUtils {
    public static void addSharedElement(FragmentTransaction fragmentTransaction, String sourceName, String targetName) {
        if (fragmentTransaction instanceof BackStackRecord) {
            if (FragmentTransition.supportsTransition()) {
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
