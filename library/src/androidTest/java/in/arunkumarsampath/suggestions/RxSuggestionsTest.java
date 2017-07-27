package in.arunkumarsampath.suggestions;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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
        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();
        assertTrue(!nextEvents.isEmpty() && nextEvents.size() == 1);
        assertTrue(nextEvents.get(0).size() == maxSuggestions);
    }

    @Test
    public void fetchSuggestionsNoErrorTest() throws Exception {
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        RxSuggestions.fetch("Something").subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    public void fetchSuggestionValueReceivedTest() throws Exception {
        final TestSubscriber<List<String>> testSubscriber = TestSubscriber.create();
        RxSuggestions.fetch("a").subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        final List<List<String>> nextEvents = testSubscriber.getOnNextEvents();
        for (String suggestion : nextEvents.get(0)) {
            assertTrue(!suggestion.isEmpty());
        }
    }
}
