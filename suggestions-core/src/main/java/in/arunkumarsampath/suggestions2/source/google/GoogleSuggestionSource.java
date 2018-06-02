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
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import in.arunkumarsampath.suggestions2.item.StringSuggestionItem;
import in.arunkumarsampath.suggestions2.item.SuggestionItem;
import in.arunkumarsampath.suggestions2.source.SuggestionSource;
import in.arunkumarsampath.suggestions2.util.Util;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

/**
 * A suggestion source backed by Google suggest API.
 */
public class GoogleSuggestionSource implements SuggestionSource {

    private static final String SUGGEST_URL_FORMAT = "http://suggestqueries.google.com/complete/search?client=toolbar&q=%s";
    private static final String SUGGESTION = "suggestion";

    @NonNull
    @Override
    public Flowable<SuggestionItem> getSuggestions(@NonNull String value) {
        return Flowable.just(value)
                .map(Util::prepareSearchTerm)
                .flatMap(searchTerm -> Flowable.create(emitter -> {
                    HttpURLConnection httpURLConnection = null;

                    final String suggestUrl = String.format(SUGGEST_URL_FORMAT, searchTerm);
                    try {
                        final URL url = new URL(suggestUrl);
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.connect();

                        try (final InputStream inputStream = httpURLConnection.getInputStream()) {
                            final XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
                            xmlParser.setInput(inputStream, "");

                            int eventType = xmlParser.getEventType();

                            // Perform back pressure aware iteration.
                            while (eventType != END_DOCUMENT && !emitter.isCancelled() && emitter.requested() >= 0) {
                                boolean validEvent = eventType == START_TAG && xmlParser.getName().equalsIgnoreCase(SUGGESTION);
                                if (validEvent) {
                                    final String suggestion = xmlParser.getAttributeValue(0);
                                    emitter.onNext(new StringSuggestionItem(suggestion));
                                }
                                eventType = xmlParser.next();
                            }
                            emitter.onComplete();
                        }
                    } finally {
                        cancel(httpURLConnection);
                    }

                    // Cleanup resources on finish
                    HttpURLConnection httpUrlConnCopy = httpURLConnection;
                    emitter.setCancellable(() -> cancel(httpUrlConnCopy));
                }, BackpressureStrategy.LATEST));
    }

    private void cancel(@Nullable HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }
}
