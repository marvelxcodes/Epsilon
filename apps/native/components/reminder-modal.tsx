import React, { useState } from 'react';
import {
  Modal,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Platform,
} from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
const DAYS_OF_WEEK: string[] = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

interface Frequency {
  type: 'daily' | 'weekly' | 'custom';
  daysOfWeek?: number[];
  times: Date[];
}

interface Props {
  visible: boolean;
  onClose: () => void;
  onSave: (payload: { name: string; frequency: Frequency; notes?: string }) => void;
}

export default function ReminderModal({ visible, onClose, onSave }: Props) {
  const [name, setName] = useState('');
  const [times, setTimes] = useState<Date[]>([new Date()]);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [currentTimeIndex, setCurrentTimeIndex] = useState(0);
  const [selectedDays, setSelectedDays] = useState<number[]>([]);
  const [frequencyType, setFrequencyType] = useState<'daily' | 'weekly' | 'custom'>('daily');
  const [notes, setNotes] = useState('');

  const toggleDay = (i: number) => {
    setSelectedDays(prev => (prev.includes(i) ? prev.filter(d => d !== i) : [...prev, i]));
  };

  const onTimeChange = (event: any, selected?: Date) => {
    setShowTimePicker(Platform.OS === 'ios');
    if (selected) {
      const copy = [...times];
      copy[currentTimeIndex] = selected;
      setTimes(copy);
    }
  };

  const addTime = () => setTimes(prev => [...prev, new Date()]);
  const removeTime = (index: number) => setTimes(prev => prev.filter((_, i) => i !== index));

  const handleSave = () => {
    if (!name.trim()) return;

    const frequency: Frequency = {
      type: frequencyType,
      times,
      daysOfWeek: frequencyType === 'daily' ? undefined : selectedDays,
    };

    onSave({ name: name.trim(), frequency, notes: notes.trim() || undefined });
    // reset
    setName('');
    setTimes([new Date()]);
    setSelectedDays([]);
    setFrequencyType('daily');
    setNotes('');
    onClose();
  };

  return (
    <Modal visible={visible} animationType="slide" onRequestClose={onClose}>
      <View className="flex-1 p-6 bg-background">
        <Text className="text-2xl font-bold text-foreground mb-4">New Reminder</Text>

        <Text className="text-foreground mb-2">Medicine</Text>
        <TextInput
          value={name}
          onChangeText={setName}
          placeholder="e.g., Aspirin"
          placeholderTextColor="#999"
          className="bg-card border border-border rounded-md p-3 mb-4 text-foreground"
        />

        <Text className="text-foreground mb-2">Frequency</Text>
        <View className="flex-row mb-3">
          <TouchableOpacity
            className={`mr-2 py-2 px-4 rounded-md ${frequencyType === 'daily' ? 'bg-primary' : 'bg-background border border-border'}`}
            onPress={() => setFrequencyType('daily')}
          >
            <Text className={frequencyType === 'daily' ? 'text-white' : 'text-foreground'}>Daily</Text>
          </TouchableOpacity>
          <TouchableOpacity
            className={`mr-2 py-2 px-4 rounded-md ${frequencyType === 'weekly' ? 'bg-primary' : 'bg-background border border-border'}`}
            onPress={() => setFrequencyType('weekly')}
          >
            <Text className={frequencyType === 'weekly' ? 'text-white' : 'text-foreground'}>Weekly</Text>
          </TouchableOpacity>
          <TouchableOpacity
            className={`py-2 px-4 rounded-md ${frequencyType === 'custom' ? 'bg-primary' : 'bg-background border border-border'}`}
            onPress={() => setFrequencyType('custom')}
          >
            <Text className={frequencyType === 'custom' ? 'text-white' : 'text-foreground'}>Custom</Text>
          </TouchableOpacity>
        </View>

        {frequencyType !== 'daily' && (
          <View className="mb-4">
            <Text className="text-foreground mb-2">Days</Text>
            <View className="flex-row flex-wrap">
              {DAYS_OF_WEEK.map((d, i) => (
                <TouchableOpacity
                  key={d}
                  onPress={() => toggleDay(i)}
                  className={`mr-2 mb-2 py-2 px-3 rounded-md ${selectedDays.includes(i) ? 'bg-primary' : 'bg-background border border-border'}`}
                >
                  <Text className={selectedDays.includes(i) ? 'text-white' : 'text-foreground'}>{d.slice(0,3)}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        )}

        <Text className="text-foreground mb-2">Times</Text>
        {times.map((t, idx) => (
          <View key={idx} className="flex-row items-center mb-2">
            <TouchableOpacity
              className="flex-1 bg-card border border-border rounded-md p-3"
              onPress={() => { setCurrentTimeIndex(idx); setShowTimePicker(true); }}
            >
              <Text className="text-foreground">{t.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</Text>
            </TouchableOpacity>
            {times.length > 1 && (
              <TouchableOpacity className="ml-2 bg-destructive py-2 px-3 rounded-md" onPress={() => removeTime(idx)}>
                <Text className="text-white">Remove</Text>
              </TouchableOpacity>
            )}
          </View>
        ))}
        <TouchableOpacity className="mb-4 bg-background border border-border py-2 px-4 rounded-md" onPress={addTime}>
          <Text className="text-foreground text-center">Add Time</Text>
        </TouchableOpacity>

        {showTimePicker && (
          <DateTimePicker value={times[currentTimeIndex] || new Date()} mode="time" is24Hour={false} display="default" onChange={onTimeChange} />
        )}

        <Text className="text-foreground mb-2">Notes (optional)</Text>
        <TextInput
          value={notes}
          onChangeText={setNotes}
          placeholder="e.g., take with food"
          placeholderTextColor="#999"
          className="bg-card border border-border rounded-md p-3 mb-4 text-foreground"
          multiline
        />

        <View className="flex-row justify-end">
          <TouchableOpacity className="mr-2 py-2 px-4 rounded-md bg-muted" onPress={onClose}>
            <Text className="text-foreground">Cancel</Text>
          </TouchableOpacity>
          <TouchableOpacity className="py-2 px-4 rounded-md bg-primary" onPress={handleSave}>
            <Text className="text-white">Save</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}
