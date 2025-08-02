#!/bin/bash

echo "🧹 YOGA COURSE DATABASE CLEARING UTILITY"
echo "========================================"
echo ""

echo "This script will help you clear all databases in your Yoga Course project."
echo ""

# Function to clear Android app data
clear_android_data() {
    echo "📱 Clearing Android App Data..."
    
    # Check if adb is available
    if command -v adb &> /dev/null; then
        echo "Found ADB, clearing app data..."
        adb shell pm clear com.universalyoga.adminapp
        echo "✅ Android app data cleared"
    else
        echo "⚠️  ADB not found. Please manually clear app data:"
        echo "   - Go to Android Settings > Apps > Yoga Course Admin"
        echo "   - Tap 'Storage' > 'Clear Data'"
    fi
}

# Function to clear React Native app data
clear_react_native_data() {
    echo "📱 Clearing React Native App Data..."
    
    cd YogaBookingApp
    
    # Check if node_modules exists
    if [ -d "node_modules" ]; then
        echo "Clearing React Native local storage..."
        
        # Create a temporary script to clear storage
        cat > temp_clear_storage.js << 'EOF'
import { clearAllLocalStorage } from './clearStorage.js';

clearAllLocalStorage()
  .then(result => {
    console.log('Result:', result);
    process.exit(0);
  })
  .catch(error => {
    console.error('Error:', error);
    process.exit(1);
  });
EOF

        # Run the script
        npx expo run:ios --clear-cache 2>/dev/null || echo "⚠️  Could not clear iOS cache"
        npx expo run:android --clear-cache 2>/dev/null || echo "⚠️  Could not clear Android cache"
        
        # Remove temporary file
        rm temp_clear_storage.js
        
        echo "✅ React Native app cache cleared"
    else
        echo "⚠️  React Native app not set up. Please run 'npm install' in YogaBookingApp directory first."
    fi
    
    cd ..
}

# Function to provide Firebase clearing instructions
clear_firebase_data() {
    echo "🔥 Firebase Data Clearing Instructions:"
    echo ""
    echo "1. Go to Firebase Console: https://console.firebase.google.com"
    echo "2. Select your project"
    echo "3. For Realtime Database:"
    echo "   - Go to Realtime Database"
    echo "   - Delete the 'customer_bookings' node"
    echo "4. For Firestore:"
    echo "   - Go to Firestore Database"
    echo "   - Delete the 'bookings' collection"
    echo ""
    echo "⚠️  WARNING: This will permanently delete all Firebase data!"
    echo ""
}

# Function to clear local SQLite database
clear_sqlite_data() {
    echo "🗄️  Local SQLite Database:"
    echo "The Android app has a built-in database reset feature."
    echo "1. Open the Yoga Course Admin app"
    echo "2. Go to Settings > Database Management"
    echo "3. Tap 'Reset All Data'"
    echo "4. Confirm the action"
    echo ""
}

# Main menu
while true; do
    echo "Choose an option:"
    echo "1) Clear Android App Data"
    echo "2) Clear React Native App Data"
    echo "3) Show Firebase Clearing Instructions"
    echo "4) Show SQLite Database Clearing Instructions"
    echo "5) Clear All (Android + React Native)"
    echo "6) Exit"
    echo ""
    read -p "Enter your choice (1-6): " choice
    
    case $choice in
        1)
            clear_android_data
            ;;
        2)
            clear_react_native_data
            ;;
        3)
            clear_firebase_data
            ;;
        4)
            clear_sqlite_data
            ;;
        5)
            echo "🧹 Clearing all local app data..."
            clear_android_data
            clear_react_native_data
            echo ""
            clear_firebase_data
            clear_sqlite_data
            ;;
        6)
            echo "Goodbye! 👋"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please enter 1-6."
            ;;
    esac
    
    echo ""
    echo "Press Enter to continue..."
    read
    echo ""
done 