import React, { useEffect, useState, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput, RefreshControl } from 'react-native';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import Button from '../components/ui/button';
import { Badge } from '@components/ui/badge';
import { FontAwesome } from '@expo/vector-icons';
import { fetchClassesWithFreshness, isDataFresh } from '../services/firebase';
import { useAppContext } from '../context/AppContext';

const daysOfWeek = [
  'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'
];

const timesOfDay = [
  'Morning', // 05:00-11:59
  'Afternoon', // 12:00-16:59
  'Evening', // 17:00-21:59
];

function getTimeOfDay(time) {
  const [hour] = time.split(':').map(Number);
  if (hour >= 5 && hour < 12) return 'Morning';
  if (hour >= 12 && hour < 17) return 'Afternoon';
  if (hour >= 17 && hour < 22) return 'Evening';
  return 'Other';
}

// Helper to get 3-letter abbreviation from full day name
const getDayAbbr = (day) => day ? day.slice(0, 3) : '';

export default function ClassBrowser({ onAddToCart }) {
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [dataSource, setDataSource] = useState(''); // Track data source
  const [dataFreshness, setDataFreshness] = useState(null); // Track data freshness
  const [lastUpdate, setLastUpdate] = useState(null); // Track last update time
  const [selectedDays, setSelectedDays] = useState([]); // instead of selectedDay
  const [selectedTime, setSelectedTime] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [notification, setNotification] = useState(null);

  const { cleanupStaleCartItems } = useAppContext();

  // Notification component
  const NotificationBanner = () => {
    if (!notification) return null;
    
    return (
      <View style={styles.notificationContainer}>
        <Text style={styles.notificationText}>
          {notification}
        </Text>
        <TouchableOpacity onPress={() => setNotification(null)}>
          <Text style={styles.notificationClose}>✕</Text>
        </TouchableOpacity>
      </View>
    );
  };

  const loadClasses = useCallback(async () => {
    setLoading(true);
    try {
      console.log("Starting to load classes...");
      const result = await fetchClassesWithFreshness();
      console.log("Fetch result:", result);
      
      setClasses(result.classes || []);
      setDataSource(result.dataSource);
      setDataFreshness(result.isFresh);
      setLastUpdate(result.lastUpdate);
      
      console.log(`Loaded ${result.classes?.length || 0} classes`);
      console.log(`Data source: ${result.dataSource}`);
      console.log(`Data fresh: ${result.isFresh}`);
      
      // Clean up stale cart items and show notification if needed
      const removedItems = await cleanupStaleCartItems();
      if (removedItems && removedItems.length > 0) {
        console.log(`${removedItems.length} stale items removed from cart`);
        setNotification(`${removedItems.length} class(es) removed from cart - no longer available`);
        // Auto-hide notification after 5 seconds
        setTimeout(() => setNotification(null), 5000);
      }
    } catch (error) {
      console.error("Error loading classes:", error);
      setDataSource('Error');
      setDataFreshness(false);
    } finally {
      setLoading(false);
    }
  }, []); // Empty dependency array - load only once

  useEffect(() => {
    loadClasses();
  }, []); // Empty dependency array - run only once on mount

  // Auto-refresh completely removed to prevent endless loops
  // Users can manually refresh using pull-to-refresh

  const onRefresh = useCallback(async () => {
    if (refreshing) return; // Prevent duplicate refresh calls
    
    setRefreshing(true);
    
    try {
      console.log("Force refreshing data from Firebase...");
      const result = await fetchClassesWithFreshness();
      setClasses(result.classes || []);
      setDataSource(result.dataSource);
      setDataFreshness(result.isFresh);
      setLastUpdate(result.lastUpdate);
      
      console.log(`Refreshed: ${result.classes?.length || 0} classes from ${result.dataSource}`);
      
      // Clean up stale cart items and show notification if needed
      const removedItems = await cleanupStaleCartItems();
      if (removedItems && removedItems.length > 0) {
        console.log(`${removedItems.length} stale items removed from cart`);
        setNotification(`${removedItems.length} class(es) removed from cart - no longer available`);
        // Auto-hide notification after 5 seconds
        setTimeout(() => setNotification(null), 5000);
      }
    } catch (error) {
      console.error("Error refreshing classes:", error);
      setDataSource('Error');
      setDataFreshness(false);
    } finally {
      setRefreshing(false);
    }
  }, []); // Empty dependency array to prevent loops

  const filteredClasses = classes.filter((yogaClass) => {
    // Filter by day of week and time using instance date/time
    const dayOfWeek = yogaClass.date && !isNaN(Date.parse(yogaClass.date))
      ? new Date(yogaClass.date).toLocaleDateString('en-US', { weekday: 'long' })
      : yogaClass.daysOfWeek || '';
    const dayOfWeekAbbr = getDayAbbr(dayOfWeek);
    const timeMatch = selectedTime ? getTimeOfDay(yogaClass.startTime || yogaClass.time) === selectedTime : true;
    const dayMatch = selectedDays.length > 0
      ? selectedDays.map(getDayAbbr).includes(dayOfWeekAbbr)
      : true;
    const searchMatch = searchQuery
      ? (yogaClass.courseName || yogaClass.name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (yogaClass.instructor || yogaClass.teacher || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (yogaClass.difficulty || '').toLowerCase().includes(searchQuery.toLowerCase())
      : true;

    const isVisible = timeMatch && dayMatch && searchMatch;
    
    // Debug logging for first few classes
    if (classes.indexOf(yogaClass) < 3) {
      console.log(`Class ${yogaClass.courseName || yogaClass.name}:`, {
        dayOfWeek,
        dayOfWeekAbbr,
        selectedDays: selectedDays.map(getDayAbbr),
        dayMatch,
        timeMatch,
        searchMatch,
        isVisible
      });
    }
    
    return isVisible;
  });

  console.log(`Filtered classes: ${filteredClasses.length} out of ${classes.length} total classes`);
  console.log(`Selected days: ${selectedDays.join(', ')}`);
  console.log(`Selected time: ${selectedTime}`);
  console.log(`Search query: ${searchQuery}`);

  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'beginner': return '#4CAF50';
      case 'intermediate': return '#FF9800';
      case 'advanced': return '#F44336';
      default: return '#9E9E9E';
    }
  };

  // Data status indicator component
  const DataStatusIndicator = () => {
    if (!dataSource || dataSource === 'Error') {
      return (
        <View style={styles.statusContainer}>
          <Text style={[styles.statusText, styles.errorText]}>
            ⚠️ Data source error
          </Text>
        </View>
      );
    }

    const getStatusColor = () => {
      if (dataFreshness === null) return '#9E9E9E';
      return '#4CAF50'; // Always green since auto-refresh is disabled
    };

    const getStatusText = () => {
      if (dataFreshness === null) return 'Checking data...';
      return '🟢 Live data'; // Always show as live since auto-refresh is disabled
    };

    const formatLastUpdate = () => {
      if (!lastUpdate) return 'Unknown';
      const date = new Date(lastUpdate);
      const now = new Date();
      const diffMinutes = Math.floor((now - date) / (1000 * 60));
      
      if (diffMinutes < 1) return 'Just now';
      if (diffMinutes < 60) return `${diffMinutes}m ago`;
      if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}h ago`;
      return `${Math.floor(diffMinutes / 1440)}d ago`;
    };

    return (
      <View style={styles.statusContainer}>
        <Text style={[styles.statusText, { color: getStatusColor() }]}>
          {getStatusText()}
        </Text>
        <Text style={styles.lastUpdateText}>
          Last update: {formatLastUpdate()}
        </Text>
        <Text style={styles.sourceText}>
          Source: {dataSource}
        </Text>
      </View>
    );
  };

  return (
    <ScrollView 
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.header}>
        <Text style={styles.title}>Available Classes</Text>
        <Text style={styles.subtitle}>Choose from our upcoming yoga sessions</Text>
        {dataSource && (
          <Text style={styles.dataSource}>Data from: {dataSource}</Text>
        )}
      </View>
      
      {/* Data Status Indicator */}
      <DataStatusIndicator />
      
      {/* Notification Banner */}
      <NotificationBanner />
      
      <TextInput
        style={{
          borderWidth: 1,
          borderColor: '#e5e7eb',
          borderRadius: 8,
          padding: 10,
          marginHorizontal: 16,
          marginBottom: 12,
          fontSize: 16,
        }}
        placeholder="Search by class, teacher, or difficulty"
        value={searchQuery}
        onChangeText={setSearchQuery}
      />
      {/* Remove the filterRow with Pickers and replace with button groups */}
      <Text style={{ fontSize: 16, fontWeight: 'bold', marginLeft: 16, marginBottom: 4 }}>Day:</Text>
      <View style={{ flexDirection: 'row', flexWrap: 'wrap', marginBottom: 12, justifyContent: 'center' }}>
        {daysOfWeek.map(day => {
          const isSelected = selectedDays.includes(day);
          return (
            <TouchableOpacity
              key={day}
              onPress={() => {
                setSelectedDays(isSelected
                  ? selectedDays.filter(d => d !== day)
                  : [...selectedDays, day]
                );
              }}
              style={{
                backgroundColor: isSelected ? '#2563eb' : '#f3f4f6',
                paddingVertical: 8,
                paddingHorizontal: 14,
                borderRadius: 20,
                marginHorizontal: 4,
                marginVertical: 4,
                borderWidth: isSelected ? 2 : 1,
                borderColor: isSelected ? '#2563eb' : '#e5e7eb',
              }}
            >
              <Text style={{ color: isSelected ? '#fff' : '#222', fontWeight: 'bold' }}>{day.slice(0, 3)}</Text>
            </TouchableOpacity>
          );
        })}
      </View>
      <Text style={{ fontSize: 16, fontWeight: 'bold', marginLeft: 16, marginBottom: 4 }}>Time:</Text>
      <View style={{ flexDirection: 'row', justifyContent: 'center', marginBottom: 16 }}>
        {timesOfDay.map(time => (
          <TouchableOpacity
            key={time}
            onPress={() => setSelectedTime(selectedTime === time ? '' : time)}
            style={{
              backgroundColor: selectedTime === time ? '#2563eb' : '#f3f4f6',
              paddingVertical: 8,
              paddingHorizontal: 16,
              borderRadius: 20,
              marginHorizontal: 6,
              borderWidth: selectedTime === time ? 2 : 1,
              borderColor: selectedTime === time ? '#2563eb' : '#e5e7eb',
            }}
          >
            <Text style={{ color: selectedTime === time ? '#fff' : '#222', fontWeight: 'bold' }}>{time}</Text>
          </TouchableOpacity>
        ))}
      </View>
      {loading ? (
        <Text style={styles.loading}>Loading classes...</Text>
      ) : (
        <View style={styles.classList}>
          {filteredClasses.length === 0 ? (
            <Text style={styles.noClasses}>No classes found for the selected filters.</Text>
          ) : filteredClasses.map((yogaClass, idx) => {
            // Calculate day of week and formatted date from instance date
            const dayOfWeek = yogaClass.date && !isNaN(Date.parse(yogaClass.date))
              ? new Date(yogaClass.date).toLocaleDateString('en-US', { weekday: 'long' })
              : yogaClass.daysOfWeek || '';
            const dayOfWeekAbbr = getDayAbbr(dayOfWeek);
            const formattedDate = yogaClass.date && !isNaN(Date.parse(yogaClass.date))
              ? new Date(yogaClass.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
              : yogaClass.date || '';
            return (
              <View key={idx} style={styles.cardWrapper}>
                <Card style={styles.card}>
                  <CardHeader style={styles.cardHeader}>
                    <View style={styles.cardHeaderRow}>
                      <View>
                        <CardTitle style={styles.cardTitle}>{yogaClass.courseName}</CardTitle>
                        <Text style={styles.teacher}>with {yogaClass.instructor || yogaClass.teacher}</Text>
                      </View>
                      <View style={styles.badgeContainer}>
                        {yogaClass.difficulty && (
                          <Badge style={[styles.badge, styles.difficultyBadge, getDifficultyColor(yogaClass.difficulty)]}>
                            {yogaClass.difficulty}
                          </Badge>
                        )}
                      </View>
                    </View>
                  </CardHeader>
                  <CardContent style={styles.cardContent}>
                    <View style={styles.infoGridRow}>
                      <View style={styles.infoGridCol}>
                        <FontAwesome name="calendar" size={18} color="#888" />
                        <Text style={styles.infoText}>{dayOfWeekAbbr}</Text>
                      </View>
                      <View style={styles.infoGridCol}>
                        <FontAwesome name="clock-o" size={18} color="#888" />
                        <Text style={styles.infoText}>{yogaClass.startTime || yogaClass.time}</Text>
                      </View>
                    </View>
                    <View style={styles.infoGridRow}>
                      <View style={styles.infoGridCol}>
                        <FontAwesome name="users" size={18} color="#888" />
                        <Text style={styles.infoText}>
                          {yogaClass.availableSpots !== undefined 
                            ? `${yogaClass.availableSpots} available`
                            : `${yogaClass.capacity} spots`
                          }
                        </Text>
                      </View>
                      <View style={styles.infoGridCol}>
                        <FontAwesome name="dollar" size={18} color="#888" />
                        <Text style={styles.infoText}>£{yogaClass.price}</Text>
                      </View>
                    </View>
                    <Text style={styles.infoLabel}><Text style={styles.bold}>Date:</Text> {formattedDate}</Text>
                    <Text style={styles.infoLabel}><Text style={styles.bold}>Duration:</Text> {yogaClass.duration} minutes</Text>
                    {yogaClass.description && (
                      <Text style={styles.description}>{yogaClass.description}</Text>
                    )}
                    {yogaClass.comments && (
                      <Text style={styles.comments}>Note: {yogaClass.comments}</Text>
                    )}
                    <Button onPress={() => onAddToCart && onAddToCart(yogaClass)} style={styles.addButton} size="sm">
                      <FontAwesome name="plus" size={18} color="#fff" style={{ marginRight: 8 }} />
                      Add to Cart
                    </Button>
                  </CardContent>
                </Card>
              </View>
            );
          })}
        </View>
      )}
      <NotificationBanner />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  header: { alignItems: 'center', marginVertical: 16 },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 4 },
  subtitle: { fontSize: 16, color: '#888', marginBottom: 4 },
  dataSource: { fontSize: 12, color: '#666', fontStyle: 'italic' },
  loading: { textAlign: 'center', marginTop: 20, fontSize: 16 },
  classList: { paddingHorizontal: 8 },
  noClasses: { textAlign: 'center', color: '#888', marginTop: 20 },
  cardWrapper: { marginBottom: 16 },
  card: {},
  cardHeader: {},
  cardHeaderRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  cardTitle: { fontSize: 18, fontWeight: 'bold' },
  teacher: { fontSize: 14, color: '#888' },
  badge: { alignSelf: 'flex-start', marginLeft: 4, marginBottom: 4 },
  badgeContainer: { alignItems: 'flex-end' },
  difficultyBadge: { marginTop: 4 },
  cardContent: { marginTop: 8 },
  infoGridRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
  infoGridCol: { flexDirection: 'row', alignItems: 'center', flex: 1, marginRight: 24 },
  infoText: { fontSize: 14, marginLeft: 10 },
  infoLabel: { fontSize: 14, marginTop: 8 },
  bold: { fontWeight: 'bold' },
  description: { fontSize: 14, color: '#555', marginTop: 8 },
  comments: { fontSize: 14, color: '#2563eb', backgroundColor: '#dbeafe', padding: 6, borderRadius: 6, marginTop: 8 },
  addButton: { marginTop: 12 },
  statusContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 10,
    backgroundColor: '#f0f0f0',
    borderRadius: 8,
    marginHorizontal: 16,
    marginBottom: 12,
  },
  statusText: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  errorText: {
    color: '#FF0000',
  },
  lastUpdateText: {
    fontSize: 12,
    color: '#666',
    marginLeft: 10,
  },
  sourceText: {
    fontSize: 12,
    color: '#666',
    marginLeft: 10,
  },
  notificationContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#4CAF50',
    padding: 12,
    borderRadius: 8,
    marginHorizontal: 16,
    marginBottom: 12,
  },
  notificationText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: 'bold',
    flex: 1,
  },
  notificationClose: {
    fontSize: 18,
    color: '#fff',
    fontWeight: 'bold',
  },
});