package com.arun.rxgoogleinstant.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.arun.rxgoogleinstant.RxSuggestions;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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

    private final CompositeSubscription subscription = new CompositeSubscription();
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
        subscription.add(RxTextView.afterTextChangeEvents(searchBox)
                .map(new Func1<TextViewAfterTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                        return textViewAfterTextChangeEvent.editable().toString();
                    }
                }).filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .debounce(300, TimeUnit.MILLISECONDS)
                .compose(RxSuggestions.suggestionsTransformer())
                .doOnNext(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        suggestionsAdapter.setSuggestions(strings);
                    }
                }).doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, throwable.toString());
                    }
                }).subscribe());
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscription.clear();
    }

    @OnClick(R.id.fab)
    public void onClick() {
        searchBox.setText("");
    }

    static class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ListItemHolder> {
        private final List<String> strings = new ArrayList<>();

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
                ((TextView) holder.itemView).setText(strings.get(position));
                ((TextView) holder.itemView).setTextColor(Color.BLACK);
            }
        }

        @Override
        public long getItemId(int position) {
            return strings.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return strings.size();
        }

        public void setSuggestions(@NonNull List<String> newStrings) {
            final SuggestionDiff suggestionDiff = new SuggestionDiff(strings, newStrings);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(suggestionDiff, true);
            strings.clear();
            strings.addAll(newStrings);
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
