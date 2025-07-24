import React, { createContext, useContext, useState } from 'react';

const AppContext = createContext();

export function useAppContext() {
  return useContext(AppContext);
}

export function AppProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [bookings, setBookings] = useState([]);

  const addToCart = (yogaClass) => {
    setCartItems(prev => {
      const existing = prev.find(item => item.id === yogaClass.id);
      if (existing) {
        return prev.map(item =>
          item.id === yogaClass.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prev, { ...yogaClass, quantity: 1 }];
    });
  };

  const removeFromCart = (classId) => {
    setCartItems(prev => prev.filter(item => item.id !== classId));
  };

  const updateCartQuantity = (classId, quantity) => {
    if (quantity === 0) {
      removeFromCart(classId);
      return;
    }
    setCartItems(prev =>
      prev.map(item =>
        item.id === classId ? { ...item, quantity } : item
      )
    );
  };

  const submitBooking = (email) => {
    const newBooking = {
      id: Date.now().toString(),
      email,
      classes: [...cartItems],
      bookingDate: new Date().toISOString(),
      totalAmount: cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    };
    setBookings(prev => [...prev, newBooking]);
    setCartItems([]);
    return newBooking;
  };

  return (
    <AppContext.Provider value={{
      cartItems, bookings, addToCart, removeFromCart, updateCartQuantity, submitBooking
    }}>
      {children}
    </AppContext.Provider>
  );
} 