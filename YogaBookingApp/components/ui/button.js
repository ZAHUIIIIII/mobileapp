import React from 'react';
import { TouchableOpacity, Text } from 'react-native';

export default function Button({ children, onPress, style, disabled, ...props }) {
  return (
    <TouchableOpacity
      onPress={onPress}
      activeOpacity={0.8}
      disabled={disabled}
      style={[
        {
          backgroundColor: disabled ? '#ccc' : '#2563eb',
          paddingVertical: 12,
          paddingHorizontal: 20,
          borderRadius: 6,
          alignItems: 'center',
        },
        style,
      ]}
      {...props}
    >
      <Text style={{ color: '#fff', fontWeight: 'bold', fontSize: 16 }}>
        {children}
      </Text>
    </TouchableOpacity>
  );
} 