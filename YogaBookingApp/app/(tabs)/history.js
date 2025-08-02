import React from 'react';
import BookingHistory from '../../screens/BookingHistory';
import { useAppContext } from '../../context/AppContext';

export default function HistoryScreen() {
  const { bookings } = useAppContext();
  return <BookingHistory bookings={bookings} />;
}
