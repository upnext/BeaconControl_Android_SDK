BeaconControl Android SDK
=======================

#### Integration Guide

BeaconControl Android SDK is an Android library providing APIs to manage beacon detection and react to beacon monitoring and ranging actions.


## What does this library do?

The library allows the application to start and stop beacon detection. If beacon monitoring is enabled, the application may receive callback about starting and ending of actions. Actions have to be defined earlier by the user. There are three types of actions: url, coupon and custom. Url and coupon actions may be handled automatically by the library, however custom actions must be performed manually by the user.

## Documentation

On the [project website](https://beaconcontrol.io/dev/android-sdk-docs) you will find JavaDoc [documentation](https://beaconcontrol.io/dev/android-sdk-docs/references).

## Adding library dependency

If you are using Gradle, include in your dependencies:

```groovy
dependencies {
	...
    compile 'io.upnext.beaconcontrol:beaconcontrol:1.0.0'
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

At the beginning you have to get the BeaconControl class instance to interact with BeaconControl SDK.

```java
BeaconControl beaconControl = BeaconControl.getInstance(context, clientId, clientSecret, userId);
```

You may enable logging.

```java
beaconControl.enableLogging(true);
```

The next step is defining and setting the BeaconDelegate object. If you decide to perform an action automatically, there will be a WebView activity shown for url and coupon actions.

```java
beaconControl.setBeaconDelegate(new BeaconDelegate() {
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

    @Override
    public void onBeaconsConfigurationLoaded(List<Beacon> list) {
        // you can save the beacons from the configuration for future use
    }

    @Override
    public void onBeaconProximityChanged(Beacon beacon) {
        // you may do something when beacon proximity changes
    }
});
```

However, you should handle custom action by your own. Here is an example:

```java
@Override
public void onActionStart(Action action) {
    long actionId = action.id;
    String actionName = action.name;

    switch (action.type) {
        case url:
            onUrlActionStart(actionId, actionName, action.payload);
            break;
        case coupon:
            // do something for coupon action
            break;
        case custom:
            onCustomActionStart(actionId, actionName, action.customAttributes);
            break;
        default:
      		throw new IllegalStateException("Unknown action type.");
 	}
}

private void onUrlActionStart(long actionId, String actionName, Action.Payload payload) {
	if (payload != null) {
		String url = payload.url;
		// you may do something for url action
	}
	...
}

private void onCustomActionStart(long actionId, String actionName, List<Action.CustomAttribute> customAttributes) {
	for (Action.CustomAttribute customAttribute : customAttributes) {
		long attributeId = customAttribute.id;
		String attributeName = customAttribute.name;
		String attributeValue = customAttribute.value;

		// you may do something for custom attribute
	}
	...
}
```

If you want, you may set callback for errors. Then every time an error occurs, you will be notified.

```java
beaconControl.setBeaconErrorListener(new BeaconErrorListener() {
	@Override
	public void onError(ErrorCode errorCode) {
		Log.e(TAG, "Some error occured in Beacon SDK: " + errorCode.name());
	}
});
```

BeaconSDK configuring is done now. When needed, you may start beacon monitoring and ranging.

```java
beaconControl.startScan();
```

When the scanning is started you can request at any given time a reload of configuration - it will be fetched from the backend server.
If the fetch was successful, you'll be notified in `onBeaconsConfigurationLoaded` of `BeaconDelegate` interface.
```java
beaconControl.reloadConfiguration();
```

When you do not need monitoring and ranging, you may stop beacon detection.

```java
beaconControl.stopScan();
```

## Advanced configuration

If you want to communicate with your private instance of BeaconControl backend server, you need to overwrite the url by providing it in your application's `strings.xml` file:
```xml
<string name="sdk_config__service_base_url">http://your.server.com/api/v1/</string>
```

## License

You can find license in LICENSE.txt file.

If you have any troubles, please contact us at feedback@beaconcontrol.io.