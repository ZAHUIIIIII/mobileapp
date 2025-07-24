import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

function Badge({ children, style, variant = 'default', ...props }) {
  return (
    <View style={[styles.badge, variant === 'secondary' ? styles.secondary : styles.default, style]} {...props}>
      <Text style={styles.text}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'flex-start',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 4,
    marginRight: 4,
    marginBottom: 4,
    backgroundColor: '#e0e7ff', // default bg
  },
  default: {
    backgroundColor: '#e0e7ff', // blue-100
  },
  secondary: {
    backgroundColor: '#f3f4f6', // gray-100
  },
  text: {
    color: '#3730a3', // blue-800
    fontSize: 13,
    fontWeight: '500',
  },
});

export { Badge }; 