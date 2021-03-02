package me.grishka.houseclub.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.Nullable;

import me.grishka.appkit.api.SimpleCallback;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.methods.SearchUsers;

public class SearchFragment extends UserListFragment {

    @Nullable
    private String query;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().inflateMenu(R.menu.search);
        initSearchView(getToolbar().getMenu());
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void doLoadData(int offset, int count) {
        if(currentRequest != null){
            currentRequest.cancel();
        }
        if(query == null || query.trim().isEmpty() || query.length() < 2){
            return;
        }
        currentRequest = new SearchUsers(query)
                .setCallback(new SimpleCallback<SearchUsers.Resp>(this) {
                    @Override
                    public void onSuccess(SearchUsers.Resp result) {
                        currentRequest=null;
                        data.clear();
                        onDataLoaded(result.users, false);
                    }
                })
                .exec();

    }

    private void initSearchView(Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacksAndMessages(null);
                query = newText;
                handler.postDelayed(() -> doLoadData(), 750);
                return false;
            }
        });
    }
}
