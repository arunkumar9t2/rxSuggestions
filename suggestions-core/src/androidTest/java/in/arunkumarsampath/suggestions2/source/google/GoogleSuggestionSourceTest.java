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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

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
        suggestionSource.getSuggestions("Hello")
                .test(1)
                .assertSubscribed()
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void getSuggestionsFailure() {
        suggestionSource.getSuggestions("")
                .test()
                .assertError(FileNotFoundException.class)
                .assertNotComplete()
                .assertNoValues();
    }


    @Test
    public void getSuggestions_ValuesNotEmpty() {
        suggestionSource.getSuggestions("Hello")
                .test()
                .assertComplete();
    }
}