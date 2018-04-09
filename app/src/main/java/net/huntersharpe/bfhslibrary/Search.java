package net.huntersharpe.bfhslibrary;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Search extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    private List<String> mAllValues = new ArrayList<>();
    private List<String> subTexts = new ArrayList<>();

    private String barcode = "";
    private ArrayAdapter<String> mAdapter;
    private Context mContext;

    private TextView puAuthorView;
    private TextView puDescView;
    private ImageView puImageView;
    private Button reserveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
    public void onListItemClick(ListView listView, View v, int position, long id) {
        //TODO: Change Reserve button text when book is already reserved by that user.
        String item = (String) listView.getAdapter().getItem(position);
        makeJsonRequest(item, Type.POPUP);
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.search_popup, null);
        puAuthorView = popupView.findViewById(R.id.popupAuthorTextView);
        puDescView = popupView.findViewById(R.id.popupDescTextBox);
        puImageView = popupView.findViewById(R.id.popupBookCover);
        reserveButton = popupView.findViewById(R.id.popupReserveButton);

        TextView titleView = popupView.findViewById(R.id.popupTitleTextView);
        titleView.setText(item);
        final PopupWindow window = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setElevation(5.0f);
        window.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.popupCancelButton).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        popupView.findViewById(R.id.popupReserveButton).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: reserveBook()
                DatabaseManager dbManager = new DatabaseManager();
                if(reserveButton.getText().toString().equals("Reserve")){
                    dbManager.releaseBook(barcode);
                    return;
                }
                dbManager.setUid(getContext());
                dbManager.reserveBook(barcode);
                if(dbManager.getResult()){
                    Toast.makeText(getContext(), "Reservation Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), HomeScreenActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getContext(), "Somebody already has that book reserved :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void loadSearchIntoList(String query) throws JSONException, ExecutionException,
            InterruptedException {
        mAllValues.clear();
        String searchTerms = query.replaceAll(" ", "+");
        //TODO: Load JSON Response into listView from libManager
        makeJsonRequest(searchTerms, Type.SEARCH);
    }

    private enum Type{
        SEARCH,
        POPUP
    }
    private void makeJsonRequest(final String request, final Type type){
        String url = "https://www.googleapis.com/books/v1/volumes?q=";
        if(type==Type.SEARCH){
            url += request + "&maxResults=40";
        }else{
            url += request + "&maxResults=1";
        }
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject bookObject;
                            if(type==Type.SEARCH){
                                for(int i = 0; i < response.getJSONArray("items").length();
                                    i++){
                                    bookObject = response.getJSONArray("items")
                                            .getJSONObject(i);
                                    mAllValues.add(bookObject.getJSONObject("volumeInfo")
                                            .getString("title"));
                                }
                                mAdapter = new ArrayAdapter<>(mContext,
                                        android.R.layout.simple_list_item_1, mAllValues);
                                setListAdapter(mAdapter);
                            }else{
                                DatabaseManager dbManager = new DatabaseManager();
                                bookObject = response.getJSONArray("items")
                                        .getJSONObject(0);
                                puAuthorView.setText(bookObject.getJSONObject("volumeInfo")
                                        .getString("authors"));
                                puDescView.setText(bookObject.getJSONObject("volumeInfo").getString(
                                        "description"));
                                barcode = bookObject.getJSONObject("volumeInfo").getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
                                if(dbManager.exists(barcode, "reservations")){
                                    if(dbManager.exists(barcode, "reservations", dbManager.gUid)){
                                        reserveButton.setText("Release");
                                    }
                                    reserveButton.setClickable(false);
                                    reserveButton.setAlpha(0.5f);
                                }
                                RequestQueue imageQueue = Volley.newRequestQueue(getContext());
                                ImageRequest imageRequest = new ImageRequest(
                                        bookObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("smallThumbnail"),
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap response) {
                                                puImageView.setImageBitmap(response);
                                            }
                                        },
                                        0,
                                        0,
                                        ImageView.ScaleType.CENTER_CROP,
                                        Bitmap.Config.RGB_565,
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                            }
                                        });
                                imageQueue.add(imageRequest);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Set to null or whatever
            }
        });
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onDetach() {
        super.onDetach();
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
}
