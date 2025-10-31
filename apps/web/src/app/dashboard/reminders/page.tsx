"use client";

import { AlertCircle, Bell, CheckCircle, Clock } from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface Medication {
  id: string;
  userId: string;
  name: string;
  dosage: string;
  frequency: string;
  time: string;
  startDate: string;
  endDate: string | null;
  notes: string | null;
  isActive: string;
  reminderEnabled: string;
  createdAt: string;
}

interface ReminderItem {
  medication: Medication;
  time: string;
  isPast: boolean;
}

function BellIcon() {
  return (
    <svg
      className="h-full w-full"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
      />
    </svg>
  );
}

export default function RemindersPage() {
  const [medications, setMedications] = useState<Medication[]>([]);
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMedications();
  }, []);

  const fetchMedications = async () => {
    try {
      const response = await fetch("/api/medicine?activeOnly=true");
      if (response.ok) {
        const data = await response.json();
        setMedications(
          (data.medicines || []).filter(
            (med: Medication) => med.reminderEnabled === "true"
          )
        );
      }
    } catch (error) {
      console.error("Failed to fetch medications:", error);
    } finally {
      setLoading(false);
    }
  };

  const getRemindersForDate = (date: Date): ReminderItem[] => {
    const reminders: ReminderItem[] = [];
    const now = new Date();
    const isToday = isSameDay(date, now);

    medications.forEach((medication) => {
      const times = medication.time.split(",").map((t) => t.trim());
      times.forEach((time) => {
        const [hours, minutes] = time.split(":").map(Number);
        const reminderTime = new Date(date);
        reminderTime.setHours(hours, minutes, 0, 0);

        const isPast = isToday && reminderTime < now;

        reminders.push({
          medication,
          time,
          isPast,
        });
      });
    });

    return reminders.sort((a, b) => a.time.localeCompare(b.time));
  };

  const isSameDay = (date1: Date, date2: Date): boolean =>
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate();

  const reminders = getRemindersForDate(selectedDate);
  const upcomingCount = reminders.filter((r) => !r.isPast).length;
  const completedCount = reminders.filter((r) => r.isPast).length;

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-black">
      {/* Header Section */}
      <section className="relative bg-black pt-28 pb-12 text-white">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-blue-500/20 via-transparent to-transparent opacity-50" />
        <div className="container relative mx-auto px-6">
          <div className="flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-lg bg-blue-500">
              <div className="h-8 w-8">
                <BellIcon />
              </div>
            </div>
            <div>
              <h1 className="font-bold text-4xl">Reminders</h1>
              <p className="mt-2 text-gray-300">
                Track your daily medication schedule
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="border-gray-200 border-b bg-white py-8 dark:border-gray-800 dark:bg-gray-900">
        <div className="container mx-auto px-6">
          <div className="grid gap-6 md:grid-cols-3">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="font-medium text-sm">
                  Total Medications
                </CardTitle>
                <Bell className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="font-bold text-2xl">{medications.length}</div>
                <p className="text-muted-foreground text-xs">
                  Active with reminders
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="font-medium text-sm">
                  Upcoming Today
                </CardTitle>
                <Clock className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="font-bold text-2xl text-blue-500">
                  {upcomingCount}
                </div>
                <p className="text-muted-foreground text-xs">
                  Reminders pending
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="font-medium text-sm">Completed</CardTitle>
                <CheckCircle className="h-4 w-4 text-green-500" />
              </CardHeader>
              <CardContent>
                <div className="font-bold text-2xl text-green-500">
                  {completedCount}
                </div>
                <p className="text-muted-foreground text-xs">Taken today</p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Calendar and Reminders */}
      <section className="py-12">
        <div className="container mx-auto px-6">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
            </div>
          ) : medications.length === 0 ? (
            <div className="py-20 text-center">
              <div className="mx-auto mb-4 flex h-24 w-24 items-center justify-center rounded-full bg-gray-200 dark:bg-gray-800">
                <Bell className="h-12 w-12 text-gray-400" />
              </div>
              <h3 className="mb-2 font-semibold text-xl">No reminders yet</h3>
              <p className="mb-6 text-gray-600 dark:text-gray-400">
                Add medications with reminders enabled to see them here
              </p>
              <Button
                className="bg-blue-500 text-white hover:bg-blue-600"
                onClick={() =>
                  (window.location.href = "/dashboard/medications")
                }
              >
                Go to Medications
              </Button>
            </div>
          ) : (
            <div className="grid gap-8 lg:grid-cols-3">
              {/* Calendar */}
              <div className="lg:col-span-1">
                <Card>
                  <CardHeader>
                    <CardTitle>Select Date</CardTitle>
                    <CardDescription>
                      Choose a date to view reminders
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="flex justify-center">
                    <Calendar
                      className="rounded-md border"
                      mode="single"
                      onSelect={(date) => date && setSelectedDate(date)}
                      selected={selectedDate}
                    />
                  </CardContent>
                </Card>
              </div>

              {/* Reminders List */}
              <div className="lg:col-span-2">
                <Card>
                  <CardHeader>
                    <CardTitle>
                      Reminders for{" "}
                      {selectedDate.toLocaleDateString("en-US", {
                        weekday: "long",
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </CardTitle>
                    <CardDescription>
                      {reminders.length} reminder
                      {reminders.length !== 1 ? "s" : ""} scheduled
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {reminders.length === 0 ? (
                      <div className="py-12 text-center">
                        <AlertCircle className="mx-auto mb-3 h-12 w-12 text-gray-400" />
                        <p className="text-gray-600 dark:text-gray-400">
                          No reminders scheduled for this date
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {reminders.map((reminder, index) => (
                          <div
                            className={`group flex items-center gap-4 rounded-lg border-2 p-4 transition-all ${
                              reminder.isPast
                                ? "border-green-500/20 bg-green-50 dark:bg-green-950/20"
                                : "border-blue-500/20 bg-blue-50 hover:shadow-md dark:bg-blue-950/20"
                            }`}
                            key={`${reminder.medication.id}-${index}`}
                          >
                            {/* Time */}
                            <div className="flex h-20 w-20 flex-col items-center justify-center rounded-lg border-2 border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-800">
                              <Clock
                                className={`mb-1 h-5 w-5 ${
                                  reminder.isPast
                                    ? "text-green-500"
                                    : "text-blue-500"
                                }`}
                              />
                              <span
                                className={`font-bold text-lg ${
                                  reminder.isPast
                                    ? "text-green-500"
                                    : "text-blue-500"
                                }`}
                              >
                                {reminder.time}
                              </span>
                            </div>

                            {/* Medication Info */}
                            <div className="flex-1">
                              <h4 className="mb-1 font-semibold text-lg">
                                {reminder.medication.name}
                              </h4>
                              <p className="text-gray-600 text-sm dark:text-gray-400">
                                {reminder.medication.dosage}
                              </p>
                              <p className="text-gray-500 text-xs capitalize dark:text-gray-500">
                                {reminder.medication.frequency}
                              </p>
                            </div>

                            {/* Status Icon */}
                            <div>
                              {reminder.isPast ? (
                                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-500 text-white">
                                  <CheckCircle className="h-6 w-6" />
                                </div>
                              ) : (
                                <div className="flex h-12 w-12 animate-pulse items-center justify-center rounded-full bg-blue-500 text-white">
                                  <Bell className="h-6 w-6" />
                                </div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
