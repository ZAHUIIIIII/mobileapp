import React from 'react';
import ShoppingCart from '../../screens/ShoppingCart';
import { useAppContext } from '../../context/AppContext';

export default function CartScreen() {
  const context = useAppContext();
  
  // Debug logging
  console.log('CartScreen context:', context);
  console.log('CartScreen cartItems:', { 
    cartItems: context?.cartItems, 
    cartItemsType: typeof context?.cartItems, 
    cartItemsIsArray: Array.isArray(context?.cartItems),
    cartItemsLength: context?.cartItems?.length 
  });
  
  // Safety check - ensure cartItems is always an array
  const safeCartItems = Array.isArray(context?.cartItems) ? context.cartItems : [];
  
  // Safety check - ensure callback functions are defined
  const safeRemoveFromCart = context?.removeFromCart || (() => console.log('removeFromCart not defined'));
  const safeUpdateCartQuantity = context?.updateCartQuantity || (() => console.log('updateCartQuantity not defined'));
  const safeSubmitBooking = context?.submitBooking || (() => Promise.reject('submitBooking not defined'));
  
  return (
    <ShoppingCart
      items={safeCartItems}
      onRemoveItem={safeRemoveFromCart}
      onUpdateQuantity={safeUpdateCartQuantity}
      onSubmitBooking={safeSubmitBooking}
    />
  );
}
