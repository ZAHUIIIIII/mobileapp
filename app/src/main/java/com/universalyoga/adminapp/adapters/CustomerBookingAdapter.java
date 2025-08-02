package com.universalyoga.adminapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.universalyoga.adminapp.R;
import com.universalyoga.adminapp.models.CustomerBooking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.ParseException;
import java.util.TimeZone;

public class CustomerBookingAdapter extends RecyclerView.Adapter<CustomerBookingAdapter.ViewHolder> {
    private List<CustomerBooking> bookings;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault());
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private SimpleDateFormat isoFormatNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public CustomerBookingAdapter(List<CustomerBooking> bookings) {
        this.bookings = bookings;
        
        // Set timezone to UTC for ISO parsing
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        isoFormatNoMs.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerBooking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<CustomerBooking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    private String formatBookingDate(String bookingDate) {
        try {
            
            if (bookingDate == null || bookingDate.trim().isEmpty()) {
                return "Booked on: Date not available";
            }
            
            Date date = null;
            
            // Try to parse ISO format explicitly
            if (bookingDate.contains("T") && bookingDate.contains("Z")) {
                // Handle ISO 8601 format like "2025-07-31T12:33:04.919Z"
                try {
                    date = isoFormat.parse(bookingDate);
                } catch (ParseException e) {
                    // Try without milliseconds
                    try {
                        date = isoFormatNoMs.parse(bookingDate);
                    } catch (ParseException e2) {
                    }
                }
            }
            
            // If ISO parsing failed, try other formats
            if (date == null) {
                try {
                    // Try default Date constructor as fallback
                    date = new Date(bookingDate);
                } catch (Exception e) {
                    return "Booked on: " + bookingDate;
                }
            }
            
            if (date == null) {
                return "Booked on: " + bookingDate;
            }
            
            Date now = new Date();
            
            // Calculate time difference
            long diffInMillis = now.getTime() - date.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            
            // Format the main date using local timezone
            String formattedDate = dateFormat.format(date);
            
            // Add relative time information
            String relativeTime = "";
            if (diffInDays == 0) {
                if (diffInHours == 0) {
                    if (diffInMinutes < 1) {
                        relativeTime = " (just now)";
                    } else {
                        relativeTime = " (" + diffInMinutes + " minutes ago)";
                    }
                } else {
                    relativeTime = " (" + diffInHours + " hours ago)";
                }
            } else if (diffInDays == 1) {
                relativeTime = " (yesterday)";
            } else if (diffInDays <= 7) {
                relativeTime = " (" + diffInDays + " days ago)";
            }
            
            return "Booked on: " + formattedDate + relativeTime;
        } catch (Exception e) {
            e.printStackTrace();
            return "Booked on: " + bookingDate;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCustomerEmail;
        private TextView tvBookingDate;
        private TextView tvTotalAmount;
        private TextView tvTotalClasses;
        private TextView tvClassesList;
        private TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvTotalClasses = itemView.findViewById(R.id.tvTotalClasses);
            tvClassesList = itemView.findViewById(R.id.tvClassesList);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(CustomerBooking booking) {
            tvCustomerEmail.setText(booking.getEmail());
            
            // Format booking date with relative time
            String formattedDate = formatBookingDate(booking.getBookingDate());
            tvBookingDate.setText(formattedDate);
            
            tvTotalAmount.setText(String.format("£%.2f", booking.getTotalAmount()));
            tvTotalClasses.setText(booking.getTotalClasses() + " class(es)");
            tvStatus.setText(booking.getStatus());
            
            // Build classes list
            StringBuilder classesText = new StringBuilder();
            if (booking.getClasses() != null) {
                for (CustomerBooking.BookingClass bookingClass : booking.getClasses()) {
                    classesText.append("• ")
                            .append(bookingClass.getCourseName())
                            .append(" with ")
                            .append(bookingClass.getInstructor())
                            .append(" (")
                            .append(bookingClass.getQuantity())
                            .append("x £")
                            .append(String.format("%.2f", bookingClass.getPrice()))
                            .append(")\n");
                }
            }
            tvClassesList.setText(classesText.toString());
            
            // Set status color
            if ("confirmed".equalsIgnoreCase(booking.getStatus())) {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            }
        }
    }
} 