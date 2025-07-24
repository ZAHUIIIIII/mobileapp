import React from 'react';
import { TextInput } from 'react-native';

export default function Input(props) {
  return (
    <TextInput
      {...props}
      style={[
        {
          borderWidth: 1,
          borderColor: '#ccc',
          borderRadius: 6,
          padding: 10,
          fontSize: 16,
          marginBottom: 8,
          backgroundColor: '#fff',
        },
        props.style,
      ]}
    />
  );
} 