package com.universalyoga.adminapp.utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 * Provides centralized validation methods for the entire application.
 */
public class ValidationUtils {
    
    // Validation patterns
    private static final Pattern TIME_PATTERN = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    private static final Pattern PRICE_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^[1-9]\\d*$");
    private static final Pattern CAPACITY_PATTERN = Pattern.compile("^[1-9]\\d*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Validation constants
    public static final int MIN_COURSE_NAME_LENGTH = 2;
    public static final int MAX_COURSE_NAME_LENGTH = 100;
    public static final int MIN_DESCRIPTION_LENGTH = 0;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 1000;
    public static final int MIN_DURATION = 15;
    public static final int MAX_DURATION = 480; // 8 hours
    public static final double MIN_PRICE = 0.0;
    public static final double MAX_PRICE = 10000.0;
    
    /**
     * Check if a string is empty or null
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Check if a string matches email pattern
     */
    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate course name
     */
    public static ValidationResult validateCourseName(String courseName) {
        if (isEmpty(courseName)) {
            return new ValidationResult(false, "Course name is required");
        }
        
        if (courseName.length() < MIN_COURSE_NAME_LENGTH) {
            return new ValidationResult(false, 
                "Course name must be at least " + MIN_COURSE_NAME_LENGTH + " characters");
        }
        
        if (courseName.length() > MAX_COURSE_NAME_LENGTH) {
            return new ValidationResult(false, 
                "Course name must be less than " + MAX_COURSE_NAME_LENGTH + " characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate time format (HH:MM)
     */
    public static ValidationResult validateTime(String time) {
        if (isEmpty(time)) {
            return new ValidationResult(false, "Time is required");
        }
        
        if (!TIME_PATTERN.matcher(time).matches()) {
            return new ValidationResult(false, "Please enter a valid time (HH:MM)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate days of week
     */
    public static ValidationResult validateDaysOfWeek(String daysOfWeek) {
        if (isEmpty(daysOfWeek)) {
            return new ValidationResult(false, "Please select at least one day");
        }
        
        String[] days = daysOfWeek.split(",");
        if (days.length == 0) {
            return new ValidationResult(false, "Please select at least one day");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate capacity
     */
    public static ValidationResult validateCapacity(String capacity) {
        if (isEmpty(capacity)) {
            return new ValidationResult(false, "Capacity is required");
        }
        
        if (!CAPACITY_PATTERN.matcher(capacity).matches()) {
            return new ValidationResult(false, "Capacity must be a positive number");
        }
        
        int capacityValue = Integer.parseInt(capacity);
        if (capacityValue < MIN_CAPACITY) {
            return new ValidationResult(false, 
                "Capacity must be at least " + MIN_CAPACITY);
        }
        
        if (capacityValue > MAX_CAPACITY) {
            return new ValidationResult(false, 
                "Capacity cannot exceed " + MAX_CAPACITY);
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate duration
     */
    public static ValidationResult validateDuration(String duration) {
        if (isEmpty(duration)) {
            return new ValidationResult(false, "Duration is required");
        }
        
        if (!DURATION_PATTERN.matcher(duration).matches()) {
            return new ValidationResult(false, "Duration must be a positive number");
        }
        
        int durationValue = Integer.parseInt(duration);
        if (durationValue < MIN_DURATION) {
            return new ValidationResult(false, 
                "Duration must be at least " + MIN_DURATION + " minutes");
        }
        
        if (durationValue > MAX_DURATION) {
            return new ValidationResult(false, 
                "Duration cannot exceed " + MAX_DURATION + " minutes");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate price
     */
    public static ValidationResult validatePrice(String price) {
        if (isEmpty(price)) {
            return new ValidationResult(false, "Price is required");
        }
        
        if (!PRICE_PATTERN.matcher(price).matches()) {
            return new ValidationResult(false, "Please enter a valid price");
        }
        
        double priceValue = Double.parseDouble(price);
        if (priceValue < MIN_PRICE) {
            return new ValidationResult(false, 
                "Price cannot be negative");
        }
        
        if (priceValue > MAX_PRICE) {
            return new ValidationResult(false, 
                "Price cannot exceed " + MAX_PRICE);
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate course type
     */
    public static ValidationResult validateCourseType(String courseType) {
        if (isEmpty(courseType)) {
            return new ValidationResult(false, "Course type is required");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate description (optional field)
     */
    public static ValidationResult validateDescription(String description) {
        if (isEmpty(description)) {
            return new ValidationResult(true, null); // Description is optional
        }
        
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return new ValidationResult(false, 
                "Description must be less than " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate room location (optional field)
     */
    public static ValidationResult validateRoomLocation(String roomLocation) {
        if (isEmpty(roomLocation)) {
            return new ValidationResult(true, null); // Room location is optional
        }
        
        if (roomLocation.length() > 100) {
            return new ValidationResult(false, "Room location must be less than 100 characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate email address
     */
    public static ValidationResult validateEmail(String email) {
        if (isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }
        
        if (!isValidEmail(email)) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate phone number
     */
    public static ValidationResult validatePhone(String phone) {
        if (isEmpty(phone)) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        if (phone.length() < 10) {
            return new ValidationResult(false, "Phone number must be at least 10 digits");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}