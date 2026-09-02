Fleetify

A simple Java console application to manage vehicles, drivers, routes, and trips.

Open the Project:

Open IntelliJ IDEA.
Click "Open" and select your project folder containing "main.java".
Run the Application:

Open "main.java" from the Project tool window ("src" folder).
Click the green "Run" button next to "public class main" or press "Shift + F10".
Interact with the App:

Use the "Run" window at the bottom of IntelliJ to type inputs into the terminal menu.
Main Features

Manage Fleet: Register vehicles and drivers, then track their availability ("IDLE", "IN_TRANSIT", "ON_DUTY").
Handle Trips: Schedule trips on registered routes, dispatch drivers, and mark trips as completed.
Trip safety: Automatically updates status and prevents deleting or cancelling active trips.
Quick Input Rules

All IDs, names, and plate numbers must be "more than 3 characters long".
Vehicle capacity must be a valid integer "under 20".
Always register a "Route ID" before scheduling a trip for it.
