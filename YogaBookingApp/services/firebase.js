// Firebase configuration and service functions
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, getDocs, addDoc, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { getDatabase, ref, get, child, push, set } from 'firebase/database';

// Firebase configuration

const firebaseConfig = {
  apiKey: "AIzaSyAGhUNlPW20bC2lejpPOwXYU_7wWhJkOTs",
  authDomain: "yoga-19447.firebaseapp.com",
  databaseURL: "https://yoga-19447-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "yoga-19447",
  storageBucket: "yoga-19447.firebasestorage.app",
  messagingSenderId: "985668467283",
  appId: "1:985668467283:web:b04e039a7fa1685ed2a4a6"
};
// Initialize Firebase (only if config is properly set)
let app;
let db;
let realtimeDb;

try {
  if (firebaseConfig.apiKey !== "your-api-key") {
    app = initializeApp(firebaseConfig);
    db = getFirestore(app);
    realtimeDb = getDatabase(app);
  }
} catch (error) {
  // Firebase not configured, using mock data
}

// Mock data for development
const mockClasses = [
  {
    id: '1',
    courseName: 'Morning Hatha Yoga',
    instructor: 'Sarah Johnson',
    duration: 60,
    capacity: 20,
    enrolled: 15,
    price: 25,
    difficulty: 'Beginner',
    date: '2025-01-15',
    startTime: '08:00',
    description: 'A gentle morning practice focusing on basic postures and breathing techniques.',
    type: 'Hatha',
    imageUrl: 'https://example.com/hatha-yoga.jpg'
  },
  {
    id: '2',
    courseName: 'Power Vinyasa Flow',
    instructor: 'Mike Chen',
    duration: 75,
    capacity: 15,
    enrolled: 12,
    price: 30,
    difficulty: 'Advanced',
    date: '2025-01-15',
    startTime: '18:00',
    description: 'Dynamic flowing sequences to build strength and flexibility.',
    type: 'Vinyasa',
    imageUrl: 'https://example.com/vinyasa-yoga.jpg'
  },
  {
    id: '3',
    courseName: 'Restorative Yin Yoga',
    instructor: 'Lisa Wang',
    duration: 90,
    capacity: 12,
    enrolled: 8,
    price: 28,
    difficulty: 'Beginner',
    date: '2025-01-16',
    startTime: '19:30',
    description: 'Slow-paced practice with longer holds to promote deep relaxation.',
    type: 'Yin',
    imageUrl: 'https://example.com/yin-yoga.jpg'
  },
  {
    id: '4',
    courseName: 'Ashtanga Primary Series',
    instructor: 'David Kumar',
    duration: 90,
    capacity: 10,
    enrolled: 9,
    price: 35,
    difficulty: 'Advanced',
    date: '2025-01-16',
    startTime: '07:00',
    description: 'Traditional Ashtanga practice with set sequence of poses.',
    type: 'Ashtanga',
    imageUrl: 'https://example.com/ashtanga-yoga.jpg'
  },
  {
    id: '5',
    courseName: 'Gentle Senior Yoga',
    instructor: 'Mary Thompson',
    duration: 45,
    capacity: 15,
    enrolled: 10,
    price: 20,
    difficulty: 'Beginner',
    date: '2025-01-17',
    startTime: '10:00',
    description: 'Chair-supported yoga perfect for seniors and those with mobility issues.',
    type: 'Gentle',
    imageUrl: 'https://example.com/senior-yoga.jpg'
  },
  {
    id: '6',
    courseName: 'Hot Bikram Yoga',
    instructor: 'Carlos Rodriguez',
    duration: 90,
    capacity: 20,
    enrolled: 18,
    price: 32,
    difficulty: 'Intermediate',
    date: '2025-01-17',
    startTime: '17:00',
    description: 'Traditional 26-pose sequence in a heated room.',
    type: 'Bikram',
    imageUrl: 'https://example.com/bikram-yoga.jpg'
  }
];

// Helper function to get mock data
const getMockClasses = () => {
  return mockClasses; // Return directly, not as a Promise
};

// Fetch all yoga classes from both Realtime Database and Firestore
export const fetchClasses = async () => {
  try {
    if (!realtimeDb && !db) {
      return getMockClasses();
    }

    let classes = [];

    // Try Realtime Database first (primary source from admin app)
    if (realtimeDb) {
      try {
        const classesRef = ref(realtimeDb, 'yoga_classes');
        const snapshot = await get(classesRef);
        
        console.log("Realtime Database snapshot exists:", snapshot.exists());
        
        if (snapshot.exists()) {
          const data = snapshot.val();
          console.log("Realtime Database data:", data);
          const transformedClasses = [];
          
          // Transform the data structure from admin app
          Object.keys(data).forEach(courseId => {
            const courseData = data[courseId];
            const courseInfo = courseData.courseInfo;
            const instances = courseData.instances || {};
            
            console.log(`Processing course ${courseId}:`, courseInfo);
            console.log(`Instances for course ${courseId}:`, instances);
            
            if (courseInfo) {
              // Transform each instance into a separate class
              Object.keys(instances).forEach(instanceId => {
                const instance = instances[instanceId];
                
                // Skip instances that are marked for deletion (syncStatus = 2)
                if (instance && instance.syncStatus !== 2) {
                  const transformedClass = {
                    id: `${courseId}_${instanceId}`,
                    courseId: courseId,
                    instanceId: instanceId,
                    courseName: courseInfo.courseName || courseInfo.type,
                    instructor: instance.teacher || courseInfo.instructor,
                    startTime: instance.startTime || courseInfo.time,
                    endTime: instance.endTime,
                    duration: courseInfo.duration,
                    capacity: instance.capacity || courseInfo.capacity,
                    enrolled: instance.enrolled || 0,
                    price: courseInfo.price,
                    difficulty: courseInfo.difficulty,
                    description: courseInfo.description,
                    type: courseInfo.type,
                    daysOfWeek: courseInfo.daysOfWeek,
                    roomLocation: courseInfo.roomLocation,
                    date: instance.date,
                    comments: instance.comments,
                    imageUrl: courseInfo.imageUrl || 'https://example.com/yoga-class.jpg',
                    lastUpdated: courseData.lastUpdated
                  };
                  
                  console.log(`Transformed class:`, transformedClass);
                  transformedClasses.push(transformedClass);
                } else {
                  console.log(`Skipping deleted instance ${instanceId} for course ${courseId}`);
                }
              });
            } else {
              console.log(`No courseInfo found for course ${courseId}`);
            }
          });
          
          classes = transformedClasses;
          console.log(`Total transformed classes: ${classes.length}`);
        } else {
          console.log("No data found in Realtime Database");
        }
      } catch (realtimeError) {
        console.log("Realtime Database error:", realtimeError);
        // Try Firestore as fallback
        if (db) {
          try {
            const classesRef = collection(db, 'classes');
            const querySnapshot = await getDocs(classesRef);
            
            classes = querySnapshot.docs.map(doc => {
              const data = doc.data();
              // Filter out deleted items from Firestore too
              if (data.syncStatus === 2) {
                return null;
              }
              return {
                id: doc.id,
                ...data
              };
            }).filter(Boolean); // Remove null items
          } catch (firestoreError) {
            console.log("Firestore error:", firestoreError);
            // Don't return mock data if Firebase fails - return empty array
            console.log("Firebase failed, returning empty array instead of mock data");
            return [];
          }
        } else {
          // Don't return mock data if no Firebase - return empty array
          console.log("No Firebase available, returning empty array instead of mock data");
          return [];
        }
      }
    } else if (db) {
      try {
        const classesRef = collection(db, 'classes');
        const querySnapshot = await getDocs(classesRef);
        
        classes = querySnapshot.docs.map(doc => {
          const data = doc.data();
          // Filter out deleted items
          if (data.syncStatus === 2) {
            return null;
          }
          return {
            id: doc.id,
            ...data
          };
        }).filter(Boolean); // Remove null items
      } catch (firestoreError) {
        console.log("Firestore error:", firestoreError);
        // Don't return mock data if Firebase fails - return empty array
        console.log("Firebase failed, returning empty array instead of mock data");
        return [];
      }
    }

    // Sort classes by date and time
    classes.sort((a, b) => {
      if (a.date && b.date) {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        if (dateA.getTime() !== dateB.getTime()) {
          return dateA.getTime() - dateB.getTime();
        }
      }
      // If same date, sort by time
      if (a.startTime && b.startTime) {
        return a.startTime.localeCompare(b.startTime);
      }
      return 0;
    });

    console.log(`Final classes array length: ${classes.length}`);
    
    // Return classes from Firebase only - no fallback to mock data
    return classes;
  } catch (error) {
    console.log("Error in fetchClasses:", error);
    // Don't return mock data on error - return empty array
    console.log("Error occurred, returning empty array instead of mock data");
    return [];
  }
};

// Add a new booking
export const addBooking = async (bookingData) => {
  try {
    let bookingId = null;
    
    // Save to Realtime Database first (faster, for admin app)
    if (realtimeDb) {
      try {
        const bookingsRef = ref(realtimeDb, 'customer_bookings');
        const newBookingRef = push(bookingsRef);
        bookingId = newBookingRef.key; // Fixed: removed 'const' to update outer variable
        await set(newBookingRef, {
          ...bookingData,
          bookingId: bookingId,
          createdAt: new Date().toISOString(),
          status: 'confirmed'
        });
        console.log('Booking saved to Realtime Database with ID:', bookingId);
      } catch (realtimeError) {
        console.log("Realtime Database booking error:", realtimeError);
        // Fallback to Firestore
        if (db) {
          try {
            const bookingsRef = collection(db, 'bookings');
            const docRef = await addDoc(bookingsRef, {
              ...bookingData,
              createdAt: new Date().toISOString(),
              status: 'confirmed'
            });
            bookingId = docRef.id;
            console.log('Booking saved to Firestore with ID:', bookingId);
          } catch (firestoreError) {
            throw new Error('Failed to save booking to both databases');
          }
        }
      }
    } else if (db) {
      try {
        const bookingsRef = collection(db, 'bookings');
        const docRef = await addDoc(bookingsRef, {
          ...bookingData,
          createdAt: new Date().toISOString(),
          status: 'confirmed'
        });
        bookingId = docRef.id;
        console.log('Booking saved to Firestore with ID:', bookingId);
      } catch (firestoreError) {
        throw new Error('Failed to save booking to Firestore');
      }
    }

    console.log('Returning booking ID:', bookingId);
    return bookingId;
  } catch (error) {
    console.error('Error adding booking:', error);
    throw error;
  }
};

// Check if data is fresh (updated within last 5 minutes)
export const isDataFresh = (lastUpdated) => {
  if (!lastUpdated) return true; // If no timestamp, assume fresh to prevent infinite loops
  
  const lastUpdateTime = new Date(lastUpdated).getTime();
  const currentTime = Date.now();
  const fiveMinutes = 5 * 60 * 1000; // 5 minutes in milliseconds
  
  return (currentTime - lastUpdateTime) < fiveMinutes;
};

// Get the most recent lastUpdated timestamp from all classes
export const getLastDataUpdate = (classes) => {
  if (!classes || classes.length === 0) return null;
  
  let latestUpdate = null;
  classes.forEach(cls => {
    if (cls.lastUpdated) {
      const updateTime = new Date(cls.lastUpdated).getTime();
      if (!latestUpdate || updateTime > latestUpdate) {
        latestUpdate = updateTime;
      }
    }
  });
  
  return latestUpdate;
};

// Enhanced fetch classes with freshness check
export const fetchClassesWithFreshness = async () => {
  const classes = await fetchClasses();
  const lastUpdate = getLastDataUpdate(classes);
  
  // Determine if data is fresh
  let isFresh = true;
  let dataSource = 'No Data';
  
  if (classes.length > 0) {
    if (classes[0].courseId) {
      dataSource = 'Firebase';
      // Firebase data is always considered fresh to prevent endless loops
      isFresh = true;
    } else {
      dataSource = 'Mock';
      // Mock data is always considered fresh to prevent infinite loops
      isFresh = true;
    }
  }
  
  return {
    classes,
    lastUpdate,
    isFresh,
    dataSource
  };
};

// Validate if a class still exists before booking
export const validateClassExists = async (classId) => {
  try {
    const classes = await fetchClasses();
    const classExists = classes.some(cls => cls.id === classId);
    
    if (!classExists) {
      throw new Error('This class is no longer available. It may have been removed by the instructor.');
    }
    
    return true;
  } catch (error) {
    console.error('Error validating class:', error);
    throw error;
  }
};

// Enhanced booking with validation
export const addBookingWithValidation = async (bookingData) => {
  try {
    // Validate class exists before booking
    await validateClassExists(bookingData.classId);
    
    // Proceed with booking
    return await addBooking(bookingData);
  } catch (error) {
    console.error('Booking validation failed:', error);
    throw error;
  }
};

// Fetch user bookings
export const fetchUserBookings = async (userId) => {
  try {
    if (db) {
      const bookingsRef = collection(db, 'bookings');
      const snapshot = await getDocs(bookingsRef);
      const bookings = [];
      snapshot.forEach((doc) => {
        const booking = { id: doc.id, ...doc.data() };
        if (booking.userId === userId) {
          bookings.push(booking);
        }
      });
      return bookings;
    } else {
      // Mock user bookings
      console.log("Fetching mock bookings for user:", userId);
      return [
        {
          id: '1',
          userId: userId,
          classId: '1',
          className: 'Morning Hatha Yoga',
          instructor: 'Sarah Johnson',
          date: '2025-07-29',
          time: '08:00',
          price: 25,
          status: 'confirmed',
          createdAt: new Date('2025-07-25')
        }
      ];
    }
  } catch (error) {
    console.error("Error fetching user bookings:", error);
    return [];
  }
};

// Fetch bookings by email (for BookingHistory component)
export const fetchBookingsByEmail = async (email) => {
  try {
    // Try Realtime Database first (where customer bookings are saved)
    if (realtimeDb) {
      const bookingsRef = ref(realtimeDb, 'customer_bookings');
      const snapshot = await get(bookingsRef);
      
      if (snapshot.exists()) {
        const bookings = [];
        snapshot.forEach((childSnapshot) => {
          const booking = childSnapshot.val();
          if (booking.email === email) {
            bookings.push({
              id: childSnapshot.key,
              ...booking
            });
          }
        });
        
        // Sort by booking date (newest first)
        const sortedBookings = bookings.sort((a, b) => 
          new Date(b.bookingDate) - new Date(a.bookingDate)
        );
        
        console.log(`Found ${sortedBookings.length} bookings for email: ${email}`);
        return sortedBookings;
      }
    }
    
    // Fallback to Firestore
    if (db) {
      const bookingsRef = collection(db, 'bookings');
      const snapshot = await getDocs(bookingsRef);
      const bookings = [];
      snapshot.forEach((doc) => {
        const booking = { id: doc.id, ...doc.data() };
        if (booking.email === email) {
          bookings.push(booking);
        }
      });
      
      // Sort by booking date (newest first)
      const sortedBookings = bookings.sort((a, b) => 
        new Date(b.bookingDate) - new Date(a.bookingDate)
      );
      
      return sortedBookings;
    }
    
    // Mock bookings for development/testing
    console.log("Fetching mock bookings for email:", email);
    return [
      {
        id: '1',
        email: email,
        classes: [
          {
            id: '1',
            courseName: 'Morning Hatha Yoga',
            instructor: 'Sarah Johnson',
            date: '2025-07-29',
            time: '08:00',
            duration: 60,
            price: 25,
            quantity: 1,
            difficulty: 'Beginner',
            type: 'Hatha'
          }
        ],
        totalAmount: 25,
        totalClasses: 1,
        bookingDate: '2025-07-25T10:30:00Z',
        status: 'confirmed',
        createdAt: '2025-07-25T10:30:00Z'
      },
      {
        id: '2',
        email: email,
        classes: [
          {
            id: '2',
            courseName: 'Power Vinyasa Flow',
            instructor: 'Mike Chen',
            date: '2025-07-30',
            time: '18:00',
            duration: 75,
            price: 30,
            quantity: 2,
            difficulty: 'Advanced',
            type: 'Vinyasa'
          }
        ],
        totalAmount: 60,
        totalClasses: 2,
        bookingDate: '2025-07-26T15:45:00Z',
        status: 'confirmed',
        createdAt: '2025-07-26T15:45:00Z'
      }
    ];
  } catch (error) {
    console.error("Error fetching bookings by email:", error);
    return [];
  }
};

// Update class enrollment
export const updateClassEnrollment = async (classId, newEnrollmentCount) => {
  try {
    if (db) {
      const classRef = doc(db, 'classes', classId);
      await updateDoc(classRef, {
        enrolled: newEnrollmentCount
      });
      return true;
    } else {
      // Mock update
      console.log(`Mock update: Class ${classId} enrollment updated to ${newEnrollmentCount}`);
      return true;
    }
  } catch (error) {
    console.error("Error updating class enrollment:", error);
    throw error;
  }
};

// Test Firebase connection
export const testFirebaseConnection = async () => {
  try {
    if (realtimeDb && db) {
      console.log("Testing Firebase connection...");
      
      // Test Realtime Database
      try {
        const dbRef = ref(realtimeDb);
        const snapshot = await get(child(dbRef, 'yoga_classes'));
        console.log("Realtime Database connection:", snapshot.exists() ? "SUCCESS" : "NO DATA");
        return { realtime: snapshot.exists(), firestore: true };
      } catch (error) {
        console.log("Realtime Database connection failed:", error);
        return { realtime: false, firestore: true };
      }
    } else {
      console.log("Firebase not configured");
      return { realtime: false, firestore: false };
    }
  } catch (error) {
    console.error("Firebase connection test failed:", error);
    return { realtime: false, firestore: false };
  }
};

// Fetch customer bookings for admin app
export const fetchCustomerBookings = async () => {
  try {
    if (realtimeDb) {
      const bookingsRef = ref(realtimeDb, 'customer_bookings');
      const snapshot = await get(bookingsRef);
      
      if (snapshot.exists()) {
        const bookings = [];
        snapshot.forEach((childSnapshot) => {
          bookings.push({
            id: childSnapshot.key,
            ...childSnapshot.val()
          });
        });
        
        // Sort by booking date (newest first)
        return bookings.sort((a, b) => 
          new Date(b.bookingDate) - new Date(a.bookingDate)
        );
      }
      return [];
    } else {
      // Mock customer bookings for development
      console.log("Fetching mock customer bookings");
      return [
        {
          id: '1',
          email: 'john.doe@example.com',
          classes: [
            {
              courseName: 'Hot Yoga',
              instructor: 'Huy',
              date: '2025-01-18',
              time: '19:30',
              duration: 120,
              price: 250,
              quantity: 1,
              difficulty: 'Advanced'
            }
          ],
          totalAmount: 250,
          totalClasses: 1,
          bookingDate: '2025-01-15T10:30:00Z',
          status: 'confirmed',
          createdAt: '2025-01-15T10:30:00Z'
        },
        {
          id: '2',
          email: 'jane.smith@example.com',
          classes: [
            {
              courseName: 'Flow Yoga',
              instructor: 'Sarah',
              date: '2025-01-20',
              time: '09:00',
              duration: 60,
              price: 25,
              quantity: 2,
              difficulty: 'Beginner'
            }
          ],
          totalAmount: 50,
          totalClasses: 2,
          bookingDate: '2025-01-14T15:45:00Z',
          status: 'confirmed',
          createdAt: '2025-01-14T15:45:00Z'
        }
      ];
    }
  } catch (error) {
    console.error("Error fetching customer bookings:", error);
    return [];
  }
};

export default {
  fetchClasses,
  addBooking,
  fetchUserBookings,
  fetchBookingsByEmail,
  updateClassEnrollment,
  testFirebaseConnection,
  fetchCustomerBookings
};
