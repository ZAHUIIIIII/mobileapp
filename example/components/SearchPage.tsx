import React, { useState, useMemo } from 'react';
import { Search, Calendar, User, Filter, X } from 'lucide-react';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from './ui/collapsible';
import { YogaClass, ClassInstance, DAYS_OF_WEEK } from '../types/yoga';

interface SearchPageProps {
  classes: YogaClass[];
  instances: ClassInstance[];
  onClassSelect: (yogaClass: YogaClass | null) => void;
}

interface SearchFilters {
  teacherName: string;
  dayOfWeek: string;
  date: string;
  classType: string;
}

export function SearchPage({ classes, instances, onClassSelect }: SearchPageProps) {
  const [filters, setFilters] = useState<SearchFilters>({
    teacherName: '',
    dayOfWeek: '',
    date: '',
    classType: ''
  });
  const [showAdvanced, setShowAdvanced] = useState(false);

  const classTypes = [...new Set(classes.map(cls => cls.type))];

  const searchResults = useMemo(() => {
    if (!filters.teacherName && !filters.dayOfWeek && !filters.date && !filters.classType) {
      return [];
    }

    let results: Array<{
      class: YogaClass;
      instance?: ClassInstance;
      matchType: 'teacher' | 'class' | 'both';
    }> = [];

    // Search by teacher name in instances
    if (filters.teacherName) {
      const matchingInstances = instances.filter(instance =>
        instance.teacher.toLowerCase().includes(filters.teacherName.toLowerCase())
      );

      matchingInstances.forEach(instance => {
        const yogaClass = classes.find(cls => cls.id === instance.classId);
        if (yogaClass) {
          results.push({
            class: yogaClass,
            instance,
            matchType: 'teacher'
          });
        }
      });
    }

    // Search by class properties
    let matchingClasses = classes;

    if (filters.dayOfWeek) {
      matchingClasses = matchingClasses.filter(cls => cls.dayOfWeek === filters.dayOfWeek);
    }

    if (filters.classType) {
      matchingClasses = matchingClasses.filter(cls => cls.type === filters.classType);
    }

    // Add class matches
    matchingClasses.forEach(yogaClass => {
      // Avoid duplicates from teacher search
      const existingResult = results.find(r => r.class.id === yogaClass.id);
      if (!existingResult) {
        results.push({
          class: yogaClass,
          matchType: 'class'
        });
      } else if (existingResult.matchType === 'teacher') {
        existingResult.matchType = 'both';
      }
    });

    // Filter by date if specified
    if (filters.date) {
      const searchDate = new Date(filters.date);
      const searchDayOfWeek = searchDate.toLocaleDateString('en-US', { weekday: 'long' });
      
      results = results.filter(result => {
        if (result.instance) {
          return result.instance.date === filters.date;
        } else {
          return result.class.dayOfWeek === searchDayOfWeek;
        }
      });
    }

    return results;
  }, [filters, classes, instances]);

  const clearFilters = () => {
    setFilters({
      teacherName: '',
      dayOfWeek: '',
      date: '',
      classType: ''
    });
  };

  const hasActiveFilters = Object.values(filters).some(value => value !== '');

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-GB', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    });
  };

  const getMatchTypeColor = (matchType: string) => {
    switch (matchType) {
      case 'teacher':
        return 'bg-blue-100 text-blue-800';
      case 'class':
        return 'bg-green-100 text-green-800';
      case 'both':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold mb-4">Search Classes</h2>
        
        {/* Quick Search */}
        <div className="space-y-4">
          <div>
            <Label htmlFor="teacherSearch">Search by Teacher Name</Label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                id="teacherSearch"
                placeholder="Enter teacher name..."
                value={filters.teacherName}
                onChange={(e) => setFilters(prev => ({ ...prev, teacherName: e.target.value }))}
                className="pl-10"
              />
            </div>
          </div>

          {/* Advanced Filters */}
          <Collapsible open={showAdvanced} onOpenChange={setShowAdvanced}>
            <CollapsibleTrigger asChild>
              <Button variant="outline" className="w-full">
                <Filter className="h-4 w-4 mr-2" />
                Advanced Search
                {hasActiveFilters && <Badge className="ml-2">Active</Badge>}
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent className="space-y-4 mt-4">
              <div className="grid grid-cols-1 gap-4">
                <div>
                  <Label htmlFor="dayFilter">Day of Week</Label>
                  <Select value={filters.dayOfWeek} onValueChange={(value) => setFilters(prev => ({ ...prev, dayOfWeek: value }))}>
                    <SelectTrigger>
                      <SelectValue placeholder="Any day" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">Any day</SelectItem>
                      {DAYS_OF_WEEK.map(day => (
                        <SelectItem key={day} value={day}>{day}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="typeFilter">Class Type</Label>
                  <Select value={filters.classType} onValueChange={(value) => setFilters(prev => ({ ...prev, classType: value }))}>
                    <SelectTrigger>
                      <SelectValue placeholder="Any type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">Any type</SelectItem>
                      {classTypes.map(type => (
                        <SelectItem key={type} value={type}>{type}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="dateFilter">Specific Date</Label>
                  <Input
                    id="dateFilter"
                    type="date"
                    value={filters.date}
                    onChange={(e) => setFilters(prev => ({ ...prev, date: e.target.value }))}
                  />
                </div>
              </div>

              {hasActiveFilters && (
                <Button variant="outline" onClick={clearFilters} className="w-full">
                  <X className="h-4 w-4 mr-2" />
                  Clear All Filters
                </Button>
              )}
            </CollapsibleContent>
          </Collapsible>
        </div>
      </div>

      {/* Search Results */}
      <div className="space-y-4">
        {!hasActiveFilters ? (
          <div className="text-center py-12">
            <Search className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Search for Classes</h3>
            <p className="text-muted-foreground">
              Enter a teacher name or use advanced filters to find classes
            </p>
          </div>
        ) : searchResults.length === 0 ? (
          <div className="text-center py-12">
            <Search className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No Results Found</h3>
            <p className="text-muted-foreground">
              Try adjusting your search criteria
            </p>
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between">
              <h3 className="font-semibold">
                Search Results ({searchResults.length})
              </h3>
            </div>
            
            <div className="space-y-3">
              {searchResults.map((result, index) => (
                <Card 
                  key={`${result.class.id}-${result.instance?.id || index}`}
                  className="cursor-pointer hover:shadow-md transition-shadow"
                  onClick={() => onClassSelect(result.class)}
                >
                  <CardContent className="p-4">
                    <div className="space-y-3">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <h4 className="font-semibold">{result.class.type}</h4>
                            <Badge className={`text-xs ${getMatchTypeColor(result.matchType)}`}>
                              {result.matchType === 'teacher' && 'Teacher Match'}
                              {result.matchType === 'class' && 'Class Match'}
                              {result.matchType === 'both' && 'Full Match'}
                            </Badge>
                          </div>
                          
                          <div className="space-y-1 text-sm text-muted-foreground">
                            <div className="flex items-center gap-2">
                              <Calendar className="h-4 w-4" />
                              <span>{result.class.dayOfWeek}s at {result.class.time}</span>
                            </div>
                            
                            {result.instance && (
                              <>
                                <div className="flex items-center gap-2">
                                  <Calendar className="h-4 w-4" />
                                  <span className="font-medium">{formatDate(result.instance.date)}</span>
                                </div>
                                <div className="flex items-center gap-2">
                                  <User className="h-4 w-4" />
                                  <span className="font-medium">{result.instance.teacher}</span>
                                </div>
                              </>
                            )}
                          </div>
                        </div>
                        
                        <div className="text-right">
                          <div className="font-semibold">£{result.class.price}</div>
                          <div className="text-sm text-muted-foreground">{result.class.duration} min</div>
                        </div>
                      </div>

                      {result.class.description && (
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {result.class.description}
                        </p>
                      )}

                      {result.instance?.comments && (
                        <p className="text-sm text-muted-foreground italic">
                          Note: {result.instance.comments}
                        </p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}