package com.example.habobarberinterface;

import android.app.AlertDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.habobarberinterface.Adapter.MyStateAdapter;
import com.example.habobarberinterface.Common.Common;
import com.example.habobarberinterface.Common.SpacesItemDecoration;
import com.example.habobarberinterface.Interface.IOnAllStateLoadListener;
import com.example.habobarberinterface.Model.Barber;
import com.example.habobarberinterface.Model.City;
import com.example.habobarberinterface.Model.City2;
import com.example.habobarberinterface.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {

    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    CollectionReference allSalonCollection;

    IOnAllStateLoadListener iOnAllStateLoadListener;

    MyStateAdapter adapter;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Paper.init(this);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if(TextUtils.isEmpty(user))
        {
            setContentView(R.layout.activity_main);

            ButterKnife.bind(this);

            initView();

            init();

            loadAllStateFromFireStore();
        } else
        {
            Gson gson = new Gson();
            Common.state_name = Paper.book().read(Common.STATE_KEY);
            Common.selected_salon = gson.fromJson(Paper.book().read(Common.SALON_KEY,""),
                    new TypeToken<Salon>(){}.getType());
            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY,""),
                    new TypeToken<Barber>(){}.getType());

            Intent intent = new Intent(this, StaffHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }



    }

    private void loadAllStateFromFireStore() {
        dialog.show();

        allSalonCollection
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iOnAllStateLoadListener.onAllStateLoadFailed(e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<City> cities = new ArrayList<>();
                    for(DocumentSnapshot citySnapShot:task.getResult())
                    {
                        City2 extra;
                        City city = citySnapShot.toObject(City.class);

                        if(city.getName()==null){
                            System.out.println("###############################: Activated if statement");
                            extra = citySnapShot.toObject(City2.class);
                            city = new City();
                            city.setName(extra.getName());
                        }

                        cities.add(city);

                    }
                    iOnAllStateLoadListener.onAllStateLoadSuccess(cities);
                }

            }
        });
    }


    private void init() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        iOnAllStateLoadListener = this;

        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();



    }

    private void initView() {
        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this,2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(0));

    }

    @Override
    public void onAllStateLoadSuccess(List<City> cityList) {
        adapter = new MyStateAdapter(this, cityList);
        recycler_state.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onAllStateLoadFailed(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }
}
