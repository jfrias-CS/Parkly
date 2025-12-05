🅿️ Parkly: Comprehensive Garage Operations System
Parkly is a multi-threaded, concurrent garage management system designed for automated and secure parking facility operations. It demonstrates robust architectural design by handling complex, real-time processes from entry to exit, payment, and internal employee management.

Key Functionalities:

Ticketing & Tracking: Automated generation of unique ticket IDs, complete ticket tracking, and space tracking to manage occupancy in real-time.

Access Control: Manages entry to the garage via ticket generation and exit gate operation based on validated payment or authorized user override.

Financial & Security: Tracks payment status per ticket and provides secure employee login validation coupled with employee time tracking.

Architectural Highlights:

Parkly is built using key object-oriented design patterns to ensure stability and efficiency:

Multiple Threaded Server Sockets: Enables the system to handle concurrent client requests (multiple entry/exit gates, employee terminals) simultaneously without blocking, ensuring high performance and responsiveness.

Singleton Pattern: Used for critical, centralized components (like the Ticket ID Generator or the Space Inventory Manager) to ensure a single, authoritative instance and prevent concurrency issues like duplicated IDs or incorrect space assignments.

Facade Design Pattern: Provides a simplified, unified interface to complex operational sub-systems (e.g., calling one method to process an exit handles payment validation, time tracking updates, and ticket reconciliation internally).

Overall, Parkly showcases advanced skills in concurrency, security implementation, and sophisticated systems architecture.
