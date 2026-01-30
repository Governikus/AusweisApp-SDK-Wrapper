# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
-dontobfuscate

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
-keepnames class com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel { *; }
-keepclassmembers class * extends com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel {
   public <init>(...);
}

# Ktor
-keep class io.ktor.** { *; }
-keep class io.netty.** { *; }

# Get rid of warnings about unreachable but unused classes referred to by Netty
-dontwarn io.netty.**
-dontwarn com.sun.**
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.apache.**
-dontwarn org.openjsse.**
-dontwarn java.lang.management.ManagementFactory # Needed for io.ktor:ktor-websockets
-dontwarn java.lang.management.RuntimeMXBean # Needed for io.ktor:ktor-websockets
