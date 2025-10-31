"use client";

import {
  Bell,
  BellOff,
  Calendar,
  Clock,
  Edit,
  Pill,
  Plus,
  Power,
  Trash2,
} from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";

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

function MedicationIcon() {
  return (
    <svg
      className="h-full w-full"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
      />
    </svg>
  );
}

export default function MedicationsPage() {
  const [medications, setMedications] = useState<Medication[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingMedication, setEditingMedication] = useState<Medication | null>(
    null
  );
  const [formData, setFormData] = useState({
    name: "",
    dosage: "",
    frequency: "daily",
    time: "08:00",
    startDate: new Date().toISOString().split("T")[0],
    endDate: "",
    notes: "",
    reminderEnabled: true,
  });

  useEffect(() => {
    fetchMedications();
  }, []);

  const fetchMedications = async () => {
    try {
      const response = await fetch("/api/medicine");
      if (response.ok) {
        const data = await response.json();
        setMedications(data.medicines || []);
      }
    } catch (error) {
      console.error("Failed to fetch medications:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      const url = editingMedication
        ? `/api/medicine/${editingMedication.id}`
        : "/api/medicine";
      const method = editingMedication ? "PUT" : "POST";

      const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        fetchMedications();
        setShowAddDialog(false);
        setEditingMedication(null);
        resetForm();
      }
    } catch (error) {
      console.error("Failed to save medication:", error);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this medication?")) return;

    try {
      const response = await fetch(`/api/medicine/${id}`, {
        method: "DELETE",
      });

      if (response.ok) {
        fetchMedications();
      }
    } catch (error) {
      console.error("Failed to delete medication:", error);
    }
  };

  const toggleActive = async (med: Medication) => {
    try {
      const response = await fetch(`/api/medicine/${med.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          isActive: med.isActive !== "true",
        }),
      });

      if (response.ok) {
        fetchMedications();
      }
    } catch (error) {
      console.error("Failed to toggle medication:", error);
    }
  };

  const resetForm = () => {
    setFormData({
      name: "",
      dosage: "",
      frequency: "daily",
      time: "08:00",
      startDate: new Date().toISOString().split("T")[0],
      endDate: "",
      notes: "",
      reminderEnabled: true,
    });
  };

  const openEditDialog = (med: Medication) => {
    setEditingMedication(med);
    setFormData({
      name: med.name,
      dosage: med.dosage,
      frequency: med.frequency,
      time: med.time,
      startDate: med.startDate,
      endDate: med.endDate || "",
      notes: med.notes || "",
      reminderEnabled: med.reminderEnabled === "true",
    });
    setShowAddDialog(true);
  };

  return (
    <main className="min-h-screen bg-gray-50 dark:bg-black">
      {/* Header Section */}
      <section className="relative bg-black pt-28 pb-12 text-white">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-blue-500/20 via-transparent to-transparent opacity-50" />
        <div className="container relative mx-auto px-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-lg bg-blue-500">
                <div className="h-8 w-8">
                  <MedicationIcon />
                </div>
              </div>
              <div>
                <h1 className="font-bold text-4xl">Medications</h1>
                <p className="mt-2 text-gray-300">
                  Manage your medication schedule and reminders
                </p>
              </div>
            </div>
            <Button
              className="bg-blue-500 text-white hover:bg-blue-600"
              onClick={() => {
                resetForm();
                setEditingMedication(null);
                setShowAddDialog(true);
              }}
              size="lg"
            >
              <Plus className="mr-2 h-5 w-5" />
              Add Medication
            </Button>
          </div>
        </div>
      </section>

      {/* Medications List */}
      <section className="py-12">
        <div className="container mx-auto px-6">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
            </div>
          ) : medications.length === 0 ? (
            <div className="py-20 text-center">
              <div className="mx-auto mb-4 flex h-24 w-24 items-center justify-center rounded-full bg-gray-200 dark:bg-gray-800">
                <Pill className="h-12 w-12 text-gray-400" />
              </div>
              <h3 className="mb-2 font-semibold text-xl">No medications yet</h3>
              <p className="mb-6 text-gray-600 dark:text-gray-400">
                Start by adding your first medication
              </p>
              <Button
                className="bg-blue-500 text-white hover:bg-blue-600"
                onClick={() => setShowAddDialog(true)}
              >
                <Plus className="mr-2 h-4 w-4" />
                Add Medication
              </Button>
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {medications.map((med) => (
                <div
                  className={`group rounded-lg border-2 bg-white p-6 shadow-lg transition-all hover:shadow-xl dark:bg-gray-800 ${
                    med.isActive === "true"
                      ? "border-blue-500/20"
                      : "border-gray-200 opacity-60 dark:border-gray-700"
                  }`}
                  key={med.id}
                >
                  {/* Header */}
                  <div className="mb-4 flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-500 text-white transition-transform group-hover:scale-110">
                        <Pill className="h-6 w-6" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-xl">{med.name}</h3>
                        <p className="text-gray-600 text-sm dark:text-gray-400">
                          {med.dosage}
                        </p>
                      </div>
                    </div>
                    <button
                      className={`rounded-lg p-2 transition-colors ${
                        med.isActive === "true"
                          ? "bg-blue-500/10 text-blue-500"
                          : "bg-gray-200 text-gray-500 dark:bg-gray-700"
                      }`}
                      onClick={() => toggleActive(med)}
                      type="button"
                    >
                      <Power className="h-4 w-4" />
                    </button>
                  </div>

                  {/* Details */}
                  <div className="mb-4 space-y-3">
                    <div className="flex items-center gap-2 text-sm">
                      <Clock className="h-4 w-4 text-gray-500" />
                      <span className="text-gray-700 dark:text-gray-300">
                        {med.time}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-sm">
                      <Calendar className="h-4 w-4 text-gray-500" />
                      <span className="text-gray-700 capitalize dark:text-gray-300">
                        {med.frequency}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-sm">
                      {med.reminderEnabled === "true" ? (
                        <>
                          <Bell className="h-4 w-4 text-blue-500" />
                          <span className="text-blue-500">
                            Reminders enabled
                          </span>
                        </>
                      ) : (
                        <>
                          <BellOff className="h-4 w-4 text-gray-500" />
                          <span className="text-gray-500">Reminders off</span>
                        </>
                      )}
                    </div>
                  </div>

                  {med.notes && (
                    <p className="mb-4 rounded bg-gray-50 p-3 text-gray-600 text-sm dark:bg-gray-900 dark:text-gray-400">
                      {med.notes}
                    </p>
                  )}

                  {/* Actions */}
                  <div className="flex gap-2 border-gray-200 border-t pt-4 dark:border-gray-700">
                    <Button
                      className="flex-1"
                      onClick={() => openEditDialog(med)}
                      size="sm"
                      variant="outline"
                    >
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Button>
                    <Button
                      className="text-red-500 hover:bg-red-50 dark:hover:bg-red-950"
                      onClick={() => handleDelete(med.id)}
                      size="sm"
                      variant="outline"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Add/Edit Dialog */}
      <Dialog onOpenChange={setShowAddDialog} open={showAddDialog}>
        <DialogContent className="max-h-[90vh] max-w-md overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingMedication ? "Edit Medication" : "Add Medication"}
            </DialogTitle>
            <DialogDescription>
              {editingMedication
                ? "Update the medication details below"
                : "Fill in the details to add a new medication"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">Medication Name *</Label>
              <Input
                id="name"
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                placeholder="e.g., Aspirin"
                value={formData.name}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="dosage">Dosage *</Label>
              <Input
                id="dosage"
                onChange={(e) =>
                  setFormData({ ...formData, dosage: e.target.value })
                }
                placeholder="e.g., 100mg"
                value={formData.dosage}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="frequency">Frequency *</Label>
              <Select
                onValueChange={(value) =>
                  setFormData({ ...formData, frequency: value })
                }
                value={formData.frequency}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="daily">Daily</SelectItem>
                  <SelectItem value="twice-daily">Twice Daily</SelectItem>
                  <SelectItem value="three-times-daily">
                    Three Times Daily
                  </SelectItem>
                  <SelectItem value="weekly">Weekly</SelectItem>
                  <SelectItem value="as-needed">As Needed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="time">Time *</Label>
              <Input
                id="time"
                onChange={(e) =>
                  setFormData({ ...formData, time: e.target.value })
                }
                type="time"
                value={formData.time}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date *</Label>
              <Input
                id="startDate"
                onChange={(e) =>
                  setFormData({ ...formData, startDate: e.target.value })
                }
                type="date"
                value={formData.startDate}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate">End Date (Optional)</Label>
              <Input
                id="endDate"
                onChange={(e) =>
                  setFormData({ ...formData, endDate: e.target.value })
                }
                type="date"
                value={formData.endDate}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="notes">Notes (Optional)</Label>
              <Textarea
                id="notes"
                onChange={(e) =>
                  setFormData({ ...formData, notes: e.target.value })
                }
                placeholder="Any special instructions..."
                rows={3}
                value={formData.notes}
              />
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                checked={formData.reminderEnabled}
                id="reminder"
                onCheckedChange={(checked) =>
                  setFormData({ ...formData, reminderEnabled: checked })
                }
              />
              <Label htmlFor="reminder">Enable reminders</Label>
            </div>
          </div>
          <DialogFooter>
            <Button
              onClick={() => {
                setShowAddDialog(false);
                setEditingMedication(null);
                resetForm();
              }}
              variant="outline"
            >
              Cancel
            </Button>
            <Button
              className="bg-blue-500 text-white hover:bg-blue-600"
              disabled={!(formData.name && formData.dosage && formData.time)}
              onClick={handleSubmit}
            >
              {editingMedication ? "Update" : "Add"} Medication
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </main>
  );
}
