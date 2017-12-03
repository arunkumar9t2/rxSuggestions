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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.UnknownHostException;
import java.util.List;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RxSuggestionsTest {
    public RxSuggestionsTest() {
    }

    @Test
    public void fetchSuggestionsCountTest() throws Exception {
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        int maxSuggestions = 1;
        RxSuggestions.fetch("a", maxSuggestions).subscribe(testSubscriber);

        final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();

        if (Util.isOnline()) {
            testSubscriber.assertValueCount(1);
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();
            assertTrue(!nextEvents.isEmpty() && nextEvents.size() == 1);
            assertTrue(nextEvents.get(0).size() == maxSuggestions);
        } else {
            testSubscriber.assertNoValues();
            assertTrue(testSubscriber.getOnErrorEvents().get(0) instanceof UnknownHostException);
        }
    }

    @Test
    public void fetchSuggestionsNoErrorTest() throws Exception {
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        RxSuggestions.fetch("Something").subscribe(testSubscriber);
        if (Util.isOnline()) {
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
        } else {
            assertNoValuesAndOneError(testSubscriber);
        }
    }

    public void fetchSuggestionValueReceivedTest() throws Exception {
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        RxSuggestions.fetch("a").subscribe(testSubscriber);

        if (Util.isOnline()) {
            testSubscriber.assertCompleted();
            testSubscriber.assertNoErrors();
            final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();
            for (String suggestion : nextEvents.get(0)) {
                assertTrue(!suggestion.isEmpty());
            }
        } else {
            assertNoValuesAndOneError(testSubscriber);
        }
    }

    @Test
    public void fetchSuggestionsForEmptyString() throws Exception {
        String searchTerm = "    ";
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        RxSuggestions.fetch(searchTerm).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
    }

    private void assertNoValuesAndOneError(TestSubscriber<List<String>> testSubscriber) {
        testSubscriber.assertNoValues();
        assertTrue(!testSubscriber.getOnErrorEvents().isEmpty());
    }
}
