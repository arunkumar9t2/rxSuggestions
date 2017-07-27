package in.arunkumarsampath.suggestions;


import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxSuggestions {
    private static final String BASE = "http://suggestqueries.google.com/complete/search?";
    private static final String CLIENT = "client=toolbar";
    private static final String SEARCH_URL = BASE + CLIENT + "&q=";
    private final static String UTF8 = "UTF-8";
    private static final int DEFAULT_MAX_SUGGESTIONS = 5;

    /**
     * Returns an observable containing the list of suggestions for the given {@param term}.
     * The max no of suggestions is determined by {@param maxSuggestions}. Also note that, there
     * might be not enough suggestions generated to meet {@param maxSuggestions} size, hence it is
     * not guaranteed that the no of suggestions will be always {@param maxSuggestions}
     * <p>
     * Note: This method returns a raw {@link Observable}. You have to handle threading yourself
     * by specifying {@link Observable#subscribeOn(Scheduler)} and {@link Observable#observeOn(Scheduler)}
     * This is by design to allow maximum flexibility.
     *
     * @param term           The string to fetch suggestions for.
     * @param maxSuggestions Max no of suggestions to fetch.
     * @return An observable containing list of suggestions for given {@param term}.
     * @throws NullPointerException if term is null.
     */
    public static Observable<List<String>> fetch(@NonNull String term, final int maxSuggestions) {
        term = requireNonNull(term, "Term cannot be null");
        return Observable.just(new Pair<>(term, maxSuggestions))
                .map(TERM_TO_URL_MAPPER)
                .map(URL_TO_SUGGESTION_MAPPER);
    }

    /**
     * Same as {@link #fetch(String, int)} with no of suggestions defaulting to {@link #DEFAULT_MAX_SUGGESTIONS}
     *
     * @return An observable containing list of suggestions for given {@param term}.
     * @throws NullPointerException if term is null.
     */
    public static Observable<List<String>> fetch(@NonNull String term) {
        return fetch(term, DEFAULT_MAX_SUGGESTIONS);
    }

    /**
     * Observable transformer to facilitate fetching suggestions without breaking Observable chain and
     * maintaining backpressure. Tie this to your existing Observable chain using the
     * {@link Observable#compose(Observable.Transformer)} operator. After suggestions are fetched
     * the results are emitted by default on {@link AndroidSchedulers#mainThread()}.
     * It is a good practice to use some throttling strategy to avoid fetching when term is continually
     * updated. For example, when user is actively typing in a {@link android.widget.EditText}
     *
     * @param maxSuggestions Max no of suggestions to fetch.
     * @return Transformed observable containing list of suggestions.
     */
    public static Observable.Transformer<String, List<String>> suggestionsTransformer(final int maxSuggestions) {
        return new Observable.Transformer<String, List<String>>() {
            @Override
            public Observable<List<String>> call(Observable<String> stringObservable) {
                return stringObservable
                        .observeOn(Schedulers.io())
                        .onBackpressureLatest()
                        .flatMap(new Func1<String, Observable<List<String>>>() {
                            @Override
                            public Observable<List<String>> call(String term) {
                                return fetch(term, maxSuggestions);
                            }
                        }).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * Same as {@link #suggestionsTransformer(int)} with max suggestions set as {@link #DEFAULT_MAX_SUGGESTIONS}
     *
     * @return Transformed observable containing list of suggestions.
     */
    public static Observable.Transformer<String, List<String>> suggestionsTransformer() {
        return suggestionsTransformer(DEFAULT_MAX_SUGGESTIONS);
    }

    private static final Func1<Pair<String, Integer>, Pair<String, Integer>> TERM_TO_URL_MAPPER
            = new Func1<Pair<String, Integer>, Pair<String, Integer>>() {
        @Override
        public Pair<String, Integer> call(Pair<String, Integer> termPair) {
            return new Pair<>(SEARCH_URL.concat(termPair.first).replace(" ", "+"), termPair.second);
        }
    };

    /**
     * Observable mapper responsible for converting given search URL to list of suggestions.
     * Works by getting XML response and parsing it. The max number of suggestions is limited to
     * max suggestions parameter specified in {@link #fetch(String, int)} second parameter or the
     * the maximum suggestions returned by the Google suggest API whichever is minimum.
     */
    private static final Func1<Pair<String, Integer>, List<String>> URL_TO_SUGGESTION_MAPPER
            = new Func1<Pair<String, Integer>, List<String>>() {
        @Override
        public List<String> call(Pair<String, Integer> termPair) {
            final int maxSuggestions = termPair.second;
            final String searchUrl = termPair.first;
            final List<String> suggestions = new ArrayList<>();

            HttpURLConnection connection = null;
            try {
                final URL url = new URL(searchUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                final InputStream inputStream = connection.getInputStream();

                final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                final XmlPullParser xmlParser = factory.newPullParser();
                xmlParser.setInput(inputStream, UTF8);

                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT && suggestions.size() < maxSuggestions) {
                    if (eventType == XmlPullParser.START_TAG && xmlParser.getName().equalsIgnoreCase("suggestion")) {
                        final String suggestion = xmlParser.getAttributeValue(0);
                        suggestions.add(suggestion);
                    }
                    eventType = xmlParser.next();
                }
            } catch (IOException | XmlPullParserException e) {
                Observable.error(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return suggestions;
        }
    };

    private static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }
}
