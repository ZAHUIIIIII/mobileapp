import React, { useState, useEffect } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Alert, AlertDescription } from './ui/alert';
import { AlertCircle, Check } from 'lucide-react';
import { YogaClass, DAYS_OF_WEEK, CLASS_TYPES, DIFFICULTY_LEVELS } from '../types/yoga';

interface ClassFormProps {
  initialClass?: YogaClass | null;
  onSave: (yogaClass: Omit<YogaClass, 'id'>) => void;
  onCancel: () => void;
}

export function ClassForm({ initialClass, onSave, onCancel }: ClassFormProps) {
  const [formData, setFormData] = useState({
    dayOfWeek: '',
    time: '',
    capacity: '',
    duration: '',
    price: '',
    type: '',
    description: '',
    location: '',
    difficulty: '',
    instructor: ''
  });
  
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showConfirmation, setShowConfirmation] = useState(false);

  useEffect(() => {
    if (initialClass) {
      setFormData({
        dayOfWeek: initialClass.dayOfWeek,
        time: initialClass.time,
        capacity: initialClass.capacity.toString(),
        duration: initialClass.duration.toString(),
        price: initialClass.price.toString(),
        type: initialClass.type,
        description: initialClass.description || '',
        location: initialClass.location || '',
        difficulty: initialClass.difficulty || '',
        instructor: initialClass.instructor || ''
      });
    }
  }, [initialClass]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.dayOfWeek) newErrors.dayOfWeek = 'Day of week is required';
    if (!formData.time) newErrors.time = 'Time is required';
    if (!formData.capacity) newErrors.capacity = 'Capacity is required';
    else if (isNaN(Number(formData.capacity)) || Number(formData.capacity) <= 0) {
      newErrors.capacity = 'Capacity must be a positive number';
    }
    if (!formData.duration) newErrors.duration = 'Duration is required';
    else if (isNaN(Number(formData.duration)) || Number(formData.duration) <= 0) {
      newErrors.duration = 'Duration must be a positive number';
    }
    if (!formData.price) newErrors.price = 'Price is required';
    else if (isNaN(Number(formData.price)) || Number(formData.price) < 0) {
      newErrors.price = 'Price must be a valid number';
    }
    if (!formData.type) newErrors.type = 'Class type is required';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setShowConfirmation(true);
  };

  const confirmSave = () => {
    const yogaClass: Omit<YogaClass, 'id'> = {
      dayOfWeek: formData.dayOfWeek,
      time: formData.time,
      capacity: Number(formData.capacity),
      duration: Number(formData.duration),
      price: Number(formData.price),
      type: formData.type,
      description: formData.description || undefined,
      location: formData.location || undefined,
      difficulty: formData.difficulty || undefined,
      instructor: formData.instructor || undefined
    };

    onSave(yogaClass);
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  if (showConfirmation) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-green-600">
          <Check className="h-5 w-5" />
          <h2 className="text-lg font-semibold">Confirm Class Details</h2>
        </div>
        
        <Card>
          <CardContent className="p-4 space-y-3">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Day</Label>
                <p>{formData.dayOfWeek}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Time</Label>
                <p>{formData.time}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Type</Label>
                <p>{formData.type}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Duration</Label>
                <p>{formData.duration} minutes</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Capacity</Label>
                <p>{formData.capacity} people</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Price</Label>
                <p>£{formData.price}</p>
              </div>
            </div>
            
            {formData.difficulty && (
              <div>
                <Label className="text-muted-foreground">Difficulty</Label>
                <p>{formData.difficulty}</p>
              </div>
            )}
            
            {formData.location && (
              <div>
                <Label className="text-muted-foreground">Location</Label>
                <p>{formData.location}</p>
              </div>
            )}
            
            {formData.instructor && (
              <div>
                <Label className="text-muted-foreground">Instructor</Label>
                <p>{formData.instructor}</p>
              </div>
            )}
            
            {formData.description && (
              <div>
                <Label className="text-muted-foreground">Description</Label>
                <p>{formData.description}</p>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex gap-3">
          <Button onClick={() => setShowConfirmation(false)} variant="outline" className="flex-1">
            Edit Details
          </Button>
          <Button onClick={confirmSave} className="flex-1">
            Confirm & Save
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-lg font-semibold">
        {initialClass ? 'Edit Class' : 'Add New Class'}
      </h2>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Required Fields */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Required Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="dayOfWeek">Day of Week *</Label>
              <Select value={formData.dayOfWeek} onValueChange={(value) => handleInputChange('dayOfWeek', value)}>
                <SelectTrigger className={errors.dayOfWeek ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select day" />
                </SelectTrigger>
                <SelectContent>
                  {DAYS_OF_WEEK.map(day => (
                    <SelectItem key={day} value={day}>{day}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.dayOfWeek && (
                <p className="text-sm text-destructive mt-1">{errors.dayOfWeek}</p>
              )}
            </div>

            <div>
              <Label htmlFor="time">Time *</Label>
              <Input
                id="time"
                type="time"
                value={formData.time}
                onChange={(e) => handleInputChange('time', e.target.value)}
                className={errors.time ? 'border-destructive' : ''}
              />
              {errors.time && (
                <p className="text-sm text-destructive mt-1">{errors.time}</p>
              )}
            </div>

            <div>
              <Label htmlFor="type">Class Type *</Label>
              <Select value={formData.type} onValueChange={(value) => handleInputChange('type', value)}>
                <SelectTrigger className={errors.type ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select class type" />
                </SelectTrigger>
                <SelectContent>
                  {CLASS_TYPES.map(type => (
                    <SelectItem key={type} value={type}>{type}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.type && (
                <p className="text-sm text-destructive mt-1">{errors.type}</p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="capacity">Capacity *</Label>
                <Input
                  id="capacity"
                  type="number"
                  min="1"
                  placeholder="e.g. 20"
                  value={formData.capacity}
                  onChange={(e) => handleInputChange('capacity', e.target.value)}
                  className={errors.capacity ? 'border-destructive' : ''}
                />
                {errors.capacity && (
                  <p className="text-sm text-destructive mt-1">{errors.capacity}</p>
                )}
              </div>

              <div>
                <Label htmlFor="duration">Duration (min) *</Label>
                <Input
                  id="duration"
                  type="number"
                  min="1"
                  placeholder="e.g. 60"
                  value={formData.duration}
                  onChange={(e) => handleInputChange('duration', e.target.value)}
                  className={errors.duration ? 'border-destructive' : ''}
                />
                {errors.duration && (
                  <p className="text-sm text-destructive mt-1">{errors.duration}</p>
                )}
              </div>
            </div>

            <div>
              <Label htmlFor="price">Price (£) *</Label>
              <Input
                id="price"
                type="number"
                min="0"
                step="0.01"
                placeholder="e.g. 15.00"
                value={formData.price}
                onChange={(e) => handleInputChange('price', e.target.value)}
                className={errors.price ? 'border-destructive' : ''}
              />
              {errors.price && (
                <p className="text-sm text-destructive mt-1">{errors.price}</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Optional Fields */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Additional Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="difficulty">Difficulty Level</Label>
              <Select value={formData.difficulty} onValueChange={(value) => handleInputChange('difficulty', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select difficulty" />
                </SelectTrigger>
                <SelectContent>
                  {DIFFICULTY_LEVELS.map(level => (
                    <SelectItem key={level} value={level}>{level}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                placeholder="e.g. Studio A, Main Hall"
                value={formData.location}
                onChange={(e) => handleInputChange('location', e.target.value)}
              />
            </div>

            <div>
              <Label htmlFor="instructor">Default Instructor</Label>
              <Input
                id="instructor"
                placeholder="e.g. Sarah Johnson"
                value={formData.instructor}
                onChange={(e) => handleInputChange('instructor', e.target.value)}
              />
            </div>

            <div>
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                placeholder="Class description, special notes, equipment needed..."
                value={formData.description}
                onChange={(e) => handleInputChange('description', e.target.value)}
                rows={3}
              />
            </div>
          </CardContent>
        </Card>

        {Object.keys(errors).length > 0 && (
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              Please fix the errors above before continuing.
            </AlertDescription>
          </Alert>
        )}

        <div className="flex gap-3">
          <Button type="button" variant="outline" onClick={onCancel} className="flex-1">
            Cancel
          </Button>
          <Button type="submit" className="flex-1">
            {initialClass ? 'Update Class' : 'Create Class'}
          </Button>
        </div>
      </form>
    </div>
  );
}