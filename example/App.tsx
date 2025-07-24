import React, { useState, useEffect } from 'react';
import { LayoutDashboard, BookOpen, Calendar, Cloud, Plus, Menu } from 'lucide-react';
import { Button } from './components/ui/button';
import { Sheet, SheetContent, SheetTrigger, SheetTitle, SheetDescription } from './components/ui/sheet';
import { Dashboard } from './components/Dashboard';
import { ClassForm } from './components/ClassForm';
import { ClassList } from './components/ClassList';
import { InstanceManager } from './components/InstanceManager';
import { CloudSync } from './components/CloudSync';
import { YogaClass, ClassInstance, Activity } from './types/yoga';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [classes, setClasses] = useState<YogaClass[]>([]);
  const [instances, setInstances] = useState<ClassInstance[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [selectedClass, setSelectedClass] = useState<YogaClass | null>(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [showClassForm, setShowClassForm] = useState(false);

  // Load data from localStorage on mount
  useEffect(() => {
    const savedClasses = localStorage.getItem('yogaClasses');
    const savedInstances = localStorage.getItem('yogaInstances');
    const savedActivities = localStorage.getItem('yogaActivities');
    
    if (savedClasses) {
      setClasses(JSON.parse(savedClasses));
    }
    if (savedInstances) {
      setInstances(JSON.parse(savedInstances));
    }
    if (savedActivities) {
      setActivities(JSON.parse(savedActivities));
    }
  }, []);

  // Save to localStorage whenever data changes
  useEffect(() => {
    localStorage.setItem('yogaClasses', JSON.stringify(classes));
  }, [classes]);

  useEffect(() => {
    localStorage.setItem('yogaInstances', JSON.stringify(instances));
  }, [instances]);

  useEffect(() => {
    localStorage.setItem('yogaActivities', JSON.stringify(activities));
  }, [activities]);

  const addActivity = (type: string, description: string, relatedId?: string) => {
    const newActivity: Activity = {
      id: Date.now().toString(),
      type,
      description,
      timestamp: new Date().toISOString(),
      relatedId
    };
    setActivities(prev => [newActivity, ...prev.slice(0, 49)]); // Keep last 50 activities
  };

  const addClass = (newClass: Omit<YogaClass, 'id'>) => {
    const yogaClass: YogaClass = {
      ...newClass,
      id: Date.now().toString(),
      lastModified: new Date().toISOString(),
      syncStatus: 'pending'
    };
    setClasses(prev => [...prev, yogaClass]);
    addActivity('class_created', `Created class "${yogaClass.type}" for ${yogaClass.dayOfWeek}s`, yogaClass.id);
  };

  const updateClass = (updatedClass: YogaClass) => {
    const classWithMeta = {
      ...updatedClass,
      lastModified: new Date().toISOString(),
      syncStatus: 'pending' as const
    };
    setClasses(prev => prev.map(cls => cls.id === updatedClass.id ? classWithMeta : cls));
    addActivity('class_updated', `Updated class "${updatedClass.type}"`, updatedClass.id);
  };

  const deleteClass = (id: string) => {
    const classToDelete = classes.find(cls => cls.id === id);
    setClasses(prev => prev.filter(cls => cls.id !== id));
    setInstances(prev => prev.filter(inst => inst.classId !== id));
    if (classToDelete) {
      addActivity('class_deleted', `Deleted class "${classToDelete.type}"`, id);
    }
  };

  const addInstance = (instance: Omit<ClassInstance, 'id'>) => {
    const newInstance: ClassInstance = {
      ...instance,
      id: Date.now().toString(),
      lastModified: new Date().toISOString(),
      syncStatus: 'pending'
    };
    setInstances(prev => [...prev, newInstance]);
    const relatedClass = classes.find(cls => cls.id === instance.classId);
    addActivity('instance_created', `Scheduled ${relatedClass?.type || 'class'} for ${new Date(instance.date).toLocaleDateString()}`, newInstance.id);
  };

  const updateInstance = (updatedInstance: ClassInstance) => {
    const instanceWithMeta = {
      ...updatedInstance,
      lastModified: new Date().toISOString(),
      syncStatus: 'pending' as const
    };
    setInstances(prev => prev.map(inst => inst.id === updatedInstance.id ? instanceWithMeta : inst));
    addActivity('instance_updated', `Updated class instance for ${new Date(updatedInstance.date).toLocaleDateString()}`, updatedInstance.id);
  };

  const deleteInstance = (id: string) => {
    const instanceToDelete = instances.find(inst => inst.id === id);
    setInstances(prev => prev.filter(inst => inst.id !== id));
    if (instanceToDelete) {
      addActivity('instance_deleted', `Deleted class instance for ${new Date(instanceToDelete.date).toLocaleDateString()}`, id);
    }
  };

  const handleSyncComplete = () => {
    // Mark all items as synced
    setClasses(prev => prev.map(cls => ({ ...cls, syncStatus: 'synced' as const })));
    setInstances(prev => prev.map(inst => ({ ...inst, syncStatus: 'synced' as const })));
    addActivity('data_synced', 'Data synchronized with cloud successfully');
  };

  const handleSyncStart = () => {
    addActivity('sync_started', 'Started syncing data to cloud');
  };

  const handleSyncError = (error: string) => {
    addActivity('sync_failed', `Sync failed: ${error}`);
  };

  const navigation = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'classes', label: 'Classes', icon: BookOpen },
    { id: 'instances', label: 'Instances', icon: Calendar },
    { id: 'sync', label: 'Sync', icon: Cloud }
  ];

  const getPageTitle = () => {
    switch (activeTab) {
      case 'dashboard':
        return 'Dashboard';
      case 'classes':
        return showClassForm ? (selectedClass ? 'Edit Class' : 'Add Class') : 'Classes';
      case 'instances':
        return 'Class Instances';
      case 'sync':
        return 'Cloud Sync';
      default:
        return 'Yoga Admin';
    }
  };

  const getPendingChanges = () => {
    const pendingClasses = classes.filter(cls => cls.syncStatus === 'pending').length;
    const pendingInstances = instances.filter(inst => inst.syncStatus === 'pending').length;
    return pendingClasses + pendingInstances;
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return (
          <Dashboard
            classes={classes}
            instances={instances}
            activities={activities}
            onNavigate={(tab, classData) => {
              if (classData) setSelectedClass(classData);
              setActiveTab(tab);
            }}
          />
        );
      case 'classes':
        if (showClassForm) {
          return (
            <ClassForm
              initialClass={selectedClass}
              onSave={(cls) => {
                if (selectedClass) {
                  updateClass({ ...cls, id: selectedClass.id });
                  setSelectedClass(null);
                } else {
                  addClass(cls);
                }
                setShowClassForm(false);
              }}
              onCancel={() => {
                setSelectedClass(null);
                setShowClassForm(false);
              }}
            />
          );
        }
        return (
          <ClassList
            classes={classes}
            instances={instances}
            onEdit={(cls) => {
              setSelectedClass(cls);
              setShowClassForm(true);
            }}
            onDelete={deleteClass}
            onManageInstances={(cls) => {
              setSelectedClass(cls);
              setActiveTab('instances');
            }}
            onAdd={() => {
              setSelectedClass(null);
              setShowClassForm(true);
            }}
          />
        );
      case 'instances':
        return (
          <InstanceManager
            classes={classes}
            instances={instances}
            selectedClass={selectedClass}
            onAddInstance={addInstance}
            onUpdateInstance={updateInstance}
            onDeleteInstance={deleteInstance}
            onBack={() => {
              setSelectedClass(null);
              setActiveTab('classes');
            }}
          />
        );
      case 'sync':
        return (
          <CloudSync
            classes={classes}
            instances={instances}
            onSyncComplete={handleSyncComplete}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Mobile-optimized header with better touch targets */}
      <div className="sticky top-0 z-50 bg-gradient-to-r from-primary to-primary/90 text-primary-foreground shadow-xl">
        <div className="px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary-foreground/20 rounded-xl flex items-center justify-center">
              <LayoutDashboard className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-lg font-semibold">{getPageTitle()}</h1>
              <div className="flex items-center gap-2 text-xs text-primary-foreground/80">
                <span>{classes.length} classes</span>
                <span>•</span>
                <span>{instances.length} instances</span>
                {getPendingChanges() > 0 && (
                  <>
                    <span>•</span>
                    <span className="text-yellow-300">{getPendingChanges()} pending sync</span>
                  </>
                )}
              </div>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            {activeTab === 'classes' && (
              <Button 
                variant="ghost" 
                size="icon" 
                className="text-primary-foreground hover:bg-primary-foreground/20 w-10 h-10 rounded-xl"
                onClick={() => {
                  setSelectedClass(null);
                  setShowClassForm(true);
                }}
              >
                <Plus className="h-5 w-5" />
              </Button>
            )}
            <Sheet open={isMenuOpen} onOpenChange={setIsMenuOpen}>
              <SheetTrigger asChild>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="text-primary-foreground hover:bg-primary-foreground/20 w-10 h-10 rounded-xl"
                >
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-80">
                <SheetTitle className="sr-only">Navigation Menu</SheetTitle>
                <SheetDescription className="sr-only">
                  Navigate between different sections of the yoga admin app
                </SheetDescription>
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center">
                    <LayoutDashboard className="h-6 w-6 text-primary-foreground" />
                  </div>
                  <div>
                    <h2 className="font-semibold">Yoga Admin</h2>
                    <p className="text-sm text-muted-foreground">Mobile Studio Manager</p>
                  </div>
                </div>
                <nav className="space-y-2">
                  {navigation.map((item) => {
                    const Icon = item.icon;
                    const isActive = activeTab === item.id;
                    return (
                      <Button
                        key={item.id}
                        variant={isActive ? "default" : "ghost"}
                        className="w-full justify-start gap-3 h-14 text-left"
                        onClick={() => {
                          setActiveTab(item.id);
                          setIsMenuOpen(false);
                          setShowClassForm(false);
                          if (item.id !== 'instances') {
                            setSelectedClass(null);
                          }
                        }}
                      >
                        <Icon className="h-5 w-5" />
                        <span>{item.label}</span>
                        {item.id === 'sync' && getPendingChanges() > 0 && (
                          <div className="ml-auto w-6 h-6 bg-yellow-500 text-white rounded-full flex items-center justify-center text-xs">
                            {getPendingChanges()}
                          </div>
                        )}
                      </Button>
                    );
                  })}
                </nav>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>

      {/* Main Content optimized for mobile with better spacing */}
      <div className="px-4 py-6 pb-28 max-w-md mx-auto">
        {renderContent()}
      </div>

      {/* Enhanced Bottom Navigation with better touch targets */}
      <div className="fixed bottom-0 left-0 right-0 bg-card/95 backdrop-blur-sm border-t border-border/50 shadow-2xl">
        <div className="flex max-w-md mx-auto">
          {navigation.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            const pendingCount = item.id === 'sync' ? getPendingChanges() : 0;
            
            return (
              <Button
                key={item.id}
                variant="ghost"
                className={`flex-1 flex-col gap-1 h-16 rounded-none transition-all duration-200 relative ${
                  isActive 
                    ? 'text-primary bg-primary/10 scale-105' 
                    : 'text-muted-foreground hover:text-foreground hover:bg-accent/50'
                }`}
                onClick={() => {
                  setActiveTab(item.id);
                  setShowClassForm(false);
                  if (item.id !== 'instances') {
                    setSelectedClass(null);
                  }
                }}
              >
                <div className="relative">
                  <Icon className={`h-6 w-6 transition-transform ${isActive ? 'scale-110' : ''}`} />
                  {pendingCount > 0 && (
                    <div className="absolute -top-2 -right-2 w-5 h-5 bg-yellow-500 text-white rounded-full flex items-center justify-center text-xs font-medium">
                      {pendingCount > 9 ? '9+' : pendingCount}
                    </div>
                  )}
                </div>
                <span className="text-xs font-medium">{item.label}</span>
                {isActive && (
                  <div className="absolute top-0 left-1/2 transform -translate-x-1/2 w-10 h-1 bg-primary rounded-full" />
                )}
              </Button>
            );
          })}
        </div>
      </div>
    </div>
  );
}