import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

function Card({ style, children, ...props }) {
  return (
    <View style={[styles.card, style]} {...props}>
      {children}
    </View>
  );
}

function CardHeader({ style, children, ...props }) {
  return (
    <View style={[styles.header, style]} {...props}>
      {children}
    </View>
  );
}

function CardTitle({ style, children, ...props }) {
  return (
    <Text style={[styles.title, style]} {...props}>
      {children}
    </Text>
  );
}

function CardContent({ style, children, ...props }) {
  return (
    <View style={[styles.content, style]} {...props}>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    marginVertical: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 2,
    overflow: 'hidden',
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#22223b',
  },
  content: {
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
});

export { Card, CardHeader, CardContent, CardTitle }; 