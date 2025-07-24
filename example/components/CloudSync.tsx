import React, { useState, useEffect } from 'react';
import { 
  Upload, Download, Wifi, WifiOff, Check, AlertCircle, RefreshCw, Cloud, 
  Database, Shield, Clock, BarChart3, Settings, History, PlayCircle,
  PauseCircle, XCircle, AlertTriangle, Server, Lock, GitMerge,
  ChevronRight, ChevronDown, Filter, MoreHorizontal, Trash2
} from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Alert, AlertDescription } from './ui/alert';
import { Progress } from './ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Switch } from './ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from './ui/collapsible';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from './ui/alert-dialog';
import { YogaClass, ClassInstance, SyncHistory, SyncError, SyncQueue, SyncStats } from '../types/yoga';

interface CloudSyncProps {
  classes: YogaClass[];
  instances: ClassInstance[];
  onSyncComplete?: () => void;
}

interface SyncStatus {
  isOnline: boolean;
  isAutoSyncEnabled: boolean;
  currentSync: SyncHistory | null;
  syncQueue: SyncQueue[];
  syncHistory: SyncHistory[];
  syncStats: SyncStats;
  lastError: SyncError | null;
  pendingChanges: number;
}

export function CloudSync({ classes, instances, onSyncComplete }: CloudSyncProps) {
  const [syncStatus, setSyncStatus] = useState<SyncStatus>({
    isOnline: navigator.onLine,
    isAutoSyncEnabled: localStorage.getItem('autoSyncEnabled') === 'true',
    currentSync: null,
    syncQueue: [],
    syncHistory: [],
    syncStats: {
      totalSyncs: 0,
      successfulSyncs: 0,
      failedSyncs: 0,
      averageSyncTime: 0,
      totalDataSynced: 0,
      syncFrequency: { daily: 0, weekly: 0, monthly: 0 }
    },
    lastError: null,
    pendingChanges: 0
  });

  const [activeTab, setActiveTab] = useState('overview');
  const [historyFilter, setHistoryFilter] = useState('all');
  const [expandedHistory, setExpandedHistory] = useState<string | null>(null);

  // Load sync data on mount
  useEffect(() => {
    const savedHistory = localStorage.getItem('syncHistory');
    const savedStats = localStorage.getItem('syncStats');
    const savedQueue = localStorage.getItem('syncQueue');
    
    if (savedHistory) {
      setSyncStatus(prev => ({ ...prev, syncHistory: JSON.parse(savedHistory) }));
    }
    if (savedStats) {
      setSyncStatus(prev => ({ ...prev, syncStats: JSON.parse(savedStats) }));
    }
    if (savedQueue) {
      setSyncStatus(prev => ({ ...prev, syncQueue: JSON.parse(savedQueue) }));
    }
  }, []);

  // Network status listeners
  useEffect(() => {
    const handleOnline = () => {
      setSyncStatus(prev => ({ ...prev, isOnline: true }));
      if (syncStatus.isAutoSyncEnabled && syncStatus.pendingChanges > 0) {
        handleAutoSync();
      }
    };
    
    const handleOffline = () => {
      setSyncStatus(prev => ({ ...prev, isOnline: false }));
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [syncStatus.isAutoSyncEnabled, syncStatus.pendingChanges]);

  // Save sync data to localStorage
  useEffect(() => {
    localStorage.setItem('syncHistory', JSON.stringify(syncStatus.syncHistory));
    localStorage.setItem('syncStats', JSON.stringify(syncStatus.syncStats));
    localStorage.setItem('syncQueue', JSON.stringify(syncStatus.syncQueue));
    localStorage.setItem('autoSyncEnabled', syncStatus.isAutoSyncEnabled.toString());
  }, [syncStatus.syncHistory, syncStatus.syncStats, syncStatus.syncQueue, syncStatus.isAutoSyncEnabled]);

  const createSyncRecord = (type: SyncHistory['type'], trigger: SyncHistory['trigger']): SyncHistory => {
    return {
      id: Date.now().toString(),
      timestamp: new Date().toISOString(),
      status: 'pending',
      type,
      trigger,
      recordsProcessed: { classes: 0, instances: 0, total: 0 },
      recordsUploaded: { classes: 0, instances: 0, total: 0 },
      recordsSkipped: { classes: 0, instances: 0, total: 0 },
      errors: [],
      retryCount: 0,
      dataSize: calculateDataSize(),
      networkSpeed: getNetworkSpeed(),
      deviceInfo: getDeviceInfo()
    };
  };

  const calculateDataSize = () => {
    const data = { classes, instances };
    return Math.round(JSON.stringify(data).length / 1024 * 100) / 100; // KB
  };

  const getNetworkSpeed = (): 'fast' | 'slow' | 'offline' => {
    if (!navigator.onLine) return 'offline';
    // Simulate network speed detection
    const connection = (navigator as any).connection;
    if (connection) {
      return connection.effectiveType === '4g' ? 'fast' : 'slow';
    }
    return 'fast';
  };

  const getDeviceInfo = () => {
    const battery = (navigator as any).getBattery ? 85 : undefined; // Simulated
    return {
      battery: battery || 0,
      storage: '2.3 GB free',
      connection: syncStatus.isOnline ? 'Wi-Fi' : 'Offline'
    };
  };

  const simulateCloudSync = async (syncRecord: SyncHistory) => {
    if (!syncStatus.isOnline) {
      throw new Error('No internet connection');
    }

    setSyncStatus(prev => ({ 
      ...prev, 
      currentSync: { ...syncRecord, status: 'in-progress' }
    }));

    const startTime = Date.now();
    let processedClasses = 0;
    let processedInstances = 0;
    const errors: SyncError[] = [];

    try {
      // Simulate upload progress with realistic steps
      const totalSteps = 8;
      for (let step = 1; step <= totalSteps; step++) {
        await new Promise(resolve => setTimeout(resolve, 200 + Math.random() * 300));
        
        switch (step) {
          case 1:
            // Validate data
            if (Math.random() < 0.1) {
              errors.push({
                id: Date.now().toString(),
                type: 'validation',
                message: 'Invalid date format in class instance',
                timestamp: new Date().toISOString(),
                resolved: false
              });
            }
            break;
          case 2:
          case 3:
            // Process classes
            processedClasses = classes.length;
            break;
          case 4:
          case 5:
          case 6:
            // Process instances
            processedInstances = instances.length;
            break;
          case 7:
            // Server processing
            if (Math.random() < 0.05) {
              throw new Error('Server temporarily unavailable');
            }
            break;
          case 8:
            // Finalize
            break;
        }

        const progress = (step / totalSteps) * 100;
        setSyncStatus(prev => ({
          ...prev,
          currentSync: prev.currentSync ? {
            ...prev.currentSync,
            recordsProcessed: {
              classes: Math.min(processedClasses, classes.length),
              instances: Math.min(processedInstances, instances.length),
              total: Math.min(processedClasses + processedInstances, classes.length + instances.length)
            }
          } : null
        }));
      }

      const duration = Date.now() - startTime;
      const completedSync: SyncHistory = {
        ...syncRecord,
        status: errors.length > 0 ? 'failed' : 'completed',
        duration,
        recordsProcessed: {
          classes: processedClasses,
          instances: processedInstances,
          total: processedClasses + processedInstances
        },
        recordsUploaded: {
          classes: errors.length > 0 ? Math.max(0, processedClasses - 1) : processedClasses,
          instances: errors.length > 0 ? Math.max(0, processedInstances - 1) : processedInstances,
          total: errors.length > 0 ? Math.max(0, (processedClasses + processedInstances) - 1) : (processedClasses + processedInstances)
        },
        recordsSkipped: {
          classes: errors.length > 0 ? 1 : 0,
          instances: 0,
          total: errors.length > 0 ? 1 : 0
        },
        errors
      };

      // Update sync history and stats
      setSyncStatus(prev => ({
        ...prev,
        currentSync: null,
        syncHistory: [completedSync, ...prev.syncHistory.slice(0, 49)],
        syncStats: {
          ...prev.syncStats,
          totalSyncs: prev.syncStats.totalSyncs + 1,
          successfulSyncs: prev.syncStats.successfulSyncs + (errors.length === 0 ? 1 : 0),
          failedSyncs: prev.syncStats.failedSyncs + (errors.length > 0 ? 1 : 0),
          averageSyncTime: Math.round(((prev.syncStats.averageSyncTime * prev.syncStats.totalSyncs) + duration) / (prev.syncStats.totalSyncs + 1)),
          totalDataSynced: prev.syncStats.totalDataSynced + syncRecord.dataSize
        },
        lastError: errors.length > 0 ? errors[0] : null,
        pendingChanges: Math.max(0, prev.pendingChanges - completedSync.recordsUploaded.total)
      }));

      if (errors.length === 0) {
        onSyncComplete?.();
      }

      return completedSync;

    } catch (error) {
      const duration = Date.now() - startTime;
      const failedSync: SyncHistory = {
        ...syncRecord,
        status: 'failed',
        duration,
        errors: [{
          id: Date.now().toString(),
          type: 'network',
          message: error instanceof Error ? error.message : 'Unknown error',
          timestamp: new Date().toISOString(),
          resolved: false
        }]
      };

      setSyncStatus(prev => ({
        ...prev,
        currentSync: null,
        syncHistory: [failedSync, ...prev.syncHistory.slice(0, 49)],
        syncStats: {
          ...prev.syncStats,
          totalSyncs: prev.syncStats.totalSyncs + 1,
          failedSyncs: prev.syncStats.failedSyncs + 1
        },
        lastError: failedSync.errors[0]
      }));

      throw error;
    }
  };

  const handleManualSync = async () => {
    const syncRecord = createSyncRecord('manual', 'user');
    try {
      await simulateCloudSync(syncRecord);
    } catch (error) {
      // Error already handled in simulateCloudSync
    }
  };

  const handleAutoSync = async () => {
    if (!syncStatus.isAutoSyncEnabled || !syncStatus.isOnline) return;
    
    const syncRecord = createSyncRecord('auto', 'schedule');
    try {
      await simulateCloudSync(syncRecord);
    } catch (error) {
      // Error already handled in simulateCloudSync
    }
  };

  const retrySync = async (historyId: string) => {
    const originalSync = syncStatus.syncHistory.find(h => h.id === historyId);
    if (!originalSync) return;

    const retryRecord = createSyncRecord(originalSync.type, 'retry');
    retryRecord.retryCount = originalSync.retryCount + 1;
    
    try {
      await simulateCloudSync(retryRecord);
    } catch (error) {
      // Error already handled in simulateCloudSync
    }
  };

  const cancelCurrentSync = () => {
    if (syncStatus.currentSync) {
      const cancelledSync: SyncHistory = {
        ...syncStatus.currentSync,
        status: 'cancelled',
        duration: Date.now() - new Date(syncStatus.currentSync.timestamp).getTime()
      };

      setSyncStatus(prev => ({
        ...prev,
        currentSync: null,
        syncHistory: [cancelledSync, ...prev.syncHistory.slice(0, 49)]
      }));
    }
  };

  const clearSyncHistory = () => {
    setSyncStatus(prev => ({
      ...prev,
      syncHistory: [],
      syncStats: {
        totalSyncs: 0,
        successfulSyncs: 0,
        failedSyncs: 0,
        averageSyncTime: 0,
        totalDataSynced: 0,
        syncFrequency: { daily: 0, weekly: 0, monthly: 0 }
      }
    }));
  };

  const getStatusIcon = (status: SyncHistory['status']) => {
    switch (status) {
      case 'completed':
        return <Check className="h-4 w-4 text-green-600" />;
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-600" />;
      case 'in-progress':
        return <RefreshCw className="h-4 w-4 text-blue-600 animate-spin" />;
      case 'pending':
        return <Clock className="h-4 w-4 text-yellow-600" />;
      case 'cancelled':
        return <PauseCircle className="h-4 w-4 text-gray-600" />;
    }
  };

  const getStatusColor = (status: SyncHistory['status']) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'failed':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'in-progress':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'cancelled':
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const formatDuration = (ms?: number) => {
    if (!ms) return 'N/A';
    const seconds = Math.round(ms / 1000);
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const filteredHistory = syncStatus.syncHistory.filter(sync => {
    if (historyFilter === 'all') return true;
    return sync.status === historyFilter;
  });

  const getTotalRecords = () => classes.length + instances.length;
  const getSyncProgress = () => {
    if (!syncStatus.currentSync) return 0;
    const { recordsProcessed } = syncStatus.currentSync;
    const total = getTotalRecords();
    return total > 0 ? (recordsProcessed.total / total) * 100 : 0;
  };

  return (
    <div className="space-y-4">
      {/* Mobile-optimized header */}
      <div className="text-center space-y-2">
        <h2 className="text-xl font-semibold">Cloud Sync</h2>
        <p className="text-sm text-muted-foreground">
          Keep your data synchronized across devices
        </p>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-3 h-12">
          <TabsTrigger value="overview" className="text-xs">Overview</TabsTrigger>
          <TabsTrigger value="history" className="text-xs">History</TabsTrigger>
          <TabsTrigger value="settings" className="text-xs">Settings</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4 mt-4">
          {/* Connection Status */}
          <Card className={`border-2 ${
            syncStatus.isOnline 
              ? 'border-green-200 bg-green-50/50' 
              : 'border-red-200 bg-red-50/50'
          }`}>
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                  syncStatus.isOnline ? 'bg-green-500' : 'bg-red-500'
                }`}>
                  {syncStatus.isOnline ? (
                    <Wifi className="h-5 w-5 text-white" />
                  ) : (
                    <WifiOff className="h-5 w-5 text-white" />
                  )}
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <h3 className="font-medium">Network Status</h3>
                    <Badge className={syncStatus.isOnline ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}>
                      {syncStatus.isOnline ? 'Online' : 'Offline'}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {syncStatus.isOnline 
                      ? 'Ready to sync' 
                      : 'Connect to internet to sync'
                    }
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Current Sync Progress */}
          {syncStatus.currentSync && (
            <Card>
              <CardContent className="p-4">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <RefreshCw className="h-4 w-4 animate-spin text-blue-600" />
                      <span className="font-medium">Syncing...</span>
                    </div>
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={cancelCurrentSync}
                      className="text-xs h-8"
                    >
                      Cancel
                    </Button>
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>Progress</span>
                      <span>{Math.round(getSyncProgress())}%</span>
                    </div>
                    <Progress value={getSyncProgress()} className="h-2" />
                  </div>

                  <div className="text-xs text-muted-foreground">
                    {syncStatus.currentSync.recordsProcessed.total} / {getTotalRecords()} records processed
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Quick Stats */}
          <div className="grid grid-cols-2 gap-3">
            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-2xl font-bold text-primary">{getTotalRecords()}</div>
                <div className="text-sm text-muted-foreground">Records</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {syncStatus.pendingChanges} pending
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-2xl font-bold text-primary">{syncStatus.syncStats.successfulSyncs}</div>
                <div className="text-sm text-muted-foreground">Successful</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {syncStatus.syncStats.failedSyncs} failed
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Last Error */}
          {syncStatus.lastError && (
            <Alert className="border-red-200 bg-red-50">
              <AlertTriangle className="h-4 w-4 text-red-600" />
              <AlertDescription className="text-red-700">
                <div className="font-medium">Last sync error:</div>
                <div className="text-sm">{syncStatus.lastError.message}</div>
              </AlertDescription>
            </Alert>
          )}

          {/* Sync Actions */}
          <div className="space-y-3">
            <Button
              onClick={handleManualSync}
              disabled={!syncStatus.isOnline || !!syncStatus.currentSync || getTotalRecords() === 0}
              className="w-full h-12 gap-2"
              size="lg"
            >
              {syncStatus.currentSync ? (
                <>
                  <RefreshCw className="h-5 w-5 animate-spin" />
                  Syncing...
                </>
              ) : (
                <>
                  <Upload className="h-5 w-5" />
                  Sync Now
                </>
              )}
            </Button>

            {/* Auto-sync toggle */}
            <div className="flex items-center justify-between p-3 bg-accent/50 rounded-lg">
              <div>
                <div className="font-medium text-sm">Auto Sync</div>
                <div className="text-xs text-muted-foreground">
                  Automatically sync when online
                </div>
              </div>
              <Switch
                checked={syncStatus.isAutoSyncEnabled}
                onCheckedChange={(checked) => 
                  setSyncStatus(prev => ({ ...prev, isAutoSyncEnabled: checked }))
                }
              />
            </div>
          </div>
        </TabsContent>

        <TabsContent value="history" className="space-y-4 mt-4">
          {/* History Filter */}
          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-muted-foreground" />
            <Select value={historyFilter} onValueChange={setHistoryFilter}>
              <SelectTrigger className="w-32 h-8 text-xs">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="failed">Failed</SelectItem>
                <SelectItem value="cancelled">Cancelled</SelectItem>
              </SelectContent>
            </Select>
            
            {syncStatus.syncHistory.length > 0 && (
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button variant="ghost" size="sm" className="ml-auto h-8 text-xs">
                    <Trash2 className="h-3 w-3 mr-1" />
                    Clear
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Clear Sync History</AlertDialogTitle>
                    <AlertDialogDescription>
                      Are you sure you want to clear all sync history? This action cannot be undone.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction onClick={clearSyncHistory}>
                      Clear History
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            )}
          </div>

          {/* History List */}
          {filteredHistory.length === 0 ? (
            <div className="text-center py-12">
              <History className="h-12 w-12 mx-auto text-muted-foreground mb-3" />
              <h3 className="font-medium mb-1">No Sync History</h3>
              <p className="text-sm text-muted-foreground">
                {historyFilter === 'all' 
                  ? 'Your sync history will appear here'
                  : `No ${historyFilter} syncs found`
                }
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {filteredHistory.map((sync) => (
                <Card key={sync.id}>
                  <Collapsible 
                    open={expandedHistory === sync.id}
                    onOpenChange={(open) => setExpandedHistory(open ? sync.id : null)}
                  >
                    <CollapsibleTrigger asChild>
                      <CardContent className="p-4 cursor-pointer hover:bg-accent/50 transition-colors">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            {getStatusIcon(sync.status)}
                            <div>
                              <div className="flex items-center gap-2">
                                <span className="font-medium text-sm capitalize">{sync.type} Sync</span>
                                <Badge className={`text-xs ${getStatusColor(sync.status)}`}>
                                  {sync.status}
                                </Badge>
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {formatTimestamp(sync.timestamp)}
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <div className="text-right">
                              <div className="text-sm font-medium">
                                {sync.recordsUploaded.total}/{sync.recordsProcessed.total}
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {formatDuration(sync.duration)}
                              </div>
                            </div>
                            <ChevronRight className={`h-4 w-4 transition-transform ${
                              expandedHistory === sync.id ? 'rotate-90' : ''
                            }`} />
                          </div>
                        </div>
                      </CardContent>
                    </CollapsibleTrigger>
                    
                    <CollapsibleContent>
                      <CardContent className="pt-0 pb-4 px-4">
                        <div className="border-t pt-3 space-y-3">
                          {/* Detailed Stats */}
                          <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                              <div className="text-muted-foreground">Trigger</div>
                              <div className="capitalize">{sync.trigger}</div>
                            </div>
                            <div>
                              <div className="text-muted-foreground">Data Size</div>
                              <div>{sync.dataSize} KB</div>
                            </div>
                            <div>
                              <div className="text-muted-foreground">Classes</div>
                              <div>{sync.recordsUploaded.classes}/{sync.recordsProcessed.classes}</div>
                            </div>
                            <div>
                              <div className="text-muted-foreground">Instances</div>
                              <div>{sync.recordsUploaded.instances}/{sync.recordsProcessed.instances}</div>
                            </div>
                          </div>

                          {/* Errors */}
                          {sync.errors.length > 0 && (
                            <div className="space-y-2">
                              <div className="text-sm font-medium text-red-600">Errors ({sync.errors.length})</div>
                              {sync.errors.map((error) => (
                                <div key={error.id} className="bg-red-50 border border-red-200 rounded-lg p-3">
                                  <div className="flex items-center gap-2 mb-1">
                                    <AlertCircle className="h-4 w-4 text-red-600" />
                                    <span className="text-sm font-medium capitalize">{error.type} Error</span>
                                  </div>
                                  <div className="text-sm text-red-700">{error.message}</div>
                                </div>
                              ))}
                            </div>
                          )}

                          {/* Retry Button */}
                          {sync.status === 'failed' && (
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => retrySync(sync.id)}
                              disabled={!syncStatus.isOnline || !!syncStatus.currentSync}
                              className="w-full gap-2"
                            >
                              <RefreshCw className="h-4 w-4" />
                              Retry Sync
                            </Button>
                          )}
                        </div>
                      </CardContent>
                    </CollapsibleContent>
                  </Collapsible>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="settings" className="space-y-4 mt-4">
          {/* Sync Preferences */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Sync Preferences</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium text-sm">Auto Sync</div>
                  <div className="text-xs text-muted-foreground">
                    Automatically sync when data changes
                  </div>
                </div>
                <Switch
                  checked={syncStatus.isAutoSyncEnabled}
                  onCheckedChange={(checked) => 
                    setSyncStatus(prev => ({ ...prev, isAutoSyncEnabled: checked }))
                  }
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium text-sm">Wi-Fi Only</div>
                  <div className="text-xs text-muted-foreground">
                    Only sync over Wi-Fi connections
                  </div>
                </div>
                <Switch defaultChecked />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="font-medium text-sm">Background Sync</div>
                  <div className="text-xs text-muted-foreground">
                    Continue syncing when app is in background
                  </div>
                </div>
                <Switch defaultChecked />
              </div>
            </CardContent>
          </Card>

          {/* Storage & Analytics */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Storage & Analytics</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Total Data Synced</span>
                  <span>{syncStatus.syncStats.totalDataSynced.toFixed(1)} KB</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Average Sync Time</span>
                  <span>{formatDuration(syncStatus.syncStats.averageSyncTime)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Success Rate</span>
                  <span>
                    {syncStatus.syncStats.totalSyncs > 0 
                      ? Math.round((syncStatus.syncStats.successfulSyncs / syncStatus.syncStats.totalSyncs) * 100)
                      : 0
                    }%
                  </span>
                </div>
              </div>

              <div className="pt-3 border-t">
                <Button 
                  variant="outline" 
                  size="sm" 
                  className="w-full"
                  onClick={clearSyncHistory}
                >
                  Clear All Data
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Device Info */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Device Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Storage Available</span>
                <span>2.3 GB</span>
              </div>
              <div className="flex justify-between">
                <span>Connection Type</span>
                <span>{syncStatus.isOnline ? 'Wi-Fi' : 'Offline'}</span>
              </div>
              <div className="flex justify-between">
                <span>Network Speed</span>
                <span className="capitalize">{getNetworkSpeed()}</span>
              </div>
              <div className="flex justify-between">
                <span>App Version</span>
                <span>1.0.0</span>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}