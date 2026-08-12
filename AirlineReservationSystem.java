
import java.io.*;
import java.util.*;

// -------------------- INTERFACE --------------------
interface Bookable {
    int assignSeatInClass(String seatClass, int preferredSeat) throws SeatNotAvailableException;
}

// -------------------- CUSTOM EXCEPTION --------------------
class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}

// -------------------- ABSTRACT CLASS --------------------
abstract class Flight implements Bookable {
    private String flightId, flightName, source, destination, flightType, departureTime;
    private int totalSeats, durationMinutes, economySeats, premiumSeats, businessSeats;
    private double ticketPrice;
    private List<Integer> bookedSeats = new ArrayList<>();

    public Flight(String flightId, String flightName, String source, String destination,
                  int totalSeats, double ticketPrice, String flightType,
                  String departureTime, int durationMinutes) {
        this.flightId = flightId; this.flightName = flightName;
        this.source = source; this.destination = destination;
        this.totalSeats = Math.max(1, totalSeats);
        this.ticketPrice = ticketPrice;
        this.flightType = flightType; this.departureTime = departureTime;
        this.durationMinutes = Math.max(0, durationMinutes);

        this.economySeats = Math.max(0, (int)(this.totalSeats * 0.5));
        this.premiumSeats = Math.max(0, (int)(this.totalSeats * 0.3));
        this.businessSeats = this.totalSeats - economySeats - premiumSeats;
        if(this.businessSeats < 0) this.businessSeats = 0;
    }

    // Getters
    public String getFlightId() { return flightId; }
    public String getFlightName() { return flightName; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getFlightType() { return flightType; }
    public double getTicketPrice() { return ticketPrice; }
    public int getEconomySeats() { return economySeats; }
    public int getPremiumSeats() { return premiumSeats; }
    public int getBusinessSeats() { return businessSeats; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getTotalSeats() { return totalSeats; }
    public List<Integer> getBookedSeats() { return bookedSeats; }

    public void displayFlight() {
        System.out.printf("%-6s %-20s %-12s %-12s %-8s %-8s %6d %10.2f %-12s%n",
                flightId, flightName, source, destination, departureTime,
                calculateArrivalTime(), totalSeats, ticketPrice, flightType);
    }

    @Override
    public int assignSeatInClass(String seatClass, int preferredSeat) throws SeatNotAvailableException {
        int availableSeats = switch(seatClass) {
            case "Economy" -> economySeats;
            case "Premium" -> premiumSeats;
            case "Business" -> businessSeats;
            default -> 0;
        };
        if(availableSeats <= 0) throw new SeatNotAvailableException("No seats available in " + seatClass);

        if(bookedSeats.size() >= totalSeats)
            throw new SeatNotAvailableException("All seats booked on this flight.");

        int assigned = -1;
        if(preferredSeat <= 0 || bookedSeats.contains(preferredSeat)) {
            for(int s=1; s<=totalSeats; s++) {
                if(!bookedSeats.contains(s)) { assigned = s; break; }
            }
            if(assigned == -1) throw new SeatNotAvailableException("No free seats found.");
        } else {
            if(preferredSeat > totalSeats) throw new SeatNotAvailableException("Seat exceeds total seats.");
            assigned = preferredSeat;
        }
        bookedSeats.add(assigned);

        switch(seatClass) {
            case "Economy" -> economySeats--;
            case "Premium" -> premiumSeats--;
            case "Business" -> businessSeats--;
        }
        return assigned;
    }

    public void cancelSeat(int seatNumber, String seatClass) {
        bookedSeats.remove((Integer) seatNumber);
        switch(seatClass) {
            case "Economy" -> economySeats++;
            case "Premium" -> premiumSeats++;
            case "Business" -> businessSeats++;
        }
    }

    public String calculateArrivalTime() {
        if(departureTime==null || !departureTime.contains(":")) return "??:??";
        try {
            String[] parts = departureTime.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int min = Integer.parseInt(parts[1].trim());
            min += durationMinutes;
            hour += min / 60;
            min = min % 60;
            hour = (hour % 24 + 24) % 24;
            return String.format("%02d:%02d", hour, min);
        } catch (Exception e) { return "??:??"; }
    }

    public abstract void displayWelcomeMessage();

    public String toFileString() {
        return flightId+","+flightName+","+source+","+destination+","+
                totalSeats+","+ticketPrice+","+flightType+","+departureTime+","+durationMinutes;
    }
}

// -------------------- SUBCLASSES --------------------
class DomesticFlight extends Flight {
    public DomesticFlight(String flightId,String flightName,String source,String destination,
                          int totalSeats,double ticketPrice,String departureTime,int durationMinutes) {
        super(flightId,flightName,source,destination,totalSeats,ticketPrice,"Domestic",departureTime,durationMinutes);
    }

    @Override
    public void displayWelcomeMessage() {
        System.out.println("\nWelcome aboard Domestic Flight: "+getFlightName()+" ✈️");
        System.out.println("No passport required. Enjoy your flight!");
    }
}

class InternationalFlight extends Flight {
    public InternationalFlight(String flightId,String flightName,String source,String destination,
                               int totalSeats,double ticketPrice,String departureTime,int durationMinutes) {
        super(flightId,flightName,source,destination,totalSeats,ticketPrice,"International",departureTime,durationMinutes);
    }

    @Override
    public void displayWelcomeMessage() {
        System.out.println("\nWelcome aboard International Flight: "+getFlightName()+" 🌍");
        System.out.println("Please ensure your passport and visa are ready!");
    }
}

// -------------------- PASSENGER CLASS --------------------
class Passenger {
    private String passengerId, name, gender, seatClass;
    private int age, seatNumber;
    private double fare;
    private Flight flight;

    public Passenger(String passengerId,String name,int age,String gender,
                     Flight flight,String seatClass,int seatNumber,double fare) {
        this.passengerId = passengerId; this.name = name; this.age = age; this.gender = gender;
        this.flight = flight; this.seatClass = seatClass; this.seatNumber = seatNumber; this.fare = fare;
    }

    public void viewTicket() {
        System.out.println("\n================== TICKET ==================");
        System.out.println("Passenger ID : "+passengerId);
        System.out.println("Name         : "+name);
        System.out.println("Age          : "+age);
        System.out.println("Gender       : "+gender);
        System.out.println("--------------------------------------------");
        System.out.println("Flight ID    : "+flight.getFlightId());
        System.out.println("Flight Name  : "+flight.getFlightName());
        System.out.println("Source       : "+flight.getSource());
        System.out.println("Destination  : "+flight.getDestination());
        System.out.println("Arrival      : "+flight.calculateArrivalTime());
        System.out.println("Flight Type  : "+flight.getFlightType());
        System.out.println("Seat Class   : "+seatClass);
        System.out.println("Seat Number  : "+seatNumber);
        System.out.println("Total Fare   : ₹"+fare);
        System.out.println("============================================\n");
    }

    public String getPassengerId() { return passengerId; }
    public Flight getFlight() { return flight; }
    public String getSeatClass() { return seatClass; }
    public int getSeatNumber() { return seatNumber; }

    public String toFileString() {
        return passengerId+","+name+","+age+","+gender+","+
                flight.getFlightId()+","+flight.getFlightName()+","+
                flight.getSource()+","+flight.getDestination()+","+
                flight.calculateArrivalTime()+","+seatClass+","+seatNumber+","+fare;
    }
}

// -------------------- FILE HANDLER --------------------
class FileHandler {

    public static List<Flight> loadFlights(String filename) {
        List<Flight> flights = new ArrayList<>();
        File f = new File(filename);
        if(!f.exists()) return flights;
        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] data = line.split(",",-1);
                if(data.length<9) continue;
                String id=data[0],name=data[1],src=data[2],dest=data[3],type=data[6],dep=data[7];
                int seats=Integer.parseInt(data[4]);
                double price=Double.parseDouble(data[5]);
                int dur=Integer.parseInt(data[8]);
                if(type.equalsIgnoreCase("Domestic")) flights.add(new DomesticFlight(id,name,src,dest,seats,price,dep,dur));
                else flights.add(new InternationalFlight(id,name,src,dest,seats,price,dep,dur));
            }
        } catch(Exception e) { System.out.println("Error loading flights: "+e.getMessage()); }
        return flights;
    }

    public static void savePassenger(Passenger p, String filename) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename,true))) {
            bw.write(p.toFileString());
            bw.newLine();
        } catch(Exception e) { System.out.println("Error saving passenger: "+e.getMessage()); }
    }

    public static List<Passenger> loadPassengers(String filename, List<Flight> flights) {
        List<Passenger> list = new ArrayList<>();
        File f = new File(filename);
        if(!f.exists()) return list;
        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while((line=br.readLine())!=null) {
                String[] data = line.split(",",-1);
                if(data.length<12) continue;
                String pid=data[0],name=data[1],gender=data[3],seatClass=data[9];
                int age=Integer.parseInt(data[2]),seatNumber=Integer.parseInt(data[10]);
                double fare=Double.parseDouble(data[11]);
                String fid=data[4];
                Flight flight = flights.stream().filter(fl->fl.getFlightId().equals(fid)).findFirst().orElse(null);
                if(flight==null) continue;
                list.add(new Passenger(pid,name,age,gender,flight,seatClass,seatNumber,fare));
            }
        } catch(Exception e) { System.out.println("Error reading passengers: "+e.getMessage()); }
        return list;
    }

    public static void overwritePassengers(List<Passenger> passengers,String filename) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for(Passenger p:passengers) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch(Exception e) { System.out.println("Error updating passengers: "+e.getMessage()); }
    }
}

// -------------------- VIEW BOOKINGS --------------------
class ViewBooking {
    public static void showAllBookings(List<Passenger> passengers) {
        if(passengers.isEmpty()) { System.out.println("No bookings found."); return; }
        for(Passenger p: passengers) p.viewTicket();
    }
}

// -------------------- MAIN SYSTEM --------------------
public class AirlineReservationSystem {
    static Scanner sc = new Scanner(System.in);
    static List<Flight> flights = FileHandler.loadFlights("flights.txt");
    static List<Passenger> passengers = FileHandler.loadPassengers("passengers.txt", flights);

    private static int readInt(String prompt) {
        while(true) {
            try { System.out.print(prompt); return Integer.parseInt(sc.nextLine().trim()); }
            catch(Exception e) { System.out.println("Enter a valid integer."); }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt); return sc.nextLine().trim();
    }

    public static void main(String[] args) {
        if(flights.isEmpty()) { System.out.println("No flight data found!"); return; }
        int choice;
        do {
            System.out.println("\nMenu:");
            System.out.println("1. View All Flights");
            System.out.println("2. Search Flights");
            System.out.println("3. Book a Ticket");
            System.out.println("4. Cancel Booking");
            System.out.println("5. Update Booking");
            System.out.println("6. View My Bookings");
            System.out.println("7. Exit");
            choice = readInt("Enter choice: ");

            switch(choice) {
                case 1 -> viewAllFlights();
                case 2 -> searchFlights();
                case 3 -> bookTicket();
                case 4 -> cancelBooking();
                case 5 -> updateBooking();
                case 6 -> ViewBooking.showAllBookings(passengers);
                case 7 -> System.out.println("Thank you for using Airline System!");
                default -> System.out.println("Invalid choice!");
            }
        } while(choice!=7);
    }

    private static void viewAllFlights() {
        System.out.printf("%-6s %-20s %-12s %-12s %-8s %-8s %6s %10s %-12s%n",
                "ID","Flight Name","Source","Destination","Dep","Arr","Seats","Price","Type");
        System.out.println("--------------------------------------------------------------------------------");
        for(Flight f: flights) f.displayFlight();
    }

    private static void searchFlights() {
        String src = readLine("Enter Source: ");
        String dest = readLine("Enter Destination: ");
        List<Flight> available = new ArrayList<>();
        for(Flight f:flights)
            if(f.getSource().equalsIgnoreCase(src) && f.getDestination().equalsIgnoreCase(dest)
                    && (f.getEconomySeats()+f.getPremiumSeats()+f.getBusinessSeats())>0)
                available.add(f);
        if(available.isEmpty()) { System.out.println("No flights found."); return; }
        System.out.printf("%-6s %-20s %-12s %-12s %-8s %-8s %6s %10s %-12s%n",
                "ID","Flight Name","Source","Destination","Dep","Arr","Seats","Price","Type");
        System.out.println("--------------------------------------------------------------------------------");
        for(Flight f: available) f.displayFlight();
    }

    private static void bookTicket() {
        try {
            String name = readLine("Enter Name: ");
            int age = readInt("Enter Age: ");
            String gender = readLine("Enter Gender: ");
            String src = readLine("Enter Source: ");
            String dest = readLine("Enter Destination: ");

            List<Flight> available = new ArrayList<>();
            for(Flight f: flights)
                if(f.getSource().equalsIgnoreCase(src) && f.getDestination().equalsIgnoreCase(dest)
                        && (f.getEconomySeats()+f.getPremiumSeats()+f.getBusinessSeats())>0)
                    available.add(f);

            if(available.isEmpty()) { System.out.println("No flights available."); return; }

            System.out.println("Available Flights:");
            for(Flight f:available) f.displayFlight();

            String fid = readLine("Enter Flight ID: ");
            Flight flight = flights.stream().filter(f->f.getFlightId().equalsIgnoreCase(fid)).findFirst().orElse(null);
            if(flight==null) { System.out.println("Invalid Flight ID"); return; }

            System.out.println("Seat Class Options: Economy, Premium, Business");
            String seatClass = readLine("Enter Seat Class: ");
            int seatNumber = readInt("Preferred Seat Number (0 for auto-assign): ");

            int assigned = flight.assignSeatInClass(seatClass, seatNumber);
            double fare = flight.getTicketPrice(); // Can adjust per seat class if needed
            String pid = "P"+(passengers.size()+1);
            Passenger p = new Passenger(pid,name,age,gender,flight,seatClass,assigned,fare);
            passengers.add(p);
            FileHandler.savePassenger(p,"passengers.txt");
            System.out.println("Booking successful!");
            p.viewTicket();
        } catch(SeatNotAvailableException e) { System.out.println("Booking failed: "+e.getMessage()); }
    }

    private static void cancelBooking() {
        String pid = readLine("Enter Passenger ID to cancel: ");
        Passenger p = passengers.stream().filter(ps->ps.getPassengerId().equalsIgnoreCase(pid)).findFirst().orElse(null);
        if(p==null) { System.out.println("Passenger not found."); return; }
        p.getFlight().cancelSeat(p.getSeatNumber(), p.getSeatClass());
        passengers.remove(p);
        FileHandler.overwritePassengers(passengers,"passengers.txt");
        System.out.println("Booking cancelled successfully.");
    }

       private static void updateBooking() {
        String pid = readLine("Enter Passenger ID to update: ");
        Passenger p = passengers.stream().filter(ps -> ps.getPassengerId().equalsIgnoreCase(pid)).findFirst().orElse(null);
        if (p == null) {
            System.out.println("Passenger not found.");
            return;
        }

        // Cancel old seat first
        p.getFlight().cancelSeat(p.getSeatNumber(), p.getSeatClass());

        try {
            String name = readLine("Enter Name [" + p.getPassengerId() + "]: ");
            int age = readInt("Enter Age [" + p.getPassengerId() + "]: ");
            String gender = readLine("Enter Gender [" + p.getPassengerId() + "]: ");
            System.out.println("Seat Class Options: Economy, Premium, Business");
            String seatClass = readLine("Enter Seat Class: ");
            int seatNumber = readInt("Preferred Seat Number (0 for auto-assign): ");

            int assigned = p.getFlight().assignSeatInClass(seatClass, seatNumber);

            Passenger updated = new Passenger(pid, name, age, gender, p.getFlight(), seatClass, assigned, p.getFlight().getTicketPrice());
            passengers.remove(p);
            passengers.add(updated);

            FileHandler.overwritePassengers(passengers, "passengers.txt");

            System.out.println("Booking updated successfully!");
            updated.viewTicket();

        } catch (SeatNotAvailableException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }
}

