"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { ModeToggle } from "./mode-toggle";
import UserMenu from "./user-menu";
import { authClient } from "@/lib/auth-client";
import { Button } from "./ui/button";

export default function Header() {
  const pathname = usePathname();
  const { data: session } = authClient.useSession();

  const links = [
    { to: "/", label: "Home" },
    { to: "/about", label: "About Us" },
  ] as const;

  return (
    <header className="fixed top-0 right-0 left-0 z-50 border-b bg-background/95 backdrop-blur-md shadow-sm">
      <div className="container mx-auto">
        <div className="flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Link className="flex items-center gap-2 hover:opacity-80 transition-opacity" href="/">
              <svg
                className="h-7 w-7 text-blue-600"
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
              <span className="font-bold text-xl bg-clip-text text-transparent bg-linear-to-r from-blue-600 to-blue-400">Epsilon</span>
            </Link>
            <nav className="hidden gap-8 md:flex">
              {links.map(({ to, label }) => {
                const isActive = pathname === to;
                return (
                  <Link
                    className={`font-medium text-sm transition-colors hover:text-blue-600 ${
                      isActive ? "text-blue-600 font-semibold" : "text-foreground/70"
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
            {session ? (
              <>
                <Link href="/dashboard">
                  <Button className="bg-blue-600 hover:bg-blue-700 text-white">
                    Dashboard
                  </Button>
                </Link>
                <UserMenu />
              </>
            ) : (
              <Link href="/login">
                <Button className="bg-blue-600 hover:bg-blue-700 text-white">
                  Sign In
                </Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
