import React, { useState, useEffect, useMemo } from 'react';
import { ArrowLeft, Plus, Edit2, Trash2, Calendar, User, MessageSquare, Users, Search, Filter, X } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from './ui/collapsible';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from './ui/alert-dialog';
import { YogaClass, ClassInstance, DAYS_OF_WEEK } from '../types/yoga';

interface InstanceManagerProps {
  classes: YogaClass[];
  instances: ClassInstance[];
  selectedClass?: YogaClass | null;
  onAddInstance: (instance: Omit<ClassInstance, 'id'>) => void;
  onUpdateInstance: (instance: ClassInstance) => void;
  onDeleteInstance: (id: string) => void;
  onBack: () => void;
}

interface InstanceFormData {
  classId: string;
  date: string;
  teacher: string;
  comments: string;
  attendees: string;
  status: 'scheduled' | 'completed' | 'cancelled';
}

interface SearchFilters {
  searchText: string;
  classId: string;
  status: string;
  dateRange: string;
}

export function InstanceManager({ 
  classes, 
  instances, 
  selectedClass, 
  onAddInstance, 
  onUpdateInstance, 
  onDeleteInstance, 
  onBack 
}: InstanceManagerProps) {
  const [showForm, setShowForm] = useState(false);
  const [editingInstance, setEditingInstance] = useState<ClassInstance | null>(null);
  const [formData, setFormData] = useState<InstanceFormData>({
    classId: selectedClass?.id || '',
    date: '',
    teacher: '',
    comments: '',
    attendees: '',
    status: 'scheduled'
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [filters, setFilters] = useState<SearchFilters>({
    searchText: '',
    classId: selectedClass?.id || 'all',
    status: 'all',
    dateRange: 'all'
  });
  const [showAdvanced, setShowAdvanced] = useState(false);

  const filteredInstances = useMemo(() => {
    let result = selectedClass 
      ? instances.filter(inst => inst.classId === selectedClass.id)
      : instances;

    // Text search
    if (filters.searchText) {
      const searchLower = filters.searchText.toLowerCase();
      result = result.filter(instance =>
        instance.teacher.toLowerCase().includes(searchLower) ||
        instance.comments?.toLowerCase().includes(searchLower) ||
        classes.find(cls => cls.id === instance.classId)?.type.toLowerCase().includes(searchLower)
      );
    }

    // Class filter
    if (filters.classId && filters.classId !== 'all' && !selectedClass) {
      result = result.filter(instance => instance.classId === filters.classId);
    }

    // Status filter
    if (filters.status && filters.status !== 'all') {
      result = result.filter(instance => (instance.status || 'scheduled') === filters.status);
    }

    // Date range filter
    if (filters.dateRange && filters.dateRange !== 'all') {
      const today = new Date();
      const todayStr = today.toISOString().split('T')[0];
      
      switch (filters.dateRange) {
        case 'today':
          result = result.filter(instance => instance.date === todayStr);
          break;
        case 'upcoming':
          result = result.filter(instance => instance.date >= todayStr);
          break;
        case 'past':
          result = result.filter(instance => instance.date < todayStr);
          break;
        case 'this-week':
          const weekStart = new Date(today);
          weekStart.setDate(today.getDate() - today.getDay());
          const weekEnd = new Date(weekStart);
          weekEnd.setDate(weekStart.getDate() + 6);
          const weekStartStr = weekStart.toISOString().split('T')[0];
          const weekEndStr = weekEnd.toISOString().split('T')[0];
          result = result.filter(instance => 
            instance.date >= weekStartStr && instance.date <= weekEndStr
          );
          break;
      }
    }

    return result.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }, [instances, selectedClass, filters, classes]);

  const validateDate = (dateStr: string, classId: string) => {
    const selectedDate = new Date(dateStr);
    const dayOfWeek = selectedDate.toLocaleDateString('en-US', { weekday: 'long' });
    const yogaClass = classes.find(cls => cls.id === classId);
    
    if (yogaClass && yogaClass.dayOfWeek !== dayOfWeek) {
      return `Date must be a ${yogaClass.dayOfWeek}`;
    }
    return null;
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.classId) newErrors.classId = 'Class is required';
    if (!formData.date) newErrors.date = 'Date is required';
    if (!formData.teacher) newErrors.teacher = 'Teacher is required';

    if (formData.date && formData.classId) {
      const dateError = validateDate(formData.date, formData.classId);
      if (dateError) newErrors.date = dateError;
    }

    if (formData.attendees) {
      const attendeesNum = Number(formData.attendees);
      if (isNaN(attendeesNum) || attendeesNum < 0) {
        newErrors.attendees = 'Attendees must be a valid number';
      } else {
        const yogaClass = classes.find(cls => cls.id === formData.classId);
        if (yogaClass && attendeesNum > yogaClass.capacity) {
          newErrors.attendees = `Cannot exceed capacity of ${yogaClass.capacity}`;
        }
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    const instanceData = {
      classId: formData.classId,
      date: formData.date,
      teacher: formData.teacher,
      comments: formData.comments || undefined,
      attendees: formData.attendees ? Number(formData.attendees) : undefined,
      status: formData.status
    };

    if (editingInstance) {
      onUpdateInstance({ ...instanceData, id: editingInstance.id });
    } else {
      onAddInstance(instanceData);
    }

    resetForm();
  };

  const resetForm = () => {
    setFormData({
      classId: selectedClass?.id || '',
      date: '',
      teacher: '',
      comments: '',
      attendees: '',
      status: 'scheduled'
    });
    setErrors({});
    setShowForm(false);
    setEditingInstance(null);
  };

  const handleEdit = (instance: ClassInstance) => {
    setEditingInstance(instance);
    setFormData({
      classId: instance.classId,
      date: instance.date,
      teacher: instance.teacher,
      comments: instance.comments || '',
      attendees: instance.attendees?.toString() || '',
      status: instance.status || 'scheduled'
    });
    setShowForm(true);
  };

  const clearFilters = () => {
    setFilters({
      searchText: '',
      classId: selectedClass?.id || 'all',
      status: 'all',
      dateRange: 'all'
    });
    setShowAdvanced(false);
  };

  const hasActiveFilters = filters.searchText || 
    (filters.status && filters.status !== 'all') || 
    (filters.dateRange && filters.dateRange !== 'all') || 
    (!selectedClass && filters.classId && filters.classId !== 'all');

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'cancelled':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-blue-100 text-blue-800 border-blue-200';
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const today = new Date();
    const isToday = date.toDateString() === today.toDateString();
    const isTomorrow = date.toDateString() === new Date(today.getTime() + 86400000).toDateString();
    
    let dateText = date.toLocaleDateString('en-GB', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });

    if (isToday) dateText = `Today, ${dateText.split(', ').slice(1).join(', ')}`;
    if (isTomorrow) dateText = `Tomorrow, ${dateText.split(', ').slice(1).join(', ')}`;

    return dateText;
  };

  if (showForm) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={resetForm}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h2 className="text-xl font-semibold">
              {editingInstance ? 'Edit Instance' : 'Add Class Instance'}
            </h2>
            {selectedClass && (
              <p className="text-sm text-muted-foreground">
                {selectedClass.type} - {selectedClass.dayOfWeek}s at {selectedClass.time}
              </p>
            )}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Instance Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {!selectedClass && (
                <div>
                  <Label htmlFor="classId">Yoga Class *</Label>
                  <Select 
                    value={formData.classId} 
                    onValueChange={(value) => setFormData(prev => ({ ...prev, classId: value }))}
                  >
                    <SelectTrigger className={errors.classId ? 'border-destructive' : ''}>
                      <SelectValue placeholder="Select a class" />
                    </SelectTrigger>
                    <SelectContent>
                      {classes.map(cls => (
                        <SelectItem key={cls.id} value={cls.id}>
                          {cls.type} - {cls.dayOfWeek}s at {cls.time}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.classId && (
                    <p className="text-sm text-destructive mt-1">{errors.classId}</p>
                  )}
                </div>
              )}

              <div className="grid grid-cols-1 gap-4">
                <div>
                  <Label htmlFor="date">Date *</Label>
                  <Input
                    id="date"
                    type="date"
                    value={formData.date}
                    onChange={(e) => setFormData(prev => ({ ...prev, date: e.target.value }))}
                    className={errors.date ? 'border-destructive' : ''}
                  />
                  {errors.date && (
                    <p className="text-sm text-destructive mt-1">{errors.date}</p>
                  )}
                </div>

                <div>
                  <Label htmlFor="teacher">Teacher *</Label>
                  <Input
                    id="teacher"
                    placeholder="Enter teacher name"
                    value={formData.teacher}
                    onChange={(e) => setFormData(prev => ({ ...prev, teacher: e.target.value }))}
                    className={errors.teacher ? 'border-destructive' : ''}
                  />
                  {errors.teacher && (
                    <p className="text-sm text-destructive mt-1">{errors.teacher}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="attendees">Attendees</Label>
                  <Input
                    id="attendees"
                    type="number"
                    min="0"
                    placeholder="Number of attendees"
                    value={formData.attendees}
                    onChange={(e) => setFormData(prev => ({ ...prev, attendees: e.target.value }))}
                    className={errors.attendees ? 'border-destructive' : ''}
                  />
                  {errors.attendees && (
                    <p className="text-sm text-destructive mt-1">{errors.attendees}</p>
                  )}
                </div>

                <div>
                  <Label htmlFor="status">Status</Label>
                  <Select 
                    value={formData.status} 
                    onValueChange={(value: 'scheduled' | 'completed' | 'cancelled') => 
                      setFormData(prev => ({ ...prev, status: value }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="scheduled">Scheduled</SelectItem>
                      <SelectItem value="completed">Completed</SelectItem>
                      <SelectItem value="cancelled">Cancelled</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div>
                <Label htmlFor="comments">Additional Comments</Label>
                <Textarea
                  id="comments"
                  placeholder="Any special notes or comments..."
                  value={formData.comments}
                  onChange={(e) => setFormData(prev => ({ ...prev, comments: e.target.value }))}
                  rows={3}
                />
              </div>
            </CardContent>
          </Card>

          <div className="flex gap-3">
            <Button type="button" variant="outline" onClick={resetForm} className="flex-1">
              Cancel
            </Button>
            <Button type="submit" className="flex-1">
              {editingInstance ? 'Update Instance' : 'Add Instance'}
            </Button>
          </div>
        </form>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        {selectedClass && (
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
        )}
        <div className="flex-1">
          <h2 className="text-xl font-semibold">Class Instances</h2>
          {selectedClass ? (
            <p className="text-sm text-muted-foreground">
              {selectedClass.type} - {selectedClass.dayOfWeek}s at {selectedClass.time}
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              {filteredInstances.length} of {instances.length} instances
              {hasActiveFilters && ' (filtered)'}
            </p>
          )}
        </div>
        <Button onClick={() => setShowForm(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Add Instance
        </Button>
      </div>

      {/* Search and Filters */}
      <Card>
        <CardContent className="p-4 space-y-4">
          {/* Quick Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search teachers, classes, or comments..."
              value={filters.searchText}
              onChange={(e) => setFilters(prev => ({ ...prev, searchText: e.target.value }))}
              className="pl-10"
            />
          </div>

          {/* Advanced Filters */}
          <Collapsible open={showAdvanced} onOpenChange={setShowAdvanced}>
            <CollapsibleTrigger asChild>
              <Button variant="outline" className="w-full gap-2">
                <Filter className="h-4 w-4" />
                Advanced Filters
                {hasActiveFilters && <Badge variant="secondary" className="ml-2">Active</Badge>}
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent className="space-y-4 mt-4">
              <div className="grid grid-cols-1 gap-4">
                {!selectedClass && (
                  <Select value={filters.classId} onValueChange={(value) => setFilters(prev => ({ ...prev, classId: value }))}>
                    <SelectTrigger>
                      <SelectValue placeholder="Filter by class" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All classes</SelectItem>
                      {classes.map(cls => (
                        <SelectItem key={cls.id} value={cls.id}>
                          {cls.type} - {cls.dayOfWeek}s
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}

                <Select value={filters.status} onValueChange={(value) => setFilters(prev => ({ ...prev, status: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="Filter by status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All statuses</SelectItem>
                    <SelectItem value="scheduled">Scheduled</SelectItem>
                    <SelectItem value="completed">Completed</SelectItem>
                    <SelectItem value="cancelled">Cancelled</SelectItem>
                  </SelectContent>
                </Select>

                <Select value={filters.dateRange} onValueChange={(value) => setFilters(prev => ({ ...prev, dateRange: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="Filter by date" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All dates</SelectItem>
                    <SelectItem value="today">Today</SelectItem>
                    <SelectItem value="upcoming">Upcoming</SelectItem>
                    <SelectItem value="this-week">This Week</SelectItem>
                    <SelectItem value="past">Past</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {hasActiveFilters && (
                <Button variant="outline" onClick={clearFilters} className="w-full gap-2">
                  <X className="h-4 w-4" />
                  Clear Filters
                </Button>
              )}
            </CollapsibleContent>
          </Collapsible>
        </CardContent>
      </Card>

      {/* Instances List */}
      {filteredInstances.length === 0 ? (
        <div className="text-center py-16">
          <div className="w-16 h-16 bg-gradient-to-br from-primary/20 to-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <Calendar className="h-8 w-8 text-primary" />
          </div>
          <h3 className="text-xl font-semibold mb-2">
            {instances.length === 0 ? 'No Instances Yet' : 'No Results Found'}
          </h3>
          <p className="text-muted-foreground mb-6 max-w-sm mx-auto">
            {instances.length === 0 
              ? 'Add specific dates and teachers for your classes'
              : 'Try adjusting your search criteria'
            }
          </p>
          {instances.length === 0 ? (
            <Button onClick={() => setShowForm(true)} className="gap-2">
              <Plus className="h-4 w-4" />
              Add First Instance
            </Button>
          ) : hasActiveFilters ? (
            <Button variant="outline" onClick={clearFilters}>
              Clear Filters
            </Button>
          ) : null}
        </div>
      ) : (
        <div className="space-y-4">
          {filteredInstances.map((instance) => {
            const yogaClass = classes.find(cls => cls.id === instance.classId);
            const isUpcoming = new Date(instance.date) >= new Date();
            
            return (
              <Card key={instance.id} className="overflow-hidden hover:shadow-md transition-all duration-200">
                <CardContent className="p-4">
                  <div className="space-y-4">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        {!selectedClass && yogaClass && (
                          <div className="font-semibold text-sm mb-1 text-primary">
                            {yogaClass.type}
                          </div>
                        )}
                        <div className="flex items-center gap-2 mb-2">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          <span className="font-medium">{formatDate(instance.date)}</span>
                          <Badge className={`text-xs ${getStatusColor(instance.status)}`}>
                            {instance.status || 'scheduled'}
                          </Badge>
                          {isUpcoming && (
                            <Badge variant="outline" className="text-xs text-blue-600 border-blue-200">
                              Upcoming
                            </Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <User className="h-4 w-4" />
                          <span>{instance.teacher}</span>
                          {yogaClass && (
                            <span className="text-muted-foreground/70">• {yogaClass.time}</span>
                          )}
                        </div>
                      </div>
                      {instance.attendees !== undefined && yogaClass && (
                        <div className="text-right">
                          <div className="flex items-center gap-1 text-sm">
                            <Users className="h-4 w-4" />
                            <span className="font-medium">
                              {instance.attendees}/{yogaClass.capacity}
                            </span>
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {Math.round((instance.attendees / yogaClass.capacity) * 100)}% full
                          </div>
                        </div>
                      )}
                    </div>

                    {instance.comments && (
                      <div className="flex items-start gap-2 text-sm bg-accent/50 rounded-lg p-3">
                        <MessageSquare className="h-4 w-4 text-muted-foreground mt-0.5 flex-shrink-0" />
                        <span className="text-muted-foreground leading-relaxed">{instance.comments}</span>
                      </div>
                    )}

                    <div className="flex gap-2 pt-2 border-t">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleEdit(instance)}
                        className="flex-1 gap-1"
                      >
                        <Edit2 className="h-4 w-4" />
                        Edit
                      </Button>
                      
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button variant="outline" size="sm" className="text-destructive hover:text-destructive border-destructive/20 hover:border-destructive">
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Delete Instance</AlertDialogTitle>
                            <AlertDialogDescription>
                              Are you sure you want to delete this class instance for {formatDate(instance.date)}? This action cannot be undone.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Cancel</AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => onDeleteInstance(instance.id)}
                              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                            >
                              Delete Instance
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}