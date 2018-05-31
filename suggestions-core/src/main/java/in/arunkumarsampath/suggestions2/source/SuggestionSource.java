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

package in.arunkumarsampath.suggestions2.source;

import android.support.annotation.NonNull;

import in.arunkumarsampath.suggestions2.item.SuggestionItem;
import io.reactivex.Flowable;

/**
 * Suggestions source is the definition for a class that provides suggestions for a given string.
 * <p>
 * To enable functionality override {@link #getSuggestions(String)}, add your suggestion logic
 * and return a {@link Flowable<SuggestionItem>}
 * <p>
 * {@link #getSuggestions(String)} can be called repeatedly and in any order. If stateful code is added,
 * be sure to handle it correctly for each invocation.
 */
public interface SuggestionSource {

    @NonNull
    Flowable<SuggestionItem> getSuggestions(@NonNull final String value);
}
