import React, { useState, useMemo } from "react";
import {
  Edit2,
  Trash2,
  Calendar,
  Users,
  Clock,
  MapPin,
  Star,
  Search,
  Filter,
  Plus,
  X,
} from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Card, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "./ui/collapsible";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "./ui/alert-dialog";
import {
  YogaClass,
  ClassInstance,
  DAYS_OF_WEEK,
  CLASS_TYPES,
} from "../types/yoga";

interface ClassListProps {
  classes: YogaClass[];
  instances: ClassInstance[];
  onEdit: (yogaClass: YogaClass) => void;
  onDelete: (id: string) => void;
  onManageInstances: (yogaClass: YogaClass) => void;
  onAdd: () => void;
}

interface SearchFilters {
  searchText: string;
  dayOfWeek: string;
  classType: string;
  difficulty: string;
}

export function ClassList({
  classes,
  instances,
  onEdit,
  onDelete,
  onManageInstances,
  onAdd,
}: ClassListProps) {
  const [filters, setFilters] = useState<SearchFilters>({
    searchText: "",
    dayOfWeek: "",
    classType: "",
    difficulty: "",
  });
  const [showAdvanced, setShowAdvanced] = useState(false);

  const classTypes = [
    ...new Set(classes.map((cls) => cls.type)),
  ];
  const difficulties = [
    ...new Set(
      classes.map((cls) => cls.difficulty).filter(Boolean),
    ),
  ];

  const filteredClasses = useMemo(() => {
    return classes.filter((yogaClass) => {
      // Text search
      if (filters.searchText) {
        const searchLower = filters.searchText.toLowerCase();
        const matchesText =
          yogaClass.type.toLowerCase().includes(searchLower) ||
          yogaClass.description
            ?.toLowerCase()
            .includes(searchLower) ||
          yogaClass.instructor
            ?.toLowerCase()
            .includes(searchLower) ||
          yogaClass.location
            ?.toLowerCase()
            .includes(searchLower);
        if (!matchesText) return false;
      }

      // Day filter
      if (
        filters.dayOfWeek &&
        yogaClass.dayOfWeek !== filters.dayOfWeek
      ) {
        return false;
      }

      // Type filter
      if (
        filters.classType &&
        yogaClass.type !== filters.classType
      ) {
        return false;
      }

      // Difficulty filter
      if (
        filters.difficulty &&
        yogaClass.difficulty !== filters.difficulty
      ) {
        return false;
      }

      return true;
    });
  }, [classes, filters]);

  const getInstanceCount = (classId: string) => {
    return instances.filter((inst) => inst.classId === classId)
      .length;
  };

  const getUpcomingInstanceCount = (classId: string) => {
    const today = new Date().toISOString().split("T")[0];
    return instances.filter(
      (inst) => inst.classId === classId && inst.date >= today,
    ).length;
  };

  const clearFilters = () => {
    setFilters({
      searchText: "",
      dayOfWeek: "",
      classType: "",
      difficulty: "",
    });
    setShowAdvanced(false);
  };

  const hasActiveFilters = Object.values(filters).some(
    (value) => value !== "",
  );

  const getDifficultyColor = (difficulty?: string) => {
    switch (difficulty?.toLowerCase()) {
      case "beginner":
        return "bg-green-100 text-green-800 border-green-200";
      case "intermediate":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "advanced":
        return "bg-red-100 text-red-800 border-red-200";
      default:
        return "bg-blue-100 text-blue-800 border-blue-200";
    }
  };

  if (classes.length === 0) {
    return (
      <div className="text-center py-16">
        <div className="w-16 h-16 bg-gradient-to-br from-primary/20 to-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
          <Calendar className="h-8 w-8 text-primary" />
        </div>
        <h3 className="text-xl font-semibold mb-2">
          No Classes Yet
        </h3>
        <p className="text-muted-foreground mb-6 max-w-sm mx-auto">
          Get started by creating your first yoga class. You can
          add schedules, set capacity, and manage everything
          from here.
        </p>
        <Button onClick={onAdd} size="lg" className="gap-2">
          <Plus className="h-5 w-5" />
          Create First Class
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with Add Button */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold">Classes</h2>
          <p className="text-sm text-muted-foreground">
            {filteredClasses.length} of {classes.length} classes
            {hasActiveFilters && " (filtered)"}
          </p>
        </div>
        <Button onClick={onAdd} className="gap-2">
          <Plus className="h-4 w-4" />
          Add Class
        </Button>
      </div>

      {/* Search and Filters */}
      <Card>
        <CardContent className="p-4 space-y-4">
          {/* Quick Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search classes, instructors, locations..."
              value={filters.searchText}
              onChange={(e) =>
                setFilters((prev) => ({
                  ...prev,
                  searchText: e.target.value,
                }))
              }
              className="pl-10"
            />
          </div>

          {/* Advanced Filters */}
          <Collapsible
            open={showAdvanced}
            onOpenChange={setShowAdvanced}
          >
            <CollapsibleTrigger asChild>
              <Button
                variant="outline"
                className="w-full gap-2"
              >
                <Filter className="h-4 w-4" />
                Advanced Filters
                {hasActiveFilters && (
                  <Badge variant="secondary" className="ml-2">
                    Active
                  </Badge>
                )}
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent className="space-y-4 mt-4">
              <div className="grid grid-cols-1 gap-4">
                <Select
                  value={filters.dayOfWeek}
                  onValueChange={(value) =>
                    setFilters((prev) => ({
                      ...prev,
                      dayOfWeek: value,
                    }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Filter by day" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">
                      All days
                    </SelectItem>
                    {DAYS_OF_WEEK.map((day) => (
                      <SelectItem key={day} value={day}>
                        {day}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select
                  value={filters.classType}
                  onValueChange={(value) =>
                    setFilters((prev) => ({
                      ...prev,
                      classType: value,
                    }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Filter by type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">
                      All types
                    </SelectItem>
                    {classTypes.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                {difficulties.length > 0 && (
                  <Select
                    value={filters.difficulty}
                    onValueChange={(value) =>
                      setFilters((prev) => ({
                        ...prev,
                        difficulty: value,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Filter by difficulty" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">
                        All levels
                      </SelectItem>
                      {difficulties.map((diff) => (
                        <SelectItem key={diff} value={diff}>
                          {diff}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              </div>

              {hasActiveFilters && (
                <Button
                  variant="outline"
                  onClick={clearFilters}
                  className="w-full gap-2"
                >
                  <X className="h-4 w-4" />
                  Clear Filters
                </Button>
              )}
            </CollapsibleContent>
          </Collapsible>
        </CardContent>
      </Card>

      {/* Classes List */}
      {filteredClasses.length === 0 ? (
        <div className="text-center py-12">
          <Search className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            No Results Found
          </h3>
          <p className="text-muted-foreground mb-4">
            Try adjusting your search criteria
          </p>
          {hasActiveFilters && (
            <Button variant="outline" onClick={clearFilters}>
              Clear Filters
            </Button>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {filteredClasses.map((yogaClass) => {
            const instanceCount = getInstanceCount(
              yogaClass.id,
            );
            const upcomingCount = getUpcomingInstanceCount(
              yogaClass.id,
            );

            return (
              <Card
                key={yogaClass.id}
                className="overflow-hidden hover:shadow-md transition-all duration-200"
              >
                <CardContent className="p-0">
                  <div className="p-4 space-y-4">
                    {/* Header */}
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="text-lg font-semibold">
                            {yogaClass.type}
                          </h3>
                          {yogaClass.difficulty && (
                            <Badge
                              className={`text-xs ${getDifficultyColor(yogaClass.difficulty)}`}
                            >
                              {yogaClass.difficulty}
                            </Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Calendar className="h-4 w-4" />
                            {yogaClass.dayOfWeek}
                          </span>
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {yogaClass.time}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xl font-bold text-primary">
                          £{yogaClass.price}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          {yogaClass.duration} min
                        </div>
                      </div>
                    </div>

                    {/* Details Grid */}
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div className="flex items-center gap-2">
                        <Users className="h-4 w-4 text-muted-foreground" />
                        <span>
                          Capacity: {yogaClass.capacity}
                        </span>
                      </div>
                      {yogaClass.location && (
                        <div className="flex items-center gap-2">
                          <MapPin className="h-4 w-4 text-muted-foreground" />
                          <span className="truncate">
                            {yogaClass.location}
                          </span>
                        </div>
                      )}
                    </div>

                    {yogaClass.instructor && (
                      <div className="flex items-center gap-2 text-sm">
                        <Star className="h-4 w-4 text-muted-foreground" />
                        <span>
                          Instructor: {yogaClass.instructor}
                        </span>
                      </div>
                    )}

                    {yogaClass.description && (
                      <p className="text-sm text-muted-foreground line-clamp-2 leading-relaxed">
                        {yogaClass.description}
                      </p>
                    )}

                    {/* Instance Stats */}
                    {instanceCount > 0 && (
                      <div className="flex items-center gap-4 text-xs text-muted-foreground bg-accent/50 rounded-lg p-3">
                        <span>
                          {instanceCount} total instances
                        </span>
                        {upcomingCount > 0 && (
                          <span className="text-primary font-medium">
                            {upcomingCount} upcoming
                          </span>
                        )}
                      </div>
                    )}

                    {/* Actions */}
                    <div className="flex gap-2 pt-2 border-t">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() =>
                          onManageInstances(yogaClass)
                        }
                        className="flex-1 gap-1"
                      >
                        <Calendar className="h-4 w-4" />
                        Instances
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onEdit(yogaClass)}
                        className="flex-1 gap-1"
                      >
                        <Edit2 className="h-4 w-4" />
                        Edit
                      </Button>

                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            variant="outline"
                            size="sm"
                            className="text-destructive hover:text-destructive border-destructive/20 hover:border-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>
                              Delete Class
                            </AlertDialogTitle>
                            <AlertDialogDescription>
                              Are you sure you want to delete "
                              {yogaClass.type}" on{" "}
                              {yogaClass.dayOfWeek}s? This will
                              also delete all {instanceCount}{" "}
                              associated class instances. This
                              action cannot be undone.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>
                              Cancel
                            </AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() =>
                                onDelete(yogaClass.id)
                              }
                              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                            >
                              Delete Class
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