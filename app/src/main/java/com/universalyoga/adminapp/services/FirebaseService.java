package com.universalyoga.adminapp.services;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.universalyoga.adminapp.models.YogaCourse;
import com.universalyoga.adminapp.models.YogaInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    
    // Firebase instances
    private static FirebaseDatabase realtimeDatabase;
    private static FirebaseFirestore firestore;
    
    // Database references
    private static DatabaseReference yogaClassesRef;
    private static CollectionReference classesCollection;
    private static CollectionReference bookingsCollection;
    
    // Initialize Firebase services
    public static void initialize() {
        try {
            // Initialize Realtime Database
            realtimeDatabase = FirebaseDatabase.getInstance();
            yogaClassesRef = realtimeDatabase.getReference("yoga_classes");
            
            // Initialize Firestore
            firestore = FirebaseFirestore.getInstance();
            classesCollection = firestore.collection("classes");
            bookingsCollection = firestore.collection("bookings");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase services", e);
        }
    }
    
    // Upload courses to Realtime Database (existing functionality)
    public static CompletableFuture<Boolean> uploadCoursesToRealtimeDB(List<YogaCourse> courses, List<YogaInstance> instances) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (yogaClassesRef == null) {
                    Log.e(TAG, "Realtime Database reference is null");
                    return false;
                }
                
                // Group instances by courseId
                Map<Integer, Map<String, Object>> instancesByCourse = new HashMap<>();
                for (YogaInstance instance : instances) {
                    int courseId = instance.getCourseId();
                    if (!instancesByCourse.containsKey(courseId)) {
                        instancesByCourse.put(courseId, new HashMap<>());
                    }
                    instancesByCourse.get(courseId).put(String.valueOf(instance.getId()), instance);
                }
                
                // Upload each course with its instances
                for (YogaCourse course : courses) {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("courseInfo", course);
                    courseData.put("instances", instancesByCourse.getOrDefault(course.getId(), new HashMap<>()));
                    courseData.put("lastUpdated", System.currentTimeMillis());
                    
                    yogaClassesRef.child(String.valueOf(course.getId())).setValue(courseData);
                }
                
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to upload courses to Realtime Database", e);
                return false;
            }
        });
    }
    
    // Upload courses to Firestore (new functionality)
    public static CompletableFuture<Boolean> uploadCoursesToFirestore(List<YogaCourse> courses, List<YogaInstance> instances) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (classesCollection == null) {
                    Log.e(TAG, "Firestore collection reference is null");
                    return false;
                }
                
                // Group instances by courseId
                Map<Integer, List<YogaInstance>> instancesByCourse = new HashMap<>();
                for (YogaInstance instance : instances) {
                    int courseId = instance.getCourseId();
                    if (!instancesByCourse.containsKey(courseId)) {
                        instancesByCourse.put(courseId, new java.util.ArrayList<>());
                    }
                    instancesByCourse.get(courseId).add(instance);
                }
                
                // Upload each course with its instances
                for (YogaCourse course : courses) {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("courseInfo", course);
                    courseData.put("instances", instancesByCourse.getOrDefault(course.getId(), new java.util.ArrayList<>()));
                    courseData.put("lastUpdated", System.currentTimeMillis());
                    
                    classesCollection.document(String.valueOf(course.getId())).set(courseData);
                }
                
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to upload courses to Firestore", e);
                return false;
            }
        });
    }
    
    // Add booking to Firestore
    public static CompletableFuture<String> addBooking(Map<String, Object> bookingData) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            bookingData.put("createdAt", new java.util.Date());
            bookingData.put("status", "confirmed");
            
            bookingsCollection.add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Booking added with ID: " + documentReference.getId());
                    future.complete(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add booking", e);
                    future.completeExceptionally(new RuntimeException("Failed to add booking", e));
                });
        } catch (Exception e) {
            Log.e(TAG, "Failed to add booking", e);
            future.completeExceptionally(new RuntimeException("Failed to add booking", e));
        }
        
        return future;
    }
    
    // Get Firebase instances for direct access
    public static FirebaseDatabase getRealtimeDatabase() {
        return realtimeDatabase;
    }
    
    public static FirebaseFirestore getFirestore() {
        return firestore;
    }
    
    public static DatabaseReference getYogaClassesRef() {
        return yogaClassesRef;
    }
    
    public static CollectionReference getClassesCollection() {
        return classesCollection;
    }
    
    public static CollectionReference getBookingsCollection() {
        return bookingsCollection;
    }
    
    // Check if Firebase is properly initialized
    public static boolean isInitialized() {
        return realtimeDatabase != null && firestore != null && 
               yogaClassesRef != null && classesCollection != null;
    }
    
    // Get Firebase connection status
    public static String getConnectionStatus() {
        if (!isInitialized()) {
            return "Not initialized";
        }
        return "Connected to Firebase";
    }
    
    // Delete course from Firebase (Realtime Database)
    public static CompletableFuture<Boolean> deleteCourseFromRealtimeDB(int courseId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (yogaClassesRef == null) {
                    Log.e(TAG, "Realtime Database reference is null");
                    return false;
                }
                
                // Remove the course and all its instances
                yogaClassesRef.child(String.valueOf(courseId)).removeValue();
                Log.d(TAG, "Course deleted from Realtime Database: " + courseId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete course from Realtime Database", e);
                return false;
            }
        });
    }
    
    // Delete course from Firebase (Firestore)
    public static CompletableFuture<Boolean> deleteCourseFromFirestore(int courseId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (classesCollection == null) {
                    Log.e(TAG, "Firestore collection reference is null");
                    return false;
                }
                
                // Remove the course document
                classesCollection.document(String.valueOf(courseId)).delete();
                Log.d(TAG, "Course deleted from Firestore: " + courseId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete course from Firestore", e);
                return false;
            }
        });
    }
    
    // Delete instance from Firebase (Realtime Database)
    public static CompletableFuture<Boolean> deleteInstanceFromRealtimeDB(int courseId, int instanceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (yogaClassesRef == null) {
                    Log.e(TAG, "Realtime Database reference is null");
                    return false;
                }
                
                // Remove the specific instance from the course
                yogaClassesRef.child(String.valueOf(courseId))
                             .child("instances")
                             .child(String.valueOf(instanceId))
                             .removeValue();
                Log.d(TAG, "Instance deleted from Realtime Database: " + instanceId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete instance from Realtime Database", e);
                return false;
            }
        });
    }
    
    // Delete instance from Firebase (Firestore)
    public static CompletableFuture<Boolean> deleteInstanceFromFirestore(int courseId, int instanceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (classesCollection == null) {
                    Log.e(TAG, "Firestore collection reference is null");
                    return false;
                }
                
                // Get the current course document
                var courseDoc = classesCollection.document(String.valueOf(courseId));
                courseDoc.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null && data.containsKey("instances")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> instances = (List<Map<String, Object>>) data.get("instances");
                            
                            // Remove the specific instance
                            instances.removeIf(instance -> {
                                Object id = instance.get("id");
                                return id != null && id.toString().equals(String.valueOf(instanceId));
                            });
                            
                            // Update the document with the new instances list
                            data.put("instances", instances);
                            data.put("lastUpdated", System.currentTimeMillis());
                            courseDoc.set(data);
                        }
                    }
                });
                
                Log.d(TAG, "Instance deleted from Firestore: " + instanceId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete instance from Firestore", e);
                return false;
            }
        });
    }
    
    // Sync deletions to Firebase (both Realtime DB and Firestore)
    public static CompletableFuture<Boolean> syncDeletionsToFirebase(List<Integer> courseIdsToDelete, 
                                                                   List<Map<String, Integer>> instanceIdsToDelete) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean allSuccess = true;
                
                // Delete courses
                for (Integer courseId : courseIdsToDelete) {
                    boolean realtimeSuccess = deleteCourseFromRealtimeDB(courseId).get();
                    boolean firestoreSuccess = deleteCourseFromFirestore(courseId).get();
                    
                    if (!realtimeSuccess || !firestoreSuccess) {
                        allSuccess = false;
                        Log.e(TAG, "Failed to delete course from Firebase: " + courseId);
                    }
                }
                
                // Delete instances
                for (Map<String, Integer> instanceInfo : instanceIdsToDelete) {
                    Integer courseId = instanceInfo.get("courseId");
                    Integer instanceId = instanceInfo.get("instanceId");
                    
                    if (courseId != null && instanceId != null) {
                        boolean realtimeSuccess = deleteInstanceFromRealtimeDB(courseId, instanceId).get();
                        boolean firestoreSuccess = deleteInstanceFromFirestore(courseId, instanceId).get();
                        
                        if (!realtimeSuccess || !firestoreSuccess) {
                            allSuccess = false;
                            Log.e(TAG, "Failed to delete instance from Firebase: " + instanceId);
                        }
                    }
                }
                
                return allSuccess;
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync deletions to Firebase", e);
                return false;
            }
        });
    }
} 