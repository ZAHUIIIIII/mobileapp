package com.universalyoga.adminapp.models;

import java.util.List;

public class CustomerBooking {
    private String id;
    private String email;
    private List<BookingClass> classes;
    private double totalAmount;
    private int totalClasses;
    private String bookingDate;
    private String status;
    private String createdAt;

    // Default constructor for Firebase
    public CustomerBooking() {}

    public CustomerBooking(String email, List<BookingClass> classes, double totalAmount, int totalClasses, String bookingDate, String status) {
        this.email = email;
        this.classes = classes;
        this.totalAmount = totalAmount;
        this.totalClasses = totalClasses;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<BookingClass> getClasses() { return classes; }
    public void setClasses(List<BookingClass> classes) { this.classes = classes; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Inner class for booking class details
    public static class BookingClass {
        private String id;
        private String courseName;
        private String instructor;
        private String date;
        private String time;
        private int duration;
        private double price;
        private int quantity;
        private String difficulty;
        private String type;

        // Default constructor for Firebase
        public BookingClass() {}

        public BookingClass(String id, String courseName, String instructor, String date, String time, 
                          int duration, double price, int quantity, String difficulty, String type) {
            this.id = id;
            this.courseName = courseName;
            this.instructor = instructor;
            this.date = date;
            this.time = time;
            this.duration = duration;
            this.price = price;
            this.quantity = quantity;
            this.difficulty = difficulty;
            this.type = type;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getInstructor() { return instructor; }
        public void setInstructor(String instructor) { this.instructor = instructor; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
} 