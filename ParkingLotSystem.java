import java.util.*;

enum VehicleType {
    BIKE, CAR, TRUCK
}

abstract class Vehicle {
    String licensePlate;
    VehicleType type;
    int requiredSpots;

    public Vehicle(String licensePlate, VehicleType type, int requiredSpots) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.requiredSpots = requiredSpots;
    }

    public VehicleType getType() {
        return type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getRequiredSpots() {
        return requiredSpots;
    }
}

class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, VehicleType.BIKE, 1);
    }
}

class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR, 1);
    }
}

class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK, 2);
    }
}

class ParkingSpot {
    int spotNumber;
    boolean isOccupied;
    Vehicle currentVehicle;

    public ParkingSpot(int spotNumber) {
        this.spotNumber = spotNumber;
        this.isOccupied = false;
    }

    public boolean isAvailable() {
        return !isOccupied;
    }

    public void parkVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.isOccupied = true;
    }

    public void leaveVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }

    public boolean isOccupiedBy(String licensePlate) {
        return currentVehicle != null && currentVehicle.getLicensePlate().equals(licensePlate);
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }
}

class ParkingFloor {
    int floorNumber;
    List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<Integer> spotConfig) {
        this.floorNumber = floorNumber;
        this.spots = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < spotConfig.size(); i++) {
            for (int j = 0; j < spotConfig.get(i); j++) {
                spots.add(new ParkingSpot(i * 100 + j + 1));
            }
        }
    }

    public synchronized int getAvailableSpots() {
        int available = 0;
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) available++;
        }
        return available;
    }

    public synchronized String findVehicle(String licensePlate) {
        for (ParkingSpot spot : spots) {
            if (!spot.isAvailable() && spot.isOccupiedBy(licensePlate)) {
                return "Floor " + floorNumber + " Spot " + spot.spotNumber;
            }
        }
        return "Not Found";
    }

    public synchronized boolean parkVehicle(Vehicle vehicle) {
        if (vehicle instanceof Truck) {
            for (int i = 0; i < spots.size() - 1; i++) {
                if (spots.get(i).isAvailable() && spots.get(i + 1).isAvailable()) {
                    spots.get(i).parkVehicle(vehicle);
                    spots.get(i + 1).parkVehicle(vehicle);
                    return true;
                }
            }
        } else {
            for (ParkingSpot spot : spots) {
                if (spot.isAvailable()) {
                    spot.parkVehicle(vehicle);
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean removeVehicle(String licensePlate) {
        boolean removed = false;
        for (ParkingSpot spot : spots) {
            if (spot.isOccupiedBy(licensePlate)) {
                spot.leaveVehicle();
                removed = true;
            }
        }
        return removed;
    }
}

class ParkingLot {
    List<ParkingFloor> floors;

    public ParkingLot(List<List<Integer>> floorConfigs) {
        floors = new ArrayList<>();
        for (int i = 0; i < floorConfigs.size(); i++) {
            floors.add(new ParkingFloor(i + 1, floorConfigs.get(i)));
        }
    }

    public synchronized void displayAvailableSpots() {
        for (ParkingFloor floor : floors) {
            System.out.println("Floor " + floor.floorNumber + ": " + floor.getAvailableSpots() + " spots available");
        }
    }

    public synchronized String findVehicle(String licensePlate) {
        for (ParkingFloor floor : floors) {
            String location = floor.findVehicle(licensePlate);
            if (!location.equals("Not Found")) return location;
        }
        return "Vehicle Not Found";
    }

    public synchronized boolean parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            if (floor.parkVehicle(vehicle)) return true;
        }
        return false;
    }

    public synchronized boolean removeVehicle(String licensePlate) {
        for (ParkingFloor floor : floors) {
            if (floor.removeVehicle(licensePlate)) return true;
        }
        return false;
    }
}

public class ParkingLotSystem {
    public static void main(String[] args) {
        List<List<Integer>> floorConfigs = new ArrayList<>();
        floorConfigs.add(Arrays.asList(5, 3, 2));
        floorConfigs.add(Arrays.asList(4, 4, 2));
        floorConfigs.add(Arrays.asList(6, 2, 2));

        ParkingLot parkingLot = new ParkingLot(floorConfigs);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Park Vehicle\n2. Remove Vehicle\n3. Find Vehicle\n4. Display Available Spots\n5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Vehicle Type (BIKE/CAR/TRUCK): ");
                    String type = scanner.nextLine().toUpperCase();
                    System.out.print("Enter License Plate: ");
                    String license = scanner.nextLine();
                    Vehicle vehicle = switch (type) {
                        case "BIKE" -> new Bike(license);
                        case "CAR" -> new Car(license);
                        case "TRUCK" -> new Truck(license);
                        default -> null;
                    };
                    if (vehicle != null) {
                        System.out.println("Parking: " + (parkingLot.parkVehicle(vehicle) ? "Success" : "Failed"));
                    } else {
                        System.out.println("Invalid vehicle type. Try again.");
                    }
                }
                case 2 -> {
                    System.out.print("Enter License Plate: ");
                    String license = scanner.nextLine();
                    System.out.println("Removing: " + (parkingLot.removeVehicle(license) ? "Success" : "Failed"));
                }
                case 3 -> {
                    System.out.print("Enter License Plate: ");
                    String license = scanner.nextLine();
                    System.out.println("Location: " + parkingLot.findVehicle(license));
                }
                case 4 -> parkingLot.displayAvailableSpots();
                case 5 -> {
                    scanner.close();
                    return;
                }
            }
        }
    }
}
