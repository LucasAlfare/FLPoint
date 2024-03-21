# FLPoint

My own work point service system from scratch: register your entering and exiting timestamps from your job though this system!

This project is a simple way for me to understand and practice concepts about server/client architecture. Also, this should be minimal usable after API and automate tests become more stable.

This project uses:
- `Gradle` (kotlin DSL) to manage the global system project;
- `Kotlin-Exposed` to handle relation database via API instead direct SQL;
- `Ktor` to handle HTTP requests;
- `Ktor` to programmatically perform HTTP requests from client side (Android/Desktop);
- `JBCrypt` to help hash and check hashed passwords;
- `JWT` tokens approach to authorize users accesses in protected areas of the application.

Other tools should be integrated the future, such as that related to biometry.

In therms of business logic, this system defines a lot of rules to be followed in order, such as limits to `point registrations` in last `X` milliseconds.