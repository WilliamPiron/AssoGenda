package fr.paris10.projet.assogenda.assogenda.ui.activites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fr.paris10.projet.assogenda.assogenda.R;
import fr.paris10.projet.assogenda.assogenda.daos.DAOUser;
import fr.paris10.projet.assogenda.assogenda.model.Association;
import fr.paris10.projet.assogenda.assogenda.ui.adapter.SearchAssociationAdapter;


public class ListAssociationActivity extends AppCompatActivity {

    public ArrayList<Association> associations;
    private SearchAssociationAdapter adapter;
    private ListView listView;
    private DatabaseReference database;
    private SearchView searchView;
    private DAOUser userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_association);

        database = FirebaseDatabase.getInstance().getReference("association");
        userDatabase = DAOUser.getInstance();

        associations = new ArrayList<>();
        adapter = new SearchAssociationAdapter(this, associations);

        listView = (ListView) findViewById(R.id.list_association_activity_list_view);
        listView.setAdapter(adapter);

        loadAssociations();

        searchView = (SearchView) findViewById(R.id.list_association_activity_search);
        loadSearchListener();
    }

    /**
     * Display association corresponding to user input
     */
    private void loadSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                searchView.setIconified(true);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        /**
         * Display back all association
         */
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.filter("");
                searchView.clearFocus();
                return true;
            }
        });
    }

    /**
     * Load all association in database and if association is followed or not
     */
    private void loadAssociations() {
        database.orderByChild("name").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Association association = dataSnapshot.getValue(Association.class);
                association.id = dataSnapshot.getKey();
                associations.add(association);
                adapter.add(association);
                adapter.notifyDataSetChanged();
                FirebaseDatabase.getInstance().getReference("user-follow-asso")
                        .child(userDatabase.getCurrentUserId())
                        .child(association.id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d("ASSOFOLLOWED", "followed");
                                    association.followed = true;
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d("ASSOFOLLOWED", "not followed");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
