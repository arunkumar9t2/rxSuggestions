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

    @NonNull
    public static Observable<List<String>> fetch(@NonNull String searchTerm, final int maxSuggestions) {
        searchTerm = requireNonNull(searchTerm, "searchTerm cannot be null");
        return Observable.just(new Pair<>(searchTerm, maxSuggestions))
                .map(SEARCH_TERM_TO_URL_MAPPER)
                .flatMap(RxSuggestionsInternal::suggestionsObservable);
    }

    @NonNull
    public static Observable<List<String>> fetch(@NonNull String searchTerm) {
        return fetch(searchTerm, DEFAULT_NO_SUGGESTIONS);
    }

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
