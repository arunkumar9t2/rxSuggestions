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

package in.arunkumarsampath.suggestions.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.arunkumarsampath.suggestions.RxSuggestions;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_box)
    EditText searchBox;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private final CompositeSubscription subs = new CompositeSubscription();

    private SuggestionsAdapter suggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        suggestionsAdapter = new SuggestionsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(suggestionsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subs.add(RxTextView.afterTextChangeEvents(searchBox)
                .map(changeEvent -> changeEvent.editable().toString())
                .compose(RxSuggestions.suggestionsTransformer())
                .subscribe(this::setSuggestions, Throwable::printStackTrace));
    }

    private void setSuggestions(@NonNull List<String> suggestions) {
        recyclerView.post(() -> suggestionsAdapter.setSuggestions(suggestions));
    }

    @Override
    protected void onPause() {
        super.onPause();
        subs.clear();
    }

    @OnClick(R.id.fab)
    public void onClick() {
        searchBox.setText("");
    }

    static class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ListItemHolder> {
        private final List<String> suggestions = new ArrayList<>();

        SuggestionsAdapter() {
            setHasStableIds(true);
        }

        @Override
        public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListItemHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(ListItemHolder holder, int position) {
            if (holder.itemView instanceof TextView) {
                ((TextView) holder.itemView).setText(suggestions.get(position));
                ((TextView) holder.itemView).setTextColor(Color.BLACK);
            }
        }

        @Override
        public long getItemId(int position) {
            return suggestions.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

        void setSuggestions(@NonNull List<String> newStrings) {
            final SuggestionDiff suggestionDiff = new SuggestionDiff(suggestions, newStrings);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(suggestionDiff, true);
            suggestions.clear();
            suggestions.addAll(newStrings);
            diffResult.dispatchUpdatesTo(this);
        }

        static class ListItemHolder extends RecyclerView.ViewHolder {
            ListItemHolder(View itemView) {
                super(itemView);
            }
        }

        private static class SuggestionDiff extends DiffUtil.Callback {

            private final List<String> newList;
            private final List<String> oldList;

            SuggestionDiff(@NonNull List<String> oldList, @NonNull List<String> newList) {
                this.oldList = oldList;
                this.newList = newList;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return caseInsensitiveComparison(oldItemPosition, newItemPosition);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return caseInsensitiveComparison(oldItemPosition, newItemPosition);
            }

            private boolean caseInsensitiveComparison(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equalsIgnoreCase(newList.get(newItemPosition));
            }
        }
    }
}
