package com.universalyoga.adminapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.adapters.CustomerBookingAdapter;
import com.universalyoga.adminapp.models.CustomerBooking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomerBookingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private CustomerBookingAdapter adapter;
    private List<CustomerBooking> bookings;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private TextView tvLoading;
    private DatabaseReference bookingsRef;
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private SimpleDateFormat isoFormatNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_bookings, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadCustomerBookings();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.rvCustomerBookings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvLoading = view.findViewById(R.id.tvLoading);
        
        // Initialize Firebase reference
        bookingsRef = FirebaseDatabase.getInstance().getReference("customer_bookings");
        
        // Set timezone for ISO parsing
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        isoFormatNoMs.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private void setupRecyclerView() {
        bookings = new ArrayList<>();
        adapter = new CustomerBookingAdapter(bookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCustomerBookings);
    }

    private void loadCustomerBookings() {
        showLoading(true);
        
        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookings.clear();
                
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        try {
                            CustomerBooking booking = bookingSnapshot.getValue(CustomerBooking.class);
                            if (booking != null) {
                                booking.setId(bookingSnapshot.getKey());
                                
                                bookings.add(booking);
                            }
                        } catch (Exception e) {
                            // Handle individual booking parsing errors
                            continue;
                        }
                    }
                    
                    // Sort by booking date (newest first) using proper date comparison
                    bookings.sort((a, b) -> {
                        try {
                            Date dateA = parseBookingDate(a.getBookingDate());
                            Date dateB = parseBookingDate(b.getBookingDate());
                            
                            if (dateA != null && dateB != null) {
                                return dateB.compareTo(dateA); // Newest first
                            } else if (dateA != null) {
                                return -1; // A comes first
                            } else if (dateB != null) {
                                return 1; // B comes first
                            } else {
                                // Fallback to string comparison
                                return b.getBookingDate().compareTo(a.getBookingDate());
                            }
                        } catch (Exception e) {
                            // Fallback to string comparison
                            return b.getBookingDate().compareTo(a.getBookingDate());
                        }
                    });
                }
                
                adapter.notifyDataSetChanged();
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (bookings.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private Date parseBookingDate(String bookingDate) {
        if (bookingDate == null || bookingDate.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try to parse ISO format explicitly
            if (bookingDate.contains("T") && bookingDate.contains("Z")) {
                try {
                    return isoFormat.parse(bookingDate);
                } catch (ParseException e) {
                    try {
                        return isoFormatNoMs.parse(bookingDate);
                    } catch (ParseException e2) {
                        // Fallback to default Date constructor
                        return new Date(bookingDate);
                    }
                }
            } else {
                // Fallback to default Date constructor
                return new Date(bookingDate);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void showLoading(boolean show) {
        if (tvLoading != null) {
            tvLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
} 