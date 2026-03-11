import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Assignment {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    static class ParkingSpot {
        String licensePlate;
        LocalDateTime entryTime;
        Status status;

        ParkingSpot() {
            status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity;
    private int size;

    private int totalProbes = 0;
    private int operations = 0;

    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public Assignment(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];

        for (int i = 0; i < capacity; i++)
            table[i] = new ParkingSpot();
    }

    private int hash(String plate) {
        return Math.abs(plate.hashCode()) % capacity;
    }

    public void parkVehicle(String plate) {

        int index = hash(plate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;

            if (probes >= capacity) {
                System.out.println("Parking Full");
                return;
            }
        }

        table[index].licensePlate = plate;
        table[index].entryTime = LocalDateTime.now();
        table[index].status = Status.OCCUPIED;

        size++;

        totalProbes += probes;
        operations++;

        int hour = LocalDateTime.now().getHour();
        hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

        System.out.println("parkVehicle(\"" + plate + "\") → Assigned spot #" + index +
                " (" + probes + " probes)");
    }
    public void exitVehicle(String plate) {

        int index = hash(plate);
        int probes = 0;

        while (table[index].status != Status.EMPTY) {

            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(plate)) {

                LocalDateTime exit = LocalDateTime.now();

                Duration duration =
                        Duration.between(table[index].entryTime, exit);

                long minutes = duration.toMinutes();

                double fee = minutes * 0.1;

                table[index].status = Status.DELETED;
                table[index].licensePlate = null;

                size--;

                System.out.println(
                        "exitVehicle(\"" + plate + "\") → Spot #" + index +
                                " freed, Duration: " + minutes + " mins, Fee: $" +
                                String.format("%.2f", fee)
                );

                return;
            }

            index = (index + 1) % capacity;
            probes++;

            if (probes >= capacity)
                break;
        }

        System.out.println("Vehicle not found.");
    }

    public int findNearestSpot() {

        for (int i = 0; i < capacity; i++) {
            if (table[i].status != Status.OCCUPIED)
                return i;
        }

        return -1;
    }

    public void getStatistics() {

        double occupancy = (size * 100.0) / capacity;

        double avgProbes =
                operations == 0 ? 0 : (double) totalProbes / operations;

        int peakHour = -1;
        int max = 0;

        for (int hour : hourlyTraffic.keySet()) {

            if (hourlyTraffic.get(hour) > max) {
                max = hourlyTraffic.get(hour);
                peakHour = hour;
            }
        }

        System.out.println("\nParking Statistics:");
        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Avg Probes: " + String.format("%.2f", avgProbes));
        System.out.println("Peak Hour: " + peakHour + "-" + (peakHour + 1));
    }
    public static void main(String[] args) {

        Assignment parking = new Assignment(500);

        parking.parkVehicle("ABC-1234");
        parking.parkVehicle("ABC-1235");
        parking.parkVehicle("XYZ-9999");

        parking.exitVehicle("ABC-1234");

        parking.getStatistics();
    }
}