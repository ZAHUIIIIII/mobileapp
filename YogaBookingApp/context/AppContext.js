import React, { createContext, useContext, useState } from 'react';
import { addBookingWithValidation } from '../services/firebase';

const AppContext = createContext();

export function useAppContext() {
  return useContext(AppContext);
}

export function AppProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [bookings, setBookings] = useState([]);

  // Debug logging
  console.log('AppContext cartItems:', { 
    cartItems: cartItems, 
    cartItemsType: typeof cartItems, 
    cartItemsIsArray: Array.isArray(cartItems),
    cartItemsLength: cartItems?.length 
  });

  // Ensure cartItems is always an array
  const safeCartItems = Array.isArray(cartItems) ? cartItems : [];

  const addToCart = (yogaClass) => {
    console.log('addToCart called with:', yogaClass);
    setCartItems(prev => {
      // Safety check - ensure prev is always an array
      const safePrev = Array.isArray(prev) ? prev : [];
      console.log('addToCart prev state:', safePrev);
      
      const existing = safePrev.find(item => item.id === yogaClass.id);
      if (existing) {
        return safePrev.map(item =>
          item.id === yogaClass.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...safePrev, { ...yogaClass, quantity: 1 }];
    });
  };

  const removeFromCart = (classId) => {
    setCartItems(prev => {
      const safePrev = Array.isArray(prev) ? prev : [];
      return safePrev.filter(item => item.id !== classId);
    });
  };

  const updateCartQuantity = (classId, quantity) => {
    if (quantity === 0) {
      removeFromCart(classId);
      return;
    }
    setCartItems(prev => {
      const safePrev = Array.isArray(prev) ? prev : [];
      return safePrev.map(item =>
        item.id === classId ? { ...item, quantity } : item
      );
    });
  };

  // Clean up stale cart items (remove items that no longer exist)
  const cleanupStaleCartItems = async () => {
    try {
      const { fetchClasses } = await import('../services/firebase');
      const availableClasses = await fetchClasses();
      const availableClassIds = new Set(availableClasses.map(cls => cls.id));
      
      let removedItems = [];
      
      setCartItems(prev => {
        removedItems = prev.filter(item => !availableClassIds.has(item.id));
        const updatedItems = prev.filter(item => availableClassIds.has(item.id));
        
        if (removedItems.length > 0) {
          console.log(`Removed ${removedItems.length} stale items from cart:`, removedItems.map(item => item.courseName || item.name));
        }
        
        return updatedItems;
      });
      
      return removedItems;
    } catch (error) {
      console.error('Error cleaning up stale cart items:', error);
      return [];
    }
  };

  const submitBooking = async (email) => {
    try {
      if (!cartItems || !Array.isArray(cartItems) || cartItems.length === 0) {
        throw new Error('No items in cart to book');
      }
      
      const totalAmount = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
      
      // Validate all classes in cart still exist and create booking objects
      const bookingPromises = cartItems.map(async (item) => {
        const bookingId = await addBookingWithValidation({
          classId: item.id,
          email: email,
          classes: [{
            id: item.id,
            courseName: item.courseName || item.name,
            instructor: item.instructor || item.teacher,
            date: item.date,
            startTime: item.startTime,
            time: item.startTime || item.time,
            duration: item.duration,
            price: item.price,
            quantity: item.quantity,
            difficulty: item.difficulty,
            type: item.type
          }],
          totalAmount: item.price * item.quantity,
          totalClasses: item.quantity,
          bookingDate: new Date().toISOString(),
          status: 'confirmed'
        });

        // Create a complete booking object for the success screen
        return {
          id: bookingId,
          email: email,
          classes: [{
            id: item.id,
            courseName: item.courseName || item.name,
            instructor: item.instructor || item.teacher,
            date: item.date,
            startTime: item.startTime,
            time: item.startTime || item.time,
            duration: item.duration,
            price: item.price,
            quantity: item.quantity,
            difficulty: item.difficulty,
            type: item.type
          }],
          totalAmount: item.price * item.quantity,
          totalClasses: item.quantity,
          bookingDate: new Date().toISOString(),
          status: 'confirmed'
        };
      });

      // Wait for all validations and bookings to complete
      const savedBookings = await Promise.all(bookingPromises);
      
      // Update local state
      setBookings(prev => [...prev, ...savedBookings]);
      setCartItems([]);
      
      return savedBookings;
    } catch (error) {
      console.error('Error submitting booking:', error);
      
      // If validation failed, show specific error message
      if (error.message.includes('no longer available')) {
        throw new Error('One or more classes in your cart are no longer available. Please refresh and try again.');
      }
      
      throw error;
    }
  };

  return (
    <AppContext.Provider value={{
      cartItems: Array.isArray(cartItems) ? cartItems : [], bookings, addToCart, removeFromCart, updateCartQuantity, submitBooking, cleanupStaleCartItems
    }}>
      {children}
    </AppContext.Provider>
  );
} 