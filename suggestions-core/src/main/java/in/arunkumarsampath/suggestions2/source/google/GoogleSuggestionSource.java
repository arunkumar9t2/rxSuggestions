/*
 * Copyright 2018 Arunkumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.arunkumarsampath.suggestions2.source.google;

import android.support.annotation.NonNull;

import in.arunkumarsampath.suggestions2.item.SuggestionItem;
import in.arunkumarsampath.suggestions2.source.SuggestionSource;
import io.reactivex.Flowable;

/**
 * A suggestion source backed by Google suggest API. Hits the API, parses and
 */
public class GoogleSuggestionSource implements SuggestionSource {

    @NonNull
    @Override
    public Flowable<SuggestionItem> getSuggestions(@NonNull String value) {
        return Flowable.empty();
    }
}
