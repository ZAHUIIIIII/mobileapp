import React from 'react';
import ClassBrowser from '../../screens/ClassBrowser';
import { useAppContext } from '../../context/AppContext';

export default function ClassBrowserScreen() {
  const { addToCart } = useAppContext();
  return <ClassBrowser onAddToCart={addToCart} />;
}
