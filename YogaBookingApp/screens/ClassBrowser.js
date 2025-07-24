import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput } from 'react-native';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import Button from '../components/ui/button';
import { Badge } from '@components/ui/badge';
import { FontAwesome } from '@expo/vector-icons';
import { fetchClasses } from '../services/firebase';
import { Picker } from '@react-native-picker/picker';

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
  const [selectedDays, setSelectedDays] = useState([]); // instead of selectedDay
  const [selectedTime, setSelectedTime] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    async function loadClasses() {
      setLoading(true);
      const data = await fetchClasses();
      // Flatten to a list of instance cards
      const classList = [];
      if (data && typeof data === 'object') {
        Object.values(data).forEach(item => {
          const course = item.courseInfo;
          if (item.instances) {
            Object.values(item.instances).forEach(instance => {
              classList.push({
                ...course,      // course fields (type, teacher, etc.)
                ...instance     // instance fields (date, startTime, etc.)
              });
            });
          }
        });
      }
      setClasses(classList);
      setLoading(false);
    }
    loadClasses();
  }, []);

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

    // Keyword search (case-insensitive, partial match)
    const keyword = searchQuery.trim().toLowerCase();
    const keywordMatch = !keyword ||
      (yogaClass.courseName && yogaClass.courseName.toLowerCase().includes(keyword)) ||
      (yogaClass.teacher && yogaClass.teacher.toLowerCase().includes(keyword)) ||
      (yogaClass.type && yogaClass.type.toLowerCase().includes(keyword));

    return dayMatch && timeMatch && keywordMatch;
  });

  const getTypeColor = (type) => {
    switch (type) {
      case 'Flow Yoga':
        return { backgroundColor: '#e0e7ff' };
      case 'Aerial Yoga':
        return { backgroundColor: '#ede9fe' };
      case 'Family Yoga':
        return { backgroundColor: '#dcfce7' };
      default:
        return { backgroundColor: '#f3f4f6' };
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Available Classes</Text>
        <Text style={styles.subtitle}>Choose from our upcoming yoga sessions</Text>
      </View>
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
        placeholder="Search by class, teacher, or type"
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
                        <Text style={styles.teacher}>with {yogaClass.teacher}</Text>
                      </View>
                      <Badge style={[styles.badge, getTypeColor(yogaClass.type)]}>
                        {yogaClass.type}
                      </Badge>
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
                        <Text style={styles.infoText}>{yogaClass.capacity} spots</Text>
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
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  header: { alignItems: 'center', marginVertical: 16 },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 4 },
  subtitle: { fontSize: 16, color: '#888' },
  filterRow: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 16 },
  pickerContainer: { flex: 1, alignItems: 'center' },
  filterLabel: { fontSize: 14, marginBottom: 4 },
  picker: { width: 120, height: 40 },
  loading: { textAlign: 'center', marginTop: 20, fontSize: 16 },
  classList: { paddingHorizontal: 8 },
  noClasses: { textAlign: 'center', color: '#888', marginTop: 20 },
  cardWrapper: { marginBottom: 16 },
  card: {},
  cardHeader: {},
  cardHeaderRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  cardTitle: { fontSize: 18, fontWeight: 'bold' },
  teacher: { fontSize: 14, color: '#888' },
  badge: { alignSelf: 'flex-start', marginLeft: 8 },
  cardContent: { marginTop: 8 },
  infoGridRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
  infoGridCol: { flexDirection: 'row', alignItems: 'center', flex: 1, marginRight: 24 },
  infoText: { fontSize: 14, marginLeft: 10 },
  infoLabel: { fontSize: 14, marginTop: 8 },
  bold: { fontWeight: 'bold' },
  description: { fontSize: 14, color: '#555', marginTop: 8 },
  comments: { fontSize: 14, color: '#2563eb', backgroundColor: '#dbeafe', padding: 6, borderRadius: 6, marginTop: 8 },
  addButton: { marginTop: 12 },
});