.. _setup-android:

Setup Android
=============

The AusweisApp SDK Wrapper for android is available as a maven package in the default
repository of Android. To use it, make the following adjustments to your Gradle configuration:

1. Make sure that the repositories section includes the governikus repository:

  .. code-block:: groovy

    allprojects {
        [...]
        repositories {
            [...]
            mavenCentral()
        }
    }

2. Add the library to the dependencies section:

  .. code-block:: groovy

    implementation 'com.governikus.ausweisapp:sdkwrapper:2.3.0'

App Bundle
----------

The AusweisApp SDK Wrapper uses native libraries which need to be extracted when used in an App Bundle,
otherwise the SDK will not work correctly.

Add the following statement to your app's build.gradle file:

.. code-block:: groovy

    android {
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }


Initialization of the Android Application
-----------------------------------------
The integrated SDK used by the AusweisApp SDK Wrapper creates a fork of the Android "main"
Application once started.  Due to this, the Application is instantiated a
second time. Thus, it must ensure that any initialization
(e.g. Firebase connections) is only carried out
once. To do so the following snippet may prove useful:

.. code-block:: java

  class MyAwesomeApp : Application() {
      override fun onCreate() {
          super.onCreate()

          if (getProcessName().endsWith("ausweisapp2_service"))
              return
      }
  }
