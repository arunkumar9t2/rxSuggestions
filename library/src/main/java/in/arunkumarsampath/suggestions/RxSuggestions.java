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

package in.arunkumarsampath.suggestions;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static in.arunkumarsampath.suggestions.RxSuggestionsInternal.SEARCH_TERM_TO_URL_MAPPER;
import static in.arunkumarsampath.suggestions.RxSuggestionsInternal.emptyStringFilter;
import static in.arunkumarsampath.suggestions.RxSuggestionsInternal.requireNonNull;

public class RxSuggestions {
    private static final String TAG = RxSuggestions.class.getSimpleName();
    private static final int DEFAULT_NO_SUGGESTIONS = 5;

    /**
     * Returns an Observable that emits a list of suggestions for given {@code searchTerm}. The # of
     * suggestions is limited by {@code maxSuggestions}.
     * <p>
     * Note: This Observable does not run on any particular {@link Schedulers}.
     *
     * @param searchTerm     Keyword
     * @param maxSuggestions The upper bound for no of suggestions.
     * @return Observable of list of suggestions.
     */
    @NonNull
    public static Observable<List<String>> fetch(@NonNull String searchTerm, final int maxSuggestions) {
        searchTerm = requireNonNull(searchTerm, "searchTerm cannot be null");
        return Observable.just(searchTerm)
                .compose(emptyStringFilter())
                .map(term -> new Pair<>(term, maxSuggestions))
                .map(SEARCH_TERM_TO_URL_MAPPER)
                .flatMap(RxSuggestionsInternal::suggestionsObservable);
    }

    /**
     * Same as {@link #fetch(String, int)} but with default no of suggestions specified by {@link #DEFAULT_NO_SUGGESTIONS}
     *
     * @param searchTerm Keyword
     * @return Observable of list of suggestions.
     */
    @NonNull
    public static Observable<List<String>> fetch(@NonNull String searchTerm) {
        return fetch(searchTerm, DEFAULT_NO_SUGGESTIONS);
    }

    /**
     * Convenient Transformer which transforms a stream of keywords into their suggestions. This
     * transformer modifies the input stream to reduce the number of requests when the user is actively
     * typing. The transformer applies correct {@link Schedulers} to perform network call and then
     * return safely to the UI thread. This makes it easy to use this with RxBinding library if you want
     * to generate suggestions for a widget like {@link android.widget.EditText}
     *
     * @param maxSuggestions The upper bound for no of suggestions.
     * @return Transformed observable.
     */
    @NonNull
    public static Observable.Transformer<String, List<String>> suggestionsTransformer(final int maxSuggestions) {
        //noinspection Convert2MethodRef
        return stringObservable -> stringObservable
                .compose(emptyStringFilter())
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .switchMap(searchTerm -> fetch(searchTerm, maxSuggestions)
                        .onErrorReturn(throwable -> Collections.emptyList()))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public static Observable.Transformer<String, List<String>> suggestionsTransformer() {
        return suggestionsTransformer(DEFAULT_NO_SUGGESTIONS);
    }
}
