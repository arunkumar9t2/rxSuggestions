/*
 * Copyright 2017 Arunkumar
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
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
    private static final String SUGGESTION = "suggestion";

    private final static String UTF8 = "UTF-8";
    private final static String ISO = "ISO-8859-1";
    /**
     * Pre-processor to transform search term into the suggestions API URL.
     */
    @NonNull
    static final Func1<Pair<String, Integer>, Pair<String, Integer>> SEARCH_TERM_TO_URL_MAPPER
            = termPair -> Pair.create(SEARCH_URL.concat(termPair.first.trim()).replace(" ", "+"), termPair.second);

    /**
     * Returns an Observable which emits a list of suggestions for given search term and no of
     * suggestions encapsulated in {@param searchPair}
     *
     * @param searchPair Pair containing search term and no of suggestions.
     * @return
     */
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
                        xmlParser.setInput(inputStream, extractEncoding(httpURLConnection.getContentType()));

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
                } catch (Exception e) {
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

    /**
     * Tries to extract type of encoding for the given content type.
     *
     * @param contentType Content type gotten from {@link HttpURLConnection#getContentType()}
     * @return
     */
    @NonNull
    static String extractEncoding(@Nullable String contentType) {
        final String[] values;
        if (contentType != null) {
            values = contentType.split(";");
        } else {
            values = new String[0];
        }
        String charset = "";

        for (String value : values) {
            value = value.trim().toLowerCase();
            if (value.startsWith("charset="))
                charset = value.substring("charset=".length());
        }
        // http1.1 says ISO-8859-1 is the default charset
        if (charset.length() == 0)
            charset = ISO;
        return charset;
    }

    static Observable.Transformer<String, String> emptyStringFilter() {
        //noinspection Convert2MethodRef
        return stringObservable -> stringObservable
                .filter(s -> s != null)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    static <T> T requireNonNull(T obj, @NonNull String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }
}
