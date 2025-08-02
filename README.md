# Yoga Course Management App

A comprehensive Android application for managing yoga classes, instances, and customer bookings. Built with modern Android development practices and Material Design 3.

## Features

- **Dashboard**: Overview of classes, instances, and recent activities
- **Course Management**: Add, edit, and delete yoga courses with detailed information
- **Instance Management**: Create and manage class instances with dates and teachers
- **Customer Bookings**: Track customer reservations and attendance
- **Cloud Sync**: Automatic synchronization with Firebase Realtime Database and Firestore
- **Database Management**: Comprehensive tools for data management and reset
- **Activity Logging**: Track all user actions for audit purposes

## Architecture

- **MVVM Pattern**: Using ViewModels and LiveData
- **Room Database**: Local SQLite database with Room ORM
- **Firebase Integration**: Cloud storage and synchronization
- **Material Design 3**: Modern UI/UX following Google's design guidelines
- **Repository Pattern**: Clean separation of data sources

## Technology Stack

- **Language**: Java 17
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Database**: Room (SQLite)
- **Cloud**: Firebase Realtime Database & Firestore
- **UI**: Material Design 3 Components
- **Architecture**: MVVM with Repository Pattern

## Setup

1. Clone the repository
2. Open in Android Studio
3. Configure Firebase:
   - Add `google-services.json` to the app directory
   - Enable Realtime Database and Firestore in Firebase Console
4. Build and run the application

## Default Login

- **Username**: admin
- **Password**: admin123

*Note: Replace with proper authentication system for production use.*

## Database Structure

- **Courses**: Yoga class definitions with schedules and details
- **Instances**: Individual class sessions with dates and teachers
- **Customer Bookings**: Customer reservations and attendance
- **Activity Log**: Audit trail of all user actions
- **Sync History**: Cloud synchronization records

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.
