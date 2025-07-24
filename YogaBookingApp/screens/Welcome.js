import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";

export default function Welcome({ navigation }) {
  return (
    <View style={styles.container}>
      <View style={styles.main}>
        <Text style={styles.title}>Welcome to Universal Yoga</Text>
        <Text style={styles.subtitle}>Book your next class with ease</Text>
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('ClassBrowser')}
        >
          <Text style={styles.buttonText}>Browse Classes</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('ShoppingCart')}
        >
          <Text style={styles.buttonText}>View Cart</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('BookingHistory')}
        >
          <Text style={styles.buttonText}>Booking History</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    padding: 24,
  },
  main: {
    flex: 1,
    justifyContent: "center",
    maxWidth: 960,
    marginHorizontal: "auto",
    alignItems: 'center',
  },
  title: {
    fontSize: 36,
    fontWeight: "bold",
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 20,
    color: "#38434D",
    marginBottom: 32,
  },
  button: {
    backgroundColor: '#4F46E5',
    paddingVertical: 14,
    paddingHorizontal: 32,
    borderRadius: 8,
    marginVertical: 8,
    width: 220,
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
}); 