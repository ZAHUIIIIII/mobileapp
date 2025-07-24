import React, { useState, createContext, useContext } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { FontAwesome, MaterialIcons } from '@expo/vector-icons';

import ClassBrowser from '../screens/ClassBrowser';

import ShoppingCart from '../screens/ShoppingCart';
import BookingHistory from '../screens/BookingHistory';
import { AppProvider, useAppContext } from '../context/AppContext';

function ClassBrowserScreen() {
  const { addToCart } = useAppContext();
  return <ClassBrowser onAddToCart={addToCart} />;
}
function ShoppingCartScreen() {
  const { cartItems, removeFromCart, updateCartQuantity, submitBooking } = useAppContext();
  return (
    <ShoppingCart
      items={cartItems}
      onRemoveItem={removeFromCart}
      onUpdateQuantity={updateCartQuantity}
      onSubmitBooking={submitBooking}
    />
  );
}
function BookingHistoryScreen() {
  const { bookings } = useAppContext();
  return <BookingHistory bookings={bookings} />;
}

const Tab = createBottomTabNavigator();

function CartTabIcon({ color, size }) {
  const { cartItems } = useAppContext();
  const cartItemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);
  return (
    <View>
      <MaterialIcons name="shopping-cart" size={size} color={color} />
      {cartItemCount > 0 && (
        <View style={styles.cartBadge}>
          <Text style={styles.cartBadgeText}>{cartItemCount}</Text>
        </View>
      )}
    </View>
  );
}

export default function App() {
  return (
    <AppProvider>
      <NavigationContainer>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Yoga Studio</Text>
          <Text style={styles.headerSubtitle}>Book Your Perfect Class</Text>
        </View>
        <Tab.Navigator
          screenOptions={({ route }) => ({
            tabBarIcon: ({ color, size }) => {
              if (route.name === 'Browse') {
                return <FontAwesome name="calendar" size={size} color={color} />;
              } else if (route.name === 'Cart') {
                return <CartTabIcon color={color} size={size} />;
              } else if (route.name === 'History') {
                return <FontAwesome name="history" size={size} color={color} />;
              }
            },
            tabBarActiveTintColor: '#2563eb',
            tabBarInactiveTintColor: '#888',
            headerShown: false,
          })}
        >
          <Tab.Screen name="Browse" component={ClassBrowserScreen} />
          <Tab.Screen name="Cart" component={ShoppingCartScreen} />
          <Tab.Screen name="History" component={BookingHistoryScreen} />
        </Tab.Navigator>
      </NavigationContainer>
    </AppProvider>
  );
}

const styles = StyleSheet.create({
  header: {
    backgroundColor: '#2563eb',
    paddingTop: 48,
    paddingBottom: 16,
    alignItems: 'center',
  },
  headerTitle: {
    color: '#fff',
    fontSize: 22,
    fontWeight: 'bold',
  },
  headerSubtitle: {
    color: '#e0e7ff',
    fontSize: 14,
    marginTop: 2,
  },
  cartBadge: {
    position: 'absolute',
    right: -8,
    top: -4,
    backgroundColor: '#ef4444',
    borderRadius: 8,
    minWidth: 16,
    height: 16,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 3,
    zIndex: 1,
  },
  cartBadgeText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: 'bold',
  },
});
