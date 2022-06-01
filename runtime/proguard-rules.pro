# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.bennyhuo.tieguanyin.annotations.*

-keep @com.bennyhuo.tieguanyin.annotations.Generated public class ** {
    public static void inject(android.app.Activity, android.os.Bundle);
    public static void saveState(android.app.Activity, android.os.Bundle);
}

-keep @com.bennyhuo.tieguanyin.annotations.Generated public class ** {
    public static void inject(androidx.fragment.app.Fragment, android.os.Bundle);
    public static void saveState(androidx.fragment.app.Fragment, android.os.Bundle);
}

-keep public class androidx.fragment.app.Fragment {
    java.lang.String mWho;
}