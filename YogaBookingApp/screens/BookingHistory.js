import React, { useState } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import Button from '../components/ui/button';
import { Badge } from '@components/ui/badge';
import Input from '../components/ui/input';
import { FontAwesome, MaterialIcons } from '@expo/vector-icons';
import { fetchBookingsByEmail } from '../services/firebase';

export default function BookingHistory() {
  const [email, setEmail] = useState('');
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const getTypeColor = (type) => {
    switch (type) {
      case 'Flow Yoga':
        return 'bg-blue-100 text-blue-800';
      case 'Aerial Yoga':
        return 'bg-purple-100 text-purple-800';
      case 'Family Yoga':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleFetchBookings = async () => {
    setError('');
    setBookings([]);
    if (!email.trim()) {
      setError('Please enter your email address');
      return;
    }
    setLoading(true);
    try {
      const data = await fetchBookingsByEmail(email.trim());
      // Convert object to array
      const bookingsArr = data ? Object.keys(data).map((id) => ({ id, ...data[id] })) : [];
      setBookings(bookingsArr);
    } catch (err) {
      setError('Failed to fetch bookings. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={{ padding: 16 }}>
      <View style={{ alignItems: 'center', marginBottom: 16 }}>
        <Text style={{ fontSize: 22, fontWeight: 'bold', marginBottom: 4 }}>Booking History</Text>
        <Text style={{ color: '#888', fontSize: 14 }}>View your past bookings by email</Text>
      </View>
      <View style={{ marginBottom: 16 }}>
        <Input
          value={email}
          onChangeText={setEmail}
          placeholder="your.email@example.com"
          keyboardType="email-address"
          autoCapitalize="none"
          required
          editable={!loading}
        />
        <Button onPress={handleFetchBookings} disabled={loading} style={{ marginTop: 8 }}>
          {loading ? 'Loading...' : 'Fetch Bookings'}
        </Button>
        {error ? (
          <Text style={{ color: 'red', fontSize: 13, marginTop: 6 }}>{error}</Text>
        ) : null}
      </View>
      {bookings.length === 0 && !loading && !error && (
        <Card style={{ marginBottom: 16 }}>
          <CardContent style={{ alignItems: 'center', paddingVertical: 48 }}>
            <FontAwesome name="history" size={64} color="#888" style={{ marginBottom: 16 }} />
            <Text style={{ color: '#888', marginBottom: 8 }}>No bookings yet</Text>
            <Text style={{ color: '#888', fontSize: 12 }}>Your booking history will appear here after you make your first booking</Text>
          </CardContent>
        </Card>
      )}
      {bookings.length > 0 && (
        <View style={{ gap: 16 }}>
          {[...bookings].sort((a, b) => new Date(b.bookingDate) - new Date(a.bookingDate)).map((booking) => (
            <Card key={booking.id} style={{ marginBottom: 16 }}>
              <CardHeader style={{ paddingBottom: 8 }}>
                <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <View>
                    <CardTitle style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                      <FontAwesome name="file-text" size={18} color="#888" />
                      <Text style={{ fontSize: 16, fontWeight: 'bold' }}>Booking #{booking.id}</Text>
                    </CardTitle>
                    <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 4 }}>
                      <MaterialIcons name="email" size={16} color="#888" />
                      <Text style={{ color: '#888', fontSize: 13, marginLeft: 4 }}>{booking.email}</Text>
                    </View>
                  </View>
                  <View style={{ alignItems: 'flex-end' }}>
                    <Text style={{ fontWeight: 'bold', fontSize: 16 }}>£{booking.totalAmount}</Text>
                    <Text style={{ color: '#888', fontSize: 13 }}>{formatDate(booking.bookingDate)}</Text>
                    <Text style={{ color: '#aaa', fontSize: 12 }}>{formatTime(booking.bookingDate)}</Text>
                  </View>
                </View>
              </CardHeader>
              <CardContent style={{ gap: 12 }}>
                <View style={{ gap: 8 }}>
                  <Text style={{ fontWeight: 'bold', fontSize: 15 }}>Booked Classes ({booking.classes.length})</Text>
                  {booking.classes.map((classItem) => (
                    <View 
                      key={`${booking.id}-${classItem.id}`}
                      style={{ backgroundColor: '#f3f4f6', padding: 12, borderRadius: 8, marginBottom: 8 }}
                    >
                      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <View>
                          <Text style={{ fontWeight: 'bold', fontSize: 15 }}>{classItem.type}</Text>
                          <Text style={{ color: '#888', fontSize: 13 }}>with {classItem.teacher}</Text>
                        </View>
                        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                          <Badge style={getTypeColor(classItem.type)}>
                            {classItem.type}
                          </Badge>
                          {classItem.quantity > 1 && (
                            <Badge style={{ backgroundColor: '#e0e7ff', color: '#222', marginLeft: 6 }}>
                              x{classItem.quantity}
                            </Badge>
                          )}
                        </View>
                      </View>
                      <View style={{ flexDirection: 'row', gap: 16, marginTop: 6 }}>
                        <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 12 }}>
                          <FontAwesome name="calendar" size={14} color="#888" />
                          <Text style={{ marginLeft: 4 }}>{classItem.dayOfWeek}</Text>
                        </View>
                        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                          <FontAwesome name="clock-o" size={14} color="#888" />
                          <Text style={{ marginLeft: 4 }}>{classItem.time}</Text>
                        </View>
                      </View>
                      <View style={{ marginTop: 6 }}>
                        <Text><Text style={{ fontWeight: 'bold' }}>Date:</Text> {formatDate(classItem.date)}</Text>
                        <Text><Text style={{ fontWeight: 'bold' }}>Duration:</Text> {classItem.duration} minutes</Text>
                        <Text><Text style={{ fontWeight: 'bold' }}>Price:</Text> £{classItem.price} {classItem.quantity > 1 && `x ${classItem.quantity} = £${classItem.price * classItem.quantity}`}</Text>
                      </View>
                      {classItem.description && (
                        <Text style={{ color: '#888', fontSize: 13 }}>{classItem.description}</Text>
                      )}
                      {classItem.comments && (
                        <Text style={{ color: '#2563eb', backgroundColor: '#dbeafe', padding: 6, borderRadius: 6, marginTop: 8 }}>
                          <Text style={{ fontWeight: 'bold' }}>Note:</Text> {classItem.comments}
                        </Text>
                      )}
                    </View>
                  ))}
                </View>
                <View style={{ backgroundColor: '#f1f5f9', padding: 12, borderRadius: 8, marginTop: 8 }}>
                  <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Text style={{ fontWeight: 'bold' }}>Total Amount Paid:</Text>
                    <Text style={{ fontWeight: 'bold' }}>£{booking.totalAmount}</Text>
                  </View>
                </View>
              </CardContent>
            </Card>
          ))}
        </View>
      )}
    </ScrollView>
  );
}