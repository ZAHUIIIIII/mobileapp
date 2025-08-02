import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { MaterialIcons, FontAwesome, Ionicons } from '@expo/vector-icons';

const ShoppingCart = ({ items = [], onRemoveItem, onUpdateQuantity, onSubmitBooking }) => {
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [lastBooking, setLastBooking] = useState(null);
  const [emailError, setEmailError] = useState('');

  // Debug logging
  console.log('ShoppingCart props:', { 
    items: items, 
    itemsType: typeof items, 
    itemsIsArray: Array.isArray(items),
    itemsLength: items?.length 
  });

  // Debug lastBooking
  console.log('ShoppingCart lastBooking:', { 
    lastBooking: lastBooking, 
    lastBookingType: typeof lastBooking, 
    lastBookingIsArray: Array.isArray(lastBooking),
    lastBookingLength: lastBooking?.length,
    lastBookingId: lastBooking?.id
  });

  const getTotalPrice = () => {
    if (!items || !Array.isArray(items)) return 0;
    return items.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const getTotalItems = () => {
    if (!items || !Array.isArray(items)) return 0;
    return items.reduce((total, item) => total + item.quantity, 0);
  };

  const validateGmail = (email) => {
    if (!email || typeof email !== 'string') return false;
    const gmailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    return gmailRegex.test(email.trim());
  };

  const handleEmailChange = (text) => {
    setEmail(text);
    setEmailError('');
    
    if (text && text.trim() && !validateGmail(text.trim())) {
      setEmailError('Please enter a valid Gmail address');
    }
  };

  const handleQuantityChange = (id, newQuantity) => {
    if (newQuantity <= 0) {
      onRemoveItem(id);
    } else {
      onUpdateQuantity(id, newQuantity);
    }
  };

  const handleSubmitBooking = async () => {
    if (!email.trim()) {
      Alert.alert('Email Required', 'Please enter your Gmail address to continue.');
      return;
    }

    if (!validateGmail(email.trim())) {
      Alert.alert('Invalid Email', 'Please enter a valid Gmail address (e.g., yourname@gmail.com).');
      return;
    }

    if (!items || !Array.isArray(items) || items.length === 0) {
      Alert.alert('Empty Cart', 'Please add some classes to your cart first.');
      return;
    }

    setIsSubmitting(true);

    try {
      // Quick response for better UX
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const booking = await onSubmitBooking(email);
      setLastBooking(booking);
      setEmail('');
      setEmailError('');
      
      Alert.alert('Success', 'Booking confirmed! Check your Gmail for details.');
    } catch (error) {
      Alert.alert('Error', 'Failed to submit booking. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'beginner':
        return { backgroundColor: '#dcfce7', color: '#16a34a' };
      case 'intermediate':
        return { backgroundColor: '#fef3c7', color: '#d97706' };
      case 'advanced':
        return { backgroundColor: '#fce7f3', color: '#be185d' };
      case 'expert':
        return { backgroundColor: '#fef2f2', color: '#dc2626' };
      default:
        return { backgroundColor: '#f3f4f6', color: '#374151' };
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short'
    });
  };

  const renderCartItem = ({ item }) => {
    const difficultyColors = getDifficultyColor(item.difficulty);
    
    return (
      <View style={styles.cartItem}>
        <View style={styles.itemInfo}>
          <View style={styles.itemHeader}>
            <Text style={styles.className}>{item.courseName || item.name}</Text>
            {item.difficulty && (
              <View style={[styles.difficultyBadge, { backgroundColor: difficultyColors.backgroundColor }]}>
                <Text style={[styles.difficultyText, { color: difficultyColors.color }]}>{item.difficulty}</Text>
              </View>
            )}
          </View>
          
          <Text style={styles.instructor}>with {item.instructor || item.teacher}</Text>
          
          <View style={styles.itemDetails}>
            <View style={styles.detailRow}>
              <FontAwesome name="calendar" size={14} color="#6b7280" />
              <Text style={styles.detailText}>
                {item.date && !isNaN(Date.parse(item.date))
                  ? new Date(item.date).toLocaleDateString('en-US', { weekday: 'long' })
                  : item.daysOfWeek || 'TBD'
                } at {item.startTime || item.time}
              </Text>
            </View>
            
            <View style={styles.detailRow}>
              <FontAwesome name="clock-o" size={14} color="#6b7280" />
              <Text style={styles.detailText}>Date: {formatDate(item.date)}</Text>
            </View>
            
            <View style={styles.detailRow}>
              <FontAwesome name="hourglass-half" size={14} color="#6b7280" />
              <Text style={styles.detailText}>Duration: {item.duration} min</Text>
            </View>
            
            <View style={styles.detailRow}>
              <FontAwesome name="dollar" size={14} color="#6b7280" />
              <Text style={styles.detailText}>Price: £{item.price}</Text>
            </View>
          </View>
        </View>
        
        <View style={styles.itemActions}>
          <View style={styles.quantityControls}>
            <TouchableOpacity
              style={[styles.quantityButton, item.quantity <= 1 && styles.quantityButtonDisabled]}
              onPress={() => handleQuantityChange(item.id, item.quantity - 1)}
              disabled={item.quantity <= 1}
            >
              <MaterialIcons name="remove" size={16} color={item.quantity <= 1 ? "#9ca3af" : "#374151"} />
            </TouchableOpacity>
            
            <Text style={styles.quantity}>{item.quantity}</Text>
            
            <TouchableOpacity
              style={styles.quantityButton}
              onPress={() => handleQuantityChange(item.id, item.quantity + 1)}
            >
              <MaterialIcons name="add" size={16} color="#374151" />
            </TouchableOpacity>
          </View>
          
          <View style={styles.itemPrice}>
            <Text style={styles.itemPriceText}>£{(item.price * item.quantity).toFixed(2)}</Text>
          </View>
          
          <TouchableOpacity
            style={styles.removeButton}
            onPress={() => onRemoveItem(item.id)}
          >
            <MaterialIcons name="delete-outline" size={20} color="#ef4444" />
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  // Show success state if there's a lastBooking, regardless of cart state
  if (lastBooking && (lastBooking.id || (Array.isArray(lastBooking) && lastBooking.length > 0))) {
    return (
      <ScrollView style={styles.container}>
        <View style={styles.successContainer}>
          <View style={styles.successContent}>
            <View style={styles.successIcon}>
              <MaterialIcons name="check-circle" size={64} color="#10b981" />
            </View>
            <Text style={styles.successTitle}>Booking Confirmed!</Text>
            <Text style={styles.successSubtitle}>Your classes have been booked successfully</Text>
            
            <View style={styles.bookingDetails}>
              <View style={styles.bookingDetailRow}>
                <Text style={styles.bookingLabel}>Booking ID:</Text>
                <Text style={styles.bookingValue}>
                  {Array.isArray(lastBooking) && lastBooking.length > 0 
                    ? lastBooking[0]?.id || 'N/A'
                    : lastBooking?.id || 'N/A'
                  }
                </Text>
              </View>
              <View style={styles.bookingDetailRow}>
                <Text style={styles.bookingLabel}>Email:</Text>
                <Text style={styles.bookingValue}>
                  {Array.isArray(lastBooking) && lastBooking.length > 0 
                    ? lastBooking[0]?.email || 'N/A'
                    : lastBooking?.email || 'N/A'
                  }
                </Text>
              </View>
              <View style={styles.bookingDetailRow}>
                <Text style={styles.bookingLabel}>Total Classes:</Text>
                <Text style={styles.bookingValue}>
                  {Array.isArray(lastBooking) 
                    ? lastBooking.length 
                    : lastBooking?.classes?.length || 0
                  }
                </Text>
              </View>
              <View style={styles.bookingDetailRow}>
                <Text style={styles.bookingLabel}>Total Amount:</Text>
                <Text style={styles.bookingValue}>
                  £{Array.isArray(lastBooking) 
                    ? lastBooking.reduce((sum, booking) => sum + (booking?.totalAmount || 0), 0).toFixed(2)
                    : (lastBooking?.totalAmount || 0).toFixed(2)
                  }
                </Text>
              </View>
              <View style={styles.bookingDetailRow}>
                <Text style={styles.bookingLabel}>Booked on:</Text>
                <Text style={styles.bookingValue}>
                  {Array.isArray(lastBooking) && lastBooking.length > 0
                    ? (lastBooking[0]?.bookingDate ? new Date(lastBooking[0].bookingDate).toLocaleDateString('en-GB') : 'N/A')
                    : (lastBooking?.bookingDate ? new Date(lastBooking.bookingDate).toLocaleDateString('en-GB') : 'N/A')
                  }
                </Text>
              </View>
            </View>
            
            <View style={styles.emailAlert}>
              <MaterialIcons name="email" size={16} color="#6b7280" />
              <Text style={styles.emailAlertText}>
                A confirmation email has been sent to {
                  Array.isArray(lastBooking) && lastBooking.length > 0 
                    ? lastBooking[0]?.email || 'your email'
                    : lastBooking?.email || 'your email'
                }
              </Text>
            </View>
            
            <TouchableOpacity
              style={styles.continueButton}
              onPress={() => {
                setLastBooking(null);
                setEmail('');
                setEmailError('');
              }}
            >
              <Text style={styles.continueButtonText}>Continue Browsing</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    );
  }

  // Show empty cart state if no items and no lastBooking
  if (!items || !Array.isArray(items) || items.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <View style={styles.emptyContent}>
          <View style={styles.emptyIcon}>
            <MaterialIcons name="shopping-cart" size={64} color="#d1d5db" />
          </View>
          <Text style={styles.emptyTitle}>Your cart is empty</Text>
          <Text style={styles.emptySubtitle}>Browse classes to get started</Text>
        </View>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView 
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Shopping Cart</Text>
        <Text style={styles.headerSubtitle}>
          {getTotalItems()} {getTotalItems() === 1 ? 'class' : 'classes'} • £{getTotalPrice().toFixed(2)}
        </Text>
      </View>

      <FlatList
        data={items}
        renderItem={renderCartItem}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
      />
      
      <View style={styles.footer}>
        <View style={styles.bookingForm}>
          <Text style={styles.formTitle}>Complete Your Booking</Text>
          
          <View style={styles.emailInputContainer}>
            <View style={styles.inputLabelContainer}>
              <Text style={styles.inputLabel}>Gmail Address</Text>
              <View style={styles.gmailIndicator}>
                <Ionicons name="mail" size={16} color="#4285f4" />
                <Text style={styles.gmailText}>Gmail Only</Text>
              </View>
            </View>
            <TextInput
              style={[
                styles.emailInput,
                emailError ? styles.emailInputError : null
              ]}
              value={email}
              onChangeText={handleEmailChange}
              placeholder="yourname@gmail.com"
              placeholderTextColor="#9ca3af"
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isSubmitting}
            />
            <Text style={styles.inputHelpText}>
              Booking confirmation will be sent to your Gmail
            </Text>
            {emailError ? (
              <Text style={styles.errorText}>{emailError}</Text>
            ) : null}
          </View>

          <View style={styles.summaryContainer}>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Total Classes:</Text>
              <Text style={styles.summaryValue}>{getTotalItems()}</Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Total Amount:</Text>
              <Text style={styles.summaryValue}>£{getTotalPrice().toFixed(2)}</Text>
            </View>
          </View>



          <TouchableOpacity
            style={[
              styles.bookButton,
              (!email.trim() || isSubmitting || !!emailError) && styles.bookButtonDisabled
            ]}
            onPress={handleSubmitBooking}
            disabled={!email.trim() || isSubmitting || !!emailError}
          >
            <Text style={styles.bookButtonText}>
              {isSubmitting 
                ? 'Processing...' 
                : `Book ${getTotalItems()} ${getTotalItems() === 1 ? 'Class' : 'Classes'} - £${getTotalPrice().toFixed(2)}`
              }
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  header: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
    textAlign: 'center',
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
  },
  listContainer: {
    padding: 16,
  },
  cartItem: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  itemInfo: {
    flex: 1,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  className: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
    flex: 1,
    marginRight: 8,
  },
  difficultyBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  difficultyText: {
    fontSize: 12,
    fontWeight: '600',
  },
  instructor: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 12,
  },
  itemDetails: {
    marginBottom: 12,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  detailText: {
    fontSize: 14,
    color: '#374151',
    marginLeft: 8,
  },
  itemActions: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  quantityControls: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    padding: 4,
  },
  quantityButton: {
    width: 28,
    height: 28,
    borderRadius: 6,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 1,
    elevation: 1,
  },
  quantityButtonDisabled: {
    backgroundColor: '#f9fafb',
  },
  quantity: {
    fontSize: 16,
    fontWeight: 'bold',
    marginHorizontal: 12,
    minWidth: 20,
    textAlign: 'center',
    color: '#111827',
  },
  itemPrice: {
    alignItems: 'center',
  },
  itemPriceText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#2563eb',
  },
  removeButton: {
    padding: 8,
    borderRadius: 8,
    backgroundColor: '#fef2f2',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  emptyContent: {
    alignItems: 'center',
  },
  emptyIcon: {
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#6b7280',
    marginBottom: 8,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: 16,
    color: '#9ca3af',
    textAlign: 'center',
  },
  successContainer: {
    flex: 1,
    padding: 20,
  },
  successContent: {
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  successIcon: {
    marginBottom: 16,
  },
  successTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 8,
    textAlign: 'center',
  },
  successSubtitle: {
    fontSize: 16,
    color: '#6b7280',
    marginBottom: 24,
    textAlign: 'center',
  },
  bookingDetails: {
    width: '100%',
    backgroundColor: '#f0fdf4',
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  bookingDetailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  bookingLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  bookingValue: {
    fontSize: 14,
    color: '#111827',
  },
  emailAlert: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    padding: 12,
    marginBottom: 20,
  },
  emailAlertText: {
    fontSize: 14,
    color: '#6b7280',
    marginLeft: 8,
    flex: 1,
  },
  continueButton: {
    backgroundColor: '#2563eb',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 24,
    width: '100%',
  },
  continueButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  footer: {
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
  },
  bookingForm: {
    padding: 20,
  },
  formTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 16,
  },
  emailInputContainer: {
    marginBottom: 16,
  },
  inputLabelContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  gmailIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#e8f0fe',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  gmailText: {
    fontSize: 12,
    color: '#4285f4',
    fontWeight: '500',
    marginLeft: 4,
  },
  emailInput: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#fff',
    color: '#111827',
  },
  emailInputError: {
    borderColor: '#ef4444',
    backgroundColor: '#fef2f2',
  },
  inputHelpText: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 4,
  },
  errorText: {
    fontSize: 12,
    color: '#ef4444',
    marginTop: 4,
  },
  summaryContainer: {
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  summaryLabel: {
    fontSize: 14,
    color: '#374151',
  },
  summaryValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
  },
  bookButton: {
    backgroundColor: '#2563eb',
    borderRadius: 8,
    paddingVertical: 16,
    alignItems: 'center',
  },
  bookButtonDisabled: {
    backgroundColor: '#9ca3af',
  },
  bookButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  difficultyBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  difficultyText: {
    fontSize: 12,
    fontWeight: '600',
  },
});

export default ShoppingCart;
