"use client";

import { Button } from "@/components/ui/button";
import Link from "next/link";
import { useEffect, useRef } from "react";

const steps = [
  {
    step: "01",
    title: "Download App",
    desc: "Get the Epsilon app from Play Store or App Store",
  },
  {
    step: "02",
    title: "Connect Watch",
    desc: "Pair your wearable device via WiFi in seconds",
  },
  {
    step: "03",
    title: "Configure Contacts",
    desc: "Add emergency contacts and customize settings",
  },
  {
    step: "04",
    title: "Stay Protected",
    desc: "24/7 monitoring with instant emergency response",
  },
];

export default function Home() {
  const heroRef = useRef<HTMLDivElement>(null);
  const watchRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleScroll = () => {
      if (watchRef.current) {
        const scrollY = window.scrollY;
        const rotation = scrollY * 0.3;
        const scale = Math.max(0.8, 1 - scrollY * 0.0005);
        watchRef.current.style.transform = `rotateY(${rotation}deg) scale(${scale})`;
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <main className="flex min-h-screen flex-col overflow-x-hidden">
      {/* Hero Section */}
      <section
        ref={heroRef}
        className="relative min-h-screen flex items-center justify-center bg-linear-to-br from-slate-50 via-blue-50 to-slate-100 dark:from-slate-900 dark:via-blue-950 dark:to-slate-900 pt-20"
      >
        {/* Animated background blobs */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 -left-20 w-96 h-96 bg-blue-400/20 dark:bg-blue-600/20 rounded-full blur-3xl animate-pulse" />
          <div className="absolute bottom-1/4 -right-20 w-[500px] h-[500px] bg-blue-500/10 dark:bg-blue-400/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '700ms' }} />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-linear-to-br from-blue-300/5 to-transparent dark:from-blue-500/10 rounded-full blur-3xl" />
        </div>

        <div className="container mx-auto px-6 relative z-10">
          <div className="grid lg:grid-cols-2 gap-16 items-center">
            {/* Left Content */}
            <div className="space-y-8">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-100 dark:bg-blue-950 border border-blue-200 dark:border-blue-800">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                <span className="text-sm font-medium text-blue-700 dark:text-blue-300">
                  Team Epsilon
                </span>
              </div>

              <h1 className="text-5xl md:text-7xl font-bold leading-tight">
                <span className="bg-clip-text text-transparent bg-linear-to-r from-slate-900 to-slate-700 dark:from-white dark:to-slate-300">
                  Visionary
                </span>
                <br />
                <span className="bg-clip-text text-transparent bg-linear-to-r from-blue-600 to-blue-400 italic">
                  Intelligence
                </span>
              </h1>

              <p className="text-xl text-slate-600 dark:text-slate-300 leading-relaxed max-w-xl">
                Smart wearable technology redefined with best-in-class elderly safety.
                We craft intelligent solutions to ensure peace of mind for your loved ones.
              </p>

              <div className="flex flex-wrap gap-4 pt-4">
                <Link href="/login">
                  <Button
                    size="lg"
                    className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-6 text-lg rounded-full shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105"
                  >
                    Get Started
                  </Button>
                </Link>
                <a
                  target="_blank"
                  href="https://drive.google.com/drive/folders/17TV-YBChv-CcmzILdUiMXs1KitCvrxVa?usp=sharing"
                  rel="noopener noreferrer"
                >
                  <Button
                    size="lg"
                    variant="outline"
                    className="border-2 border-blue-600 text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950 px-8 py-6 text-lg rounded-full transition-all duration-300"
                  >
                    Download App
                  </Button>
                </a>
              </div>

              {/* Trust badges */}
              <div className="flex items-center gap-4 pt-8">
                <div className="flex -space-x-2">
                  {[1, 2, 3].map((i) => (
                    <div
                      key={i}
                      className="w-10 h-10 rounded-full bg-linear-to-br from-blue-400 to-blue-600 border-2 border-white dark:border-slate-900 flex items-center justify-center text-white font-semibold text-sm"
                    >
                      {String.fromCharCode(64 + i)}
                    </div>
                  ))}
                </div>
                <div>
                  <div className="flex items-center gap-1">
                    {[...Array(5)].map((_, i) => (
                      <svg
                        key={i}
                        className="w-4 h-4 text-yellow-400 fill-current"
                        viewBox="0 0 20 20"
                      >
                        <path d="M10 15l-5.878 3.09 1.123-6.545L.489 6.91l6.572-.955L10 0l2.939 5.955 6.572.955-4.756 4.635 1.123 6.545z" />
                      </svg>
                    ))}
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
                    Trusted by families worldwide
                  </p>
                </div>
              </div>
            </div>

            {/* Right - 3D Watch Visual */}
            <div className="relative h-[600px] flex items-center justify-center">
              <div
                ref={watchRef}
                className="relative w-full h-full flex items-center justify-center"
                style={{
                  perspective: "1000px",
                  transformStyle: "preserve-3d",
                  transition: "transform 0.1s ease-out",
                }}
              >
                {/* Watch Container */}
                <div className="relative w-80 h-80">
                  {/* Watch Body */}
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="relative w-56 h-56 bg-linear-to-br from-slate-800 to-slate-950 rounded-[3rem] shadow-2xl overflow-hidden border-4 border-blue-500/50">
                      {/* Watch Screen */}
                      <div className="absolute inset-2 bg-black rounded-[2.5rem] flex flex-col items-center justify-center p-6">
                        {/* Time Display */}

                      </div>

                      {/* Side Button */}
                      <div className="absolute -right-1 top-1/3 w-2 h-12 bg-blue-600 rounded-l-lg" />

                      {/* LED Indicator */}

                    </div>
                  </div>

                  {/* Watch Strap - Top */}
                  <div className="absolute -top-20 left-1/2 -translate-x-1/2 w-24 h-24">
                    <div className="w-full h-full bg-linear-to-b from-blue-600 to-blue-700 rounded-t-3xl shadow-lg">
                      <div className="grid grid-cols-4 gap-1 p-2">
                        {[...Array(16)].map((_, i) => (
                          <div
                            key={i}
                            className="w-full aspect-square bg-blue-800/50 rounded-sm"
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Watch Strap - Bottom */}
                  <div className="absolute -bottom-20 left-1/2 -translate-x-1/2 w-24 h-24">
                    <div className="w-full h-full bg-linear-to-t from-blue-600 to-blue-700 rounded-b-3xl shadow-lg">
                      <div className="grid grid-cols-4 gap-1 p-2">
                        {[...Array(16)].map((_, i) => (
                          <div
                            key={i}
                            className="w-full aspect-square bg-blue-800/50 rounded-sm"
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Floating Elements */}
                  <div className="absolute -top-8 -right-8 w-20 h-20 bg-blue-500/20 rounded-full blur-xl animate-pulse" />
                  <div className="absolute -bottom-8 -left-8 w-24 h-24 bg-blue-400/20 rounded-full blur-xl animate-pulse" style={{ animationDelay: '500ms' }} />

                  {/* Alert Badge */}
                  <div className="absolute -top-2 -right-2 w-14 h-14 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center shadow-lg animate-bounce">
                    <svg
                      viewBox="0 0 24 24"
                      className="w-7 h-7 text-white"
                      fill="none"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                      />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Scroll Indicator */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
          <svg
            className="w-6 h-6 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 14l-7 7m0 0l-7-7m7 7V3"
            />
          </svg>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-32 bg-white dark:bg-slate-900 relative"
      >
        <div className="container mx-auto px-6">
          <div className="text-center mb-20">
            <h2 className="text-4xl md:text-5xl font-bold mb-6 bg-clip-text text-transparent bg-linear-to-r from-slate-900 to-slate-700 dark:from-white dark:to-slate-300">
              Comprehensive Safety Features
            </h2>
            <p className="text-xl text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
              Advanced technology designed to keep your loved ones safe, connected, and
              independent
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Fall Detection */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Fall Detection
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                AI-powered fall detection with 5-second warning LED and automatic emergency
                contact system. Never alone during emergencies.
              </p>
            </div>

            {/* SOS Alert */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                SOS Alert
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                One-touch emergency button or triple-press activation. Instant notifications
                to caregivers with location tracking.
              </p>
            </div>

            {/* Medication Reminders */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Smart Reminders
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Configurable medication schedules with smart notifications. Manage remotely
                through web or mobile dashboard.
              </p>
            </div>

            {/* WiFi Connectivity */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Always Connected
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Easy WiFi setup through mobile app. Seamless connectivity ensures real-time
                monitoring and instant alerts.
              </p>
            </div>

            {/* Health Monitoring */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Health Tracking
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Monitor vital signs and activity patterns. Get insights into daily routines
                and health trends.
              </p>
            </div>

            {/* Remote Management */}
            <div className="group relative bg-linear-to-br from-slate-50 to-blue-50 dark:from-slate-800 dark:to-blue-950 p-8 rounded-3xl border border-blue-100 dark:border-blue-900 hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
              <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform shadow-lg">
                <svg
                  viewBox="0 0 24 24"
                  className="w-8 h-8 text-white"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"
                  />
                </svg>
              </div>
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Remote Dashboard
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Web and mobile apps for family members. Configure settings, view history, and
                manage contacts remotely.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-32 bg-slate-900 text-white">
        <div className="container mx-auto px-6">
          <div className="text-center mb-20">
            <h2 className="text-5xl font-bold mb-6">How It Works</h2>
            <p className="text-xl text-slate-300 max-w-2xl mx-auto">
              Simple setup, powerful protection. Get started in minutes.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 max-w-6xl mx-auto">
            {/* Step 1 */}
            <div className="text-center">
              <div className="text-6xl font-bold text-white mb-4">01</div>
              <div className="w-20 h-20 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-xl">
                <svg
                  viewBox="0 0 24 24"
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-3">Download App</h3>
              <p className="text-slate-400">Get the Epsilon app from Play Store or App Store</p>
            </div>

            {/* Step 2 */}
            <div className="text-center">
              <div className="text-6xl font-bold text-white  mb-4">02</div>
              <div className="w-20 h-20 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-xl">
                <svg
                  viewBox="0 0 24 24"
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-3">Connect Watch</h3>
              <p className="text-slate-400">Pair your wearable device via WiFi in seconds</p>
            </div>

            {/* Step 3 */}
            <div className="text-center">
              <div className="text-6xl font-bold text-white  mb-4">03</div>
              <div className="w-20 h-20 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-xl">
                <svg
                  viewBox="0 0 24 24"
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-3">Configure Contacts</h3>
              <p className="text-slate-400">Add emergency contacts and customize settings</p>
            </div>

            {/* Step 4 */}
            <div className="text-center">
              <div className="text-6xl font-bold text-white mb-4">04</div>
              <div className="w-20 h-20 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-xl">
                <svg
                  viewBox="0 0 24 24"
                  className="w-10 h-10 text-white"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold mb-3">Stay Protected</h3>
              <p className="text-slate-400">24/7 monitoring with instant emergency response</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-32 bg-blue-600 text-white">
        <div className="container mx-auto px-6 text-center">
          <h2 className="text-5xl md:text-6xl font-bold mb-6">
            Ready to ensure safety for your loved ones?
          </h2>
          <p className="text-xl mb-12 max-w-2xl mx-auto">
            Join thousands of families who trust Epsilon for elderly care and safety
          </p>
          <Link href="/login">
            <Button
              size="lg"
              className="bg-white text-blue-600 hover:bg-blue-50 px-12 py-6 text-xl rounded-full shadow-2xl"
            >
              Get Started Now →
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-slate-900 text-slate-400 py-12">
        <div className="container mx-auto px-6">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center gap-2 mb-4 md:mb-0">
              <svg
                className="h-8 w-8 text-blue-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                />
              </svg>
              <span className="font-bold text-white text-xl">Team Epsilon</span>
            </div>
            <p className="text-sm">© 2025 Epsilon. Smart Wearable Solution.</p>
          </div>
        </div>
      </footer>
    </main>
  );
}
