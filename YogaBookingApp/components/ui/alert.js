import * as React from "react";
import { View, Text, StyleSheet } from "react-native";

function Alert({ style, children, ...props }) {
  return (
    <View style={[styles.alert, style]} {...props}>
      {children}
    </View>
  );
}

function AlertTitle({ style, children, ...props }) {
  return (
    <Text style={[styles.title, style]} {...props}>
      {children}
    </Text>
  );
}

function AlertDescription({ style, children, ...props }) {
  return (
    <Text style={[styles.description, style]} {...props}>
      {children}
    </Text>
  );
}

const styles = StyleSheet.create({
  alert: {
    width: '100%',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    backgroundColor: '#f8fafc',
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginVertical: 8,
  },
  title: {
    fontWeight: 'bold',
    fontSize: 16,
    marginBottom: 4,
    color: '#1e293b',
  },
  description: {
    fontSize: 14,
    color: '#64748b',
  },
});

export { Alert, AlertTitle, AlertDescription }; 