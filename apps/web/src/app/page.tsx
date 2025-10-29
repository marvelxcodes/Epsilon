"use client";

import { Button } from "@/components/ui/button";
import Link from "next/link";

function WearableIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      className="w-full h-full"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2"
      />
    </svg>
  );
}

function FallDetectionIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      className="w-full h-full"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"
      />
    </svg>
  );
}

function SOSIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      className="w-full h-full"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
      />
    </svg>
  );
}

function MedicationIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      className="w-full h-full"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
      />
    </svg>
  );
}

function ConfigIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      className="w-full h-full"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
      />
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
      />
    </svg>
  );
}

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col">
      {/* Hero Section */}
      <section className="relative bg-black text-white pt-28 pb-20">
  <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-blue-500/20 via-transparent to-transparent opacity-50"></div>
        <div className="container mx-auto px-6 relative">
          <div className="flex flex-col lg:flex-row items-center justify-between gap-16">
            <div className="lg:w-1/2 space-y-10">
              <h1 className="text-6xl font-bold leading-tight tracking-tight">
                Advanced Care for{" "}
                <span className="text-blue-500 block mt-2">Elderly Safety</span>{" "}
                <span className="mt-2 block">and Peace of Mind</span>
              </h1>
              <p className="text-xl text-gray-300 leading-relaxed">
                A comprehensive wearable solution featuring fall detection, SOS alerts, and
                smart home monitoring for enhanced elderly care and safety.
              </p>
              <div className="flex gap-4 pt-4">
                <Link href="/dashboard">
                  <Button size="lg" className="bg-blue-500 text-white hover:bg-blue-600 text-lg px-8">
                    Get Started →
                  </Button>
                </Link>
				<a target="_blank" href="https://drive.google.com/drive/folders/17TV-YBChv-CcmzILdUiMXs1KitCvrxVa?usp=sharing" rel="noopener noreferrer">

                <Button size="lg" variant="outline" className="border-2 border-blue-500 text-blue-500 hover:bg-blue-500/10 text-lg px-8">
                  Download App
                  <svg className="ml-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                </Button>
				</a>
              </div>
            </div>
            <div className="lg:w-1/2 mt-10 lg:mt-0">
              <div className="relative w-full h-[500px] rounded-lg p-8">
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="w-96 h-96 relative">
                    {/* Main device illustration */}
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="w-64 h-64 bg-blue-500 rounded-4xl shadow-2xl relative overflow-hidden">
                        {/* Screen content */}
                        <div className="absolute inset-3 bg-black rounded-3xl flex flex-col items-center justify-center p-4">
                          {/* Health monitoring waves */}
                          <svg viewBox="0 0 100 40" className="w-full h-16 text-blue-500">
                            <path
                              d="M0 20 Q 15 20 20 10 Q 25 0 30 20 Q 35 40 40 20 Q 45 0 50 20 Q 55 40 60 20 Q 65 0 70 20 Q 75 40 80 20 Q 85 0 90 20 Q 95 40 100 20"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                              className="animate-pulse"
                            />
                          </svg>
                          {/* SOS Text */}
                          <div className="text-blue-500 text-xl font-bold mt-2">SOS</div>
                        </div>
                        {/* Side button */}
                        <div className="absolute -right-1 top-1/3 h-8 w-2 bg-gray-800 rounded-l-lg" />
                      </div>
                    </div>
                    {/* Decorative elements */}
                    <div className="absolute -top-8 -right-8 w-32 h-32 bg-blue-500/20 rounded-full animate-pulse" />
                    <div className="absolute -bottom-4 -left-4 w-24 h-24 bg-blue-500/20 rounded-full animate-pulse delay-150" />
                    {/* Alert indicators */}
                    <div className="absolute -top-2 -right-2 w-16 h-16 bg-blue-500 rounded-2xl flex items-center justify-center animate-bounce">
                      <svg viewBox="0 0 24 24" className="w-8 h-8 text-black">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          stroke="currentColor"
                          fill="none"
                          d="M15 10l-5 5M10 10l5 5M12 3c7.2 0 9 1.8 9 9s-1.8 9-9 9-9-1.8-9-9 1.8-9 9-9z"
                        />
                      </svg>
                    </div>
                  </div>
                </div>
                <div className="absolute -bottom-4 left-1/2 transform -translate-x-1/2 w-full max-w-md">
                  <div className="text-center bg-blue-500 text-white rounded-lg p-6 shadow-2xl">
                    <h3 className="text-2xl font-bold mb-1">Team Epsilon</h3>
                    <p className="text-black/80 font-medium">Smart Wearable Solution</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gray-50">
        <div className="container mx-auto px-6">
          <h2 className="text-3xl font-bold text-center mb-16">
            Comprehensive Safety Features
          </h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Fall Detection */}
            <div className="group bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg hover:shadow-xl transition-all">
              <div className="w-12 h-12 bg-blue-500 text-white rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <div className="w-6 h-6">
                  <FallDetectionIcon />
                </div>
              </div>
              <h3 className="text-xl font-semibold mb-2">Fall Detection</h3>
              <p className="text-gray-600 dark:text-gray-400">
                Advanced fall detection with a 5-second blue warning glow and automatic emergency contact system.
              </p>
            </div>

            {/* SOS Alert */}
            <div className="group bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg hover:shadow-xl transition-all">
              <div className="w-12 h-12 bg-blue-500 text-white rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <div className="w-6 h-6">
                  <SOSIcon />
                </div>
              </div>
              <h3 className="text-xl font-semibold mb-2">SOS Alert</h3>
              <p className="text-gray-600 dark:text-gray-400">
                Triple-press emergency button for instant contact with designated caregivers. One-touch SOS activation.
              </p>
            </div>

            {/* Medication Reminders */}
            <div className="group bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg hover:shadow-xl transition-all">
              <div className="w-12 h-12 bg-blue-500 text-white rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                <div className="w-6 h-6">
                  <MedicationIcon />
                </div>
              </div>
              <h3 className="text-xl font-semibold mb-2">Smart Reminders</h3>
              <p className="text-gray-600 dark:text-gray-400">
                Configurable medication schedules with smart notifications. Manage through web or mobile app.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20 bg-black text-white">
        <div className="container mx-auto px-6">
          <h2 className="text-3xl font-bold text-center mb-16">Smart Safety Features</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center group">
              <div className="w-16 h-16 bg-blue-500 text-white rounded-full flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                <div className="w-8 h-8">
                  <WearableIcon />
                </div>
              </div>
              <h3 className="font-semibold mb-2">WiFi Configuration</h3>
              <p className="text-gray-400">
                Easy device setup with WiFi configuration for constant connectivity
              </p>
            </div>
            <div className="text-center group">
              <div className="w-16 h-16 bg-blue-500 text-white rounded-full flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                <div className="w-8 h-8">
                  <ConfigIcon />
                </div>
              </div>
              <h3 className="font-semibold mb-2">Smart Configuration</h3>
              <p className="text-gray-400">
                Customize fall detection sensitivity and emergency contact settings
              </p>
            </div>
            <div className="text-center group">
              <div className="w-16 h-16 bg-blue-500 text-white rounded-full flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                <div className="w-8 h-8">
                  <SOSIcon />
                </div>
              </div>
              <h3 className="font-semibold mb-2">Instant Alerts</h3>
              <p className="text-gray-400">
                5-second warning with a blue glow before automatic emergency contact
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-blue-500">
        <div className="container mx-auto px-6 text-center">
          <h2 className="text-3xl font-bold mb-8">
            Ready to ensure safety for your loved ones?
          </h2>x
          <Link href="/dashboard">
            <Button className="bg-black text-white hover:bg-gray-900">
              Get Started Now →
            </Button>
          </Link>
        </div>
      </section>
    </main>
  );
}
