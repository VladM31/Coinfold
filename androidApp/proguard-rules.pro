# R8 shrinks and optimizes the release build but does not rename anything:
# stack traces in Crashlytics stay readable and reflection-based libraries cannot break on renamed classes.
-dontobfuscate

# Keep file names and line numbers in crash reports.
-keepattributes SourceFile,LineNumberTable
