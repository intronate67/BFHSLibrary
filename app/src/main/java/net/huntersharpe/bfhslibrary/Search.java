package net.huntersharpe.bfhslibrary;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Search extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private static Bitmap bm = null;
    private List<String> mAllValues = new ArrayList<>();
    private List<String> subTexts = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private Context mContext;
    private static LibraryManager libManager;
    private static String coverUrl = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        //populateList();
        libManager = LibraryManager.getInstance();
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        String item = (String) listView.getAdapter().getItem(position);
        Log.i("ItemForPopup:", item);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.search_popup, null);
        TextView authView = popupView.findViewById(R.id.popupAuthorTextView);
        TextView titleView = popupView.findViewById(R.id.popupTitleTextView);
        titleView.setText(item);
        TextView descView = popupView.findViewById(R.id.popupDescTextBox);
        try {
            //TODO: Find book specific cover
            //Get parent node.
            JSONObject book = ((JSONObject)libManager.getAsyncInstance()
                                    .execute(LibraryManager.CallType.SEARCH_QUERY).get())
                                    .getJSONArray("items").getJSONObject(position)
                                    .getJSONObject("volumeInfo");
            coverUrl = book.getJSONObject("imageLinks").getString("thumbnail");
            descView.setText(book.getString("description"));
            authView.setText(book.getString("authors"));
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        AsyncBitmapRequest task = new AsyncBitmapRequest();
        task.execute();
        ((ImageView)popupView.findViewById(R.id.popupBookCover)).setImageBitmap(bm);
        final PopupWindow window = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setElevation(5.0f);
        window.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        popupView.findViewById(R.id.popupCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        popupView.findViewById(R.id.popupReserveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: reserveBook()
                Toast.makeText(getContext(), "Reserved!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ListView listView = getView().findViewById(android.R.id.list);
        TextView emptyTextView = getView().findViewById(android.R.id.empty);
        listView.setEmptyView(emptyTextView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bottom_navigation_main, menu);
        SearchView searchView = getView().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");
        super.onCreateOptionsMenu(menu, inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            loadSearchIntoList(query);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    private void loadSearchIntoList(String query) throws JSONException, ExecutionException, InterruptedException {
        mAllValues.clear();
        String searchTerms = query.replaceAll(" ", "+");
        Log.i("loadSearchIntoList:", searchTerms);
        LibraryManager.content = searchTerms + "&maxResults=40";
        Log.i("loadSearchIntoList:", "Set Search Terms");
        JSONObject jsonResponse = (JSONObject)libManager.getAsyncInstance().execute(LibraryManager.CallType.SCAN_INFO).get();
        Log.i("jsonInfo", jsonResponse.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("title"));
        for(int i = 0; i < jsonResponse.getJSONArray("items").length(); i++){
            JSONObject tempObject = jsonResponse.getJSONArray("items").getJSONObject(i);
            String title = tempObject.getJSONObject("volumeInfo").getString("title");
            mAllValues.add(title);
            //TODO: Add subtexts
        }
        mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mAllValues);
        setListAdapter(mAdapter);
    }

    private static class AsyncBitmapRequest extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            bm = getImageBitmap(coverUrl);
            return null;
        }
    }
    private static Bitmap getImageBitmap(String url){
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Test", "Error getting bitmap", e);
        }
        return bm;
    }

}
