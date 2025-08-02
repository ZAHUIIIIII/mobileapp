import React, { useState, useCallback } from 'react';
import { ScrollView, View, Text, RefreshControl } from 'react-native';
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
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  const getTypeColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'beginner':
        return 'bg-green-100 text-green-800';
      case 'intermediate':
        return 'bg-yellow-100 text-yellow-800';
      case 'advanced':
        return 'bg-orange-100 text-orange-800';
      case 'expert':
        return 'bg-red-100 text-red-800';
      case 'flow yoga':
        return 'bg-blue-100 text-blue-800';
      case 'aerial yoga':
        return 'bg-purple-100 text-purple-800';
      case 'family yoga':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    try {
      let date;
      
      // Handle different date formats
      if (typeof dateString === 'string') {
        // If it's already a formatted date string like "Monday, 18/08/2025"
        if (dateString.includes(',') && dateString.includes('/')) {
          const parts = dateString.split(',')[1]?.trim().split('/');
          if (parts && parts.length === 3) {
            const [day, month, year] = parts;
            date = new Date(year, month - 1, day);
          } else {
            date = new Date(dateString);
          }
        } else {
          date = new Date(dateString);
        }
      } else {
        date = new Date(dateString);
      }
      
      if (isNaN(date.getTime())) {
        return 'N/A';
      }
      
      return date.toLocaleDateString('en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      });
    } catch (error) {
      return 'N/A';
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'N/A';
      return date.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'N/A';
    }
  };

  const formatBookingDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'N/A';
      
      const now = new Date();
      const diffTime = Math.abs(now - date);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      // Format as "31 Jul 2025 at 12:08 PM"
      const day = date.getDate();
      const month = date.toLocaleDateString('en-GB', { month: 'short' });
      const year = date.getFullYear();
      const time = date.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
      
      let dateText = `${day} ${month} ${year} at ${time}`;
      
      // Add relative time for recent bookings
      if (diffDays === 1) {
        dateText += ' (yesterday)';
      } else if (diffDays === 0) {
        dateText += ' (today)';
      } else if (diffDays <= 7) {
        dateText += ` (${diffDays} days ago)`;
      }
      
      return dateText;
    } catch (error) {
      return 'N/A';
    }
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
      // Data is already an array from the updated fetchBookingsByEmail function
      setBookings(data || []);
    } catch (err) {
      setError('Failed to fetch bookings. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const onRefresh = useCallback(async () => {
    if (!email.trim()) return;
    
    setRefreshing(true);
    try {
      const data = await fetchBookingsByEmail(email.trim());
      setBookings(data || []);
    } catch (err) {
      setError('Failed to refresh bookings. Please try again.');
    } finally {
      setRefreshing(false);
    }
  }, [email]);

  const handleEmailSubmit = () => {
    if (email.trim()) {
      handleFetchBookings();
    }
  };

  return (
    <ScrollView 
      contentContainerStyle={{ padding: 16, paddingBottom: 100 }}
      refreshControl={
        <RefreshControl
          refreshing={refreshing}
          onRefresh={onRefresh}
          enabled={!!email.trim()}
        />
      }
      showsVerticalScrollIndicator={false}
    >
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
          onSubmitEditing={handleEmailSubmit}
          returnKeyType="search"
        />
        <Button onPress={handleFetchBookings} disabled={loading} style={{ marginTop: 8 }}>
          {loading ? 'Loading...' : 'Fetch Bookings'}
        </Button>
        {error ? (
          <Text style={{ color: 'red', fontSize: 13, marginTop: 6 }}>{error}</Text>
        ) : null}
      </View>
      {bookings.length === 0 && !loading && !error && email.trim() && (
        <Card style={{ marginBottom: 16 }}>
          <CardContent style={{ alignItems: 'center', paddingVertical: 48 }}>
            <FontAwesome name="history" size={64} color="#888" style={{ marginBottom: 16 }} />
            <Text style={{ color: '#888', marginBottom: 8 }}>No bookings found</Text>
            <Text style={{ color: '#888', fontSize: 12 }}>No booking history found for {email.trim()}</Text>
          </CardContent>
        </Card>
      )}
      {bookings.length === 0 && !loading && !error && !email.trim() && (
        <Card style={{ marginBottom: 16 }}>
          <CardContent style={{ alignItems: 'center', paddingVertical: 48 }}>
            <FontAwesome name="search" size={64} color="#888" style={{ marginBottom: 16 }} />
            <Text style={{ color: '#888', marginBottom: 8 }}>Enter your email</Text>
            <Text style={{ color: '#888', fontSize: 12 }}>Enter your email address above to view your booking history</Text>
          </CardContent>
        </Card>
      )}
      {bookings.length > 0 && (
        <View style={{ marginBottom: 16 }}>
          <Text style={{ color: '#22c55e', fontSize: 14, textAlign: 'center' }}>
            Found {bookings.length} {bookings.length === 1 ? 'booking' : 'bookings'} for {email.trim()}
          </Text>
        </View>
      )}
      {bookings.length > 0 && (
        <View style={{ gap: 16 }}>
          {[...bookings].sort((a, b) => new Date(b.bookingDate) - new Date(a.bookingDate)).map((booking) => (
            <Card key={booking.id} style={{ marginBottom: 16 }}>
              <CardHeader style={{ paddingBottom: 8 }}>
                <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <View style={{ flex: 1, marginRight: 12 }}>
                    <CardTitle style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 4 }}>
                      <FontAwesome name="file-text" size={18} color="#888" />
                      <Text style={{ fontSize: 16, fontWeight: 'bold', marginLeft: 8, flex: 1 }} numberOfLines={1}>
                        Booking #{booking.id}
                      </Text>
                    </CardTitle>
                    <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 4 }}>
                      <MaterialIcons name="email" size={16} color="#888" />
                      <Text style={{ color: '#888', fontSize: 13, marginLeft: 4, flex: 1 }} numberOfLines={1}>
                        {booking.email}
                      </Text>
                    </View>
                    <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 2 }}>
                      <MaterialIcons name="event" size={16} color="#888" />
                      <Text style={{ color: '#888', fontSize: 13, marginLeft: 4 }}>
                        {booking.totalClasses} {booking.totalClasses === 1 ? 'class' : 'classes'}
                      </Text>
                    </View>
                    <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 4 }}>
                      <MaterialIcons name="schedule" size={16} color="#888" />
                      <Text style={{ color: '#888', fontSize: 13, marginLeft: 4, flex: 1 }}>
                        Booked on: {formatBookingDate(booking.bookingDate)}
                      </Text>
                    </View>
                  </View>
                  <View style={{ alignItems: 'flex-end', minWidth: 80 }}>
                    <Text style={{ fontWeight: 'bold', fontSize: 16 }}>£{booking.totalAmount}</Text>
                    <View style={{ 
                      backgroundColor: booking.status === 'confirmed' ? '#dcfce7' : '#fef3c7',
                      paddingHorizontal: 8,
                      paddingVertical: 2,
                      borderRadius: 12,
                      marginTop: 4
                    }}>
                      <Text style={{ 
                        color: booking.status === 'confirmed' ? '#16a34a' : '#d97706',
                        fontSize: 11,
                        fontWeight: 'bold',
                        textTransform: 'uppercase'
                      }}>
                        {booking.status}
                      </Text>
                    </View>
                  </View>
                </View>
              </CardHeader>
              <CardContent style={{ gap: 12 }}>
                <View style={{ gap: 8 }}>
                  <Text style={{ fontWeight: 'bold', fontSize: 15 }}>Booked Classes ({booking.classes?.length || 0})</Text>
                  {(booking.classes || []).map((classItem, index) => (
                      <View 
                        key={`${booking.id}-${classItem.id || index}`}
                        style={{ backgroundColor: '#f3f4f6', padding: 12, borderRadius: 8, marginBottom: 8 }}
                      >
                      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <View style={{ flex: 1, marginRight: 8 }}>
                          <Text style={{ fontWeight: 'bold', fontSize: 15 }} numberOfLines={1}>
                            {classItem.courseName || classItem.type}
                          </Text>
                          <Text style={{ color: '#888', fontSize: 13 }} numberOfLines={1}>
                            with {classItem.instructor || classItem.teacher}
                          </Text>
                        </View>
                        <View style={{ flexDirection: 'row', alignItems: 'center', flexShrink: 0 }}>
                          <Badge style={getTypeColor(classItem.difficulty || classItem.type)}>
                            {classItem.difficulty || classItem.type}
                          </Badge>
                          {classItem.quantity > 1 && (
                            <Badge style={{ backgroundColor: '#e0e7ff', color: '#222', marginLeft: 6 }}>
                              x{classItem.quantity}
                            </Badge>
                          )}
                        </View>
                      </View>
                      <View style={{ flexDirection: 'row', flexWrap: 'wrap', marginTop: 6 }}>
                        <View style={{ flexDirection: 'row', alignItems: 'center', marginRight: 16, marginBottom: 4 }}>
                          <FontAwesome name="calendar" size={14} color="#888" />
                          <Text style={{ marginLeft: 4, fontSize: 13 }}>
                            {formatDate(classItem.date) || 
                             formatDate(classItem.startTime) || 
                             formatDate(classItem.bookingDate) ||
                             classItem.date || 
                             classItem.startTime || 
                             'N/A'}
                          </Text>
                        </View>
                        <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: 4 }}>
                          <FontAwesome name="clock-o" size={14} color="#888" />
                          <Text style={{ marginLeft: 4, fontSize: 13 }}>{classItem.time || 'N/A'}</Text>
                        </View>
                      </View>
                      <View style={{ marginTop: 6 }}>
                        <Text style={{ fontSize: 13, marginBottom: 2 }}>
                          <Text style={{ fontWeight: 'bold' }}>Duration:</Text> {classItem.duration || 'N/A'} minutes
                        </Text>
                        <Text style={{ fontSize: 13 }}>
                          <Text style={{ fontWeight: 'bold' }}>Price:</Text> £{classItem.price || 'N/A'}
                          {classItem.quantity > 1 && ` x ${classItem.quantity} = £${(classItem.price || 0) * classItem.quantity}`}
                        </Text>
                      </View>
                      {classItem.description && (
                        <Text style={{ color: '#888', fontSize: 13, marginTop: 6 }} numberOfLines={3}>
                          {classItem.description}
                        </Text>
                      )}
                      {classItem.comments && (
                        <Text style={{ color: '#2563eb', backgroundColor: '#dbeafe', padding: 6, borderRadius: 6, marginTop: 8 }} numberOfLines={3}>
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