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

import android.text.TextUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import in.arunkumarsampath.suggestions2.Util;
import in.arunkumarsampath.suggestions2.item.SimpleSuggestionItem;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

public class GoogleSuggestionSourceTest {

    private static final String TAG = GoogleSuggestionSourceTest.class.getSimpleName();

    private GoogleSuggestionSource suggestionSource;

    @Before
    public void setUp() {
        suggestionSource = new GoogleSuggestionSource();
    }

    @After
    public void tearDown() {
        suggestionSource = null;
    }

    @Test
    public void getSuggestions() {
        Flowable<SimpleSuggestionItem> suggestions = suggestionSource.getSuggestions("Hello");
        if (Util.isOnline()) {
            suggestions.test(1)
                    .assertSubscribed()
                    .assertComplete()
                    .assertNoErrors();

            suggestions.test(3)
                    .assertValueCount(3)
                    .assertComplete()
                    .assertNoErrors();
        } else {
            testFailureNoEvents(suggestions);
        }
    }

    @Test
    public void getSuggestionsFailure() {
        Flowable<SimpleSuggestionItem> suggestions = suggestionSource.getSuggestions("");
        if (Util.isOnline()) {
            suggestions
                    .test()
                    .assertError(FileNotFoundException.class)
                    .assertNotComplete()
                    .assertNoValues();
        } else {
            testFailureNoEvents(suggestions);
        }
    }


    @Test
    public void getSuggestions_ValuesNotEmpty() {
        Flowable<SimpleSuggestionItem> suggestions = suggestionSource.getSuggestions("Hello");

        if (Util.isOnline()) {
            final TestSubscriber<SimpleSuggestionItem> suggestionItemTestSubscriber = suggestions.test();
            suggestionItemTestSubscriber
                    .assertNoErrors()
                    .assertComplete();

            final List<List<Object>> events = suggestionItemTestSubscriber.getEvents();
            final List<Object> onNextEvents = events.get(0);
            for (Object suggestionItem : onNextEvents) {
                Assert.assertTrue(suggestionItem instanceof SimpleSuggestionItem);
                Assert.assertNotNull(((SimpleSuggestionItem) suggestionItem).value());
                Assert.assertTrue(!TextUtils.isEmpty(((SimpleSuggestionItem) suggestionItem).value()));
            }
        } else {
            testFailureNoEvents(suggestions);
        }
    }

    private void testFailureNoEvents(Flowable<SimpleSuggestionItem> suggestions) {
        suggestions.test()
                .assertNoValues()
                .assertNotComplete();
    }
}