package com.example.dvidr_000.lighthauzproject;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestSentFragment extends Fragment implements DataAdapter.ItemClickCallback{
    private RecyclerView recView;
    private DataAdapter adapter;
    private SessionManager sessionManager;
    private HashMap<String,String> user;
    private List<User> users;
    private String idStr;

    private TextView notice;
    private ProgressBar pb;

    public RequestSentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request_sent,container,false);

        getActivity().setTitle(R.string.RequestsSent);

        sessionManager = new SessionManager(getContext());
        user = sessionManager.getUserDetails();
        idStr = user.get(SessionManager.KEY_ID);

        users = new ArrayList<>();

        notice = (TextView) v.findViewById(R.id.reqSentNotice);
        pb = (ProgressBar) v.findViewById(R.id.pBarReqSent);
        recView = (RecyclerView) v.findViewById(R.id.rec_list_req_sent);
        recView.setLayoutManager(new LinearLayoutManager(getActivity()));

        requestList();

        adapter = new DataAdapter(users, getActivity(), "USER");
        adapter.setItemClickCallback(this);
        return v;
    }

    @Override
    public void onItemClick(int p, View view) {
        Bundle args = new Bundle();
        args.putString("USER_ID",users.get(p).getId());

        ViewProfileFragment fragment = new ViewProfileFragment();
        fragment.setArguments(args);

        FragmentTransaction tr = getActivity().getSupportFragmentManager().beginTransaction();
        tr.replace(R.id.fragment_container_detail,fragment);
        tr.addToBackStack(null);
        tr.commit();
    }

    public void requestList(){
        pb.setVisibility(View.VISIBLE);

        // Tag used to cancel the request
        String tag_json = "json_object_req";

        String url = "http://lighthauz.herokuapp.com/api/connections/requests/sent/" + idStr;

        JsonObjectRequest req = new JsonObjectRequest(url,null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyLog.d(response.toString());
                        users.clear();
                        JSONArray myArray;
                        try{
                            myArray = response.getJSONArray("sentByUser");
                            if (myArray.length()==0){
                                notice.setVisibility(View.VISIBLE);
                            }
                            else {
                                for (int i = 0; i < myArray.length(); i++) {
                                    String userId = myArray.getJSONObject(i).getString("id");
                                    String name = myArray.getJSONObject(i).getString("name");
                                    String bio = myArray.getJSONObject(i).getString("bio");
                                    String profilePic = myArray.getJSONObject(i).getString("profilePic");

                                    User newUser = new User(userId, name, profilePic);
                                    newUser.setBio(bio);
                                    users.add(newUser);
                                }
                                recView.setAdapter(adapter);
                            }
                        }

                        catch (JSONException e){
                            Log.e("MYAPP", "unexpected JSON exception", e);
                        }
                        pb.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pb.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        AppSingleton.getInstance(getContext()).addToRequestQueue(req, tag_json);
    }

}
