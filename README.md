[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg)](https://android-arsenal.com/api?level=16)[ ![Download](https://api.bintray.com/packages/arunkumar9t2/maven/suggestions/images/download.svg) ](https://bintray.com/arunkumar9t2/maven/suggestions/_latestVersion)
# Notice

Version 2 supporting RxJava 2 is in development in `master` branch. For version 1 with RxJava 1 support, visit [v 1.0 branch](https://github.com/arunkumar9t2/rxSuggestions/tree/v1.0)

# RxSuggestions

A RxJava library to fetch search suggestions backed by `Google Suggest`.

If you have a content based app, this library makes it easier to show search suggestions so that your users spend less time typing the whole word.

The implementation is decoupled meaning the suggestions fetching can be used for any purpose and not only limited to search.

# Demo
![RxSuggestionsDemo](https://raw.githubusercontent.com/arunkumar9t2/rxSuggestions/master/art/demo.gif)
# Download

Library is available via `jcenter()`.

* Add `jcenter()` to your project level `build.gradle` file.

```groovy
allprojects {
    repositories {
        jcenter()
    }
}
```

* Add library dependency to module level `build.gradle` file.

```groovy
dependencies{
    compile 'in.arunkumarsampath:suggestions:1.0.3'
}
```

# Project Setup

This library is compiled with Java 8 and thus requires your project to have Java 8 compilation enabled. This was done since the old Jack compiler is deprecated in favor of improved Java 8 compiler starting from Android Studio 3.0+ and to take advantage of `lamdas` and `try with resources`.

If you are not using Android Studio 3.0+, [follow this guide.](https://developer.android.com/studio/write/java8-support.html) Else if you already are, then add these lines to force compilation with Java 8.

```groovy
android {
  // Configure only for each module that uses Java 8
  // language features (either in its source code or
  // through dependencies).
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

# Usage

This library provides two different ways to fetch suggestions.

* Using `RxSuggestion#fetch(String)`. Example: Fetch suggestions for the word `Batman`:

    ```java
    RxSuggestions.fetch("Batman")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(suggestions -> {
                    // List of suggestions here.
                });
    ```

* This library provides a convenient `Transformer` which takes a stream of strings and converts them in their suggestions. This is very helpful if you use library such as `RxBinding` with `EditText`.

    ```java
    RxTextView.afterTextChangeEvents(searchBox)
                .map(changeEvent -> changeEvent.editable().toString())
                .compose(RxSuggestions.suggestionsTransformer())
                .doOnNext(this::setSuggestions) // Your suggestions
                .doOnError(t -> Log.e(TAG, t.toString()))
                .subscribe()
    ```
    Input transforming to reduce network requests is also handled by library itself so you don't have to use `debounce` etc.

# Motivation

This feature was initially developed for use in my browser app [Chromer.](https://github.com/arunkumar9t2/chromer)

# Contributions

If you are a developer and would like to improve this library, please consider making a pull request or create an issue so I can look into it.

* Code style is Android Studio default.
* No hungarian notation.

# License

Copyright 2017 Arunkumar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
