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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RxSuggestionsTransformerTest {
    public RxSuggestionsTransformerTest() {
    }

    @Test
    public void fetchSuggestionsTransformerTest() {
        final Observable<List<String>> listObservable = Observable
                .just("1", "2", "3")
                .concatMap(each -> Observable.just(each).delay(5, TimeUnit.SECONDS))
                .compose(RxSuggestions.suggestionsTransformer());

        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        listObservable.subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        if (Util.isOnline()) {
            testSubscriber.assertNoErrors();
            assertTrue(testSubscriber.getOnNextEvents().size() == 3);
            assertAllListContentsNotEmpty(testSubscriber);
        } else {
            testSubscriber.assertValueCount(3);
            assertTrue(testSubscriber.getOnNextEvents().size() == 3);
            testSubscriber.assertNoErrors();
            // Transformer swallows error to continue stream.
            for (List<String> suggestions : testSubscriber.getOnNextEvents()) {
                assertTrue(suggestions.isEmpty());
            }
        }
    }

    @Test
    public void transformerEmptyFilterTest() {
        final Observable<List<String>> listObservable = Observable
                .just("1", "        ")
                .concatMap(each -> Observable.just(each).delay(5, TimeUnit.SECONDS))
                .compose(RxSuggestions.suggestionsTransformer());

        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        listObservable.subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);

        if (Util.isOnline()) {
            for (List<String> suggestions : testSubscriber.getOnNextEvents()) {
                for (String suggestion : suggestions) {
                    assertTrue(!suggestion.isEmpty());
                }
            }
        } else {
            for (List<String> suggestions : testSubscriber.getOnNextEvents()) {
                assertTrue(suggestions.isEmpty());
            }
        }
    }

    private void assertAllListContentsNotEmpty(TestSubscriber<List<String>> testSubscriber) {
        for (List<String> suggestions : testSubscriber.getOnNextEvents()) {
            for (String suggestion : suggestions) {
                assertTrue(!suggestion.isEmpty());
            }
        }
    }

}
