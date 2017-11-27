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
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RxSuggestionsTest {
    public RxSuggestionsTest() {
    }

    @Test
    public void fetchSuggestionsCountTest() throws Exception {
        int maxSuggestions = 1;
        final String searchTerm = "a";

        final TestSubscriber<List<String>> fetchTestSubscriber = TestSubscriber.create();
        RxSuggestions.fetch(searchTerm, maxSuggestions).subscribe(fetchTestSubscriber);

        final TestSubscriber<List<String>> transformerTestSubscriber = TestSubscriber.create();
        getTransformerObservable(searchTerm, maxSuggestions).subscribe(transformerTestSubscriber);
        transformerTestSubscriber.awaitTerminalEvent();

        for (TestSubscriber<List<String>> testSubscriber : Arrays.asList(fetchTestSubscriber, transformerTestSubscriber)) {
            testSubscriber.assertValueCount(maxSuggestions);
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();

            final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();
            assertTrue(!nextEvents.isEmpty() && nextEvents.size() == maxSuggestions);
            assertTrue(nextEvents.get(0).size() == maxSuggestions);
        }
    }

    @Test
    public void fetchSuggestionsNoErrorTest() throws Exception {
        String searchTerm = "Something";

        final TestSubscriber<List<String>> fetchTestSubscriber = TestSubscriber.create();
        RxSuggestions.fetch(searchTerm).subscribe(fetchTestSubscriber);

        final TestSubscriber<List<String>> transformerTestSubscriber = getTransformerTestSubscriber(searchTerm);

        for (TestSubscriber<List<String>> testSubscriber : Arrays.asList(fetchTestSubscriber, transformerTestSubscriber)) {
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
        }
    }

    @Test
    public void fetchSuggestionValueReceivedTest() throws Exception {
        String searchTerm = "a";

        final TestSubscriber<List<String>> fetchTestSubscriber = TestSubscriber.create();
        RxSuggestions.fetch(searchTerm).subscribe(fetchTestSubscriber);

        final TestSubscriber<List<String>> transformerTestSubscriber = getTransformerTestSubscriber(searchTerm);

        for (TestSubscriber<List<String>> testSubscriber : Arrays.asList(fetchTestSubscriber, transformerTestSubscriber)) {
            final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();
            for (String suggestion : nextEvents.get(0)) {
                assertTrue(!suggestion.isEmpty());
            }
        }
    }

    @Test
    public void fetchSuggestionsForEmptyString() throws Exception {
        String searchTerm = "    ";

        final TestSubscriber<List<String>> fetchTestSubscriber = TestSubscriber.create();
        RxSuggestions.fetch(searchTerm).subscribe(fetchTestSubscriber);

        final TestSubscriber<List<String>> transformerTestSubscriber = getTransformerTestSubscriber(searchTerm);

        for (TestSubscriber<List<String>> testSubscriber : Arrays.asList(transformerTestSubscriber)) {
            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();
        }
    }

    @NonNull
    private TestSubscriber<List<String>> getTransformerTestSubscriber(String searchTerm) {
        final TestSubscriber<List<String>> transformerTestSubscriber = TestSubscriber.create();
        getTransformerObservable(searchTerm).subscribe(transformerTestSubscriber);
        transformerTestSubscriber.awaitTerminalEvent();
        return transformerTestSubscriber;
    }

    /**
     * Helper to get a stream with {@link RxSuggestions#suggestionsTransformer()} applied.
     *
     * @param searchTerm Search Term
     * @return
     */
    private Observable<List<String>> getTransformerObservable(String searchTerm) {
        return Observable.just(searchTerm).compose(RxSuggestions.suggestionsTransformer());
    }

    private Observable<List<String>> getTransformerObservable(String searchTerm, final int count) {
        return Observable.just(searchTerm).compose(RxSuggestions.suggestionsTransformer(count));
    }
}
