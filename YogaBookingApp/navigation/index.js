import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import BookingHistory from '../screens/BookingHistory';
import ClassBrowser from '../screens/ClassBrowser';
import ShoppingCart from '../screens/ShoppingCart';
import Welcome from '../screens/Welcome';

const Stack = createStackNavigator();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Welcome">
        <Stack.Screen name="Welcome" component={Welcome} />
        <Stack.Screen name="ClassBrowser" component={ClassBrowser} />
        <Stack.Screen name="BookingHistory" component={BookingHistory} />
        <Stack.Screen name="ShoppingCart" component={ShoppingCart} />
      </Stack.Navigator>
    </NavigationContainer>
  );
} 