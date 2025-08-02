import React from 'react';
import ShoppingCart from '../../screens/ShoppingCart';
import { useAppContext } from '../../context/AppContext';

export default function CartScreen() {
  const context = useAppContext();
  
  const safeCartItems = Array.isArray(context?.cartItems) ? context.cartItems : [];
  
  const safeRemoveFromCart = context?.removeFromCart || (() => {});
  const safeUpdateCartQuantity = context?.updateCartQuantity || (() => {});
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
