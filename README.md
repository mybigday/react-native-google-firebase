# React Native Google Firbase

Firebase 3 (google version) native framework wrap.

## This is not finish yet!!!!!

## Installation
### Mostly automatic install

```bash
$ npm install rnpm --global
$ npm install react-native-google-firebase --save
$ rnpm link
```

### Android Installation
For android you will need change your `${project}/android/build.gradle` file
```
buildscript {
    ...
    dependencies {
        classpath 'com.android.tools.build:gradle:1.3.1'
        classpath 'com.google.gms:google-services:3.0.0'
        // Add this line
        ...
    }
    ...
}
```

And change change your `${project}/android/app/build.gradle` file

```
apply plugin: 'com.google.gms.google-services'
// Add this line end of file
```

Last step put your `google-service.json` download from firebase website at `${project}/android/app/`

### iOS Installation
For ios download sdk from https://dl.google.com/firebase/sdk/ios/3_4_0/Firebase.zip
Unzip and see the README file for which Frameworks to include in to your project.

And add following library at `Build Phase -> Link Binary with ibraries`
```
libicucore.tbd

```

Last step put your `GoogleService-Info.plist` download from firebase website to your Xcode project 
