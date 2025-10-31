"use client";

import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function AboutPage() {
  return (
    <main className="flex min-h-screen flex-col pt-16">
      {/* Hero Section */}
      <section className="relative bg-linear-to-br from-blue-600 to-blue-700 text-white py-32">
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-10" />
        <div className="container mx-auto px-6 relative z-10">
          <div className="max-w-4xl mx-auto text-center space-y-6">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30">
              <span className="text-sm font-medium">Team Epsilon</span>
            </div>
            <h1 className="text-5xl md:text-7xl font-bold">About Our Mission</h1>
            <p className="text-xl text-blue-100 leading-relaxed">
              Redefining elderly safety through intelligent wearable technology and
              compassionate care solutions
            </p>
          </div>
        </div>
      </section>

      {/* Mission Section */}
      <section className="py-20 bg-white dark:bg-slate-900">
        <div className="container mx-auto px-6">
          <div className="grid lg:grid-cols-2 gap-16 items-center">
            <div className="space-y-6">
              <h2 className="text-4xl font-bold text-slate-900 dark:text-white">
                Our Story
              </h2>
              <p className="text-lg text-slate-600 dark:text-slate-400 leading-relaxed">
                Team Epsilon was born from a simple yet profound observation: elderly
                safety is often overlooked until it's too late. Falls are one of the
                leading causes of injuries among older adults, and quick response time can
                make all the difference.
              </p>
              <p className="text-lg text-slate-600 dark:text-slate-400 leading-relaxed">
                We noticed that many families rely on phone calls or manual check-ins —
                which are not reliable in real-time emergencies. That's why we decided to
                build a smart, connected, and automatic solution that can detect falls, send
                alerts instantly, and allow families to respond immediately.
              </p>
            </div>
            <div className="relative h-96 rounded-3xl overflow-hidden shadow-2xl">
              <div className="absolute inset-0 bg-linear-to-br from-blue-500/20 to-blue-600/20 backdrop-blur-sm flex items-center justify-center">
                <div className="text-center space-y-4">
                  <div className="w-32 h-32 mx-auto bg-white dark:bg-slate-800 rounded-full flex items-center justify-center shadow-xl">
                    <svg
                      className="w-16 h-16 text-blue-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M13 10V3L4 14h7v7l9-11h-7z"
                      />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-bold text-slate-900 dark:text-white">
                    Team Epsilon
                  </h3>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Problem Statement */}
      <section className="py-20 bg-slate-50 dark:bg-slate-950">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto space-y-12">
            <div className="text-center space-y-4">
              <h2 className="text-4xl font-bold text-slate-900 dark:text-white">
                Why We Chose This Challenge
              </h2>
              <p className="text-xl text-slate-600 dark:text-slate-400">
                Addressing a critical gap in elderly care
              </p>
            </div>

            <div className="grid md:grid-cols-2 gap-8">
              <div className="bg-white dark:bg-slate-900 p-8 rounded-2xl border border-blue-100 dark:border-blue-900">
                <div className="w-12 h-12 bg-linear-to-br from-blue-500 to-blue-600 rounded-xl flex items-center justify-center mb-4">
                  <svg
                    className="w-6 h-6 text-white"
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
                <h3 className="text-xl font-bold mb-3 text-slate-900 dark:text-white">
                  The Problem
                </h3>
                <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                  Falls are the leading cause of injury among older adults. Many elderly
                  individuals live alone or have limited mobility, making quick response
                  times critical during emergencies.
                </p>
              </div>

              <div className="bg-white dark:bg-slate-900 p-8 rounded-2xl border border-blue-100 dark:border-blue-900">
                <div className="w-12 h-12 bg-linear-to-br from-blue-500 to-blue-600 rounded-xl flex items-center justify-center mb-4">
                  <svg
                    className="w-6 h-6 text-white"
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
                <h3 className="text-xl font-bold mb-3 text-slate-900 dark:text-white">
                  Our Solution
                </h3>
                <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                  A smart wearable that automatically detects falls, sends instant alerts,
                  and provides 24/7 monitoring — ensuring elderly individuals are never
                  left unattended during emergencies.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20 bg-white dark:bg-slate-900">
        <div className="container mx-auto px-6">
          <div className="max-w-5xl mx-auto space-y-12">
            <div className="text-center space-y-4">
              <h2 className="text-4xl font-bold text-slate-900 dark:text-white">
                How Epsilon Works
              </h2>
              <p className="text-xl text-slate-600 dark:text-slate-400">
                A comprehensive workflow designed for seamless safety
              </p>
            </div>

            <div className="space-y-8">
              {[
                {
                  step: "1",
                  title: "Setup & Configuration",
                  description:
                    "Users log into the mobile app and connect the wearable device using WiFi. Configure SSID, password, and emergency contact numbers through an intuitive interface.",
                },
                {
                  step: "2",
                  title: "Continuous Monitoring",
                  description:
                    "The wearable stays connected to the internet 24/7, monitoring for falls and emergency situations. Caregivers can manage settings remotely through the web dashboard.",
                },
                {
                  step: "3",
                  title: "Emergency Response",
                  description:
                    "SOS feature with single-button trigger or triple-press watch activation. Fall detection with 5-second warning LED gives users a chance to cancel false alarms.",
                },
                {
                  step: "4",
                  title: "Instant Alerts",
                  description:
                    "If no response is received after fall detection, the system automatically sends calls and notifications to designated contacts with location information.",
                },
              ].map((item, i) => (
                <div
                  key={i}
                  className="flex gap-6 items-start bg-slate-50 dark:bg-slate-950 p-6 rounded-2xl border border-blue-100 dark:border-blue-900"
                >
                  <div className="flex-shrink-0 w-12 h-12 bg-linear-to-br from-blue-600 to-blue-500 text-white rounded-xl flex items-center justify-center font-bold text-xl shadow-lg">
                    {item.step}
                  </div>
                  <div className="space-y-2">
                    <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                      {item.title}
                    </h3>
                    <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                      {item.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Impact & Future */}
      <section className="py-20 bg-linear-to-br from-blue-50 to-slate-50 dark:from-slate-950 dark:to-blue-950">
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto space-y-12">
            <div className="text-center space-y-4">
              <h2 className="text-4xl font-bold text-slate-900 dark:text-white">
                Impact & Scalability
              </h2>
            </div>

            <div className="grid md:grid-cols-3 gap-8">
              <div className="text-center space-y-4">
                <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mx-auto shadow-lg">
                  <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                    />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                  Elderly Users
                </h3>
                <p className="text-slate-600 dark:text-slate-400">
                  Independent living with 24/7 safety monitoring and instant emergency
                  response
                </p>
              </div>

              <div className="text-center space-y-4">
                <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mx-auto shadow-lg">
                  <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                    />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                  Healthcare Facilities
                </h3>
                <p className="text-slate-600 dark:text-slate-400">
                  Scalable for assisted-living facilities, nursing homes, and home healthcare
                  providers
                </p>
              </div>

              <div className="text-center space-y-4">
                <div className="w-16 h-16 bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mx-auto shadow-lg">
                  <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
                    />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                  Global Impact
                </h3>
                <p className="text-slate-600 dark:text-slate-400">
                  Addressing the needs of a rapidly aging global population with smart,
                  connected care
                </p>
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 p-8 rounded-2xl border border-blue-100 dark:border-blue-900">
              <h3 className="text-2xl font-bold mb-4 text-slate-900 dark:text-white">
                Future Development
              </h3>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed mb-4">
                Beyond our current MVP, we plan to integrate:
              </p>
              <ul className="space-y-3 text-slate-600 dark:text-slate-400">
                <li className="flex items-start gap-3">
                  <svg
                    className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span>
                    <strong>AI-based fall detection</strong> for improved accuracy and reduced
                    false positives
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <svg
                    className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span>
                    <strong>Camera-based intruder detection</strong> for enhanced home security
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <svg
                    className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span>
                    <strong>Memory assistance features</strong> to help users locate misplaced
                    items
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <svg
                    className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <span>
                    <strong>Advanced health monitoring</strong> including heart rate, blood
                    pressure, and activity tracking
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 bg-linear-to-br from-blue-600 to-blue-700 text-white">
        <div className="container mx-auto px-6 text-center">
          <h2 className="text-4xl font-bold mb-6">
            Join us in making independent living safer
          </h2>
          <p className="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
            With a rapidly aging global population, Epsilon's wearable solution can make
            independent living safer, smarter, and more connected.
          </p>
          <Link href="/login">
            <Button
              size="lg"
              className="bg-white text-blue-600 hover:bg-blue-50 px-12 py-8 text-xl rounded-full shadow-2xl hover:scale-105 transition-all duration-300"
            >
              Get Started Today →
            </Button>
          </Link>
        </div>
      </section>
    </main>
  );
}
