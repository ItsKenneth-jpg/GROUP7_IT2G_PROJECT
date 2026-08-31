import jdk.swing.interop.SwingInterOpUtils;

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum VehicleStatus { IDLE, IN_TRANSIT, MAINTENANCE }
enum DriverStatus { AVAILABLE, ON_DUTY, OFF_DUTY }
enum TripStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }

class Vehicle {
    private String id;
    private String plateNumber;
    private int capacity;
    private VehicleStatus status;

    public Vehicle(String id, String plateNumber, int capacity, VehicleStatus status) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public String getId() { return id; }
    public String getPlateNumber() { return plateNumber; }
    public int getCapacity() { return capacity; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
}

class Driver {
    private String id;
    private String name;
    private String licenseNumber;
    private DriverStatus status;

    public Driver(String id, String name, String licenseNumber, DriverStatus status) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
}

class Route {
    private String id;
    private String origin;
    private String destination;

    public Route(String id, String origin, String destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }

    public String getId() { return id; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
}

class Trip {
    private String id;
    private String routeId;
    private String vehicleId;
    private String driverId;
    private LocalDateTime departureTime;
    private TripStatus status;

    public Trip(String id, String routeId, String vehicleId, String driverId, LocalDateTime departureTime, TripStatus status) {
        this.id = id;
        this.routeId = routeId;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.departureTime = departureTime;
        this.status = status;
    }

    public String getId() { return id; }
    public String getRouteId() { return routeId; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
}

class FleetSystem {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Driver> drivers = new ArrayList<>();
    private List<Route> routes = new ArrayList<>();
    private List<Trip> trips = new ArrayList<>();

    public boolean addVehicle(String id, String plate, int capacity) {
        if (vehicles.stream().anyMatch(v -> v.getId().equalsIgnoreCase(id))) {
            System.out.println("Error: Vehicle ID already exists.");
            return false;
        }
        vehicles.add(new Vehicle(id, plate, capacity, VehicleStatus.IDLE));
        return true;
    }

    public boolean addDriver(String id, String name, String license) {
        if (drivers.stream().anyMatch(d -> d.getId().equalsIgnoreCase(id))) {
            System.out.println("Error: Driver ID already exists.");
            return false;
        }
        drivers.add(new Driver(id, name, license, DriverStatus.AVAILABLE));
        return true;
    }

    public boolean addRoute(String id, String origin, String dest) {
        if (routes.stream().anyMatch(r -> r.getId().equalsIgnoreCase(id))) {
            System.out.println("Error: Route ID already exists.");
            return false;
        }
        routes.add(new Route(id, origin, dest));
        return true;
    }

    public boolean scheduleTrip(String tripId, String routeId) {
        if (trips.stream().anyMatch(t -> t.getId().equalsIgnoreCase(tripId))) {
            System.out.println("Error: Trip ID already exists.");
            return false;
        }
        boolean routeExists = routes.stream().anyMatch(r -> r.getId().equalsIgnoreCase(routeId));
        if (!routeExists) {
            System.out.println("Error: Route ID non-existent. Please register the route first.");
            return false;
        }
        trips.add(new Trip(tripId, routeId, "", "", LocalDateTime.now().plusHours(1), TripStatus.SCHEDULED));
        return true;
    }

    public boolean dispatchTrip(String tripId, String vehicleId, String driverId) {
        Trip trip = trips.stream().filter(t -> t.getId().equalsIgnoreCase(tripId)).findFirst().orElse(null);
        Vehicle vehicle = vehicles.stream().filter(v -> v.getId().equalsIgnoreCase(vehicleId)).findFirst().orElse(null);
        Driver driver = drivers.stream().filter(d -> d.getId().equalsIgnoreCase(driverId)).findFirst().orElse(null);

        if (trip == null || vehicle == null || driver == null) {
            System.out.println("Error: Invalid Trip, Vehicle, or Driver ID.");
            return false;
        }

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            System.out.println("Error: Trip is not in SCHEDULED status.");
            return false;
        }

        if (vehicle.getStatus() != VehicleStatus.IDLE) {
            System.out.println("Error: Vehicle " + vehicleId + " is currently " + vehicle.getStatus());
            return false;
        }

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            System.out.println("Error: Driver " + driverId + " is currently " + driver.getStatus());
            return false;
        }

        trip.setVehicleId(vehicleId);
        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.IN_PROGRESS);
        vehicle.setStatus(VehicleStatus.IN_TRANSIT);
        driver.setStatus(DriverStatus.ON_DUTY);

        return true;
    }

    public boolean completeTrip(String tripId) {
        Trip trip = trips.stream().filter(t -> t.getId().equalsIgnoreCase(tripId)).findFirst().orElse(null);

        if (trip == null || trip.getStatus() != TripStatus.IN_PROGRESS) {
            System.out.println("Error: Trip not found or not currently in progress.");
            return false;
        }

        Vehicle vehicle = vehicles.stream().filter(v -> v.getId().equalsIgnoreCase(trip.getVehicleId())).findFirst().orElse(null);
        Driver driver = drivers.stream().filter(d -> d.getId().equalsIgnoreCase(trip.getDriverId())).findFirst().orElse(null);

        trip.setStatus(TripStatus.COMPLETED);
        if (vehicle != null) vehicle.setStatus(VehicleStatus.IDLE);
        if (driver != null) driver.setStatus(DriverStatus.AVAILABLE);

        return true;
    }


    public void printFleetStatus() {
        System.out.println("\n--- VEHICLE FLEET ---");
        System.out.printf("%-10s %-12s %-10s %-15s\n", "ID", "Plate", "Capacity", "Status");
        System.out.println("----------------------------------------------");
        for (Vehicle v : vehicles) {
            System.out.printf("%-10s %-12s %-10d %-15s\n", v.getId(), v.getPlateNumber(), v.getCapacity(), v.getStatus());
        }

        System.out.println("\n--- DRIVERS ---");
        System.out.printf("%-10s %-20s %-15s\n", "ID", "Name", "Status");
        System.out.println("----------------------------------------------");
        for (Driver d : drivers) {
            System.out.printf("%-10s %-20s %-15s\n", d.getId(), d.getName(), d.getStatus());
        }
    }

    public void printTrips() {
        System.out.println("\n--- SCHEDULED & ACTIVE TRIPS ---");
        System.out.printf("%-10s %-10s %-10s %-10s %-15s\n", "Trip ID", "Route ID", "Vehicle", "Driver", "Status");
        System.out.println("---------------------------------------------------------");
        for (Trip t : trips) {
            System.out.printf("%-10s %-10s %-10s %-10s %-15s\n",
                    t.getId(), t.getRouteId(),
                    t.getVehicleId().isEmpty() ? "N/A" : t.getVehicleId(),
                    t.getDriverId().isEmpty() ? "N/A" : t.getDriverId(),
                    t.getStatus());
        }
    }

    public boolean delete(String tripId){
        Trip trip = trips.stream()

                .filter(t -> t.getId().equalsIgnoreCase(tripId))
                .findFirst()
                .orElse(null);

        if (trip == null){
            System.out.println("Error: Trip ID does not exist on the list.");
            return false;
        }
        if (trip.getStatus() == TripStatus.IN_PROGRESS){
            System.out.println("Error: Cannot Delete a trip that is currently in pogress.");
            return false;
        }
        trips.remove(trip);
        return true;
    }

    public boolean cancel(String tripId){
        Trip trip = trips.stream()

                .filter(t -> t.getId().equalsIgnoreCase(tripId))
                .findFirst()
                .orElse(null);

        if(trip == null){
            System.out.println("Error: Trip id does not exist on the list.");
            return false;
        }
        if(trip.getStatus() == TripStatus.IN_PROGRESS){
            System.out.println("Error: Cannot cancel trip that is currently in progress.");
            return false;
        }
        if(trip.getStatus() == TripStatus.COMPLETED){
            System.out.println("Error: Cannot cancel trip that is already completed.");
            return false;
        }
        if (trip.getStatus() == TripStatus.CANCELLED){
            System.out.println("Error: Trip is already cancelled.");
        }
        if (!trip.getVehicleId().isEmpty()){
            vehicles.stream()
                    .filter(v -> v.getId().equalsIgnoreCase(trip.getVehicleId()))
                    .findFirst()
                    .ifPresent(v -> v.setStatus(VehicleStatus.IDLE));

        }
        if (!trip.getDriverId().isEmpty()){
            drivers.stream()
                    .filter(d -> d.getId().equalsIgnoreCase(trip.getDriverId()))
                    .findFirst()
                    .ifPresent(d -> d.setStatus(DriverStatus.AVAILABLE));
        }
        trips.remove(trip);
        return true;
    }

}

public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FleetSystem system = new FleetSystem();

        while (true) {
            System.out.println("\n========== FLEETIFY ==========");
            System.out.println("[1] Register Vehicle");
            System.out.println("[2] Register Driver");
            System.out.println("[3] Register Route");
            System.out.println("[4] Schedule New Trip");
            System.out.println("[5] Dispatch Trip");
            System.out.println("[6] Complete Trip");
            System.out.println("[7] View Fleet & Driver Status");
            System.out.println("[8] View All Trips");
            System.out.println("[9] Delete Trip");
            System.out.println("[10] Cancel Trip");
            System.out.println("[0] Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Vehicle ID: ");
                    String vId = scanner.nextLine();
                    if (vId.length() <= 3){
                        System.out.println("Invalid Vehicle ID number.");
                        break;
                    }
                    System.out.print("Enter Plate Number: ");
                    String plate = scanner.nextLine();


                    if(plate.length() <= 3){
                        System.out.println("Invalid Plate Number.");
                    }
                    System.out.print("Enter Passenger Capacity: ");
                    try {
                        int cap = Integer.parseInt(scanner.nextLine());
                        if(cap >= 20){
                            System.out.println("Invalid Passenger Capacity.");
                            break;
                        }

                        if (system.addVehicle(vId, plate, cap)) {
                            System.out.println("Vehicle registered successfully.");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Error: Capacity must be a valid integer.");

                    }
                    break;

                case "2":
                    System.out.print("Enter Driver ID: ");
                    String dId = scanner.nextLine();

                    if (dId.length() <= 3){
                        System.out.println("Invalid Driver License.");
                        break;

                    }
                    System.out.print("Enter Driver Name: ");
                    String name = scanner.nextLine();

                    if (name.length() <= 3){
                        System.out.println("Invalid Driver Name.");
                        break;
                    }

                    System.out.print("Enter License Number: ");
                    String lic = scanner.nextLine();

                    if (lic.length() <= 3){
                        System.out.println("Invalid License Number.");
                    }

                    if (system.addDriver(dId, name, lic)) {
                        System.out.println("Driver registered successfully.");
                        break;
                    }
                    break;

                case "3":
                    System.out.print("Enter Route ID: ");
                    String rId = scanner.nextLine();

                    if (rId.length() <= 3){
                        System.out.println("Invalid Route ID.");
                        break;
                    }

                    System.out.print("Enter Starting Point: ");
                    String orig = scanner.nextLine();

                    if (orig.length() <= 3){
                        System.out.println("Error: Cannot Identify starting point.");
                        break;
                    }

                    System.out.print("Enter Destination: ");
                    String dest = scanner.nextLine();

                    if (dest.length() <= 3){
                        System.out.println("Error: Cannot Identify starting point.");
                        break;
                    }

                    if (system.addRoute(rId, orig, dest)) {
                        System.out.println("Route registered successfully.");
                    }
                    break;

                case "4":
                    System.out.print("Enter Trip ID: ");
                    String tId = scanner.nextLine();

                    if (tId.length() <= 3){
                        System.out.println("Invalid Trip ID.");
                        break;
                    }

                    System.out.print("Enter Route ID: ");
                    String routeId = scanner.nextLine();

                    if (routeId.length() <= 3){
                        System.out.println("Invalid Route ID");
                        break;
                    }
                    if (system.scheduleTrip(tId, routeId)) {
                        System.out.println("Trip scheduled successfully.");
                    }
                    break;

                case "5":
                    System.out.print("Enter Trip ID to Dispatch: ");
                    String dispTrip = scanner.nextLine();

                    System.out.print("Enter Vehicle ID to Assign: ");
                    String dispVeh = scanner.nextLine();

                    System.out.print("Enter Driver ID to Assign: ");
                    String dispDrv = scanner.nextLine();
                    if (system.dispatchTrip(dispTrip, dispVeh, dispDrv)) {
                        System.out.println("Trip dispatched successfully!");
                    }
                    break;

                case "6":
                    System.out.print("Enter Trip ID to Complete: ");
                    String compTrip = scanner.nextLine();
                    if (system.completeTrip(compTrip)) {
                        System.out.println("Trip marked as COMPLETED.");
                    }
                    break;

                case "7":
                    system.printFleetStatus();
                    break;

                case "8":
                    system.printTrips();
                    break;

                case "9":
                    System.out.print("Enter Trip to delete:");
                    String delTrip = scanner.nextLine().trim();

                    if (delTrip.length() <= 3) {
                        System.out.println("Invalid Trip Id.");
                        break;
                    }

                    if(system.delete(delTrip)){
                        System.out.println("Trip Deleted Successfully.");
                    }
                    break;
                case "10":
                    System.out.print("Enter Trip to cancel:");
                    String cancel = scanner.nextLine().trim();

                    if(cancel.length() <= 3){
                        System.out.println("Invalid Trip Id.");
                        break;
                    }
                    if(system.cancel(cancel)){
                        System.out.println("Trip Cancelled.");
                    }
                    break;


                case "0":
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }
}