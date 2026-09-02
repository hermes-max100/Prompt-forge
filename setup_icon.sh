#!/bin/bash
set -e

mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/mipmap-anydpi-v26

cat << 'BG' > app/src/main/res/drawable/ic_launcher_background.xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#1c1c19"
        android:pathData="M0,0h108v108h-108z" />
</vector>
BG

cat << 'FG' > app/src/main/res/drawable/ic_launcher_foreground.xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#8aa37a"
        android:pathData="M54,20L20,70h68z" />
    <path
        android:fillColor="#f2efe9"
        android:pathData="M54,35L35,65h38z" />
</vector>
FG

cat << 'ADAPTIVE' > app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
ADAPTIVE

cp app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

