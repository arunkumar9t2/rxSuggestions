package in.arunkumarsampath.suggestions;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static in.arunkumarsampath.suggestions.RxSuggestionsInternal.SEARCH_TERM_TO_URL_MAPPER;
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
        return Observable.just(new Pair<>(searchTerm, maxSuggestions))
                .map(SEARCH_TERM_TO_URL_MAPPER)
                .flatMap(RxSuggestionsInternal::suggestionsObservable);
    }

    /**
     * Same as {@link #fetch(String, int)} but with default no of suggestions specified by {@link #DEFAULT_NO_SUGGESTIONS}
     *
     * @param searchTerm
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
        return stringObservable -> stringObservable
                .filter(s -> s != null && !s.isEmpty())
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .switchMap(searchTerm -> fetch(searchTerm, maxSuggestions))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public static Observable.Transformer<String, List<String>> suggestionsTransformer() {
        return suggestionsTransformer(DEFAULT_NO_SUGGESTIONS);
    }
}
