package com.universalyoga.adminapp.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.repository.CourseRepository;
import com.universalyoga.adminapp.utils.ValidationUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for course management.
 * Handles business logic and provides data to the UI.
 */
public class CourseViewModel extends AndroidViewModel {
    
    private final CourseRepository repository;
    private final MutableLiveData<List<YogaCourse>> filteredCourses;
    private final MutableLiveData<String> searchQuery;
    private final MutableLiveData<String> sortBy;
    private final MutableLiveData<Boolean> sortAscending;
    
    // Filter state
    private final MutableLiveData<String> dayFilter = new MutableLiveData<>("");
    private final MutableLiveData<String> typeFilter = new MutableLiveData<>("");
    private final MutableLiveData<String> difficultyFilter = new MutableLiveData<>("");
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    public CourseViewModel(Application application) {
        super(application);
        repository = new CourseRepository(application);
        filteredCourses = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>("");
        sortBy = new MutableLiveData<>("name");
        sortAscending = new MutableLiveData<>(true);
    }
    
    // BaseViewModel methods
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    protected void setLoading(boolean loading) {
        isLoading.postValue(loading);
    }
    
    protected void setError(String error) {
        errorMessage.postValue(error);
    }
    
    protected void setSuccess(String success) {
        successMessage.postValue(success);
    }
    
    public void clearError() {
        errorMessage.postValue(null);
    }
    
    public void clearSuccess() {
        successMessage.postValue(null);
    }
    
    /**
     * Get all courses
     */
    public LiveData<List<YogaCourse>> getAllCourses() {
        return repository.getAllCourses();
    }
    
    /**
     * Get filtered courses
     */
    public LiveData<List<YogaCourse>> getFilteredCourses() {
        return filteredCourses;
    }
    
    /**
     * Get search query
     */
    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }
    
    /**
     * Get sort by field
     */
    public LiveData<String> getSortBy() {
        return sortBy;
    }
    
    /**
     * Get sort direction
     */
    public LiveData<Boolean> getSortAscending() {
        return sortAscending;
    }
    
    /**
     * Get course statistics
     */
    public LiveData<Integer> getCourseCount() {
        return repository.getCount();
    }
    
    public LiveData<Integer> getTotalCapacity() {
        return repository.getTotalCapacity();
    }
    
    public LiveData<Double> getAveragePrice() {
        return repository.getAveragePrice();
    }
    
    /**
     * Set search query and filter courses
     */
    public void setSearchQuery(String query) {
        searchQuery.postValue(query != null ? query.trim() : "");
        filterAndSortCourses();
    }
    
    /**
     * Set sort field and sort courses
     */
    public void setSortBy(String field) {
        sortBy.postValue(field);
        filterAndSortCourses();
    }
    
    /**
     * Toggle sort direction
     */
    public void toggleSortDirection() {
        Boolean current = sortAscending.getValue();
        sortAscending.postValue(current != null ? !current : true);
        filterAndSortCourses();
    }
    
    /**
     * Set filters
     */
    public void setFilters(String day, String type, String difficulty) {
        dayFilter.postValue(day);
        typeFilter.postValue(type);
        difficultyFilter.postValue(difficulty);
        filterAndSortCourses();
    }
    
    /**
     * Filter and sort courses based on current search, filter, and sort settings
     */
    private void filterAndSortCourses() {
        List<YogaCourse> allCourses = repository.getAllCourses().getValue();
        if (allCourses == null) return;
        
        String query = searchQuery.getValue();
        String sortField = sortBy.getValue();
        Boolean ascending = sortAscending.getValue();
        String day = dayFilter.getValue();
        String type = typeFilter.getValue();
        String difficulty = difficultyFilter.getValue();
        
        // Filter by search query
        List<YogaCourse> filtered = allCourses;
        if (query != null && !query.trim().isEmpty()) {
            String searchTerm = query.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(course -> 
                        (course.getDaysOfWeek() != null && course.getDaysOfWeek().toLowerCase().contains(searchTerm)) ||
                        (course.getType() != null && course.getType().toLowerCase().contains(searchTerm)) ||
                        (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchTerm)) ||
                        (course.getRoomLocation() != null && course.getRoomLocation().toLowerCase().contains(searchTerm)) ||
                        (course.getInstructor() != null && course.getInstructor().toLowerCase().contains(searchTerm)) ||
                        (course.getDifficulty() != null && course.getDifficulty().toLowerCase().contains(searchTerm)))
                    .collect(Collectors.toList());
        }
        
        // Filter by day
        if (day != null && !day.trim().isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getDaysOfWeek() != null && 
                                    course.getDaysOfWeek().equalsIgnoreCase(day))
                    .collect(Collectors.toList());
        }
        
        // Filter by type
        if (type != null && !type.trim().isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getType() != null && 
                                    course.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        
        // Filter by difficulty
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            filtered = filtered.stream()
                    .filter(course -> course.getDifficulty() != null && 
                                    course.getDifficulty().equalsIgnoreCase(difficulty))
                    .collect(Collectors.toList());
        }
        
        // Sort courses
        if (sortField != null && ascending != null) {
            filtered = filtered.stream()
                    .sorted((c1, c2) -> {
                        int comparison = 0;
                        switch (sortField) {
                            case "name":
                                comparison = c1.getCourseName().compareToIgnoreCase(c2.getCourseName());
                                break;
                            case "price":
                                comparison = Double.compare(c1.getPrice(), c2.getPrice());
                                break;
                            case "capacity":
                                comparison = Integer.compare(c1.getCapacity(), c2.getCapacity());
                                break;
                            case "duration":
                                comparison = Integer.compare(c1.getDuration(), c2.getDuration());
                                break;
                            case "time":
                                comparison = c1.getTime().compareTo(c2.getTime());
                                break;
                            default:
                                comparison = c1.getCourseName().compareToIgnoreCase(c2.getCourseName());
                        }
                        return ascending ? comparison : -comparison;
                    })
                    .collect(Collectors.toList());
        }
        
        filteredCourses.postValue(filtered);
    }
    
    /**
     * Add a new course
     */
    public void addCourse(YogaCourse course) {
        // Validate course data
        ValidationUtils.ValidationResult nameResult = ValidationUtils.validateCourseName(course.getCourseName());
        if (!nameResult.isValid()) {
            setError(nameResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult timeResult = ValidationUtils.validateTime(course.getTime());
        if (!timeResult.isValid()) {
            setError(timeResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult capacityResult = ValidationUtils.validateCapacity(String.valueOf(course.getCapacity()));
        if (!capacityResult.isValid()) {
            setError(capacityResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult durationResult = ValidationUtils.validateDuration(String.valueOf(course.getDuration()));
        if (!durationResult.isValid()) {
            setError(durationResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult priceResult = ValidationUtils.validatePrice(String.valueOf(course.getPrice()));
        if (!priceResult.isValid()) {
            setError(priceResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult typeResult = ValidationUtils.validateCourseType(course.getType());
        if (!typeResult.isValid()) {
            setError(typeResult.getErrorMessage());
            return;
        }
        
        // Insert course
        repository.addCourse(course, new CourseRepository.OnCourseOperationCallback() {
            @Override
            public void onSuccess(YogaCourse savedCourse) {
                setSuccess("Course added successfully");
                filterAndSortCourses();
            }
            
            @Override
            public void onError(String errorMessage) {
                setError(errorMessage);
            }
        });
    }
    
    /**
     * Update an existing course
     */
    public void updateCourse(YogaCourse course) {
        android.util.Log.d("CourseViewModel", "updateCourse called for course: " + course.getCourseName() + " (ID: " + course.getId() + ")");
        android.util.Log.d("CourseViewModel", "Course details - Time: " + course.getTime() + ", Duration: " + course.getDuration() + ", Capacity: " + course.getCapacity());
        
        // Validate course data (same as addCourse)
        ValidationUtils.ValidationResult nameResult = ValidationUtils.validateCourseName(course.getCourseName());
        if (!nameResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Name validation failed: " + nameResult.getErrorMessage());
            setError(nameResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult timeResult = ValidationUtils.validateTime(course.getTime());
        if (!timeResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Time validation failed: " + timeResult.getErrorMessage());
            setError(timeResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult capacityResult = ValidationUtils.validateCapacity(String.valueOf(course.getCapacity()));
        if (!capacityResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Capacity validation failed: " + capacityResult.getErrorMessage());
            setError(capacityResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult durationResult = ValidationUtils.validateDuration(String.valueOf(course.getDuration()));
        if (!durationResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Duration validation failed: " + durationResult.getErrorMessage());
            setError(durationResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult priceResult = ValidationUtils.validatePrice(String.valueOf(course.getPrice()));
        if (!priceResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Price validation failed: " + priceResult.getErrorMessage());
            setError(priceResult.getErrorMessage());
            return;
        }
        
        ValidationUtils.ValidationResult typeResult = ValidationUtils.validateCourseType(course.getType());
        if (!typeResult.isValid()) {
            android.util.Log.e("CourseViewModel", "Type validation failed: " + typeResult.getErrorMessage());
            setError(typeResult.getErrorMessage());
            return;
        }
        
        android.util.Log.d("CourseViewModel", "All validations passed, calling repository.updateCourse");
        
        // Update course
        repository.updateCourse(course, new CourseRepository.OnCourseOperationCallback() {
            @Override
            public void onSuccess(YogaCourse updatedCourse) {
                android.util.Log.d("CourseViewModel", "Course update successful");
                setSuccess("Course updated successfully");
                filterAndSortCourses();
            }
            
            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("CourseViewModel", "Course update failed: " + errorMessage);
                setError(errorMessage);
            }
        });
    }
    
    /**
     * Delete a course
     */
    public void deleteCourse(YogaCourse course) {
        repository.deleteCourse(course.getId(), new CourseRepository.OnCourseOperationCallback() {
            @Override
            public void onSuccess(YogaCourse deletedCourse) {
                setSuccess("Course deleted successfully");
                filterAndSortCourses();
            }
            
            @Override
            public void onError(String errorMessage) {
                setError(errorMessage);
            }
        });
    }
    
    /**
     * Delete all courses
     */
    public void deleteAllCourses() {
        // Note: deleteAllCourses method not available in new repository
        // This would need to be implemented if required
        setError("Delete all courses not implemented");
    }
    
    /**
     * Get course by ID
     */
    public LiveData<YogaCourse> getCourseById(int id) {
        return repository.getCourseById(id);
    }
    
    /**
     * Search courses
     */
    public LiveData<List<YogaCourse>> searchCourses(String query) {
        return repository.searchCourses(query);
    }
    
    /**
     * Get courses by day
     */
    public LiveData<List<YogaCourse>> getCoursesByDay(String dayOfWeek) {
        return repository.getCoursesByDay(dayOfWeek);
    }
    
    /**
     * Manually update instances for a course (for testing/debugging)
     */
    public void manuallyUpdateInstancesForCourse(int courseId) {
        android.util.Log.d("CourseViewModel", "Manually updating instances for course ID: " + courseId);
        repository.manuallyUpdateInstancesForCourse(courseId);
    }
} 