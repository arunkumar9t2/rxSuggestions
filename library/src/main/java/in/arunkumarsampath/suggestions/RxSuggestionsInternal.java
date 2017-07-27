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

import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

/**
 * Created by Arunkumar on 28-07-2017.
 */
class RxSuggestionsInternal {

    private static final String BASE_URL = "http://suggestqueries.google.com/complete/search?";
    private static final String CLIENT = "client=toolbar";
    private static final String SEARCH_URL = BASE_URL + CLIENT + "&q=";
    private final static String UTF8 = "UTF-8";
    private static final String SUGGESTION = "suggestion";

    @NonNull
    static final Func1<Pair<String, Integer>, Pair<String, Integer>> SEARCH_TERM_TO_URL_MAPPER
            = termPair -> Pair.create(SEARCH_URL.concat(termPair.first.trim()).replace(" ", "+"), termPair.second);

    static Observable<List<String>> suggestionsObservable(final Pair<String, Integer> searchPair) {
        return Observable.create(new Action1<Emitter<List<String>>>() {
            private HttpURLConnection httpURLConnection;

            @Override
            public void call(Emitter<List<String>> suggestionListEmitter) {
                // Set cancellable to clean up connection
                suggestionListEmitter.setCancellation(this::disconnect);

                final int maxSuggestions = searchPair.second;
                final String searchUrl = searchPair.first;
                final List<String> suggestions = new ArrayList<>();

                httpURLConnection = null;
                try {
                    final URL url = new URL(searchUrl);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.connect();

                    try (final InputStream inputStream = httpURLConnection.getInputStream()) {
                        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(false);
                        final XmlPullParser xmlParser = factory.newPullParser();
                        xmlParser.setInput(inputStream, UTF8);

                        int eventType = xmlParser.getEventType();
                        while (eventType != END_DOCUMENT && suggestions.size() < maxSuggestions) {
                            if (eventType == START_TAG && xmlParser.getName().equalsIgnoreCase(SUGGESTION)) {
                                final String suggestion = xmlParser.getAttributeValue(0);
                                suggestions.add(suggestion);
                            }
                            eventType = xmlParser.next();
                        }
                        suggestionListEmitter.onNext(suggestions);
                        suggestionListEmitter.onCompleted();
                    }
                } catch (IOException | XmlPullParserException e) {
                    suggestionListEmitter.onError(e);
                } finally {
                    disconnect();
                }
            }

            private void disconnect() {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    httpURLConnection = null;
                }
            }
        }, Emitter.BackpressureMode.LATEST);
    }


    static <T> T requireNonNull(T obj, @NonNull String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }
}
