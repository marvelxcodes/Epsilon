"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";

// Mock data for elderly patients
const mockPatients = [
  {
    id: 1,
    name: "Margaret Anderson",
    age: 78,
    room: "Room 204",
    image: "MA",
    heartRate: 72,
    bloodPressure: "120/80",
    lastFall: "2 days ago",
    fallCount: 3,
    status: "stable",
    deviceConnected: true,
    lastActive: "5 mins ago",
    conditions: ["Hypertension", "Arthritis"],
    medications: ["Lisinopril 10mg", "Ibuprofen 400mg"],
  },
  {
    id: 2,
    name: "Robert Johnson",
    age: 82,
    room: "Room 312",
    image: "RJ",
    heartRate: 68,
    bloodPressure: "130/85",
    lastFall: "1 week ago",
    fallCount: 5,
    status: "attention",
    deviceConnected: true,
    lastActive: "2 mins ago",
    conditions: ["Diabetes Type 2", "Heart Disease"],
    medications: ["Metformin 500mg", "Aspirin 81mg"],
  },
  {
    id: 3,
    name: "Elizabeth Chen",
    age: 75,
    room: "Room 108",
    image: "EC",
    heartRate: 75,
    bloodPressure: "125/82",
    lastFall: "Never",
    fallCount: 0,
    status: "excellent",
    deviceConnected: true,
    lastActive: "1 min ago",
    conditions: ["Osteoporosis"],
    medications: ["Calcium + Vitamin D"],
  },
  {
    id: 4,
    name: "William Martinez",
    age: 80,
    room: "Room 215",
    image: "WM",
    heartRate: 0,
    bloodPressure: "--/--",
    lastFall: "3 days ago",
    fallCount: 2,
    status: "offline",
    deviceConnected: false,
    lastActive: "2 hours ago",
    conditions: ["Alzheimer's Disease", "Hypertension"],
    medications: ["Donepezil 10mg", "Amlodipine 5mg"],
  },
];

export default function DashboardPage() {
  const [selectedPatient, setSelectedPatient] = useState(mockPatients[0]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "excellent":
        return "bg-green-500";
      case "stable":
        return "bg-blue-500";
      case "attention":
        return "bg-yellow-500";
      case "offline":
        return "bg-gray-500";
      default:
        return "bg-gray-500";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "excellent":
        return "Excellent";
      case "stable":
        return "Stable";
      case "attention":
        return "Needs Attention";
      case "offline":
        return "Device Offline";
      default:
        return "Unknown";
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Header */}
      <header className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 sticky top-0 z-10">
        <div className="px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
                Epsilon Care Dashboard
              </h1>
              <p className="text-sm text-slate-600 dark:text-slate-400">
                Real-time monitoring for elderly care facilities
              </p>
            </div>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 px-4 py-2 bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg">
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
                <span className="text-sm font-medium text-green-700 dark:text-green-300">
                  All Systems Active
                </span>
              </div>
              <Button variant="outline">Settings</Button>
            </div>
          </div>
        </div>
      </header>

      <div className="flex h-[calc(100vh-89px)]">
        {/* Patient List Sidebar */}
        <aside className="w-80 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 overflow-y-auto">
          <div className="p-4 border-b border-slate-200 dark:border-slate-800">
            <h2 className="text-lg font-semibold text-slate-900 dark:text-white mb-3">
              Patients ({mockPatients.length})
            </h2>
            <input
              type="search"
              placeholder="Search patients..."
              className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="p-4 space-y-3">
            {mockPatients.map((patient) => (
              <button
                key={patient.id}
                type="button"
                onClick={() => setSelectedPatient(patient)}
                className={`w-full text-left p-4 rounded-xl border-2 transition-all ${
                  selectedPatient.id === patient.id
                    ? "border-blue-500 bg-blue-50 dark:bg-blue-950/50"
                    : "border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700"
                }`}
              >
                <div className="flex items-start gap-3">
                  <div className="w-12 h-12 rounded-full bg-blue-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                    {patient.image}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="font-semibold text-slate-900 dark:text-white truncate">
                        {patient.name}
                      </h3>
                      {patient.deviceConnected && (
                        <div className="w-2 h-2 bg-green-500 rounded-full" />
                      )}
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mb-2">
                      {patient.room} • Age {patient.age}
                    </p>
                    <div className="flex items-center gap-2">
                      <div
                        className={`px-2 py-0.5 rounded-full text-xs text-white ${getStatusColor(
                          patient.status
                        )}`}
                      >
                        {getStatusText(patient.status)}
                      </div>
                      {patient.deviceConnected && (
                        <div className="flex items-center gap-1 text-xs text-slate-500 dark:text-slate-400">
                          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
                          </svg>
                          {patient.heartRate} BPM
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto">
          <div className="p-6 max-w-7xl mx-auto">
            {/* Patient Header */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 mb-6">
              <div className="flex items-start justify-between">
                <div className="flex items-start gap-4">
                  <div className="w-20 h-20 rounded-full bg-blue-600 flex items-center justify-center text-white text-2xl font-bold">
                    {selectedPatient.image}
                  </div>
                  <div>
                    <h2 className="text-3xl font-bold text-slate-900 dark:text-white mb-1">
                      {selectedPatient.name}
                    </h2>
                    <div className="flex items-center gap-4 text-slate-600 dark:text-slate-400">
                      <span>{selectedPatient.room}</span>
                      <span>•</span>
                      <span>{selectedPatient.age} years old</span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        {selectedPatient.deviceConnected ? (
                          <>
                            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
                            Active {selectedPatient.lastActive}
                          </>
                        ) : (
                          <>
                            <div className="w-2 h-2 bg-red-500 rounded-full" />
                            Offline {selectedPatient.lastActive}
                          </>
                        )}
                      </span>
                    </div>
                  </div>
                </div>
                <div
                  className={`px-4 py-2 rounded-full text-sm font-medium text-white ${getStatusColor(
                    selectedPatient.status
                  )}`}
                >
                  {getStatusText(selectedPatient.status)}
                </div>
              </div>
            </div>

            {/* Vital Signs */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
              <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-red-100 dark:bg-red-950 flex items-center justify-center">
                    <svg
                      className="w-5 h-5 text-red-600 dark:text-red-400"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400">Heart Rate</p>
                    <p className="text-2xl font-bold text-slate-900 dark:text-white">
                      {selectedPatient.deviceConnected
                        ? `${selectedPatient.heartRate}`
                        : "--"}
                      <span className="text-sm font-normal text-slate-600 dark:text-slate-400 ml-1">
                        BPM
                      </span>
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-950 flex items-center justify-center">
                    <svg
                      className="w-5 h-5 text-blue-600 dark:text-blue-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400">Blood Pressure</p>
                    <p className="text-2xl font-bold text-slate-900 dark:text-white">
                      {selectedPatient.bloodPressure}
                      <span className="text-sm font-normal text-slate-600 dark:text-slate-400 ml-1">
                        mmHg
                      </span>
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-yellow-100 dark:bg-yellow-950 flex items-center justify-center">
                    <svg
                      className="w-5 h-5 text-yellow-600 dark:text-yellow-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400">Total Falls</p>
                    <p className="text-2xl font-bold text-slate-900 dark:text-white">
                      {selectedPatient.fallCount}
                      <span className="text-sm font-normal text-slate-600 dark:text-slate-400 ml-1">
                        incidents
                      </span>
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-purple-100 dark:bg-purple-950 flex items-center justify-center">
                    <svg
                      className="w-5 h-5 text-purple-600 dark:text-purple-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400">Last Fall</p>
                    <p className="text-xl font-bold text-slate-900 dark:text-white">
                      {selectedPatient.lastFall}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Medical Information */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
              {/* Health Conditions */}
              <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                  <svg
                    className="w-5 h-5 text-blue-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  Health Conditions
                </h3>
                <div className="space-y-2">
                  {selectedPatient.conditions.map((condition, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 p-3 bg-slate-50 dark:bg-slate-950 rounded-lg"
                    >
                      <div className="w-2 h-2 bg-red-500 rounded-full" />
                      <span className="text-slate-700 dark:text-slate-300">{condition}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Current Medications */}
              <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6">
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                  <svg
                    className="w-5 h-5 text-green-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
                    />
                  </svg>
                  Current Medications
                </h3>
                <div className="space-y-2">
                  {selectedPatient.medications.map((medication, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 p-3 bg-slate-50 dark:bg-slate-950 rounded-lg"
                    >
                      <div className="w-2 h-2 bg-green-500 rounded-full" />
                      <span className="text-slate-700 dark:text-slate-300">{medication}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Fall Analytics */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <svg
                  className="w-5 h-5 text-yellow-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                  />
                </svg>
                Fall Detection Analytics
              </h3>
              <div className="mt-6">
                {selectedPatient.fallCount > 0 ? (
                  <div className="space-y-4">
                    <div className="p-4 bg-yellow-50 dark:bg-yellow-950/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-900 dark:text-white">
                            Fall Frequency: {selectedPatient.fallCount} incidents
                          </p>
                          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                            Last recorded: {selectedPatient.lastFall}
                          </p>
                        </div>
                        <span className="px-3 py-1 bg-yellow-200 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200 rounded-full text-xs font-medium">
                          Monitor Closely
                        </span>
                      </div>
                    </div>
                    <div className="grid grid-cols-7 gap-2">
                      {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((day, idx) => (
                        <div key={day} className="text-center">
                          <div className="text-xs text-slate-600 dark:text-slate-400 mb-2">
                            {day}
                          </div>
                          <div
                            className={`h-12 rounded ${
                              idx === 2 || idx === 5
                                ? "bg-red-500"
                                : "bg-slate-100 dark:bg-slate-800"
                            }`}
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="text-center py-8">
                    <div className="w-16 h-16 bg-green-100 dark:bg-green-950 rounded-full flex items-center justify-center mx-auto mb-4">
                      <svg
                        className="w-8 h-8 text-green-600 dark:text-green-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                    </div>
                    <p className="text-lg font-medium text-slate-900 dark:text-white">
                      No Falls Recorded
                    </p>
                    <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                      This patient has an excellent safety record
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
