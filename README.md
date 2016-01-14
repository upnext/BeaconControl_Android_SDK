BeaconControl Android SDK
=======================

#### Integration Guide ver. 1.0

BeaconControl Android SDK is an Android library providing APIs to manage beacon detection and react to beacon monitoring and ranging actions.


## What does this library do?

The library allows the application to start and stop beacon detection. If beacon monitoring is enabled, the application may receive callback about starting and ending of actions. Actions have to be defined earlier by the user. There are three types of actions: url, coupon and custom. Url and coupon actions are handled automatically by the library, however custom actions may be performed manually by the user.

## Documentation

On the [project website](https://www.beaconctrl.com/dev/androidsdk-docs/) you will find JavaDoc [documentation](https://www.beaconctrl.com/dev/androidsdk-docs/references).

## Download

To use BeaconControl Android SDK, firstly download the BeaconsControlSDK-1.0.aar file, which you can find in the out/ directory of this repository. Then, put this file into the libs/ subdirectory of the module in which you want to use BeaconControl Android SDK.

If you are using Gradle, include in your dependencies:

```groovy
dependencies {
	...
    compile(name: 'BeaconsControlSDK-1.0', ext: 'aar')
    ...
}
```

You also need to add dependencies that are used by BeaconControlSDK.

```groovy
dependencies {
	...
    compile 'org.altbeacon:android-beacon-library:2.7'

    compile 'com.squareup.retrofit:retrofit:2.0.0-beta1'

    compile "com.android.support:support-v4:23.1.1"

    compile 'com.squareup.okhttp:okhttp:2.1.0'
    compile 'com.squareup.okhttp:okhttp-urlconnection:2.1.0'
    compile 'com.squareup.retrofit:converter-jackson:1.7.1'
    ...
}
```

Please make sure that your libs/ subdirectory is in the repositories list.

```groovy
repositories {
	...
    flatDir {
        dirs 'libs'
    }
    ...
}
```

You may also need to add one or more exclusions, as shown below:

```groovy
android {
	...
    packagingOptions {
      	exclude 'META-INF/DEPENDENCIES.txt'
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/dependencies.txt'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/notice.txt'
    }
    ...
 }
```

## Quick Start

At the beginning you have to get the BeaconSDK class instance to interact with Beacon SDK.

```java
BeaconSDK beaconSDK = BeaconSDK.getInstance(context, clientId, clientSecret, userId);
```

You may enable logging.

```java
beaconSDK.enableLogging(true);
```

The next step is defining and setting the BeaconDelegate object.

```java
beaconSDK.setBeaconDelegate(new BeaconDelegate() {
  	@Override
    public boolean shouldPerformActionAutomatically() {
    	return true;
    }

  	@Override
  	public void onActionStart(Action action) {
  		// you may do something when action starts
 	}

  	@Override
  	public void onActionEnd(Action action) {
  		// you may do something when action ends
  	}
});
```

If you want, you may set callback for errors. Then every time an error occurs, you will be notified.

```java
beaconSDK.setBeaconErrorListener(new BeaconErrorListener() {
	@Override
	public void onError(ErrorCode errorCode) {
		Log.e(TAG, "Some error occured in Beacon SDK: " + errorCode.name());
	}
});
```

BeaconSDK configuring is done now. When needed, you may start beacon monitoring and ranging.

```java
beaconSDK.startScan();
```

When you do not need monitoring and ranging, you may stop beacon detection.

```java
beaconSDK.stopScan();
```

## License

You can find license in LICENSE.txt file.

If you have any troubles, please contact us at feedback@beaconctrl.com.