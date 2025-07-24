export interface YogaClass {
  id: string;
  dayOfWeek: string;
  time: string;
  capacity: number;
  duration: number;
  price: number;
  type: string;
  description?: string;
  location?: string;
  difficulty?: string;
  instructor?: string;
  lastModified?: string;
  cloudId?: string;
  syncStatus?: 'synced' | 'pending' | 'conflict' | 'error';
}

export interface ClassInstance {
  id: string;
  classId: string;
  date: string;
  teacher: string;
  comments?: string;
  attendees?: number;
  status?: 'scheduled' | 'completed' | 'cancelled';
  lastModified?: string;
  cloudId?: string;
  syncStatus?: 'synced' | 'pending' | 'conflict' | 'error';
}

export interface Activity {
  id: string;
  type: string;
  description: string;
  timestamp: string;
  relatedId?: string;
}

export interface SyncHistory {
  id: string;
  timestamp: string;
  status: 'pending' | 'in-progress' | 'completed' | 'failed' | 'cancelled';
  type: 'full' | 'incremental' | 'manual' | 'auto';
  trigger: 'user' | 'schedule' | 'data-change' | 'retry';
  duration?: number;
  recordsProcessed: {
    classes: number;
    instances: number;
    total: number;
  };
  recordsUploaded: {
    classes: number;
    instances: number;
    total: number;
  };
  recordsSkipped: {
    classes: number;
    instances: number;
    total: number;
  };
  errors: SyncError[];
  retryCount: number;
  dataSize: number; // in KB
  networkSpeed?: 'fast' | 'slow' | 'offline';
  deviceInfo?: {
    battery: number;
    storage: string;
    connection: string;
  };
}

export interface SyncError {
  id: string;
  type: 'network' | 'validation' | 'server' | 'auth' | 'quota' | 'conflict';
  message: string;
  code?: string;
  recordId?: string;
  recordType?: 'class' | 'instance';
  timestamp: string;
  resolved: boolean;
}

export interface SyncQueue {
  id: string;
  action: 'create' | 'update' | 'delete';
  recordType: 'class' | 'instance';
  recordId: string;
  data: any;
  timestamp: string;
  priority: 'high' | 'medium' | 'low';
  retryCount: number;
  status: 'pending' | 'processing' | 'failed' | 'completed';
}

export interface SyncStats {
  totalSyncs: number;
  successfulSyncs: number;
  failedSyncs: number;
  lastSuccessfulSync?: string;
  averageSyncTime: number;
  totalDataSynced: number; // in KB
  syncFrequency: {
    daily: number;
    weekly: number;
    monthly: number;
  };
}

export const DAYS_OF_WEEK = [
  'Monday',
  'Tuesday', 
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday'
];

export const CLASS_TYPES = [
  'Flow Yoga',
  'Aerial Yoga', 
  'Family Yoga',
  'Hot Yoga',
  'Restorative Yoga',
  'Vinyasa Yoga',
  'Hatha Yoga',
  'Yin Yoga'
];

export const DIFFICULTY_LEVELS = [
  'Beginner',
  'Intermediate', 
  'Advanced',
  'All Levels'
];

export const ACTIVITY_TYPES = {
  class_created: { label: 'Class Created', color: 'text-green-600' },
  class_updated: { label: 'Class Updated', color: 'text-blue-600' },
  class_deleted: { label: 'Class Deleted', color: 'text-red-600' },
  instance_created: { label: 'Instance Scheduled', color: 'text-purple-600' },
  instance_updated: { label: 'Instance Updated', color: 'text-orange-600' },
  instance_deleted: { label: 'Instance Cancelled', color: 'text-red-600' },
  data_synced: { label: 'Data Synced', color: 'text-cyan-600' },
  sync_failed: { label: 'Sync Failed', color: 'text-red-600' },
  sync_started: { label: 'Sync Started', color: 'text-blue-600' }
};

export const SYNC_ERROR_TYPES = {
  network: { label: 'Network Error', color: 'text-red-600', icon: 'wifi-off' },
  validation: { label: 'Validation Error', color: 'text-yellow-600', icon: 'alert-triangle' },
  server: { label: 'Server Error', color: 'text-red-600', icon: 'server' },
  auth: { label: 'Authentication Error', color: 'text-orange-600', icon: 'lock' },
  quota: { label: 'Quota Exceeded', color: 'text-purple-600', icon: 'database' },
  conflict: { label: 'Data Conflict', color: 'text-blue-600', icon: 'git-merge' }
};