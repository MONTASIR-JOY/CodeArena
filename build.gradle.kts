buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.5.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.8")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}

