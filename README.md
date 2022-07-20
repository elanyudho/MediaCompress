## Get Started

## Step 1.

Add token to gradle.properties

```groovy
authToken=AUTHENTICATION_TOKEN
```

## Step 2.

Then use authToken as username in your build.gradle

```groovy
allprojects {
    repositories {
        ...
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
 }
```

or if u dont find it allProject add authToken in your settings.gradle

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { 
            url 'https://jitpack.io' 
            credentials { username authToken }
        }

    }
}
```

### Step 3. Add Coroutines

```groovy
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutines}"
```

### Step 4. Try Video Compress

```kotlin
VideoCompressor.start(
   context = applicationContext, // => This is required
   uris = List<Uri>, // => Source can be provided as content uris
   isStreamable = true,
   storageConfiguration = StorageConfiguration(
      saveAt = Environment.DIRECTORY_MOVIES, // => the directory to save the compressed video(s). Will be ignored if isExternal = false.
      isExternal = true, // => false means save at app-specific file directory. Default is true.
      fileName = "output-video.mp4" // => an optional value for a custom video name.
   ),
   configureWith = Configuration(
      quality = VideoQuality.MEDIUM,
      isMinBitrateCheckEnabled = true,
      videoBitrate = 3677198, /*Int, ignore, or null*/
      disableAudio = false, /*Boolean, or ignore*/
      keepOriginalResolution = false, /*Boolean, or ignore*/
      videoWidth = 360.0, /*Double, ignore, or null*/
      videoHeight = 480.0 /*Double, ignore, or null*/
   ),
   listener = object : CompressionListener {
       override fun onProgress(index: Int, percent: Float) {
          // Update UI with progress value
          runOnUiThread {
          }
       }

       override fun onStart(index: Int) {
          // Compression start
       }

       override fun onSuccess(index: Int, size: Long, path: String?) {
         // On Compression success
       }

       override fun onFailure(index: Int, failureMessage: String) {
         // On Failure
       }

       override fun onCancelled(index: Int) {
         // On Cancelled
       }

   }
)
```
