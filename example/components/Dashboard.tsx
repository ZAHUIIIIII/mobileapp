import React from 'react';
import { Plus, Calendar, Users, Clock, Activity as ActivityIcon, BookOpen, TrendingUp, MapPin } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { YogaClass, ClassInstance, Activity, ACTIVITY_TYPES } from '../types/yoga';

interface DashboardProps {
  classes: YogaClass[];
  instances: ClassInstance[];
  activities: Activity[];
  onNavigate: (tab: string, classData?: YogaClass) => void;
}

export function Dashboard({ classes, instances, activities, onNavigate }: DashboardProps) {
  const today = new Date();
  const todayStr = today.toISOString().split('T')[0];
  const weekStart = new Date(today);
  weekStart.setDate(today.getDate() - today.getDay());
  
  // Calculate stats
  const todaysInstances = instances.filter(inst => inst.date === todayStr);
  const thisWeekInstances = instances.filter(inst => {
    const instDate = new Date(inst.date);
    return instDate >= weekStart && instDate <= today;
  });
  const upcomingInstances = instances
    .filter(inst => new Date(inst.date) > today)
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
    .slice(0, 5);

  const totalCapacity = classes.reduce((sum, cls) => sum + cls.capacity, 0);
  const averagePrice = classes.length > 0 
    ? classes.reduce((sum, cls) => sum + cls.price, 0) / classes.length 
    : 0;

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const isToday = date.toDateString() === today.toDateString();
    const isTomorrow = date.toDateString() === new Date(today.getTime() + 86400000).toDateString();
    
    if (isToday) return 'Today';
    if (isTomorrow) return 'Tomorrow';
    
    return date.toLocaleDateString('en-GB', {
      weekday: 'short',
      day: 'numeric',
      month: 'short'
    });
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    
    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short'
    });
  };

  const quickActions = [
    {
      title: 'Add Class',
      description: 'Create a new yoga class',
      icon: Plus,
      action: () => onNavigate('classes'),
      color: 'bg-green-500'
    },
    {
      title: 'Schedule Instance',
      description: 'Add a class instance',
      icon: Calendar,
      action: () => onNavigate('instances'),
      color: 'bg-blue-500'
    },
    {
      title: 'View All Classes',
      description: 'Manage your classes',
      icon: BookOpen,
      action: () => onNavigate('classes'),
      color: 'bg-purple-500'
    }
  ];

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="text-center space-y-2">
        <h2 className="text-2xl font-bold">Welcome Back!</h2>
        <p className="text-muted-foreground">
          Here's what's happening with your yoga studio
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-4">
        <Card className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center">
                <BookOpen className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-blue-700">{classes.length}</p>
                <p className="text-sm text-blue-600">Total Classes</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-green-50 to-green-100 border-green-200">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-500 rounded-lg flex items-center justify-center">
                <Calendar className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-green-700">{instances.length}</p>
                <p className="text-sm text-green-600">Instances</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-purple-50 to-purple-100 border-purple-200">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-purple-500 rounded-lg flex items-center justify-center">
                <Users className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-purple-700">{totalCapacity}</p>
                <p className="text-sm text-purple-600">Total Capacity</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-gradient-to-br from-orange-50 to-orange-100 border-orange-200">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-orange-500 rounded-lg flex items-center justify-center">
                <TrendingUp className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-2xl font-bold text-orange-700">£{averagePrice.toFixed(0)}</p>
                <p className="text-sm text-orange-600">Avg. Price</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Today's Classes */}
      {todaysInstances.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Today's Classes
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {todaysInstances.map(instance => {
              const yogaClass = classes.find(cls => cls.id === instance.classId);
              return (
                <div key={instance.id} className="flex items-center justify-between p-3 bg-accent/50 rounded-lg">
                  <div>
                    <p className="font-medium">{yogaClass?.type}</p>
                    <p className="text-sm text-muted-foreground">
                      {yogaClass?.time} • {instance.teacher}
                    </p>
                  </div>
                  <Badge className={
                    instance.status === 'completed' ? 'bg-green-100 text-green-800' :
                    instance.status === 'cancelled' ? 'bg-red-100 text-red-800' :
                    'bg-blue-100 text-blue-800'
                  }>
                    {instance.status || 'scheduled'}
                  </Badge>
                </div>
              );
            })}
          </CardContent>
        </Card>
      )}

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3">
            {quickActions.map((action, index) => {
              const Icon = action.icon;
              return (
                <Button
                  key={index}
                  variant="outline"
                  className="h-auto p-4 justify-start gap-4"
                  onClick={action.action}
                >
                  <div className={`w-10 h-10 ${action.color} rounded-lg flex items-center justify-center`}>
                    <Icon className="h-5 w-5 text-white" />
                  </div>
                  <div className="text-left">
                    <p className="font-medium">{action.title}</p>
                    <p className="text-sm text-muted-foreground">{action.description}</p>
                  </div>
                </Button>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Upcoming Classes */}
      {upcomingInstances.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Upcoming Classes
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {upcomingInstances.map(instance => {
              const yogaClass = classes.find(cls => cls.id === instance.classId);
              return (
                <div 
                  key={instance.id} 
                  className="flex items-center justify-between p-3 border rounded-lg cursor-pointer hover:bg-accent/50 transition-colors"
                  onClick={() => onNavigate('instances', yogaClass)}
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="font-medium">{yogaClass?.type}</p>
                      <Badge variant="outline" className="text-xs">
                        {formatDate(instance.date)}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span>{yogaClass?.time}</span>
                      <span>{instance.teacher}</span>
                      {yogaClass?.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="h-3 w-3" />
                          {yogaClass.location}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </CardContent>
        </Card>
      )}

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ActivityIcon className="h-5 w-5" />
            Recent Activity
          </CardTitle>
        </CardHeader>
        <CardContent>
          {activities.length === 0 ? (
            <div className="text-center py-8">
              <ActivityIcon className="h-12 w-12 mx-auto text-muted-foreground mb-2" />
              <p className="text-muted-foreground">No recent activity</p>
            </div>
          ) : (
            <div className="space-y-3">
              {activities.slice(0, 10).map(activity => {
                const activityType = ACTIVITY_TYPES[activity.type as keyof typeof ACTIVITY_TYPES];
                return (
                  <div key={activity.id} className="flex items-start gap-3 p-3 border rounded-lg">
                    <div className="w-2 h-2 rounded-full bg-primary mt-2 flex-shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <Badge variant="outline" className="text-xs">
                          {activityType?.label || activity.type}
                        </Badge>
                        <span className="text-xs text-muted-foreground">
                          {formatTime(activity.timestamp)}
                        </span>
                      </div>
                      <p className="text-sm">{activity.description}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}