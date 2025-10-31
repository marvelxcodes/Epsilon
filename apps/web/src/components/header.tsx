"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { ModeToggle } from "./mode-toggle";
import UserMenu from "./user-menu";

export default function Header() {
  const pathname = usePathname();
  const links = [
    { to: "/", label: "Home" },
    { to: "/dashboard", label: "Dashboard" },
    { to: "/dashboard/medications", label: "Medications" },
    { to: "/dashboard/reminders", label: "Reminders" },
  ] as const;

  return (
    <header className="fixed top-0 right-0 left-0 z-50 border-b bg-background/80 backdrop-blur-sm">
      <div className="container mx-auto">
        <div className="flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-6">
            <Link className="flex items-center gap-2" href="/">
              <svg
                className="h-6 w-6 text-blue-500"
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
              <span className="font-bold text-lg">Epsilon</span>
            </Link>
            <nav className="hidden gap-6 md:flex">
              {links.map(({ to, label }) => {
                const isActive = pathname === to;
                return (
                  <Link
                    className={`font-medium text-sm transition-colors hover:text-foreground/80 ${
                      isActive ? "text-foreground" : "text-foreground/60"
                    }`}
                    href={to}
                    key={to}
                  >
                    {label}
                  </Link>
                );
              })}
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <ModeToggle />
            {pathname !== "/" && <UserMenu />}
          </div>
        </div>
      </div>
    </header>
  );
}
