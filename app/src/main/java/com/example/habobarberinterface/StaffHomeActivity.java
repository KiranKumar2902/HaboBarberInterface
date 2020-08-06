package com.example.habobarberinterface;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.example.habobarberinterface.Adapter.TrackerAdapter;
import com.example.habobarberinterface.Listeners.IAppointmentsLoadListener;
import com.example.habobarberinterface.Model.Appointment;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habobarberinterface.Adapter.MyTimeSlotAdapter;
import com.example.habobarberinterface.Common.Common;
import com.example.habobarberinterface.Common.SpacesItemDecoration;
import com.example.habobarberinterface.Interface.ITimeSlotLoadListener;
import com.example.habobarberinterface.Model.TimeSlot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, IAppointmentsLoadListener {

    TextView txt_barber_name;

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    //Copy from Barber Booking App (Client Interface)
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    android.app.AlertDialog alertDialog;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;


    //===== End Copy

    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration bookingRealtimeListener;

    IAppointmentsLoadListener onAppointmentsLoadListener;
    boolean exists;
    List<Appointment> appointments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        onAppointmentsLoadListener = this;

        Calendar date = Calendar.getInstance();
        for (int i = 0; i >= -14; i--) {
            date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_MONTH, i);

            //load list of recent visits
            getRecentVisitsForADate(Common.simpleDateFormat.format(date.getTime()));
        }

        ButterKnife.bind(this);
        init();
        initView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open,
                R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_exit) {
                    logOut();
                } else if (menuItem.getItemId() == R.id.menu_visits) {
                    Intent intent = new Intent(StaffHomeActivity.this, TrackerActivity.class);
                    intent.putParcelableArrayListExtra("list", (ArrayList) appointments);
                    startActivity(intent);
                }
                return true;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        txt_barber_name = (TextView) headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText("Welcome back, " + Common.currentBarber.getName());

        //Copy from Barber Booking App (Client Interface)
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setTheme(R.style.Custom).setContext(this)
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(layoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));


        //Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .configure()
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {


                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));
                }
            }
        });


    }

    private void logOut() {
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivity);
                        finish();


                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void loadAvailableTimeSlotOfBarber(final String barberId, final String bookDate) {
        //Copy from Barber Booking App
        alertDialog.show();


        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        //Get information of booking
                        //If not created, return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selected_salon.getSalonID())
                                .collection("Barber")
                                .document(barberId)
                                .collection(bookDate);

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) // no appts
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    else {
                                        //if i have an appt
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult())
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);

                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }


    private void init() {
        iTimeSlotLoadListener = this;

        initBookingRealtimeUpdate();

    }

    private void initBookingRealtimeUpdate() {

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        //Get current date
        final Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        Common.simpleDateFormat.format(date.getTime()));

            }
        };

        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));

        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlot) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlot);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();

    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();

    }


    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onDestroy();
    }

    public void getRecentVisitsForADate(final String date) {
        CollectionReference datesRef;

        exists = false;

        datesRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(date);

        datesRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {

                                               if (task.isSuccessful()) {
                                                   exists = true;
                                                   for (QueryDocumentSnapshot shop : task.getResult()) {
                                                       Appointment appointment = shop.toObject(Appointment.class);
                                                       System.out.println("------------------------------------------" + appointment.getCustomerName());
                                                       System.out.println("------------------------------------------" + appointment.getCustomerPhone());
                                                       System.out.println("------------------------------------------" + appointment.getSlot());
                                                       System.out.println("------------------------------------------" + appointment.getTime());

                                                       if(date.compareTo(Common.simpleDateFormat.format(Calendar.getInstance().getTime()))==0){
                                                           if(checkIfCustomerArrived(appointment.getSlot())){
                                                               appointments.add(appointment);
                                                           }
                                                       }else{
                                                           appointments.add(appointment);
                                                       }

                                                   }

                                                   onAppointmentsLoadListener.onAppointmentsLoadSuccess(appointments);
                                               }
                                           }

                                       }

                ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                onAppointmentsLoadListener.onAppointmentsLoadFailed(e.getMessage());

            }
        });


    }

    @Override
    public void onAppointmentsLoadSuccess(List<Appointment> banners) {
        System.out.println("----------------------------------------------Returned list with size" + appointments.size());
    }

    @Override
    public void onAppointmentsLoadFailed(String message) {
        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

    }

    public boolean checkIfCustomerArrived(int slot){
        long arrivalTime = checkStartTime(slot);
        System.out.println("###########################################################: " + arrivalTime);

        System.out.println("###########################################################: " + Calendar.getInstance().getTimeInMillis());

        if(arrivalTime<=Calendar.getInstance().getTimeInMillis()){
            return true;
        }else{return false;}
    }

    public long checkStartTime(int slot){

        Calendar date = Calendar.getInstance();

        switch(slot) {
            case 0:
                date.set(Calendar.HOUR, 9);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 1:
                date.set(Calendar.HOUR, 9);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 2:
                date.set(Calendar.HOUR, 10);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 3:
                date.set(Calendar.HOUR, 10);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 4:
                date.set(Calendar.HOUR, 11);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 5:
                date.set(Calendar.HOUR, 11);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.AM);
                return date.getTimeInMillis();
            case 6:
                date.set(Calendar.HOUR, 12);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 7:
                date.set(Calendar.HOUR, 12);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 8:
                date.set(Calendar.HOUR, 1);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 9:
                date.set(Calendar.HOUR, 1);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 10:
                date.set(Calendar.HOUR, 2);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 11:
                date.set(Calendar.HOUR, 2);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 12:
                date.set(Calendar.HOUR, 3);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 13:
                date.set(Calendar.HOUR, 3);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 14:
                date.set(Calendar.HOUR, 4);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 15:
                date.set(Calendar.HOUR, 4);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 16:
                date.set(Calendar.HOUR, 5);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 17:
                date.set(Calendar.HOUR, 5);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 18:
                date.set(Calendar.HOUR, 6);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 19:
                date.set(Calendar.HOUR, 6);
                date.set(Calendar.MINUTE, 30);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            case 20:
                date.set(Calendar.HOUR, 7);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.AM_PM, Calendar.PM);
                return date.getTimeInMillis();
            default:
                return 0;
        }
    }
}

