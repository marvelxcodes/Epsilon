"use client";
import Link from "next/link";
import { ModeToggle } from "./mode-toggle";
import UserMenu from "./user-menu";
import { usePathname } from "next/navigation";

export default function Header() {
	const pathname = usePathname();
	const links = [
		{ to: "/", label: "Home" },
		{ to: "/dashboard", label: "Dashboard" },
	] as const;

	return (
		<header className="fixed top-0 left-0 right-0 z-50 bg-background/80 backdrop-blur-sm border-b">
			<div className="container mx-auto">
				<div className="flex h-16 items-center justify-between px-4">
					<div className="flex items-center gap-6">
						<Link href="/" className="flex items-center gap-2">
							<svg
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								className="w-6 h-6 text-blue-500"
							>
								<path
									strokeLinecap="round"
									strokeLinejoin="round"
									strokeWidth={2}
									d="M13 10V3L4 14h7v7l9-11h-7z"
									/>
							</svg>
							<span className="font-bold text-lg">Epsilon</span>
						</Link>
						<nav className="hidden md:flex gap-6">
							{links.map(({ to, label }) => {
								const isActive = pathname === to;
								return (
									<Link
										key={to}
										href={to}
										className={`text-sm font-medium transition-colors hover:text-foreground/80 ${
											isActive
												? "text-foreground"
												: "text-foreground/60"
										}`}
									>
										{label}
									</Link>
								);
							})}
						</nav>
					</div>
					<div className="flex items-center gap-4">
						<ModeToggle />
						<UserMenu />
					</div>
				</div>
			</div>
		</header>
	);
}
